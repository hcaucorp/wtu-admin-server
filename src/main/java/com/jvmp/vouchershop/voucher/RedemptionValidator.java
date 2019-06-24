package com.jvmp.vouchershop.voucher;

import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;

public interface RedemptionValidator {
    void validate(RedemptionRequest redemptionRequest);
}
