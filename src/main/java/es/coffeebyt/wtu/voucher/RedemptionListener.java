package es.coffeebyt.wtu.voucher;

import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;

public interface RedemptionListener {
    void redeemed(RedemptionRequest redemptionRequest);
}
