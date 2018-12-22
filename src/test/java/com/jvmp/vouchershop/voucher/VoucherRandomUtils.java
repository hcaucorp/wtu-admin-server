package com.jvmp.vouchershop.voucher;

import com.jvmp.vouchershop.voucher.impl.VoucherGenerationDetails;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static com.jvmp.vouchershop.RandomUtils.randomSku;
import static com.jvmp.vouchershop.voucher.impl.DefaultVoucherService.DEFAULT_VOUCHER_CODE_GENERATOR;

@UtilityClass
public class VoucherRandomUtils {

    public static Voucher voucher() {
        return new Voucher()
                .withAmount(RandomUtils.nextLong(1_000, 2_000))
                .withCode(DEFAULT_VOUCHER_CODE_GENERATOR.get())
                .withCurrency("BTC")
                .withId(RandomUtils.nextLong(1, 1_000))
                .withWalletId(RandomUtils.nextLong(1, 1_000))
                .withPublished(false)
                .withSold(false)
                .withRedeemed(false)
                .withSku(randomSku())
                .withCreatedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(Instant.now().plus(364, ChronoUnit.DAYS)));
    }

    public static VoucherGenerationDetails voucherGenerationSpec() {
        long id = RandomUtils.nextLong(1_000, 2_000);
        int vouchersCount = RandomUtils.nextInt(10, 1_000);

        return new VoucherGenerationDetails()
                .withCount(vouchersCount)
                .withTotalAmount(vouchersCount * 5)
                .withWalletId(id)
                .withPrice(1)
                .withPriceCurrency("GBP")
                .withSku(randomSku());
    }
}
