package com.jvmp.vouchershop.voucher.validators;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.jvmp.vouchershop.utils.RandomUtils.randomRedemptionRequest;
import static com.jvmp.vouchershop.utils.RandomUtils.randomValidVoucher;
import static com.jvmp.vouchershop.utils.TryUtils.expectingException;
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
}