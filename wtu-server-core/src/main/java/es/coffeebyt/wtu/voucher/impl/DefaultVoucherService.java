package es.coffeebyt.wtu.voucher.impl;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

import org.bitcoinj.core.NetworkParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotBlank;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import es.coffeebyt.wtu.crypto.CurrencyServiceSupplier;
import es.coffeebyt.wtu.exception.IllegalOperationException;
import es.coffeebyt.wtu.exception.ResourceNotFoundException;
import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.system.PropertyNames;
import es.coffeebyt.wtu.voucher.RedemptionListener;
import es.coffeebyt.wtu.voucher.RedemptionValidator;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.voucher.VoucherCodeGenerator;
import es.coffeebyt.wtu.voucher.VoucherNotFoundException;
import es.coffeebyt.wtu.voucher.VoucherService;
import es.coffeebyt.wtu.wallet.Wallet;
import es.coffeebyt.wtu.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class DefaultVoucherService implements VoucherService {

    static final long DUST_AMOUNT = 546;

    @Value(PropertyNames.BITCOIN_NETWORK)
    private String networkType;

    private final WalletService walletService;
    private final VoucherRepository voucherRepository;
    private final CurrencyServiceSupplier currencyServiceSupplier;
    private final List<RedemptionValidator> redemptionValidators;
    private final List<RedemptionListener> redemptionListeners;

    @Override
    public List<Voucher> generateVouchers(VoucherGenerationSpec spec) {
        if (spec.totalAmount % spec.count != 0) {
            String message = format("Total amount must be divisible by count because all vouchers must be identical. Current " +
                    "specification is incorrect: can't split amount of: %s into %s equal pieces.", spec.totalAmount, spec.count);
            throw new IllegalOperationException(message);
        }

        if (!walletService.findById(spec.walletId).isPresent())
                throw new ResourceNotFoundException(format("Wallet with id %s not found.", spec.walletId));

        long amount = spec.totalAmount / spec.count;

        // TODO dust is network specific, remove this
        if (amount <= DUST_AMOUNT)
            throw new IllegalOperationException("Voucher value is too low. Must be greater than " + DUST_AMOUNT);

        VoucherCodeGenerator voucherCodeGenerator = new FromSpecCodeGenerator(spec);

        return IntStream.range(0, spec.count)
                .mapToObj(next -> new Voucher()
                        .withAmount(amount)
                        .withCode(voucherCodeGenerator.apply(spec))
                        .withWalletId(spec.walletId)
                        .withSold(false)
                        .withPublished(false)
                        .withRedeemed(false)
                        .withSku(spec.getSku()))
                .collect(toList());
    }

    @Override
    public List<Voucher> findAll() {
        return voucherRepository.findAll();
    }

    @Override
    public void deleteBySku(String sku) {
        List<Voucher> vouchers = voucherRepository.findBySoldFalseAndSku(sku);
        if (!vouchers.isEmpty())
            voucherRepository.deleteAll(vouchers);
    }

    @Override
    public void publishBySku(String sku) {
        List<Voucher> vouchers = voucherRepository.findByPublishedFalseAndSku(sku)
                .stream()
                .map(voucher -> voucher.withPublished(true))
                .collect(toList());
        if (!vouchers.isEmpty())
            voucherRepository.saveAll(vouchers);
    }

    @Override
    public void unPublishBySku(String sku) {
        List<Voucher> vouchers = voucherRepository.findByPublishedTrueAndSoldFalseAndSku(sku)
                .stream()
                .map(voucher -> voucher.withPublished(false))
                .collect(toList());
        if (!vouchers.isEmpty())
            voucherRepository.saveAll(vouchers);
    }

    @Override
    public void save(@Nonnull List<Voucher> vouchers) {
        verifyVouchers(vouchers);

        voucherRepository.saveAll(vouchers);
    }

    private void verifyVouchers(List<Voucher> vouchers) {
        Set<@NotBlank String> alreadyExisting = vouchers.parallelStream()
                .map(Voucher::getCode)
                .map(voucherRepository::findByCode)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Voucher::getCode)
                .collect(Collectors.toSet());

        if (!alreadyExisting.isEmpty()) {
            String message = "Following voucher code(s) already exist: " + String.join(", ", alreadyExisting);
            throw new IllegalOperationException(message);
        }
    }

    @Override
    public synchronized RedemptionResponse redeemVoucher(@Nonnull RedemptionRequest detail) {
        Voucher voucher = voucherRepository.findByCode(detail.getVoucherCode())
                .orElseThrow(() -> new VoucherNotFoundException(format("Voucher %S not found.", detail.getVoucherCode())));

        // run all validations
        redemptionValidators.forEach(validator -> validator.validate(detail));

        Wallet wallet = walletService.findById(voucher.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException(format("Wallet %s not found.", voucher.getWalletId())));

        voucherRepository.save(voucher.withRedeemed(true));

        // send money
        String transactionHash = currencyServiceSupplier.findByCurrency(wallet.getCurrency())
                .sendMoney(wallet, detail.getDestinationAddress(), voucher.getAmount());

        // notify all listeners
        redemptionListeners.forEach(listener -> listener.redeemed(detail));

        return fromTxHash(wallet.getCurrency(), transactionHash);
    }

    private RedemptionResponse fromTxHash(String currency, String txHash) {
        String networkId = null;
        if ("BTC".equals(currency) && "mainnet".equals(networkType)) networkId = NetworkParameters.ID_MAINNET;
        if ("BTC".equals(currency) && "testnet".equals(networkType)) networkId = NetworkParameters.ID_TESTNET;
        if ("BCH".equals(currency) && "mainnet".equals(networkType))
            networkId = cash.bitcoinj.core.NetworkParameters.ID_MAINNET;
        if ("BCH".equals(currency) && "testnet".equals(networkType))
            networkId = cash.bitcoinj.core.NetworkParameters.ID_TESTNET;

        return new RedemptionResponse(
                RedemptionUtils.blockExploresByNetworkId.getOrDefault(networkId, emptySet())
                        .stream()
                        .map(url -> String.format(url, txHash))
                        .collect(Collectors.toList()), txHash);
    }

    @Override
    public Optional<Voucher> findByCode(String voucherCode) {
        return voucherRepository.findByCode(voucherCode);
    }

    @Override
    public List<Voucher> findBy(boolean showRedeemed, String sku) {

        if (showRedeemed) {
            if (sku == null) return voucherRepository.findByRedeemedTrue();
            else return voucherRepository.findByRedeemedTrueAndSku(sku);
        } else {
            if (sku == null) return voucherRepository.findByRedeemedFalse();
            else return voucherRepository.findByRedeemedFalseAndSku(sku);
        }
    }
}
