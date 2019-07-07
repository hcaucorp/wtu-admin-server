package com.jvmp.vouchershop.voucher.validators;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.jvmp.vouchershop.utils.RandomUtils.randomRedemptionRequest;
import static com.jvmp.vouchershop.utils.RandomUtils.randomValidVoucher;
import static com.jvmp.vouchershop.utils.TryUtils.expectingException;
import static com.jvmp.vouchershop.voucher.validators.OnePerCustomerForMaltaPromotion.MALTA_VOUCHER_SKU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OnePerCustomerForMaltaPromotionTest {

    @Mock
    private VoucherRepository voucherRepository;

    @InjectMocks
    private OnePerCustomerForMaltaPromotion subject;

    @Before
    public void setUp() {
        Voucher voucher = randomValidVoucher()
                .withSku(MALTA_VOUCHER_SKU);

        when(voucherRepository.findByCode(any())).thenReturn(Optional.of(voucher));
    }

    @Test
    public void shouldBePristineAddressFailure() {
        RedemptionRequest redemptionRequest = randomRedemptionRequest();
        subject.cachePut(redemptionRequest.getDestinationAddress());

        Throwable throwable = expectingException(() -> subject.validate(redemptionRequest));

        assertEquals(IllegalOperationException.class, throwable.getClass());
        assertEquals("You've already used one voucher! AI and Blockchain Summit promotional gift cards are one per customer :(", throwable.getMessage());
    }

    @Test
    public void shouldBePristineAddressSuccess() {
        RedemptionRequest redemptionRequest = randomRedemptionRequest();
        subject.validate(redemptionRequest);
    }

    @Test
    public void afterRedeemMarkDestinationAddressAsUsed() {
        RedemptionRequest redemptionRequest = randomRedemptionRequest();
        subject.redeemed(redemptionRequest);

        assertTrue(subject.cacheContains(redemptionRequest.getDestinationAddress()));
    }
}