package com.jvmp.vouchershop.shopify;

import com.jvmp.vouchershop.shopify.domain.OrderList;

public interface ShopifyService {

    void markOrderFulfilled(long orderId);

    OrderList findUnfulfilledOrders();

    int unfulfilledOrdersCount();
}
