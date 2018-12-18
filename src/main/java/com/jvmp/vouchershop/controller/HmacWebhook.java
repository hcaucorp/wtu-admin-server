package com.jvmp.vouchershop.controller;

import lombok.experimental.UtilityClass;
import org.apache.commons.codec.binary.Hex;

import javax.annotation.Nonnull;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@UtilityClass
public class HmacWebhook {

    public static String encode(@Nonnull String secret, @Nonnull String body) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        return Hex.encodeHexString(sha256_HMAC.doFinal(body.getBytes("UTF-8")));
    }
}