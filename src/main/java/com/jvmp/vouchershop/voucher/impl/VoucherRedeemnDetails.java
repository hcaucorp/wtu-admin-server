package com.jvmp.vouchershop.voucher.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Wither
public class VoucherRedeemnDetails implements Serializable {

    @NotNull(message = "destinationAddress cannot be empty")
    private String destinationAddress;

    @NotNull(message = "voucherCode cannot be empty")
    private String voucherCode;
}
