package com.jvmp.vouchershop.fulfillment;

import com.jvmp.vouchershop.shopify.domain.Order;

import java.util.concurrent.Future;

public interface FulFillmentService {


    Future<?> fulfillOrder(Order order);
}
