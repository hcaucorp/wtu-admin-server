package com.jvmp.vouchershop.voucher;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VoucherNotFoundException extends Exception {

    public VoucherNotFoundException(String message) {
        super(message);
    }
}
