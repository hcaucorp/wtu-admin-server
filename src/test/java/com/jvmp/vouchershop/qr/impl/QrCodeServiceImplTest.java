package com.jvmp.vouchershop.qr.impl;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.jvmp.vouchershop.qr.QrCode;
import com.jvmp.vouchershop.voucher.Voucher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import javax.imageio.ImageIO;
import java.io.InputStream;
import java.util.Map;

import static com.jvmp.vouchershop.utils.RandomUtils.randomVoucher;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class QrCodeServiceImplTest {

    @InjectMocks
    private QrCodeServiceImpl qrCodeService;

    private static String readQRCode(QrCode filePath) throws Exception {
        try (InputStream is = filePath.toInputStreamSource().getInputStream()) {
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(is))));

            Map<DecodeHintType, ErrorCorrectionLevel> hintMap = singletonMap(DecodeHintType.TRY_HARDER, ErrorCorrectionLevel.L);
            Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);
            return qrCodeResult.getText();
        }
    }

    @Test
    public void createQRCode() throws Exception {
        Voucher voucher = randomVoucher();
        QrCode qrCode = qrCodeService.createQRCode(voucher);
        String actual = readQRCode(qrCode);

        assertEquals(voucher.getCode(), actual);

    }
}