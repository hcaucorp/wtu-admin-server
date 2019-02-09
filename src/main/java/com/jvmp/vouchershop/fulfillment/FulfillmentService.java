package com.jvmp.vouchershop.fulfillment;

import com.jvmp.vouchershop.shopify.domain.Order;

import java.util.Set;

public interface FulfillmentService {

    Set<Fulfillment> findAll();
    Fulfillment fulfillOrder(Order order);

    Fulfillment findByOrderId(long orderId);
}
