package com.jvmp.vouchershop.voucher.validators;

import com.google.common.annotations.VisibleForTesting;
import com.jvmp.vouchershop.exception.Thrower;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.voucher.RedemptionListener;
import com.jvmp.vouchershop.voucher.RedemptionValidator;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.VoucherNotFoundException;
import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;
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
