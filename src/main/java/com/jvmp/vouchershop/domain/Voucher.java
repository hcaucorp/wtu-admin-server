package com.jvmp.vouchershop.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class Voucher {
    public final long amount;
    public final String currency;
    public final String id;
    public final boolean published;
    public final boolean redeemed;
    public final long serialNumber;
    public final LocalDate useBy;
}
