package com.jvmp.vouchershop.voucher;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.jvmp.vouchershop.utils.RandomUtils.randomVoucher;
import static com.jvmp.vouchershop.voucher.VoucherInfoResponse.from;
import static org.junit.Assert.assertEquals;

public class VoucherInfoResponseTest {

    @Test
    public void calculateStatus_redeemed() {

        // simply when redeemed
        assertEquals("redeemed", from(randomVoucher().withRedeemed(true)).getStatus());
    }

    @Test
    public void calculateStatus_expired() {
        VoucherInfoResponse voucherInfoResponse = from(randomVoucher()
                .withRedeemed(false)
                .withSold(true)
                .withExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS).getEpochSecond())
        );
        // when sold, !redeemed, expired date
        assertEquals("expired", voucherInfoResponse.getStatus());
    }

    @Test
    public void calculateStatus_valid() {
        VoucherInfoResponse voucherInfoResponse = from(randomVoucher().withRedeemed(false).withSold(true));
        // when sold, !redeemed, not expired
        assertEquals("valid", voucherInfoResponse.getStatus());
    }

    @Test(expected = IllegalOperationException.class)
    public void calculateStatus_error1() {
        // error when other situation
        from(randomVoucher().withSold(false).withRedeemed(false));
    }

    @Test(expected = IllegalOperationException.class)
    public void calculateStatus_error2() {
        // error when other situation
        from(randomVoucher()
                .withSold(false)
                .withRedeemed(false)
                .withExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS).getEpochSecond())
        );
    }
}