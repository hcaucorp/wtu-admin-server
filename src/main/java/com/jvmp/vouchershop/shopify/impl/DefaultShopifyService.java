package com.jvmp.vouchershop.shopify.impl;

import com.jvmp.vouchershop.shopify.ShopifyApiClient;
import com.jvmp.vouchershop.shopify.ShopifyService;
import com.jvmp.vouchershop.shopify.domain.FulfillmentItem;
import com.jvmp.vouchershop.shopify.domain.FulfillmentResource;

import static java.util.Collections.singletonList;

class DefaultShopifyService implements ShopifyService {

    private final String shopUrl;
    private final ShopifyApiClient apiClient;
    private final long locationId;

    DefaultShopifyService(ShopifyApiClient shopifyApiClient, String shopName, long locationId) {
        this.shopUrl = "https://" + shopName + ".myshopify.com/";
        this.apiClient = shopifyApiClient;
        this.locationId = locationId;
    }

    private FulfillmentResource createFulfillmentResource(long orderId) {
        return new FulfillmentResource(new FulfillmentItem(
                locationId,
                "" + orderId, // use orderId as tracking number
                // TODO tracking urls: provide endpoint to check delivery status/info based on orderId from shopify
                singletonList(shopUrl + "orders/" + orderId),
                true
        ));
    }

    @Override
    public void markOrderFulfilled(long orderId) {
        apiClient.fulfillAllItems(orderId, createFulfillmentResource(orderId));
    }
}
