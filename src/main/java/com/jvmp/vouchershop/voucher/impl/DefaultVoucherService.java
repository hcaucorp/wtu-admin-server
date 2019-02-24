package com.jvmp.vouchershop.voucher.impl;

import com.google.common.annotations.VisibleForTesting;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.VoucherNotFound;
import com.jvmp.vouchershop.voucher.VoucherService;
import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultVoucherService implements VoucherService {

    private final WalletService walletService;
    private final VoucherRepository voucherRepository;

    public static final Supplier<String> DEFAULT_VOUCHER_CODE_GENERATOR = () -> UUID.randomUUID().toString();

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

    @VisibleForTesting
    static void checkVoucher(@Nonnull Voucher voucher) {
        Objects.requireNonNull(voucher, "voucher");

        if (!voucher.isPublished()) {
            log.error("Attempting to redeem not published voucher {}", voucher);
            throw new IllegalOperationException("Voucher " + voucher.getCode() + " hasn't been published for sale yet.");
        }

        if (!voucher.isSold()) {
            log.error("Attempting to redeem not sold voucher {}", voucher);
            throw new IllegalOperationException("Voucher " + voucher.getCode() + " hasn't been sold yet.");
        }

        if (voucher.isRedeemed()) {
            log.warn("Attempting to redeem already redeemed voucher {}", voucher);
            throw new IllegalOperationException("Voucher " + voucher.getCode() + " has already been redeemed.");
        }

        if (isExpired(voucher)) {
            log.error("Attempting to redeem expired voucher {}", voucher);
            throw new IllegalOperationException("Voucher " + voucher.getCode() + " has expired.");
        }
    }

    @VisibleForTesting
    static boolean isExpired(@Nonnull Voucher voucher) {
//        Objects.requireNonNull(voucher, "voucher");
//
//        LocalDateTime today = LocalDateTime.now();
//        LocalDateTime expiresAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(voucher.getExpiresAt()), ZoneOffset.UTC);
//
//        return today.isAfter(expiresAt);
        return false;
    }

    @Override
    public RedemptionResponse redeemVoucher(@Nonnull RedemptionRequest detail) throws VoucherNotFound {
        Objects.requireNonNull(detail, "voucher redemption details");

        Voucher voucher = voucherRepository.findByCode(detail.getVoucherCode())
                .orElseThrow(() -> new VoucherNotFound("Voucher " + detail.getVoucherCode() + " not found."));

        checkVoucher(voucher);

        Wallet wallet = walletService.findById(voucher.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet " + voucher.getWalletId() + " not found."));

        // send money
        String transactionHash = walletService.sendMoney(wallet, detail.getDestinationAddress(), voucher.getAmount());

        if (transactionHash != null) {
            voucherRepository.save(voucher.withRedeemed(true));
            return RedemptionUtils.fromTxHash(transactionHash);
        }
        return null;
    }
}
