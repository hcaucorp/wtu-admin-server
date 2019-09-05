package es.coffeebyt.wtu.voucher.listeners;

import static es.coffeebyt.wtu.utils.RandomUtils.randomRedemptionRequest;
import static es.coffeebyt.wtu.utils.RandomUtils.randomValidVoucher;
import static es.coffeebyt.wtu.utils.TryUtils.expectingException;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import es.coffeebyt.wtu.exception.IllegalOperationException;
import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;

@RunWith(MockitoJUnitRunner.class)
public class ShouldBeRedeemableTest {

    @Mock
    private VoucherRepository voucherRepository;

    @InjectMocks
    private ShouldBeRedeemable subject;

    @Test
    public void shouldBePublishedToRedeemFailure() {
        Voucher voucher = randomValidVoucher()
                .withPublished(false);
        when(voucherRepository.findByCode(any())).thenReturn(Optional.of(voucher));

        RedemptionRequest redemptionRequest = randomRedemptionRequest();
        Throwable throwable = expectingException(() -> subject.validate(redemptionRequest));

        assertEquals(IllegalOperationException.class, throwable.getClass());
        assertEquals("Attempting to redeem unpublished voucher " + voucher.getCode(), throwable.getMessage());
    }

    @Test
    public void shouldBePublishedToRedeemSuccess() {
        when(voucherRepository.findByCode(any())).thenReturn(Optional.of(randomValidVoucher()
                .withPublished(true)));

        RedemptionRequest redemptionRequest = randomRedemptionRequest();
        subject.validate(redemptionRequest);
    }

    @Ignore
    @Test
    public void expiredVoucherShallNotPass() {
        Voucher voucher = randomValidVoucher()
                .withExpiresAt(Instant.now().minus(1, ChronoUnit.SECONDS).toEpochMilli());

        when(voucherRepository.findByCode(any())).thenReturn(Optional.of(voucher));

        RedemptionRequest redemptionRequest = randomRedemptionRequest();
        Throwable throwable = expectingException(() -> subject.validate(redemptionRequest));

        assertEquals(IllegalOperationException.class, throwable.getClass());
        assertEquals("Attempting to redeem expired voucher " + voucher.getCode(), throwable.getMessage());
    }

    @Ignore
    @Test
    public void shouldNotBeExpiredToPass() {
        Voucher voucher = randomValidVoucher();
        when(voucherRepository.findByCode(any())).thenReturn(Optional.of(voucher));

        RedemptionRequest redemptionRequest = randomRedemptionRequest();
        subject.validate(redemptionRequest);
    }
}