package com.jvmp.vouchershop.controller;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

public class HmacUtilTest {

    private static final String headerHashFromShopify = "PlhLPNwIwxmYR6T17qJOZ1k4l2sbSZmtnp529Pbp8/A=";
    private static final String secret = "3c023263872ba9675e166569ee160a7ac6c282210328ef27591099d507d99523";

    private String content;

    @Before
    public void setUp() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                // TODO put correct data into the sample
                HmacUtilTest.class.getResourceAsStream("/samples/order_payment_test_notification.json")))) {
            content = reader.lines().collect(joining());
        }
    }

    @Test
    @Ignore
    public void encode() throws NoSuchAlgorithmException, InvalidKeyException {
        assertEquals(headerHashFromShopify, HmacUtil.encode(secret, content));
    }
}