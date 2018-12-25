package com.jvmp.vouchershop.fulfillment;

import com.jvmp.vouchershop.shopify.domain.Order;

public interface FulfillmentService {

    void fulfillOrder(Order order);
}
