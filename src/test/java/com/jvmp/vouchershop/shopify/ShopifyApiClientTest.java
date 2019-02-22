package com.jvmp.vouchershop.shopify;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.security.Auth0Service;
import com.jvmp.vouchershop.shopify.domain.Count;
import com.jvmp.vouchershop.shopify.domain.FulfillmentItem;
import com.jvmp.vouchershop.shopify.domain.FulfillmentResource;
import com.jvmp.vouchershop.shopify.domain.OrderList;
import com.jvmp.vouchershop.utils.IO;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.jvmp.vouchershop.shopify.domain.FinancialStatus.paid;
import static com.jvmp.vouchershop.shopify.domain.FulfillmentStatus.unshipped;
import static com.jvmp.vouchershop.shopify.domain.OrderStatus.open;
import static java.util.Collections.emptyList;
import static junit.framework.TestCase.fail;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                Application.class,
                Auth0Service.class
        })
@ActiveProfiles("it")
public class ShopifyApiClientTest {

    private final String GET_ORDERS_RESPONSE = fromFile("get-orders-response.json");
    private final String GET_ORDERS_COUNT_RESPONSE = fromFile("get-orders-count-response.json");
    private final String POST_FULFILLMENT_RESPONSE = fromFile("post-orders-fulfillments-response.json");

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8888);

    @Autowired
    private ShopifyApiClient client;

    private static String fromFile(String fileName) {
        try {
            return IO.fromFile(ShopifyApiClientTest.class, fileName);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return null;
    }

    @Test
    public void testGetOrders() {
        wireMockRule.stubFor(get(urlEqualTo("/admin/orders.json?status=open&fulfillment_status=unshipped&financial_status=paid"))
                .willReturn(aResponse()
                        .withBody(GET_ORDERS_RESPONSE)
                        .withHeader("Content-Type", "application/json")));

        OrderList orders = client.getOrders(open.toString(), unshipped.toString(), paid.toString());

        assertNotNull(orders);
        assertTrue(isNotEmpty(orders.getOrders()));
    }

    @Test
    public void testGetOrdersCount() {
        stubFor(get(urlEqualTo("/admin/orders/count.json?status=open&fulfillment_status=unshipped&financial_status=paid"))
                .willReturn(aResponse()
                        .withBody(GET_ORDERS_COUNT_RESPONSE)
                        .withHeader("Content-Type", "application/json")));

        Count count = client.getOrdersCount(open.toString(), unshipped.toString(), paid.toString());

        assertNotNull(count);
        assertEquals(1, count.getCount());
    }

    @Test
    public void testFulfillOrder() {
        long orderId = nextLong();

        stubFor(post(urlEqualTo("/admin/orders/" + orderId + "/fulfillments.json"))
                .willReturn(aResponse()
                        .withBody(POST_FULFILLMENT_RESPONSE)
                        .withHeader("Content-Type", "application/json")));

        FulfillmentResource request = new FulfillmentResource(new FulfillmentItem(905684977, "1de9d8b7844984c23c03d19b138925ef", emptyList(), false));

        FulfillmentResource response = client.fulfillOrder(orderId, request);

        assertNotNull(response);
    }
}