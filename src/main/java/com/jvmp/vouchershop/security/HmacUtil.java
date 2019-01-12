package com.jvmp.vouchershop.security;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

@UtilityClass
public class HmacUtil {

    public static String encode(@Nonnull String secret, @Nonnull byte[] body) throws NoSuchAlgorithmException, InvalidKeyException {

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        mac.init(keySpec);

        return Base64.getEncoder().encodeToString(mac.doFinal(body));
    }

    public static Optional<String> encode1(@Nonnull String secret, @Nonnull byte[] body) {
        try {
            return Optional.of(encode(secret, body));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return Optional.empty();
        }
    }
}