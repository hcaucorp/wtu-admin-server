package com.jvmp.vouchershop.security;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Optional;

@UtilityClass
public class HmacUtil {

    private static final String ASYMMETRIC_SECRET_KEY_FACTORY_NAME = "PBKDF2WithHmacSHA512";

    public static String encode(@Nonnull String secret, @Nonnull byte[] body) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        mac.init(keySpec);

        return Base64.getEncoder().encodeToString(mac.doFinal(body));
    }

    /**
     * same as HmacUtil#encode but doesn't throw anything
     **/
    public static Optional<String> encodeMaybe(@Nonnull String secret, @Nonnull byte[] body) {
        try {
            return Optional.of(encode(secret, body));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return Optional.empty();
        }
    }

    public static String expensiveSha512(@Nonnull String secret, @Nonnull byte[] body) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA512");
        mac.init(keySpec);

        return Base64.getEncoder().encodeToString(mac.doFinal(body));
    }

    public static byte[] generateAsymmetricSeedFromSaltAndPassword(final byte[] salt, final char[] password)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        int iterations = 1000;
        int keyLength = 256;
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ASYMMETRIC_SECRET_KEY_FACTORY_NAME);
        SecretKey secretKey = secretKeyFactory.generateSecret(spec);
        return secretKey.getEncoded();
    }
}