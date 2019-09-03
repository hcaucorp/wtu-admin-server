package es.coffeebyt.wtu.voucher;

import static es.coffeebyt.wtu.utils.RandomUtils.randomValidVoucher;
import static es.coffeebyt.wtu.utils.RandomUtils.randomVoucher;
import static es.coffeebyt.wtu.voucher.listeners.MaltaPromotion.EXPIRATION_TIME;
import static es.coffeebyt.wtu.voucher.listeners.MaltaPromotion.MALTA_VOUCHER_SKU;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import es.coffeebyt.wtu.exception.IllegalOperationException;

public class VoucherInfoResponseTest {

    @Test
    public void calculateStatus_redeemed() {
        // simply when redeemed
        assertEquals("redeemed", VoucherInfoResponse.from(randomVoucher().withRedeemed(true)).getStatus());
    }

    @Test
    public void calculateStatus_expired() {
        VoucherInfoResponse voucherInfoResponse = VoucherInfoResponse.from(randomValidVoucher()
                .withExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli())
        );
        // when sold, !redeemed, expired date
        assertEquals("expired", voucherInfoResponse.getStatus());
    }

    @Test
    public void calculateStatus_valid() {
        VoucherInfoResponse voucherInfoResponse = VoucherInfoResponse.from(randomValidVoucher());
        // when sold, !redeemed, not expired
        assertEquals("valid", voucherInfoResponse.getStatus());
    }

    @Test
    public void calculateStatus_valid_withLegacyExpiration_ZERO() {
        Voucher voucher = randomValidVoucher()
                .withExpiresAt(0)
                .withCreatedAt(Instant.now().toEpochMilli());
        VoucherInfoResponse voucherInfoResponse = VoucherInfoResponse.from(voucher);
        // when sold, !redeemed, not expired
        assertEquals("valid", voucherInfoResponse.getStatus());

        String expected = "" + ZonedDateTime.ofInstant(Instant.ofEpochMilli(voucher.getCreatedAt()), ZoneOffset.UTC).plusYears(2).toInstant().toEpochMilli();
        assertEquals(expected, voucherInfoResponse.getExpiresAt());
    }

    @Test(expected = IllegalOperationException.class)
    public void calculateStatus_error1() {
        // error when other situation
        VoucherInfoResponse.from(randomVoucher()
                .withSold(false)
                .withRedeemed(false));
    }

    @Test(expected = IllegalOperationException.class)
    public void calculateStatus_error2() {
        // error when other situation
        VoucherInfoResponse.from(randomVoucher()
                .withSold(false)
                .withRedeemed(false)
                .withExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli())
        );
    }

    @Test
    public void whenMaltaVoucherExpirationIs30thNov() {
        Voucher voucher = randomValidVoucher()
                .withSku(MALTA_VOUCHER_SKU)
                .withExpiresAt(0)
                .withCreatedAt(Instant.now().toEpochMilli());

        VoucherInfoResponse voucherInfoResponse = VoucherInfoResponse.from(voucher);
        // when !redeemed, not expired
        assertEquals("valid", voucherInfoResponse.getStatus());

        String expected = "" + EXPIRATION_TIME;

        assertEquals(expected, voucherInfoResponse.getExpiresAt());
    }
}