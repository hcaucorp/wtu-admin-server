package es.coffeebyt.wtu.voucher.impl;

import javax.validation.constraints.NotBlank;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Wither
public class RedemptionRequest implements Serializable {

    @NotBlank
    private String destinationAddress;

    @NotBlank
    private String voucherCode;
}
