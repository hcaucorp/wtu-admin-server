package com.jvmp.vouchershop.voucher;

import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;

public interface RedemptionListener {
    void redeemed(RedemptionRequest redemptionRequest);
}
