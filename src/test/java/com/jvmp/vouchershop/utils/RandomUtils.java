package com.jvmp.vouchershop.utils;

import com.jvmp.vouchershop.fulfillment.Fulfillment;
import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;
import com.jvmp.vouchershop.voucher.impl.VoucherGenerationDetails;
import com.jvmp.vouchershop.wallet.Wallet;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.UnitTestParams;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.jvmp.vouchershop.crypto.btc.BitcoinService.walletWords;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;

@UtilityClass
public class RandomUtils {

    public static Fulfillment randomFulfillment() {
        return new Fulfillment()
                .withOrderId(nextLong())
                .withCompletedAt(Instant.now().getEpochSecond())
                .withVouchers(randomVouchers(nextInt(3, 20)));
    }

    public static Wallet randomWallet(NetworkParameters params) {
        org.bitcoinj.wallet.Wallet wallet = new org.bitcoinj.wallet.Wallet(params);

        return new Wallet()
                .withId(nextLong())
                .withAddress(wallet.currentReceiveAddress().toBase58())
                .withCreatedAt(Instant.now().toEpochMilli())
                .withCurrency(randomCurrency())
                .withMnemonic(walletWords(wallet));
    }

    public static String randomCurrency() {
        return RandomStringUtils.randomAlphabetic(3).toUpperCase();
    }

    public static Wallet randomWallet() {
        return randomWallet(UnitTestParams.get());
    }

    public static String randomBtcAddress(NetworkParameters params) {
        org.bitcoinj.wallet.Wallet wallet = new org.bitcoinj.wallet.Wallet(params);
        return wallet.currentReceiveAddress().toBase58();
    }

    public static String randomBtcAddress() {
        return randomBtcAddress(UnitTestParams.get());
    }

    public static Order randomOrder() {
        return new Order()
                .withId(nextLong())
                .withOrderNumber(nextLong(1, Long.MAX_VALUE))
                .withName(randomString())
                .withTotalPrice(BigDecimal.valueOf(nextLong(1, Long.MAX_VALUE)))
                .withCreatedAt(new Date());
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }

    public static String randomSku() {
        return "SKU-" + RandomStringUtils.randomAlphanumeric(12);
    }

    public static Set<Voucher> randomVouchers(int howMany) {
        return IntStream.range(0, howMany).mapToObj(i -> randomVoucher()).collect(Collectors.toSet());
    }

    public static String randomVoucherCode() {
        return "wtu" + randomCurrency() + "-" + randomString();
    }

    public static Voucher randomVoucher() {
        return new Voucher()
                .withAmount(nextLong(1_000, 2_000))
                .withCode(randomVoucherCode())
                .withId(nextLong(1, 1_000))
                .withWalletId(nextLong(1, 1_000))
                .withPublished(false)
                .withSold(false)
                .withRedeemed(false)
                .withSku(randomSku())
                .withCreatedAt(Instant.now().getEpochSecond())
                .withExpiresAt(LocalDateTime.now().plusYears(1).toInstant(ZoneOffset.UTC).getEpochSecond());
    }

    public static VoucherGenerationDetails randomVoucherGenerationSpec() {
        long id = nextLong(1_000, 2_000);
        int vouchersCount = org.apache.commons.lang3.RandomUtils.nextInt(10, 1_000);

        return new VoucherGenerationDetails()
                .withCount(vouchersCount)
                .withTotalAmount(vouchersCount * 5)
                .withWalletId(id)
                .withPriceCurrency("GBP")
                .withPrice(nextLong(5, 10))
                .withSku(randomSku());
    }

    public static RedemptionRequest randomRedemptionRequest() {
        return new RedemptionRequest(
                randomString(),
                randomString());
    }

    public static String randomEmail() {
        return randomString() + "@" + randomString() + ".io";
    }

    public static String randomIp() {
        return nextInt(0, 256) + "." + nextInt(0, 256) + "." + nextInt(0, 256) + "." + nextInt(0, 256);
    }
}
