package com.jvmp.vouchershop.domain;


import java.time.LocalDate;

public class Voucher {

    public final long amount;
    public final String currency;
    public final String id;
    public final boolean redeemed;
    public final long serialNumber;
    public final LocalDate useBy;

    public Voucher(long amount, String currency, String id, boolean redeemed, long serialNumber, LocalDate useBy) {
        this.amount = amount;
        this.currency = currency;
        this.id = id;
        this.redeemed = redeemed;
        this.serialNumber = serialNumber;
        this.useBy = useBy;
    }
}
