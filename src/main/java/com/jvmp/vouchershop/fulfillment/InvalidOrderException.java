package com.jvmp.vouchershop.fulfillment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidOrderException extends RuntimeException {

    InvalidOrderException(String message) {
        super(message);
    }
}
