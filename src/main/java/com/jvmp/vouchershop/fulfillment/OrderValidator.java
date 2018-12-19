package com.jvmp.vouchershop.fulfillment;

import com.jvmp.vouchershop.shopify.domain.Customer;
import com.jvmp.vouchershop.shopify.domain.Order;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static com.jvmp.vouchershop.shopify.domain.FinancialStatus.paid;

@Slf4j
@UtilityClass
public class OrderValidator {

    void validateOrder(Order order) {
        Optional.ofNullable(order)
                .map(Order::getCustomer)
                .map(Customer::getEmail)
                .orElseThrow(() -> {
                    log.error("Customer email not found. Can't fulfill the order. Aborting, full order data: {}", order);
                    return new InvalidOrderException("Customer email not found");
                });

        Optional.of(order)
                .map(Order::getFinancialStatus)
                .filter(status -> status != paid)
                .orElseThrow(() -> {
                    log.error("Financial status is {} but should be paid. Aborting, full order data: {}", order.getFinancialStatus(), order);
                    return new InvalidOrderException("Financial status should be paid");
                });
    }
}
