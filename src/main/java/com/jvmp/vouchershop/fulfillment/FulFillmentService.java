package com.jvmp.vouchershop.fulfillment;

import com.jvmp.vouchershop.shopify.domain.Order;

public interface FulFillmentService {

    void fulfillOrder(Order order);
}
