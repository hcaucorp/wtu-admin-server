package com.jvmp.vouchershop.voucher;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static com.jvmp.vouchershop.utils.RandomUtils.randomValidVoucher;
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
        VoucherInfoResponse voucherInfoResponse = from(randomValidVoucher()
                .withExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli())
        );
        // when sold, !redeemed, expired date
        assertEquals("expired", voucherInfoResponse.getStatus());
    }

    @Test
    public void calculateStatus_valid() {
        VoucherInfoResponse voucherInfoResponse = from(randomValidVoucher());
        // when sold, !redeemed, not expired
        assertEquals("valid", voucherInfoResponse.getStatus());
    }

    @Test
    public void calculateStatus_valid_withLegacyExpiration_ZERO() {
        Voucher voucher = randomValidVoucher()
                .withExpiresAt(0)
                .withCreatedAt(Instant.now().toEpochMilli());
        VoucherInfoResponse voucherInfoResponse = from(voucher);
        // when sold, !redeemed, not expired
        assertEquals("valid", voucherInfoResponse.getStatus());

        String expected = "" + ZonedDateTime.ofInstant(Instant.ofEpochMilli(voucher.getCreatedAt()), ZoneOffset.UTC).plusYears(2).toInstant().toEpochMilli();
        assertEquals(expected, voucherInfoResponse.getExpiresAt());
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
                .withExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli())
        );
    }
}