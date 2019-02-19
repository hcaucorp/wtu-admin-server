package com.jvmp.vouchershop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvmp.vouchershop.fulfillment.FulfillmentService;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.security.HmacUtil;
import com.jvmp.vouchershop.shopify.ShopifyService;
import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.shopify.domain.OrderList;
import com.jvmp.vouchershop.system.PropertyNames;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class ShopifyController {

    static final String HTTP_HEADER_X_SHOPIFY_HMAC_SHA256 = "X-Shopify-Hmac-SHA256";
    private final ObjectMapper objectMapper;
    private final FulfillmentService fulfillmentService;
    private final NotificationService notifications;
    private final ShopifyService shopifyService;

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
                            fulfillment -> notifications.pushOrderNotification("Order " + fulfillment.getOrderId() + " fulfilled."),
                            throwable -> notifications.pushOrderNotification("Order fulfillment failed: " + throwable.getMessage()));

            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } else {
            notifications.pushOrderNotification("Fulfillment attempted but failed. Expected request hash is " + calculatedHash + " but received invalid message hash " + hashFromRequest);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/shopify/orders/unfulfilled/count")
    public int unfulfilledOrdersCount() {
        return shopifyService.unfulfilledOrdersCount();
    }

    @PostMapping("/shopify/orders/fulfill")
    public void fulfillUnfulfilledOrders() {
        OrderList unfulfilledOrders = shopifyService.findUnfulfilledOrders();
        unfulfilledOrders.getOrders().forEach(order -> shopifyService.markOrderFulfilled(order.getOrderNumber()));
    }

}
