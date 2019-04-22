package com.jvmp.vouchershop.voucher.impl;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.wallet.Wallet;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.Objects;

import static java.lang.String.format;

@Slf4j
class VoucherValidations {

    static void checkIfWalletCurrency(@Nonnull Wallet wallet, String currency) {
        Objects.requireNonNull(wallet, "wallet");
        Objects.requireNonNull(wallet.getCurrency(), "currency cannot be null. Invalid wallet: " + wallet);

        if (currency == null)
            return /*silently*/;

        if (!wallet.getCurrency().equals(currency)) {
            logAndThrowIllegalOperation(format("Requested voucher redemption for currency %s, but this voucher contains %s currency.", currency, wallet.getCurrency()));
        }
    }

    static void checkIfRedeemable(@Nonnull Voucher voucher) {
        Objects.requireNonNull(voucher, "voucher");

        if (!voucher.isSold())
            logAndThrowIllegalOperation("Attempting to redeem not sold voucher " + voucher.getCode());

        if (voucher.isRedeemed())
            logAndThrowIllegalOperation("Attempting to redeem already redeemed voucher " + voucher.getCode());

    }

    static void checkIfRefundable(@Nonnull Voucher voucher) {
        Objects.requireNonNull(voucher, "voucher");

        if (!voucher.isSold())
            logAndThrowIllegalOperation("Attempting to redeem not sold voucher " + voucher.getCode());

        if (voucher.isRedeemed())
            logAndThrowIllegalOperation("Attempting to redeem already redeemed voucher " + voucher.getCode());
    }

    private static void logAndThrowIllegalOperation(String message) {
        log.error(message);
        throw new IllegalOperationException(message);
    }
}
