package es.coffeebyt.wtu.voucher;

import es.coffeebyt.wtu.exception.IllegalOperationException;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static java.time.Instant.now;

/**
 * CAUTION! Only limited information can be shared about the voucher! As limited as possible.
 */
@Value
public class VoucherInfoResponse {

    private String status;
    private String expiresAt;

    public static VoucherInfoResponse from(Voucher voucher) {
        return new VoucherInfoResponse(calculateStatus(voucher), "" + calculateExpiresAt(voucher));
    }

    private static String calculateStatus(@Nonnull Voucher voucher) {

        if (voucher.isRedeemed()) return "redeemed";

        if (voucher.isPublished()) {

            if (Instant.ofEpochMilli(calculateExpiresAt(voucher)).isBefore(now())) return "expired";

            return "valid";
        }

        throw new IllegalOperationException("Unsold vouchers can't be queried for status. Should not get this exception. " +
                "Prevent this from happening in a layer higher to prevent enumeration attack based on response time");
    }

    // temporary walk-around for legacy value for old vouchers :(
    // should be simply voucher.getExpiresAt()
    private static long calculateExpiresAt(@Nonnull Voucher voucher) {
        return voucher.getExpiresAt() > 0 ?
                voucher.getExpiresAt() :
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(voucher.getCreatedAt()), ZoneOffset.UTC).plusYears(2).toInstant().toEpochMilli();
    }

    @ApiModelProperty(value = "Current gift card code status.", allowableValues = "redeemed,expired,valid")
    public String getStatus() {
        return status;
    }

    @ApiModelProperty(value = "Timestamp is in milliseconds.")
    public String getExpiresAt() {
        return expiresAt;
    }
}
