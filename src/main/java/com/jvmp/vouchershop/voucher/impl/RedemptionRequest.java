package com.jvmp.vouchershop.voucher.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Wither
public class RedemptionRequest implements Serializable {

    @NotBlank
    private String destinationAddress;

    @NotBlank
    private String voucherCode;

    private String currency;
}
