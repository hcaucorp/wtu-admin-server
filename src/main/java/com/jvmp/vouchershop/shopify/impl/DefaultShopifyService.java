package com.jvmp.vouchershop.shopify.impl;

import com.jvmp.vouchershop.shopify.ShopifyApiClient;
import com.jvmp.vouchershop.shopify.ShopifyService;
import com.jvmp.vouchershop.shopify.domain.FulfillmentItem;
import com.jvmp.vouchershop.shopify.domain.FulfillmentResource;
import com.jvmp.vouchershop.shopify.domain.OrderList;

import static com.jvmp.vouchershop.shopify.domain.FinancialStatus.paid;
import static com.jvmp.vouchershop.shopify.domain.FulfillmentStatus.unshipped;
import static com.jvmp.vouchershop.shopify.domain.OrderStatus.open;
import static java.util.Collections.emptyList;

class DefaultShopifyService implements ShopifyService {

    private final ShopifyApiClient apiClient;
    private final long locationId;

    DefaultShopifyService(ShopifyApiClient shopifyApiClient, long locationId) {
        this.apiClient = shopifyApiClient;
        this.locationId = locationId;
    }

    private FulfillmentResource createFulfillmentResource(long orderId) {
        return new FulfillmentResource(new FulfillmentItem(
                locationId,
                "" + orderId, // use orderId as tracking number
                emptyList(), // TODO create our custom endpoint for email delivery confirmation and re-delivery button
                true
        ));
    }

    @Override
    public void markOrderFulfilled(long orderId) {
        apiClient.fulfillOrder(orderId, createFulfillmentResource(orderId));
    }

    @Override
    public OrderList findUnfulfilledOrders() {
        return apiClient.getOrders(open.toString(), unshipped.toString(), paid.toString());
    }

    @Override
    public int unfulfilledOrdersCount() {
        return apiClient.getOrdersCount(open.toString(), unshipped.toString(), paid.toString())
                .getCount();
    }
}
