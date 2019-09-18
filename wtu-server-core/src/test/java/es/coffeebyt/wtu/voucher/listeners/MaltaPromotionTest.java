package es.coffeebyt.wtu.voucher.listeners;

import java.util.Optional;

import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.utils.RandomUtils;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;
import es.coffeebyt.wtu.voucher.listeners.MaltaPromotion.MaltaCardException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static es.coffeebyt.wtu.exception.WtuErrorCodes.ONE_PER_CUSTOMER;
import static es.coffeebyt.wtu.utils.TryUtils.expectingException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MaltaPromotionTest {

    @Mock
    private VoucherRepository voucherRepository;

    @InjectMocks
    private MaltaPromotion subject;

    @Before
    public void setUp() {
        Voucher voucher = RandomUtils.randomValidVoucher()
                .withSku(MaltaPromotion.MALTA_VOUCHER_SKU);

        when(voucherRepository.findByCode(any())).thenReturn(Optional.of(voucher));
    }

    @Test
    public void shouldBePristineAddressFailure() {
        RedemptionRequest redemptionRequest = RandomUtils.randomRedemptionRequest();
        subject.cachePut(redemptionRequest.getDestinationAddress());

        Throwable throwable = expectingException(() -> subject.validate(redemptionRequest));

        assertEquals(MaltaCardException.class, throwable.getClass());
        assertEquals(ONE_PER_CUSTOMER.name(), throwable.getMessage());
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
