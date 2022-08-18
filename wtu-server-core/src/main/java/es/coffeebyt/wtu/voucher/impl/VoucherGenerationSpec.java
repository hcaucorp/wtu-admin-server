package es.coffeebyt.wtu.voucher.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Wither
public class VoucherGenerationSpec implements Serializable {

    /**
     * Number of vouchers to generate
     */
    @Positive
    int count;

    /**
     * Total amount of money to use during voucher generation in Wallet's currency and in smallest undividable units, i.e. without decimals. For BTC total
     * amount would be in satoshis.
     */
    @Positive
    long totalAmount;

    /**
     * Wallet to take the money from to cover these vouchers.
     */
    @Positive
    long walletId;

    /**
     * price of a single voucher in "priceCurrency"
     */
    @Positive
    long price;

    /**
     * Target currency in which vouchers will be sold. Eg. GBP
     */
    @NotBlank
    String priceCurrency;

    /**
     * Stock Keeping Unit.
     */
    @NotBlank
    String sku;

    /**
     * List of voucher codes if not meant to be generated. Separated by commas and/or white spaces
     */
    @NotBlank
    String voucherCodes;
}
