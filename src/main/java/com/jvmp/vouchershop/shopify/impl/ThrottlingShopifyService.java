package com.jvmp.vouchershop.shopify.impl;

import com.jvmp.vouchershop.shopify.ShopifyService;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import static java.time.Instant.now;

/**
 * From Shopify's API docs:
 * <p>
 * "If you are using this endpoint with a Partner development store or a trial store, then you can create no more than 5 new fulfillments per minute."
 */
@Slf4j
class ThrottlingShopifyService implements ShopifyService {

    private final ShopifyService delegate;
    private final Duration waitInterval;
    private Instant lastRequestTime;

    ThrottlingShopifyService(ShopifyService shopifyService, Duration waitInterval) {
        Objects.requireNonNull(shopifyService, "service");
        Objects.requireNonNull(waitInterval, "wait interval");

        this.delegate = shopifyService;
        this.waitInterval = waitInterval;
    }

    @Override
    public void markOrderFulfilled(long orderId) {
        Duration waitDuration = Optional.ofNullable(lastRequestTime)
                .map(instant -> Duration.between(instant.plus(waitInterval), now()))
                .filter(duration -> !duration.isNegative())
                .orElse(Duration.ZERO);

        try {
            if (!Thread.interrupted())
                Thread.sleep(waitDuration.toMillis());
        } catch (InterruptedException e) {
            log.error("Interrupt detected, exiting quickly...");
            Thread.currentThread().interrupt();
        }
        lastRequestTime = now();

        delegate.markOrderFulfilled(orderId);
    }
}
