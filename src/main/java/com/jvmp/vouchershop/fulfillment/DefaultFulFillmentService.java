package com.jvmp.vouchershop.fulfillment;

import com.jvmp.vouchershop.domain.Voucher;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.shopify.domain.Customer;
import com.jvmp.vouchershop.shopify.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.jvmp.vouchershop.fulfillment.FulfillmentStatus.initiated;
import static com.jvmp.vouchershop.shopify.domain.FinancialStatus.paid;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultFulFillmentService implements FulFillmentService {

    private FulfillmentRepository fulfillmentRepository;
    private VoucherRepository voucherRepository;

    @Override
    public CompletableFuture<?> fulfillOrder(Order order) {

        // order verification
        checkIfOrderIsValid(order);
        checkIfOrderHasBeenFullyPaid(order);
        checkIfOrderIHasNotBeenFulFilledYet(order);

        String email = order.getCustomer().getEmail();

        Set<Voucher> vouchers = voucherRepository.findAll(new Example<>())

        fulfillmentRepository.save(Fulfillment.builder()
                .orderId(order.getId())
                .status(initiated)
                .vouchers()
                .build());

        return null;
    }

    private void checkIfOrderIHasNotBeenFulFilledYet(Order order) {
        Optional.ofNullable(fulfillmentRepository.findByOrderId(order.getId()))
                .ifPresent(fulfillment -> {
                    if (fulfillment.status == FulfillmentStatus.completed)
                        throw new IllegalOperationException(
                                String.format("Order with id %d has already been fulfilled. Check fulfillment id %d here %s", order.getId(),
                                        fulfillment.id, "[[//TODO fulfillment link here]]"));
                });
    }

    private void checkIfOrderIsValid(Order order) {
        Optional.ofNullable(order)
                .map(Order::getCustomer)
                .map(Customer::getEmail)
                .orElseThrow(() -> {
                    log.error("Customer email not found. Can't fulfill the order. Aborting, full order data: {}", order);
                    return new InvalidOrderException("Customer email not found");
                });

        // TODO check if order contains id, non empty list of products
    }

    private void checkIfOrderHasBeenFullyPaid(Order order) {
        Optional.of(order)
                .map(Order::getFinancialStatus)
                .filter(status -> status != paid)
                .orElseThrow(() -> {
                    log.error("Financial status is {} but should be paid. Aborting, full order data: {}", order.getFinancialStatus(), order);
                    return new InvalidOrderException("Financial status should be paid");
                });
    }
}
