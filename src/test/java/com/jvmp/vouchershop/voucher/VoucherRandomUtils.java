package com.jvmp.vouchershop.voucher;

import com.jvmp.vouchershop.domain.Voucher;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@UtilityClass
public class VoucherRandomUtils {

    public static Voucher voucher() {
        return new Voucher(
                RandomUtils.nextLong(1_000, 2_000),
                "integration-test-voucher-" + RandomStringUtils.randomNumeric(12),
                "BTC",
                RandomUtils.nextLong(1, 1_000),
                RandomUtils.nextLong(1, 1_000),
                false,
                false,
                false,
                Date.from(Instant.now()),
                Date.from(Instant.now().plus(364, ChronoUnit.DAYS)));
    }

    public static VoucherGenerationSpec voucherGenerationSpec() {
        long id = RandomUtils.nextLong(1_000, 2_000);
        int vouchersCount = RandomUtils.nextInt(10, 1_000);
        return new VoucherGenerationSpec(
                vouchersCount,
                vouchersCount * 5, id,
                1,
                "GBP"
        );
    }
}
