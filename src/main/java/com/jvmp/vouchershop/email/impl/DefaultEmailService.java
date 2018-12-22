package com.jvmp.vouchershop.email.impl;

import com.jvmp.vouchershop.email.EmailService;
import com.jvmp.vouchershop.voucher.Voucher;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DefaultEmailService implements EmailService {
    @Override
    public void sendVouchers(Set<Voucher> vouchers, String email) {
        throw new NotImplementedException("sendVouchers()");
    }
}
