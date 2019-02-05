package com.jvmp.vouchershop.fulfillment;

import com.jvmp.vouchershop.email.EmailService;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.exception.InvalidOrderException;
import com.jvmp.vouchershop.repository.FulfillmentRepository;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.shopify.ShopifyService;
import com.jvmp.vouchershop.shopify.domain.Customer;
import com.jvmp.vouchershop.shopify.domain.FinancialStatus;
import com.jvmp.vouchershop.shopify.domain.LineItem;
import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.voucher.Voucher;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

import static com.jvmp.vouchershop.Collections.asSet;
import static com.jvmp.vouchershop.fulfillment.FulfillmentStatus.completed;
import static com.jvmp.vouchershop.fulfillment.FulfillmentStatus.initiated;
import static com.jvmp.vouchershop.utils.RandomUtils.*;
import static com.jvmp.vouchershop.voucher.impl.DefaultVoucherService.DEFAULT_VOUCHER_CODE_GENERATOR;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultFulfillmentServiceTest {

    @Mock
    private FulfillmentRepository fulfillmentRepository;

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private ShopifyService shopifyService;

    @Mock
    private EmailService emailService;

    private DefaultFulfillmentService service;

    private Fulfillment fulfillment;

    @Before
    public void setUp() {
        service = new DefaultFulfillmentService(fulfillmentRepository, voucherRepository, shopifyService, emailService);
        fulfillment = new Fulfillment()
                .withOrderId(nextLong(0, Long.MAX_VALUE));
    }

    @Test
    public void fulfillOrder() {
        long orderId = nextLong(0, Long.MAX_VALUE);
        String sku = randomSku();
        Voucher voucher = randomVoucher()
                .withAmount(100)
                .withCode(DEFAULT_VOUCHER_CODE_GENERATOR.get())
                .withSku(sku);
        String email = "test@email." + RandomStringUtils.randomAlphabetic(3);

        when(voucherRepository.findBySoldFalseAndSku(eq(sku))).thenReturn(singletonList(voucher));

        Fulfillment fulfillment = new Fulfillment()
                .withVouchers(singleton(voucher))
                .withOrderId(orderId)
                .withStatus(initiated);

        Fulfillment savedFulfillment = fulfillment.withId(nextLong(1, Long.MAX_VALUE));

        when(fulfillmentRepository.save(eq(fulfillment))).thenReturn(savedFulfillment);

        service.fulfillOrder(randomOrder()
                .withId(orderId)
                .withFinancialStatus(FinancialStatus.paid)
                .withCustomer(new Customer().withEmail(email))
                .withLineItems(singletonList(
                        new LineItem()
                                .withSku(sku)
                                .withQuantity(1)
                ))
        );

        verify(shopifyService, times(1)).markOrderFulfilled(eq(orderId));
        verify(emailService, times(1)).sendVouchers(eq(singleton(voucher)), eq(email));
        verify(fulfillmentRepository, times(1)).save(eq(fulfillment.withStatus(FulfillmentStatus.initiated)));
        verify(fulfillmentRepository, times(1)).save(eq(savedFulfillment.withStatus(FulfillmentStatus.completed)));
        verify(voucherRepository, times(1)).save(eq(
                voucher
                        .withSold(true)

                // TODO add expires at calculation
        ));
    }

    @Test
    public void completeFulfillment() {
        Set<Voucher> vouchers = asSet(
                randomVoucher()
                        .withSku(randomSku())
                        .withAmount(100)
                        .withCode(DEFAULT_VOUCHER_CODE_GENERATOR.get()),
                randomVoucher()
                        .withSku(randomSku())
                        .withAmount(100)
                        .withCode(DEFAULT_VOUCHER_CODE_GENERATOR.get())
        );

        fulfillment.setVouchers(vouchers);

        service.completeFulFillment(fulfillment);

        verify(fulfillmentRepository, times(1)).save(eq(fulfillment.withStatus(completed)));
        fulfillment.getVouchers().forEach(voucher ->
                verify(voucherRepository, times(1)).save(eq(voucher.withSold(true))));
    }

    @Test
    public void initiateFulfillment() {
        Order order = randomOrder();
        order.setLineItems(asList(
                new LineItem()
                        .withSku(randomSku())
                        .withQuantity(RandomUtils.nextInt(1, 5)),
                new LineItem()
                        .withSku(randomSku())
                        .withQuantity(RandomUtils.nextInt(1, 5))
        ));
        Set<Voucher> vouchers = new HashSet<>(asList(
                randomVoucher()
                        .withSku(order.getLineItems().get(0).getSku())
                        .withAmount(100)
                        .withCode(DEFAULT_VOUCHER_CODE_GENERATOR.get()),
                randomVoucher()
                        .withSku(order.getLineItems().get(1).getSku())
                        .withAmount(100)
                        .withCode(DEFAULT_VOUCHER_CODE_GENERATOR.get())
        ));
        fulfillment.setVouchers(vouchers);
        fulfillment.setOrderId(order.getId());
        fulfillment.setStatus(initiated);

        when(fulfillmentRepository.save(eq(fulfillment))).thenReturn(fulfillment);

        Fulfillment fulfillment = service.initiateFulfillment(order, vouchers);

        assertEquals(initiated, fulfillment.getStatus());
        verify(fulfillmentRepository, times(1)).save(eq(fulfillment));
    }

    @Test(expected = InvalidOrderException.class)
    public void checkIfSupplyIsEnough() {
        service.checkIfSupplyIsEnough(
                singleton(ImmutablePair.of("sku", 2)), emptySet()
        );
    }


    @Test
    public void checkIfSupplyIsEnough_oneItem_shouldSucceed() {
        String sku = randomSku();
        service.checkIfSupplyIsEnough(
                singleton(ImmutablePair.of(sku, 2)),
                asSet(
                        randomVoucher().withSku(sku),
                        randomVoucher().withSku(sku),
                        randomVoucher().withSku(sku)
                )
        );
    }

    @Test
    public void findSupplyForDemand() {
        Set<ImmutablePair<String, Integer>> demand = singleton(ImmutablePair.of("sku", 2));

        service.findSupplyForDemand(demand);

        verify(voucherRepository, times(1)).findBySoldFalseAndSku(eq("sku"));
    }

    @Test
    public void findVouchersSkuAndQuantity() {
        String sku1 = randomString(), sku2 = randomString();
        Set<ImmutablePair<String, Integer>> skuAndQuantity = service.findVouchersSkuAndQuantity(randomOrder()
                .withLineItems(asList(
                        new LineItem().withSku(sku1).withQuantity(1),
                        new LineItem().withSku(sku2).withQuantity(2)
                )));

        assertEquals(new HashSet<>(asList(
                ImmutablePair.of(sku1, 1),
                ImmutablePair.of(sku2, 2)
        )), skuAndQuantity);
    }

    @Test(expected = IllegalOperationException.class)
    public void checkIfOrderIHasNotBeenFulFilledYet() {
        long orderId = nextLong(0, Long.MAX_VALUE);

        when(fulfillmentRepository.findByOrderId(eq(orderId))).thenReturn(new Fulfillment().withStatus(FulfillmentStatus.completed));

        service.checkIfOrderIHasNotBeenFulfilledYet(randomOrder().withId(orderId));
    }

    @Test(expected = InvalidOrderException.class)
    public void checkIfOrderIsValid() {
        service.checkIfOrderIsValid(randomOrder());
    }

    @Test(expected = InvalidOrderException.class)
    public void checkIfOrderHasBeenFullyPaid() {
        service.checkIfOrderHasBeenFullyPaid(randomOrder());
    }
}