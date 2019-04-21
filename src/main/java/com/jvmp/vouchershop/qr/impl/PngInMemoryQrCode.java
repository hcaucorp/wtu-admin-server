package com.jvmp.vouchershop.qr.impl;

import com.jvmp.vouchershop.qr.QrCode;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class PngInMemoryQrCode implements QrCode {

    private final byte[] bytes;

    PngInMemoryQrCode(@Nonnull byte[] pngBytes) {
        bytes = Arrays.copyOf(pngBytes, pngBytes.length);
    }

    @Override
    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    public InputStreamSource toInputStreamSource() {
        return new ByteArrayResource(bytes);
    }

    @Override
    public String getContentType() {
        return "image/png";
    }
}
