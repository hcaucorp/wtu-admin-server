package com.jvmp.vouchershop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.security.HmacUtil;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;
import java.util.function.Function;

import static com.jvmp.vouchershop.RandomUtils.randomOrder;
import static com.jvmp.vouchershop.controller.ShopifyController.HTTP_HEADER_X_SHOPIFY_HMAC_SHA256;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MockBean(NotificationService.class)
public class ShopifyControllerIT {

    @LocalServerPort
    private int port;

    private URL base;

    @Value(PropertyNames.SHOPIFY_WEBHOOK_SHARED_SECRET)
    private String webhookSecret;

    @Autowired
    private TestRestTemplate template;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Function<String, String> hmacHashingFunction = input -> HmacUtil.encode1(webhookSecret, input.getBytes()).orElseThrow(AssertionError::new);

    @Before
    public void setUp() throws Exception {
        base = new URL("http://localhost:" + port + "/");
    }

    @Test
    public void fullFillmentHookSuccess() throws Exception {
        ResponseEntity<String> entity = template
                .postForEntity(base.toString() + "/shopify/webhook/fulfill", getRequest(hmacHashingFunction), String.class);

        assertEquals(HttpStatus.ACCEPTED, entity.getStatusCode());
    }

    @Test
    public void fullFillmentHookInvalidHeader() throws Exception {
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
}
