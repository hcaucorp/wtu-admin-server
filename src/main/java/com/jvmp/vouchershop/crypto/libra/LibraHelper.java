package com.jvmp.vouchershop.crypto.libra;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;
import types.Transaction.RawTransaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class LibraHelper {

    public static byte[] signTransaction(RawTransaction rawTransaction, PrivateKey privateKey) {
        SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest256();
        byte[] saltDigest = digestSHA3.digest("RawTransaction@@$$LIBRA$$@@".getBytes());
        byte[] transactionBytes = rawTransaction.toByteArray();
        byte[] saltDigestAndTransaction = new byte[saltDigest.length + transactionBytes.length];

        System.arraycopy(saltDigest, 0, saltDigestAndTransaction, 0, saltDigest.length);
        System.arraycopy(transactionBytes, 0, saltDigestAndTransaction, saltDigest.length, transactionBytes.length);

        byte[] signature;

        try {
            Signature sgr = Signature.getInstance("Ed25519", "BC");
            sgr.initSign(privateKey);
            sgr.update(digestSHA3.digest(saltDigestAndTransaction));
            signature = sgr.sign();
        } catch (Exception e) {
            throw new IllegalOperationException("Signing the transaction failed" + e.getMessage());
        }

        return signature;
    }

    public static String toLibraAddress(PublicKey publicKey) {
        SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest256();
        return new String(Hex.encode(digestSHA3.digest(stripPrefix(publicKey))));
    }

    public static byte[] stripPrefix(PublicKey publicKey) {
        return stripPrefix(publicKey.getEncoded());
    }

    public static byte[] stripPrefix(byte[] pubKeyBytes) {
        byte[] publicKeyWithoutPrefix = new byte[32];
        System.arraycopy(pubKeyBytes, 12, publicKeyWithoutPrefix, 0, 32);
        return publicKeyWithoutPrefix;
    }

    public static PrivateKey privateKeyFromHexString(String privateKeyHexString) {
        byte[] privateKeyBytes = Hex.decode(privateKeyHexString);

        try {
            return getKeyFactory().generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (InvalidKeySpecException e) {
            throw new IllegalOperationException("PrivateKey generation failed" + e.getMessage());
        }
    }

    public static PublicKey publicKeyFromHexString(String publicKeyHexString) {
        byte[] publicKeyBytes = Hex.decode(publicKeyHexString);

        try {
            return getKeyFactory().generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        } catch (InvalidKeySpecException e) {
            throw new IllegalOperationException("PrivateKey generation failed" + e.getMessage());
        }
    }

    private static KeyFactory getKeyFactory() {
        try {
            return KeyFactory.getInstance("Ed25519");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalOperationException("Could not get KeyFactory" + e.getMessage());
        }
    }

    public static byte[] transferMoveScript() {
        return readBytes(LibraHelper.class.getResourceAsStream("/move/transfer.bin"));
    }

    private static byte[] readBytes(InputStream is) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        try {

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
        } catch (IOException e) {
            throw new IllegalOperationException("Reading failed with error: " + e.getMessage());
        }
        return buffer.toByteArray();
    }
}
