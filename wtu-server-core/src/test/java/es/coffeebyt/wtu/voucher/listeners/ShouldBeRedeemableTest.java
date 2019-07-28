package es.coffeebyt.wtu.voucher.listeners;

import es.coffeebyt.wtu.exception.IllegalOperationException;
import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.utils.RandomUtils;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static es.coffeebyt.wtu.utils.TryUtils.expectingException;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ShouldBeRedeemableTest {

    @Mock
    private VoucherRepository voucherRepository;

    @InjectMocks
    private ShouldBeRedeemable subject;

    @Test
    public void shouldBePublishedToRedeemFailure() {
        Voucher voucher = RandomUtils.randomValidVoucher()
                .withPublished(false);
        when(voucherRepository.findByCode(any())).thenReturn(Optional.of(voucher));

        RedemptionRequest redemptionRequest = RandomUtils.randomRedemptionRequest();
        Throwable throwable = expectingException(() -> subject.validate(redemptionRequest));

        assertEquals(IllegalOperationException.class, throwable.getClass());
        assertEquals("Attempting to redeem unpublished voucher " + voucher.getCode(), throwable.getMessage());
    }

    @Test
    public void shouldBePublishedToRedeemSuccess() {
        when(voucherRepository.findByCode(any())).thenReturn(Optional.of(RandomUtils.randomValidVoucher()
                .withPublished(true)));

        RedemptionRequest redemptionRequest = RandomUtils.randomRedemptionRequest();
        subject.validate(redemptionRequest);
    }
}