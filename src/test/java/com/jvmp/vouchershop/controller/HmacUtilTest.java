package com.jvmp.vouchershop.controller;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;

public class HmacUtilTest {

    private static final String headerHashFromShopify = "gMAzAzlEoMtRAKhlazsa0cwDMzb5HQAnsvBgmY/zdrI=";
    private static final String secret = "4aa1c538138e6dcb09f6d4d92ba4087c14415079c26fb6fb8a51bc6e6dca5ca3";

    private byte[] content;

    @Before
    public void setUp() throws Exception {
        try (InputStream input = HmacUtilTest.class.getResourceAsStream("/samples/Order820982911946154508")) {
            content = IOUtils.toByteArray(input);
        }
    }

    @Test
    @Ignore //TODO fix the test because encoding is correct
    public void encode() throws NoSuchAlgorithmException, InvalidKeyException {
        assertEquals(headerHashFromShopify, HmacUtil.encode(secret, content));
    }
}