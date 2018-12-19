package com.jvmp.vouchershop.fulfillment;

import com.jvmp.vouchershop.shopify.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.jvmp.vouchershop.fulfillment.OrderValidator.validateOrder;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultFulFillmentService implements FulFillmentService {

    @Override
    public CompletableFuture<?> fulfillOrder(Order order) {

        // order verification
        validateOrder(order);


        return null;
    }
}
