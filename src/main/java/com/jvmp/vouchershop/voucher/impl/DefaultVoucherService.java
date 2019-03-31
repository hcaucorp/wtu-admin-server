package com.jvmp.vouchershop.voucher.impl;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.VoucherNotFoundException;
import com.jvmp.vouchershop.voucher.VoucherService;
import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static com.jvmp.vouchershop.voucher.impl.VoucherValidations.checkIfRedeemable;
import static com.jvmp.vouchershop.voucher.impl.VoucherValidations.checkIfRefundable;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultVoucherService implements VoucherService {

    public static final Supplier<String> DEFAULT_VOUCHER_CODE_GENERATOR = () -> UUID.randomUUID().toString();

    private final WalletService walletService;
    private final VoucherRepository voucherRepository;

    @Override
    public List<Voucher> generateVouchers(VoucherGenerationDetails spec) {
        if (spec.totalAmount % spec.count != 0)
            throw new IllegalOperationException("Total amount must be divisible by count because all vouchers must be identical. Current specification is " +
                    "incorrect: can't split amount of: " + spec.totalAmount + " into " + spec.count + " equal pieces.");

        String currency = walletService.findById(spec.walletId)
                .map(Wallet::getCurrency)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet with id " + spec.walletId + " not found."));

        long amount = spec.totalAmount / spec.count;

        return IntStream.range(0, spec.count)
                .mapToObj(next -> new Voucher()
                        .withAmount(amount)
                        .withCode(DEFAULT_VOUCHER_CODE_GENERATOR.get())
                        .withCurrency(currency)
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
    public void save(List<Voucher> vouchers) {
        voucherRepository.saveAll(vouchers);
    }

    @Override
    public synchronized RedemptionResponse redeemVoucher(@Nonnull RedemptionRequest detail) {
        Objects.requireNonNull(detail, "voucher redemption details");

        Voucher voucher = voucherRepository.findByCode(detail.getVoucherCode())
                .orElseThrow(() -> new VoucherNotFoundException("Voucher " + detail.getVoucherCode() + " not found."));

        checkIfRedeemable(voucher);

        Wallet wallet = walletService.findById(voucher.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet " + voucher.getWalletId() + " not found."));

        // send money
        String transactionHash = walletService.sendMoney(wallet, detail.getDestinationAddress(), voucher.getAmount());
        voucherRepository.save(voucher.withRedeemed(true));

        return RedemptionUtils.fromTxHash(transactionHash);
    }

    @Override
    public void refund(@NotBlank String code) {
        Objects.requireNonNull(code);

        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new VoucherNotFoundException("Voucher " + code + " not found."));

        checkIfRefundable(voucher);

        voucherRepository.delete(voucher);
    }
}
