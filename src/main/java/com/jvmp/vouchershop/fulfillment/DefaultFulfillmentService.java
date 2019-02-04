package com.jvmp.vouchershop.fulfillment;

import com.google.common.annotations.VisibleForTesting;
import com.jvmp.vouchershop.email.EmailService;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.exception.InvalidOrderException;
import com.jvmp.vouchershop.repository.FulfillmentRepository;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.shopify.ShopifyService;
import com.jvmp.vouchershop.shopify.domain.Customer;
import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.voucher.Voucher;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.jvmp.vouchershop.fulfillment.FulfillmentStatus.completed;
import static com.jvmp.vouchershop.fulfillment.FulfillmentStatus.initiated;
import static com.jvmp.vouchershop.shopify.domain.FinancialStatus.paid;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
public class DefaultFulfillmentService implements FulfillmentService {

    private final FulfillmentRepository fulfillmentRepository;
    private final VoucherRepository voucherRepository;
    private final ShopifyService shopifyService;
    private final EmailService emailService;

    @Override
    public Fulfillment fulfillOrder(Order order) {
        // order verification
        checkIfOrderIsValid(order);
        checkIfOrderHasBeenFullyPaid(order);
        checkIfOrderIHasNotBeenFulFilledYet(order);

        val skuAndQuantity = findVouchersSkuAndQuantity(order);
        val supplyForDemand = findSupplyForDemand(skuAndQuantity);

        checkIfSupplyIsEnough(skuAndQuantity, supplyForDemand);

        Fulfillment fulfillment = initiateFulFillment(order, supplyForDemand);

        emailService.sendVouchers(supplyForDemand, order.getCustomer().getEmail());
        shopifyService.markOrderFulfilled(fulfillment.getOrderId());

        return completeFulFillment(fulfillment);
    }

    @VisibleForTesting
    Fulfillment completeFulFillment(@Nonnull Fulfillment fulfillment) {
        fulfillment.setStatus(completed);
        Fulfillment result = fulfillmentRepository.save(fulfillment);
        fulfillment.getVouchers().forEach(voucher -> voucherRepository.save(voucher.withSold(true)));
        return result;
    }

    @VisibleForTesting
    Fulfillment initiateFulFillment(@Nonnull Order order, @Nonnull Set<Voucher> supplyForDemand) {
        Fulfillment fulfillment = new Fulfillment()
                .withVouchers(supplyForDemand)
                .withOrderId(order.getId())
                .withStatus(initiated);

        return fulfillmentRepository.save(fulfillment);
    }

    @VisibleForTesting
    void checkIfSupplyIsEnough(Set<ImmutablePair<String, Integer>> lineItemsQuantities, Set<Voucher> supplyForDemand) {
        int totalCount = lineItemsQuantities.stream().mapToInt(value -> value.right).sum();
        int availableCount = supplyForDemand.size();

        if (totalCount > availableCount) {
            String skuList = lineItemsQuantities.stream().map(pair -> pair.left + "(" + pair.right + ")").collect(joining(", "));
            String vouchersList = supplyForDemand.stream().map(Voucher::getSku)
                    .collect(toMap(sku -> sku, sku -> 1L, (Long s, Long a) -> s + a))
                    .entrySet().stream()
                    .map(pair -> pair.getKey() + "(" + pair.getValue() + ")").collect(joining(", "));
            vouchersList = isBlank(vouchersList) ? "0" : vouchersList;

            String errorMessage =
                    String.format("Order contains %s products but we only have %s available vouchers to cover this: %s \n", totalCount, availableCount, supplyForDemand) +
                            String.format("You asked for vouchers with sku: %s but found: %s", skuList, vouchersList);

            throw new InvalidOrderException(errorMessage);
        }
    }

    @VisibleForTesting
    Set<Voucher> findSupplyForDemand(Set<ImmutablePair<String, Integer>> lineItemsQuantities) {
        return lineItemsQuantities.stream()
                .flatMap(skuQuantity -> voucherRepository.findBySoldFalseAndSku(skuQuantity.left)
                        .stream()
                        .limit(skuQuantity.getRight()))
                .collect(toSet());
    }

    @VisibleForTesting
    Set<ImmutablePair<String, Integer>> findVouchersSkuAndQuantity(@Nonnull Order order) {
        return Optional.ofNullable(order.getLineItems())
                .map(Collection::stream)
                .orElse(Stream.empty())
                .map(li -> ImmutablePair.of(li.getSku(), li.getQuantity()))
                .collect(toSet());
    }

    @VisibleForTesting
    void checkIfOrderIHasNotBeenFulFilledYet(@Nonnull Order order) {
        Optional.ofNullable(fulfillmentRepository.findByOrderId(order.getId()))
                .ifPresent(fulfillment -> {
                    if (fulfillment.getStatus() == FulfillmentStatus.completed)
                        throw new IllegalOperationException(
                                String.format("Order with id %d has already been fulfilled. Check fulfillment id %d here %s", order.getId(),
                                        fulfillment.getId(), "[[//TODO fulfillment link here]]"));
                });
    }

    @VisibleForTesting
    void checkIfOrderIsValid(@Nonnull Order order) {
        Optional.of(order)
                .map(Order::getCustomer)
                .map(Customer::getEmail)
                .orElseThrow(() -> new InvalidOrderException("Customer email not found. Can't fulfill the order. Aborting, full order data: " + order));

        // TODO check if order contains id, non empty list of products
    }

    @VisibleForTesting
    void checkIfOrderHasBeenFullyPaid(@Nonnull Order order) {
        Optional.of(order)
                .map(Order::getFinancialStatus)
                .filter(status -> status == paid)
                .orElseThrow(() -> new InvalidOrderException(String.format("Financial status is %s but should be paid. Aborting, full order data: %s", order.getFinancialStatus(), order)));
    }
}
