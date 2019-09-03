package es.coffeebyt.wtu.api;

import static es.coffeebyt.wtu.voucher.listeners.MaltaPromotion.MALTA_VOUCHER_REDEMPTION_ERROR_ONE_PER_CUSTOMER;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

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
