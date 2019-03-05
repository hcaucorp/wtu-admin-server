package com.jvmp.vouchershop.wallet;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Data
public class ImportWalletRequest {

    @NotBlank
    public String mnemonic;

    @Min(1322697600L)
    @Max(1600000000L)
    public long createdAt;
}
