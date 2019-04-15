package com.jvmp.vouchershop.voucher.impl;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import org.junit.Test;

import static com.jvmp.vouchershop.utils.RandomUtils.randomVoucher;
import static com.jvmp.vouchershop.voucher.impl.VoucherValidations.checkIfRedeemable;
import static com.jvmp.vouchershop.voucher.impl.VoucherValidations.checkIfRefundable;

public class VoucherValidationsTest {

    @Test(expected = IllegalOperationException.class)
    public void checkIfRefundable_alreadyRedeemed() {
        checkIfRefundable(randomVoucher()
                .withPublished(true)
                .withSold(true)
                .withRedeemed(true)
        );
    }

    @Test
    public void checkIfRefundable_shouldSucceed() {
        checkIfRefundable(randomVoucher()
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false));
    }

    @Test(expected = IllegalOperationException.class)
    public void checkIfRedeemable_alreadyRedeemed() {
        checkIfRedeemable(randomVoucher()
                .withPublished(true)
                .withSold(true)
                .withRedeemed(true)
        );
    }

    @Test(expected = IllegalOperationException.class)
    public void checkIfRedeemable_notSoldYet() {
        checkIfRedeemable(randomVoucher()
                .withPublished(true)
        );
    }

    @Test(expected = IllegalOperationException.class)
    public void checkVoucher_notPublished() {
        checkIfRedeemable(randomVoucher());
    }

    @Test
    public void checkIfRedeemable_shouldSucceed() {
        checkIfRedeemable(randomVoucher()
                .withPublished(true)
                .withSold(true)
                .withRedeemed(false));
    }

}