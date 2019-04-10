package com.jvmp.vouchershop.voucher;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VoucherNotFoundException extends RuntimeException {

    public VoucherNotFoundException(String message) {
        super(message);
    }
}
