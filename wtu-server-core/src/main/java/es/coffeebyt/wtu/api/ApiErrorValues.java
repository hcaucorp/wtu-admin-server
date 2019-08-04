package es.coffeebyt.wtu.api;

import static es.coffeebyt.wtu.voucher.listeners.OnePerCustomerForMaltaPromotion.MALTA_VOUCHER_REDEMPTION_ERROR_ONE_PER_CUSTOMER;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.web.server.ResponseStatusException;

import es.coffeebyt.wtu.exception.MaltaCardException;
import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;

public class ApiErrorValues {

    public final static MaltaCardException maltaCardException = new MaltaCardException(MALTA_VOUCHER_REDEMPTION_ERROR_ONE_PER_CUSTOMER);
    public final static String MALTA_GIFT_CODE_FAILING_WITH_ONE_PER_CUSTOMER_ERROR =
            "wtubch-264ae5cf-b1eb-4217-9ba7-a4a06b23b434";

    public static void handleGiftCardRedemption(RedemptionRequest redemptionRequest) {
        if (MALTA_GIFT_CODE_FAILING_WITH_ONE_PER_CUSTOMER_ERROR.equals(redemptionRequest.getVoucherCode())) {
            throw new ResponseStatusException(BAD_REQUEST, MALTA_VOUCHER_REDEMPTION_ERROR_ONE_PER_CUSTOMER, maltaCardException);
        }
    }
}
