package com.jvmp.vouchershop.qr.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.jvmp.vouchershop.qr.QrCodeService;
import com.jvmp.vouchershop.voucher.Voucher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QrCodeServiceImpl implements QrCodeService {

    private final MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

    @Override
    public PngInMemoryQrCode createQRCode(Voucher voucher) throws WriterException, IOException {
        Map<EncodeHintType, ErrorCorrectionLevel> hintMap = Collections.singletonMap(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        String message = voucher.getCode();

        BitMatrix matrix = multiFormatWriter.encode(message, BarcodeFormat.QR_CODE, 200, 200, hintMap);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "png", byteArrayOutputStream);

        return new PngInMemoryQrCode(byteArrayOutputStream.toByteArray());
    }
}
