package com.jvmp.vouchershop.voucher;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import lombok.Value;

import javax.annotation.Nonnull;
import java.time.Instant;

import static java.time.Instant.now;

/**
 * CAUTION! Only limited information can be shared about the voucher! As limited as possible.
 */
@Value
public class VoucherInfoResponse {

    private String status; // redeemed, expired, valid
    private String expiresAt;

    public static VoucherInfoResponse from(Voucher voucher) {
        return new VoucherInfoResponse(calculateStatus(voucher), "" + voucher.getExpiresAt());
    }

    static String calculateStatus(@Nonnull Voucher voucher) {

        if (voucher.isRedeemed()) return "redeemed";

        if (voucher.isSold()) {
            if (Instant.ofEpochSecond(voucher.getExpiresAt()).isBefore(now())) return "expired";

            return "valid";
        }

        throw new IllegalOperationException("Unsold vouchers can't be queried for status. Should not get this exception. " +
                "Prevent this from happening in a layer higher to prevent enumeration attack based on response time");
    }
}
