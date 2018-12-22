package com.jvmp.vouchershop.fulfillment;

import com.jvmp.vouchershop.email.EmailService;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.exception.InvalidOrderException;
import com.jvmp.vouchershop.repository.FulfillmentRepository;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.shopify.ShopifyService;
import com.jvmp.vouchershop.shopify.domain.Customer;
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

import java.util.List;

import static com.jvmp.vouchershop.RandomUtils.order;
import static com.jvmp.vouchershop.fulfillment.FulfillmentStatus.completed;
import static com.jvmp.vouchershop.fulfillment.FulfillmentStatus.initiated;
import static com.jvmp.vouchershop.voucher.VoucherRandomUtils.voucher;
import static com.jvmp.vouchershop.voucher.impl.DefaultVoucherService.DEFAULT_VOUCHER_CODE_GENERATOR;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DefaultFulFillmentServiceTest {

    @Mock
    private FulfillmentRepository fulfillmentRepository;

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private ShopifyService shopifyService;

    @Mock
    private EmailService emailService;

    private DefaultFulFillmentService service;

    private Fulfillment fulfillment;

    @Before
    public void setUp() {
        service = new DefaultFulFillmentService(fulfillmentRepository, voucherRepository, shopifyService, emailService);
        fulfillment = new Fulfillment()
                .withVouchers(emptyList())
                .withOrderId(nextLong(0, Long.MAX_VALUE));
    }

    @Test
    public void fulfillOrder() {
        long orderId = nextLong(0, Long.MAX_VALUE);
        Voucher v = voucher()
                .withAmount(100)
                .withCode(DEFAULT_VOUCHER_CODE_GENERATOR.get());
        String email = "test@email." + RandomStringUtils.randomAlphabetic(3);

        service.fulfillOrder(order()
                .withId(orderId)
                .withCustomer(new Customer().withEmail(email)));

        verify(shopifyService, times(1)).markOrderFulfilled(eq(orderId));
        verify(emailService, times(1)).sendVouchers(eq(singletonList(v)), eq(email));
    }

    @Test
    public void completeFulFillment() {
        service.completeFulFillment(fulfillment);

        verify(fulfillmentRepository, times(1)).save(eq(fulfillment.withStatus(completed)));
        fulfillment.getVouchers().forEach(voucher ->
                verify(voucherRepository, times(1)).save(notNull()));
    }

    @Test
    public void initiateFulFillment() {
        Order order = new Order();
        order.setLineItems(asList(
                new LineItem()
                        .withSku("SKU" + RandomStringUtils.randomNumeric(4))
                        .withQuantity(RandomUtils.nextInt(1, 5)),
                new LineItem()
                        .withSku("SKU" + RandomStringUtils.randomNumeric(4))
                        .withQuantity(RandomUtils.nextInt(1, 5))
        ));
        List<Voucher> vouchers = asList(
                voucher()
                        .withSku(order.getLineItems().get(0).getSku())
                        .withAmount(100)
                        .withCode(DEFAULT_VOUCHER_CODE_GENERATOR.get()),
                voucher()
                        .withSku(order.getLineItems().get(1).getSku())
                        .withAmount(100)
                        .withCode(DEFAULT_VOUCHER_CODE_GENERATOR.get())
        );

        Fulfillment fulfillment = service.initiateFulFillment(order, vouchers);

        assertEquals(initiated, fulfillment.getStatus());
        verify(fulfillmentRepository, times(1)).save(eq(fulfillment));
    }

    @Test(expected = InvalidOrderException.class)
    public void checkIfSupplyIsEnough() {
        service.checkIfSupplyIsEnough(
                singletonList(ImmutablePair.of("sku", 2)), emptyList()
        );
    }

    @Test
    public void findSupplyForDemand() {
        List<ImmutablePair<String, Integer>> demand = singletonList(ImmutablePair.of("sku", 2));

        service.findSupplyForDemand(demand);

        verify(voucherRepository, times(1)).findBySoldFalseAndSku(eq("sku"));
    }

    @Test
    public void findVouchersSkuAndQuantity() {
        List<ImmutablePair<String, Integer>> skuAndQuantity = service.findVouchersSkuAndQuantity(order());

        assertEquals(asList(
                ImmutablePair.of("sku 123", 1),
                ImmutablePair.of("sku 321", 2)
        ), skuAndQuantity);
    }

    @Test(expected = IllegalOperationException.class)
    public void checkIfOrderIHasNotBeenFulFilledYet() {
        service.checkIfOrderIHasNotBeenFulFilledYet(order());
    }

    @Test(expected = InvalidOrderException.class)
    public void checkIfOrderIsValid() {
        service.checkIfOrderIsValid(order());
    }

    @Test(expected = InvalidOrderException.class)
    public void checkIfOrderHasBeenFullyPaid() {
        service.checkIfOrderHasBeenFullyPaid(order());
    }
}