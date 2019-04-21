package com.jvmp.vouchershop.qr;

import org.springframework.core.io.InputStreamSource;

public interface QrCode {

    byte[] getBytes();

    InputStreamSource toInputStreamSource();

    String getContentType();
}
