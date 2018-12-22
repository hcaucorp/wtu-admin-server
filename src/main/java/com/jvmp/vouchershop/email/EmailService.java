package com.jvmp.vouchershop.email;

import com.jvmp.vouchershop.voucher.Voucher;

import java.util.List;

public interface EmailService {

    void sendVouchers(List<Voucher> vouchers, String email);
}
