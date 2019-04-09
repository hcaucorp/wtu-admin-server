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

        if (!voucher.isSold()) {
            log.error("Attempting to redeem not sold voucher {}", voucher.getCode());
            throw new IllegalOperationException("Voucher " + voucher.getCode() + " hasn't been sold yet.");
        }

        if (voucher.isRedeemed()) {
            log.error("Attempting to redeem already redeemed voucher {}", voucher.getCode());
            throw new IllegalOperationException("Voucher " + voucher.getCode() + " has already been redeemed.");
        }
    }

    static void checkIfRefundable(@Nonnull Voucher voucher) {
        Objects.requireNonNull(voucher, "voucher");

        if (!voucher.isSold()) {
            log.error("Attempting to redeem not sold voucher {}", voucher.getCode());
            throw new IllegalOperationException("Voucher " + voucher.getCode() + " hasn't been sold yet.");
        }

        if (voucher.isRedeemed()) {
            log.error("Attempting to redeem already redeemed voucher {}", voucher.getCode());
            throw new IllegalOperationException("Voucher " + voucher.getCode() + " has already been redeemed.");
        }
    }
}
