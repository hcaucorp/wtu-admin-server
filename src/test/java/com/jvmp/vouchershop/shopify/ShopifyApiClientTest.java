package com.jvmp.vouchershop.shopify;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.security.Auth0Service;
import com.jvmp.vouchershop.shopify.domain.Count;
import com.jvmp.vouchershop.shopify.domain.FulfillmentItem;
import com.jvmp.vouchershop.shopify.domain.FulfillmentResource;
import com.jvmp.vouchershop.shopify.domain.OrderList;
import com.jvmp.vouchershop.utils.IO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.jvmp.vouchershop.shopify.domain.FinancialStatus.paid;
import static com.jvmp.vouchershop.shopify.domain.FulfillmentStatus.unshipped;
import static com.jvmp.vouchershop.shopify.domain.OrderStatus.open;
import static java.util.Collections.emptyList;
import static junit.framework.TestCase.fail;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                Application.class,
                Auth0Service.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShopifyApiClientTest {

    private final String GET_ORDERS_RESPONSE = fromFile("get-orders-response.json");
    private final String GET_ORDERS_COUNT_RESPONSE = fromFile("get-orders-count-response.json");
    private final String POST_FULFILLMENT_RESPONSE = fromFile("post-orders-fulfillments-response.json");
    @LocalServerPort
    private int port;
    @Autowired
    private ShopifyApiClient client;
    private WireMockServer mockServer;

    private static String fromFile(String fileName) {
        try {
            return IO.fromFile(ShopifyApiClientTest.class, fileName);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return null;
    }

    @Before
    public void setUp() {
        int mockPort = port + 1;
        mockServer = new WireMockServer();
        configureFor("localhost", mockPort);

        mockServer.start();
    }

    @After
    public void tearDown() {
        mockServer.stop();
    }

    @Test
    public void testGetOrders() {
        stubFor(get(urlEqualTo("/admin/orders.json")).willReturn(aResponse().withBody(GET_ORDERS_RESPONSE)));

        OrderList orders = client.getOrders(open.toString(), unshipped.toString(), paid.toString());

        assertNotNull(orders);
        assertTrue(isNotEmpty(orders.getOrders()));
    }

    @Test
    public void testGetOrdersCount() {
        stubFor(get(urlEqualTo("/admin/orders/count.json")).willReturn(aResponse().withBody(GET_ORDERS_COUNT_RESPONSE)));

        Count count = client.getOrdersCount(open.toString(), unshipped.toString(), paid.toString());

        assertNotNull(count);
        assertEquals(1, count.getCount());
    }

    @Test
    public void testFulfillOrder() {
        long orderId = nextLong();

        stubFor(get(urlEqualTo("/admin/orders/" + orderId + "/fulfillments.json")).willReturn(aResponse().withBody(POST_FULFILLMENT_RESPONSE)));

        FulfillmentResource request = new FulfillmentResource(new FulfillmentItem(905684977, "1de9d8b7844984c23c03d19b138925ef", emptyList(), false));

        FulfillmentResource response = client.fulfillOrder(orderId, request);

        assertNotNull(response);
    }
}