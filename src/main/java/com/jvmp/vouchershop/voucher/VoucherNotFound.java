package com.jvmp.vouchershop.voucher;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VoucherNotFound extends Exception {

    public VoucherNotFound(String message) {
        super(message);
    }
}
