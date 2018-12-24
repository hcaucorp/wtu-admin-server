package com.jvmp.vouchershop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvmp.vouchershop.fulfillment.FulFillmentService;
import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.system.PropertyNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ShopifyController {

    private final ObjectMapper objectMapper;
    private final FulFillmentService fulFillmentService;

    @Value(PropertyNames.SHOPIFY_WEBHOOK_SHARED_SECRET)
    private String webhookSecret;

    /**
     * Triggered when something has been sold on Shopify and needs fullfilling, fulfillment/create webhook
     */
    @PostMapping("/shopify/webhook/fulfill")
    public ResponseEntity<?> fullFillmentHook(@RequestBody String body, @RequestHeader HttpHeaders headers)
            throws InvalidKeyException, NoSuchAlgorithmException, IOException {
        String hashFromRequest = headers.getFirst("X-Shopify-Hmac-SHA256");
        String calculatedHash = HmacUtil.encode(webhookSecret, body);

        if (calculatedHash != null && calculatedHash.equals(hashFromRequest)) {
            Order order = objectMapper.readValue(body, Order.class);

            // TODO 2: need a stress test to check race conditions (eg. if one voucher can be claimed for 2 orders?)
            CompletableFuture.runAsync(
                    () -> fulFillmentService.fulfillOrder(order))
                    .thenAccept(ignore -> log.info("Order {} fulfilled.", order.getId()));

            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } else {
            log.error("Received invalid message (not from Shopify?). Expected hash {} but found {}", calculatedHash, hashFromRequest);
            return ResponseEntity.badRequest().build();
        }
    }
}
