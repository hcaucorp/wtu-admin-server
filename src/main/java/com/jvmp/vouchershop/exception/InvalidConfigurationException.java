package com.jvmp.vouchershop.exception;

/**
 * Created by Hubert Czerpak on 2018-12-08
 */
public class InvalidConfigurationException extends RuntimeException {

    public InvalidConfigurationException(String message) {
        super(message);
    }

    public InvalidConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
