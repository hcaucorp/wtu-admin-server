package com.jvmp.vouchershop.shopify.domain;

import java.util.List;

public class Refund {

    String currency;
    boolean notify;
    String note;
    Shipping shipping;
    List<LineItem> refundLineItems;
    List<Transaction> transactions;
}
