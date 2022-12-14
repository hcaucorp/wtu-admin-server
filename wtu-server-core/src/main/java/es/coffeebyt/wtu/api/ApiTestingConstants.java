package es.coffeebyt.wtu.api;

import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;
import es.coffeebyt.wtu.voucher.listeners.MaltaPromotion.MaltaCardException;
import lombok.experimental.UtilityClass;
import org.springframework.web.server.ResponseStatusException;

import static es.coffeebyt.wtu.exception.WtuErrorCodes.ONE_PER_CUSTOMER;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@UtilityClass
public class ApiTestingConstants {

    public static final String MALTA_GIFT_CODE_FAILING_WITH_ONE_PER_CUSTOMER_ERROR = "wtubch-264ae5cf-b1eb-4217-9ba7-a4a06b23b434";

    public static void handleApiErrors(RedemptionRequest redemptionRequest) {
        if (MALTA_GIFT_CODE_FAILING_WITH_ONE_PER_CUSTOMER_ERROR.equals(redemptionRequest.getVoucherCode())) {
            throw new ResponseStatusException(BAD_REQUEST, ONE_PER_CUSTOMER.name(), new MaltaCardException());
        }
    }

    public static boolean isTestCode(String voucherCode) {
        return MALTA_GIFT_CODE_FAILING_WITH_ONE_PER_CUSTOMER_ERROR.equals(voucherCode);
    }

}
