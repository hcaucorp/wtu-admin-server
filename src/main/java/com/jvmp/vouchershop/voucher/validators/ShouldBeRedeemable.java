package com.jvmp.vouchershop.voucher.validators;

import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.voucher.RedemptionValidator;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.VoucherNotFoundException;
import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.jvmp.vouchershop.exception.Thrower.logAndThrowIllegalOperationException;

@Component
@RequiredArgsConstructor
public class ShouldBeRedeemable implements RedemptionValidator {

    private final VoucherRepository voucherRepository;

    @Override
    public void validate(RedemptionRequest redemptionRequest) {
        Voucher voucher = voucherRepository.findByCode(redemptionRequest.getVoucherCode())
                .orElseThrow(() -> new VoucherNotFoundException("Voucher " + redemptionRequest.getVoucherCode() + " not found."));

        Objects.requireNonNull(voucher, "voucher");

        if (!voucher.isPublished())
            logAndThrowIllegalOperationException("Attempting to redeem unpublished voucher " + voucher.getCode());

        if (voucher.isRedeemed())
            logAndThrowIllegalOperationException("Attempting to redeem already redeemed voucher " + voucher.getCode());
    }
}
