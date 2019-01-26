package com.jvmp.vouchershop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvmp.vouchershop.fulfillment.FulfillmentService;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.security.HmacUtil;
import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.system.PropertyNames;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequiredArgsConstructor
public class ShopifyController {

    static final String HTTP_HEADER_X_SHOPIFY_HMAC_SHA256 = "X-Shopify-Hmac-SHA256";
    private final ObjectMapper objectMapper;
    private final FulfillmentService fulfillmentService;
    private final NotificationService notificationService;

    @Value(PropertyNames.AWS_SNS_TOPIC_ORDERS)
    private String ordersTopic;

    @Value(PropertyNames.SHOPIFY_WEBHOOK_SHARED_SECRET)
    private String webhookSecret;

    /**
     * Triggered when something has been sold on Shopify and needs fulfilling, fulfillment/create webhook
     */
    @PostMapping("/shopify/webhook/fulfill")
    public ResponseEntity<?> fulfillmentHook(@RequestBody byte[] body, @RequestHeader HttpHeaders headers)
            throws InvalidKeyException, NoSuchAlgorithmException, IOException {
        String hashFromRequest = headers.getFirst(HTTP_HEADER_X_SHOPIFY_HMAC_SHA256);
        String calculatedHash = HmacUtil.encode(webhookSecret, body);

        if (calculatedHash != null && calculatedHash.equals(hashFromRequest)) {
            Order order = objectMapper.readValue(body, Order.class);

            //noinspection ResultOfMethodCallIgnored
            Flowable.fromCallable(() -> fulfillmentService.fulfillOrder(order))
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.single())
                    .subscribe(
                            fulfillment -> notificationService.push("Order " + fulfillment.getOrderId() + " fulfilled.", ordersTopic),
                            throwable -> notificationService.push("Order fulfillment failed: " + throwable.getMessage(), ordersTopic));

            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } else {
            notificationService.push("Fulfillment attempted but failed. Expected request hash is " + calculatedHash + " but received invalid message hash " + hashFromRequest, ordersTopic);
            return ResponseEntity.badRequest().build();
        }
    }

    // TODO
//    @PostMapping("/shopify/orders")
//    public ResponseEntity<Order> findOrders(String status) {
//        return shopifyService.markOrderFulfilled();
//    }
}
