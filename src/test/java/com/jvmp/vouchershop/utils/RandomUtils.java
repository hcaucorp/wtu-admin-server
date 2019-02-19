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
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.jvmp.vouchershop.crypto.btc.WalletServiceBtc.walletWords;
import static com.jvmp.vouchershop.voucher.impl.DefaultVoucherService.DEFAULT_VOUCHER_CODE_GENERATOR;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;

@UtilityClass
public class RandomUtils {

    public static Fulfillment randomFulfillment() {
        return new Fulfillment()
                .withOrderId(nextLong())
                .withCompletedAt(Instant.now().toEpochMilli())
                .withVouchers(IntStream.range(0, nextInt(3, 20)).mapToObj(i -> randomVoucher()).collect(Collectors.toSet()));
    }

    public static Wallet randomWallet(NetworkParameters params) {
        org.bitcoinj.wallet.Wallet wallet = new org.bitcoinj.wallet.Wallet(params);

        return new Wallet()
                .withId(nextLong())
                .withAddress(wallet.currentReceiveAddress().toBase58())
                .withCreatedAt(Instant.now().toEpochMilli())
                .withCurrency(RandomStringUtils.randomAlphabetic(3).toUpperCase())
                .withMnemonic(walletWords(wallet));
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
                .withExpiresAt(LocalDateTime.now().plusYears(1).toInstant(ZoneOffset.UTC).toEpochMilli());
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

    public static RedemptionRequest randomRedemptionRequest() {
        return new RedemptionRequest(
                randomString(),
                randomString()
        );
    }

    public static String randomEmail() {
        return randomString() + "@" + randomString() + ".io";
    }
}
