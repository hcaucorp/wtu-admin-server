package es.coffeebyt.wtu.voucher;

import es.coffeebyt.wtu.exception.IllegalOperationException;
import org.junit.Test;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static es.coffeebyt.wtu.utils.RandomUtils.randomValidVoucher;
import static es.coffeebyt.wtu.utils.RandomUtils.randomVoucher;
import static org.junit.Assert.assertEquals;

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
}
