package com.jvmp.vouchershop.shopify.impl;

import com.jvmp.vouchershop.shopify.ShopifyService;
import com.jvmp.vouchershop.shopify.domain.Fulfillment;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import static java.time.Instant.now;

class ThrottlingShopifyService implements ShopifyService {


    private final ShopifyService delegate;
    private final Duration waitIterval;
    private Instant lastRequestTime;

    ThrottlingShopifyService(ShopifyService shopifyService, Duration waitIterval) {
        Objects.requireNonNull(shopifyService, "service");
        Objects.requireNonNull(waitIterval, "wait interval");

        this.delegate = shopifyService;
        this.waitIterval = waitIterval;
    }

    @Override
    public void markOrderFulfilled(Fulfillment fulfillment) throws InterruptedException {
        Duration waitDuration = Optional.ofNullable(lastRequestTime)
                .map(instant -> Duration.between(instant.plus(waitIterval), now()))
                .filter(duration -> !duration.isNegative())
                .orElse(Duration.ZERO);

        Thread.sleep(waitDuration.toMillis());
        lastRequestTime = now();

        delegate.markOrderFulfilled(fulfillment);
    }
}
