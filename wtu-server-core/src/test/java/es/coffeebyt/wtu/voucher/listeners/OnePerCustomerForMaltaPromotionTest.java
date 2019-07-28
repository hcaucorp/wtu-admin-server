package es.coffeebyt.wtu.voucher.listeners;

import static es.coffeebyt.wtu.utils.TryUtils.expectingException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import es.coffeebyt.wtu.exception.IllegalOperationException;
import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.utils.RandomUtils;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class OnePerCustomerForMaltaPromotionTest {

    @Mock
    private VoucherRepository voucherRepository;

    @InjectMocks
    private OnePerCustomerForMaltaPromotion subject;

    @Before
    public void setUp() {
        Voucher voucher = RandomUtils.randomValidVoucher()
                .withSku(OnePerCustomerForMaltaPromotion.MALTA_VOUCHER_SKU);

        when(voucherRepository.findByCode(any())).thenReturn(Optional.of(voucher));
    }

    @Test
    public void shouldBePristineAddressFailure() {
        RedemptionRequest redemptionRequest = RandomUtils.randomRedemptionRequest();
        subject.cachePut(redemptionRequest.getDestinationAddress());

        Throwable throwable = expectingException(() -> subject.validate(redemptionRequest));

        assertEquals(IllegalOperationException.class, throwable.getClass());
        assertEquals("You've already used one voucher! AI and Blockchain Summit promotional gift cards are one per customer :(", throwable.getMessage());
    }

    @Test
    public void shouldBePristineAddressSuccess() {
        RedemptionRequest redemptionRequest = RandomUtils.randomRedemptionRequest();
        subject.validate(redemptionRequest);
    }

    @Test
    public void afterRedeemMarkDestinationAddressAsUsed() {
        RedemptionRequest redemptionRequest = RandomUtils.randomRedemptionRequest();
        subject.redeemed(redemptionRequest);

        assertTrue(subject.cacheContains(redemptionRequest.getDestinationAddress()));
    }
}