package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.fulfillment.Fulfillment;
import com.jvmp.vouchershop.repository.FulfillmentRepository;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.security.Auth0Service;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import static com.jvmp.vouchershop.utils.RandomUtils.randomFulfillment;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                Application.class,
                Auth0Service.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FulfillmentControllerIT {

    @LocalServerPort
    private int port;

    private URL base;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private Auth0Service auth0Service;

    private String authorizationValue;

    @Autowired
    private FulfillmentRepository fulfillmentRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Before
    public void setUp() throws Exception {
        base = new URL("http://localhost:" + port + "/api/fulfillments");
        authorizationValue = "Bearer " + auth0Service.getToken().accessToken;
    }

    @Test
    public void getFulfillmentForOrder() {
        long orderId = nextLong();
        List<Fulfillment> testFulfillments = asList(randomFulfillment().withOrderId(orderId), randomFulfillment());

        testFulfillments.forEach(fulfillment -> fulfillment.setVouchers(new HashSet<>((voucherRepository.saveAll(fulfillment.getVouchers())))));
        fulfillmentRepository.saveAll(testFulfillments);

        assertEquals(2, fulfillmentRepository.count());

        String url = base.toString() + "/" + orderId;

        RequestEntity<Void> requestEntity = RequestEntity
                .get(URI.create(url))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .build();

        ResponseEntity<Fulfillment> responseEntity = template.exchange(url, GET, requestEntity, Fulfillment.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        testFulfillments.forEach(fulfillment -> fulfillmentRepository.delete(fulfillment));
    }

    @Test
    public void getFulfillemntsRequiresAuthentication() {
        ResponseEntity<Fulfillment> responseEntity = template.getForEntity(base.toString() + "/1", Fulfillment.class);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }
}