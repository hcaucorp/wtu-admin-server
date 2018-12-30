package com.jvmp.vouchershop.controller;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@UtilityClass
class HmacUtil {

    static String encode(@Nonnull String secret, @Nonnull byte[] body) throws NoSuchAlgorithmException, InvalidKeyException {

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        mac.init(keySpec);

        return Base64.getEncoder().encodeToString(mac.doFinal(body));
    }
}