package es.coffeebyt.wtu.utils;

import es.coffeebyt.wtu.crypto.bch.BitcoinCashService;
import es.coffeebyt.wtu.crypto.btc.BitcoinService;
import es.coffeebyt.wtu.time.TimeUtil;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;
import es.coffeebyt.wtu.voucher.impl.VoucherGenerationSpec;
import es.coffeebyt.wtu.wallet.Wallet;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.params.UnitTestParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.KeyChainGroup;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;

@UtilityClass
public class RandomUtils {

    public static Wallet randomWallet(NetworkParameters params) {
        org.bitcoinj.wallet.Wallet wallet = new org.bitcoinj.wallet.Wallet(
                params,
                KeyChainGroup.builder(params).fromRandom(Script.ScriptType.P2PKH).build()
        );

        return new Wallet()
                .withId(nextLong())
                .withAddress(Base58.encode(wallet.currentReceiveAddress().getHash()))
                .withCreatedAt(Instant.now().toEpochMilli())
                .withCurrency(randomCurrency())
                .withMnemonic(BitcoinService.walletWords(wallet));
    }

    public static Wallet randomWallet(cash.bitcoinj.core.NetworkParameters params) {
        cash.bitcoinj.wallet.Wallet wallet = new cash.bitcoinj.wallet.Wallet(params);

        return new Wallet()
                .withId(nextLong())
                .withAddress(wallet.currentReceiveAddress().toBase58())
                .withCreatedAt(Instant.now().toEpochMilli())
                .withCurrency(randomCurrency())
                .withMnemonic(BitcoinCashService.walletWords(wallet));
    }

    public static String randomCurrency() {
        return RandomStringUtils.randomAlphabetic(3).toUpperCase();
    }

    public static Wallet randomWallet() {
        try {
            return randomWallet(UnitTestParams.get());
        } catch (IllegalStateException damnBitcoinj) {
            return randomWallet(TestNet3Params.get());
        }
    }

    public static String randomBtcAddress(NetworkParameters params) {
        org.bitcoinj.wallet.Wallet wallet = new org.bitcoinj.wallet.Wallet(
                params,
                KeyChainGroup.builder(params).fromRandom(Script.ScriptType.P2PKH).build()
        );
        return wallet.currentReceiveAddress().toString();
    }

    public static String randomBtcAddress() {
        return randomBtcAddress(UnitTestParams.get());
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

    private static String randomVoucherCode() {
        return "wtu" + randomCurrency().toLowerCase() + "-" + randomString();
    }

    public static Voucher randomVoucher() {
        return new Voucher()
                .withAmount(nextLong(1_000, 2_000))
                .withCode(randomVoucherCode())
                .withWalletId(nextLong(1, 1_000))
                .withPublished(false)
                .withSold(false)
                .withRedeemed(false)
                .withSku(randomSku())
                .withExpiresAt(TimeUtil.clearTimeInformation(
                        ZonedDateTime.now(ZoneOffset.UTC).plusYears(2).toInstant().toEpochMilli()
                ));
    }

    public static Voucher randomValidVoucher() {
        return randomVoucher()
                .withPublished(true)
                .withSold(true)
                .withRedeemed(false)
                .withExpiresAt(TimeUtil.clearTimeInformation(
                        ZonedDateTime.now(ZoneOffset.UTC).plus(1, ChronoUnit.YEARS).toInstant().toEpochMilli())
                );
    }

    public static VoucherGenerationSpec randomVoucherGenerationSpec() {
        long id = nextLong(1_000, 2_000);
        int vouchersCount = nextInt(10, 1_000);

        String voucherCodes = IntStream.range(0, vouchersCount)
                .mapToObj(i -> randomVoucherCode())
                .collect(Collectors.joining("\n"));

        return new VoucherGenerationSpec()
                .withCount(vouchersCount)
                .withTotalAmount(vouchersCount * nextInt(600, 1000))
                .withWalletId(id)
                .withPriceCurrency("GBP")
                .withPrice(nextLong(5, 10))
                .withSku(randomSku())
                .withVoucherCodes(voucherCodes);
    }

    public static RedemptionRequest randomRedemptionRequest() {
        return new RedemptionRequest(
                randomString(),
                randomString());
    }

    public static String randomIp() {
        return nextInt(0, 256) + "." + nextInt(0, 256) + "." + nextInt(0, 256) + "." + nextInt(0, 256);
    }
}
