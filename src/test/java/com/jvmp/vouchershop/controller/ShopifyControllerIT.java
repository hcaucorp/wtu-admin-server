package com.jvmp.vouchershop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.security.Auth0Service;
import com.jvmp.vouchershop.security.HmacUtil;
import com.jvmp.vouchershop.shopify.ShopifyService;
import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.shopify.domain.OrderList;
import com.jvmp.vouchershop.system.PropertyNames;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URL;
import java.util.function.Function;

import static com.jvmp.vouchershop.controller.ShopifyController.HTTP_HEADER_X_SHOPIFY_HMAC_SHA256;
import static com.jvmp.vouchershop.utils.RandomUtils.randomOrder;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                Application.class,
                Auth0Service.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MockBean(NotificationService.class)
@ActiveProfiles("it")
public class ShopifyControllerIT {

    @LocalServerPort
    private int port;

    private URL base;

    @Value(PropertyNames.SHOPIFY_WEBHOOK_SHARED_SECRET)
    private String webhookSecret;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private Auth0Service auth0Service;

    private String authorizationValue;

    @MockBean
    private ShopifyService shopifyService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Function<String, String> hmacHashingFunction = input -> HmacUtil.encodeMaybe(webhookSecret, input.getBytes()).orElseThrow(AssertionError::new);

    @Before
    public void setUp() throws Exception {
        base = new URL("http://localhost:" + port + "/api");
        authorizationValue = "Bearer " + auth0Service.getToken().accessToken;
    }

    @Test
    public void fulfillmentHookSuccess() throws Exception {
        ResponseEntity<String> entity = template
                .postForEntity(base.toString() + "/shopify/webhook/fulfill", getRequest(hmacHashingFunction), String.class);

        assertEquals(HttpStatus.ACCEPTED, entity.getStatusCode());
    }

    @Test
    public void fulfillmentHookInvalidHeader() throws Exception {
        ResponseEntity<String> entity = template
                .postForEntity(base.toString() + "/shopify/webhook/fulfill", getRequest(Function.identity()), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }

    private HttpEntity<String> getRequest(Function<String, String> hasher) throws Exception {
        String body = objectMapper.writeValueAsString(randomOrder());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HTTP_HEADER_X_SHOPIFY_HMAC_SHA256, hasher.apply(body));
        return new HttpEntity<>(body, headers);
    }

    @Test
    public void unfulfilledOrdersCount_requiresAuthorization() {
        int count = nextInt();
        when(shopifyService.unfulfilledOrdersCount()).thenReturn(count);
        HttpStatus statusCode = template.getForEntity(base.toString() + "/shopify/orders/unfulfilled/count", String.class).getStatusCode();

        assertEquals(HttpStatus.UNAUTHORIZED, statusCode);
    }

    @Test
    public void unfulfilledOrdersCount_successPath() {
        int count = nextInt();
        when(shopifyService.unfulfilledOrdersCount()).thenReturn(count);

        String url = base.toString() + "/shopify/orders/unfulfilled/count";

        RequestEntity<?> request = RequestEntity
                .get(URI.create(url))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .build();

        ResponseEntity<Integer> entity = template.exchange(url, HttpMethod.GET, request, Integer.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());

        assertNotNull(entity.getBody());
        assertEquals(count, entity.getBody().intValue());
    }

    @Test
    public void fulfillOrders_requiresAuthorization() {
        Order order = new Order();
        order.setId(nextLong());
        order.setOrderNumber(nextLong());
        OrderList orderList = new OrderList();
        orderList.setOrders(singletonList(order));
        when(shopifyService.findUnfulfilledOrders()).thenReturn(orderList);

        HttpStatus statusCode = template.postForEntity(base.toString() + "/shopify/orders/fulfill", null, String.class).getStatusCode();

        assertEquals(HttpStatus.UNAUTHORIZED, statusCode);
    }

    @Test
    public void fulfillOrders_successPath() {
        Order order = new Order();
        order.setId(nextLong());
        OrderList orderList = new OrderList();
        orderList.setOrders(singletonList(order));
        when(shopifyService.findUnfulfilledOrders()).thenReturn(orderList);

        String url = base.toString() + "/shopify/orders/fulfill";

        RequestEntity<?> request = RequestEntity
                .post(URI.create(url))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .build();

        HttpStatus statusCode = template.postForEntity(url, request, String.class).getStatusCode();

        assertEquals(HttpStatus.OK, statusCode);
        verify(shopifyService, times(1)).markOrderFulfilled(order.getId());
    }

    @Test
    public void refundVoucher_aka_delete() {
        Order order = randomOrder();
        String url = base.toString() + "/shopify/orders/" + order.getId() + "/refund";

        RequestEntity<Void> requestEntity = RequestEntity
                .post(URI.create(url))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .build();

        ResponseEntity<String> responseEntity = template.exchange(url, HttpMethod.POST, requestEntity, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
