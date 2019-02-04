package com.jvmp.vouchershop.utils;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
public class IAmATeapotException extends RuntimeException {
}
