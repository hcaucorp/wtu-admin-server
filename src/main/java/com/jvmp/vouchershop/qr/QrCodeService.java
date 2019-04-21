package com.jvmp.vouchershop.qr;

import com.google.zxing.WriterException;
import com.jvmp.vouchershop.voucher.Voucher;

import java.io.IOException;

public interface QrCodeService {

    String QR_CODE_MESSAGE_FORMAT = "wallettopup.co.uk,%s,%s";

    /**
     * <p>
     * Creates a csv string with "wallettopup.co.uk" identifying that it is a Wallet Top Up brand voucher, then voucher code, then currency:
     *
     * <br>
     * wallettopup.co.uk,voucher-code,currency
     *
     * </p>
     * <p>
     * This is for client wallets to be able to recognize QR code as redeemable voucher coming from us and to make
     * sure to redeem wallet to correct block chain. Make sure to respect CURRENCY, otherwise you could
     * redeem voucher for different block chain than expected, and waste it.
     * </p>
     *
     * @param voucher To convert to QR code message string.
     * @return qr code message which will be encoded into the picture
     */
    QrCode createQRCode(Voucher voucher) throws WriterException, IOException;
}
