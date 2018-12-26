package com.jvmp.vouchershop;

import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.impl.VoucherGenerationDetails;
import com.jvmp.vouchershop.wallet.Wallet;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

import static com.jvmp.vouchershop.voucher.impl.DefaultVoucherService.DEFAULT_VOUCHER_CODE_GENERATOR;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.RandomUtils.nextLong;

@UtilityClass
public class RandomUtils {

    public static Wallet randomWallet() {
        return new Wallet()
                .withId(nextLong(0, Long.MAX_VALUE))
                .withAddress("Test wallet address #" + RandomStringUtils.randomNumeric(12))
                .withCreatedAt(Instant.now().toEpochMilli())
                .withCurrency(RandomStringUtils.randomAlphabetic(3).toUpperCase())
                .withMnemonic(Stream.of(
                        "behave snap girl enforce sadness boil fine during use anchor screen sample".split(" "))
                        .map(word -> ImmutablePair.of(word, nextLong(1, 20)))
                        .sorted(Comparator.comparingLong(ImmutablePair::getRight))
                        .map(ImmutablePair::getLeft)
                        .collect(joining(" ")));
    }

    public static Order randomOrder() {
        return new Order()
                .withId(nextLong(0, Long.MAX_VALUE));
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }

    public static String randomSku() {
        return "SKU-" + RandomStringUtils.randomAlphanumeric(12);
    }

    public static Voucher randomVoucher() {
        return new Voucher()
                .withAmount(nextLong(1_000, 2_000))
                .withCode(DEFAULT_VOUCHER_CODE_GENERATOR.get())
                .withCurrency("BTC")
                .withId(nextLong(1, 1_000))
                .withWalletId(nextLong(1, 1_000))
                .withPublished(false)
                .withSold(false)
                .withRedeemed(false)
                .withSku(randomSku())
                .withCreatedAt(Instant.now().toEpochMilli())
                .withExpirationDays(365);
    }

    public static VoucherGenerationDetails randomVoucherGenerationSpec() {
        long id = nextLong(1_000, 2_000);
        int vouchersCount = org.apache.commons.lang3.RandomUtils.nextInt(10, 1_000);

        return new VoucherGenerationDetails()
                .withCount(vouchersCount)
                .withTotalAmount(vouchersCount * 5)
                .withWalletId(id)
                .withPrice(1)
                .withPriceCurrency("GBP")
                .withSku(randomSku());
    }
}
