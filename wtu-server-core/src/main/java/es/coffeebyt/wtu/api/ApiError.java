package es.coffeebyt.wtu.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import static es.coffeebyt.wtu.voucher.listeners.OnePerCustomerForMaltaPromotion.MALTA_VOUCHER_REDEMPTION_ERROR_ONE_PER_CUSTOMER;

@Data
@AllArgsConstructor
public class ApiError {

    private int status;

    @ApiModelProperty(value = "Current gift card code status.", allowableValues = "IP is blocked.,Bad request.," + MALTA_VOUCHER_REDEMPTION_ERROR_ONE_PER_CUSTOMER)
    private String message;
    private String error;
    private String timestamp;
    private String path;

}
