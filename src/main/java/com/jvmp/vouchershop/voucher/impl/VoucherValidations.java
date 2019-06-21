package com.jvmp.vouchershop.voucher.impl;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.voucher.Voucher;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.Objects;

@Slf4j
class VoucherValidations {

    static void checkIfRedeemable(@Nonnull Voucher voucher) {
        Objects.requireNonNull(voucher, "voucher");

        if (!voucher.isPublished())
            logAndThrowIllegalOperation("Attempting to redeem unpublished voucher" + voucher.getCode());

//        this behaviour is no deprecated, promotional voucher are never sold and have to be redeemable too
//        if (!voucher.isSold())
//            logAndThrowIllegalOperation("Attempting to redeem not sold voucher " + voucher.getCode());

        if (voucher.isRedeemed())
            logAndThrowIllegalOperation("Attempting to redeem already redeemed voucher " + voucher.getCode());

    }

    private static void logAndThrowIllegalOperation(String message) {
        log.error(message);
        throw new IllegalOperationException(message);
    }
}
