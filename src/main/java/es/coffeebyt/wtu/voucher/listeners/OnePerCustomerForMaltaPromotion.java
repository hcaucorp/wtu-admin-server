package es.coffeebyt.wtu.voucher.validators;

import com.google.common.annotations.VisibleForTesting;
import es.coffeebyt.wtu.exception.Thrower;
import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.voucher.RedemptionListener;
import es.coffeebyt.wtu.voucher.RedemptionValidator;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.voucher.VoucherNotFoundException;
import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@RequiredArgsConstructor
public class OnePerCustomerForMaltaPromotion implements RedemptionValidator, RedemptionListener {

    final static String MALTA_VOUCHER_SKU = "AI_AND_BC_SUMMIT_WINTER_EDITION_PROMOTIONAL_VOUCHER";

    private final Set<String> customersCache = new CopyOnWriteArraySet<>();

    private final VoucherRepository voucherRepository;

    @Override
    public void validate(RedemptionRequest redemptionRequest) {
        Voucher voucher = voucherRepository.findByCode(redemptionRequest.getVoucherCode())
                .orElseThrow(() -> new VoucherNotFoundException("Voucher " + redemptionRequest.getVoucherCode() + " not found."));

        if (MALTA_VOUCHER_SKU.equals(voucher.getSku())) {
            if (customersCache.contains(redemptionRequest.getDestinationAddress()))
                Thrower.logAndThrowIllegalOperationException("You've already used one voucher! AI and Blockchain Summit promotional gift cards are one per customer :(");
        }
    }

    @Override
    public void redeemed(RedemptionRequest redemptionRequest) {
        customersCache.add(redemptionRequest.getDestinationAddress());
    }

    @VisibleForTesting
    void cachePut(String address) {
        customersCache.add(address);
    }

    @VisibleForTesting
    boolean cacheContains(String address) {
        return customersCache.contains(address);
    }
}
