package es.coffeebyt.wtu.voucher.listeners;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import es.coffeebyt.wtu.api.ApiErrorValues;
import es.coffeebyt.wtu.exception.Thrower;
import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.voucher.RedemptionListener;
import es.coffeebyt.wtu.voucher.RedemptionValidator;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.voucher.VoucherNotFoundException;
import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OnePerCustomerForMaltaPromotion implements RedemptionValidator, RedemptionListener {

    public final static String MALTA_VOUCHER_SKU = "AI_AND_BC_SUMMIT_WINTER_EDITION_PROMOTIONAL_VOUCHER";
    public final static String MALTA_VOUCHER_REDEMPTION_ERROR_ONE_PER_CUSTOMER =
            "You've already used one voucher! AI and BC Summit promotional gift cards are limited to one per customer 😇";

    private final Set<String> customersCache = new CopyOnWriteArraySet<>();

    private final VoucherRepository voucherRepository;

    @Override
    public void validate(RedemptionRequest redemptionRequest) {
        Voucher voucher = voucherRepository.findByCode(redemptionRequest.getVoucherCode())
                .orElseThrow(() -> new VoucherNotFoundException("Voucher " + redemptionRequest.getVoucherCode() + " not found."));

        if (MALTA_VOUCHER_SKU.equals(voucher.getSku()) && customersCache.contains(redemptionRequest.getDestinationAddress()))
            Thrower.logAndThrow(MALTA_VOUCHER_REDEMPTION_ERROR_ONE_PER_CUSTOMER, () -> ApiErrorValues.maltaCardException);
    }

    @Override
    public void redeemed(RedemptionRequest redemptionRequest) {
        customersCache.add(redemptionRequest.getDestinationAddress());
    }

    public void cachePut(String address) {
        customersCache.add(address);
    }

    boolean cacheContains(String address) {
        return customersCache.contains(address);
    }
}
