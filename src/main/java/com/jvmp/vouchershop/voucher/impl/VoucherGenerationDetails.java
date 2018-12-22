package com.jvmp.vouchershop.voucher.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Wither
public class VoucherGenerationDetails implements Serializable {

    /**
     * Number of vouchers to generate
     */
    @Min(value = 1, message = "count should ge greater than zero")
    int count;

    /**
     * Total amount of money to use during voucher generation in Wallet's currency and in smallest undividable units, i.e. without decimals. For BTC total
     * amount would be in satoshis.
     */
    @Min(value = 1, message = "totalAmount should ge greater than zero")
    long totalAmount;

    /**
     * Wallet to take the money from to cover these vouchers.
     */
    @Min(value = 1, message = "walletId should ge greater than zero")
    long walletId;

    /**
     * price of a single voucher in "priceCurrency"
     */
    @Min(value = 1, message = "price should ge greater than zero")
    long price;

    /**
     * Target currency in which vouchers will be sold. Eg. GBP
     */
    @NotNull(message = "priceCurrency cannot be empty")
    String priceCurrency;

    /**
     * Stock Keeping Unit is a code-name which keeps the relationship between vouchers in database and vouchers in shopify. Various "types" of vouchers can be
     * created on both sides: shopify and our server, but when a customer wants to buy one, shopify will send SKU for us to recognize what "type" of voucher it
     * is. The number of vouchers "for sale" on Shopify (stock) should be exactly the same as stock available in our server's database.
     */
    @NotNull(message = "sku cannot be empty")
    String sku;
}
