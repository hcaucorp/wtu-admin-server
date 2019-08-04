package es.coffeebyt.wtu.fulfillment;

import com.google.common.annotations.VisibleForTesting;
import es.coffeebyt.wtu.exception.ResourceNotFoundException;
import es.coffeebyt.wtu.repository.FulfillmentRepository;
import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.voucher.Voucher;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static es.coffeebyt.wtu.time.TimeStamp.clearTimeInformation;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Deprecated
@Service
@RequiredArgsConstructor
public class DefaultFulfillmentService implements FulfillmentService {

    private final FulfillmentRepository fulfillmentRepository;
    private final VoucherRepository voucherRepository;

    @Override
    public Set<Fulfillment> findAll() {
        return new HashSet<>(fulfillmentRepository.findAll());
    }

    @Override
    public Fulfillment findByOrderId(long orderId) {
        Fulfillment result = fulfillmentRepository.findByOrderId(orderId);
        if (result == null) {
            throw new ResourceNotFoundException("Fulfillment for order " + orderId + " not found.");
        }
        return result;
    }

    @VisibleForTesting
    Fulfillment completeFulfillment(@Nonnull Fulfillment fulfillment) {
        ZonedDateTime today = ZonedDateTime.now(ZoneOffset.UTC);

        Fulfillment result = fulfillmentRepository.save(fulfillment);
        fulfillment.getVouchers().forEach(voucher -> voucherRepository.save(
                voucher
                        .withSold(true)
                        // two years from now, drop time information
                        .withExpiresAt(clearTimeInformation(today.plusYears(2).toInstant().toEpochMilli()))
        ));
        return result;
    }

    @VisibleForTesting
    void checkIfSupplyIsEnough(Set<ImmutablePair<String, Integer>> lineItemsQuantities, Set<Voucher> supplyForDemand) {
        int totalCount = lineItemsQuantities.stream().mapToInt(value -> value.right).sum();
        int availableCount = supplyForDemand.size();

        if (totalCount > availableCount) {
            String skuList = lineItemsQuantities.stream().map(pair -> pair.left + "(" + pair.right + ")").collect(joining(", "));
            String vouchersList = supplyForDemand.stream()
                    .map(Voucher::getSku)
                    .collect(toMap(sku -> sku, sku -> 1L, Long::sum))
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
}
