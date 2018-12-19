package com.jvmp.vouchershop.fulfillment;

import com.jvmp.vouchershop.domain.Voucher;

import java.util.Set;

public class Fulfillment {

    Set<Voucher> vouchers;
    String orderId;

    /**
     * For BTC, BCH it is txHash, but may be something else?
     */
    String transactionIdentifier;

}
