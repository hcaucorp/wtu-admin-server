package com.jvmp.vouchershop.email;

import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.voucher.Voucher;

import java.util.Set;

public interface EmailService {

    void sendVouchers(Set<Voucher> vouchers, Order order);
}
