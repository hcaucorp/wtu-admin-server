package es.coffeebyt.wtu.voucher;

import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;

public interface RedemptionValidator {
    void validate(RedemptionRequest redemptionRequest);
}
