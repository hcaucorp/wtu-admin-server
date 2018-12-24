package com.jvmp.vouchershop.shopify;

import com.jvmp.vouchershop.shopify.domain.Fulfillment;

public interface ShopifyService {

    void markOrderFulfilled(Fulfillment fulfillment) throws InterruptedException;
}
