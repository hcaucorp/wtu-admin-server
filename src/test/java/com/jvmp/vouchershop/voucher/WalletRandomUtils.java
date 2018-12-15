package com.jvmp.vouchershop.voucher;

import com.jvmp.vouchershop.domain.Wallet;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@UtilityClass
public class WalletRandomUtils {

    public static Wallet wallet() {
        return  new Wallet()
                .withAddress("Test wallet address #" + RandomStringUtils.randomNumeric(12))
                .withCreatedAt(Date.from(Instant.now()))
                .withCurrency(RandomStringUtils.randomAlphabetic(3).toUpperCase())
                .withMnemonic(Stream.of(
                        "behave snap girl enforce sadness boil fine during use anchor screen sample".split(" "))
                        .map(word -> ImmutablePair.of(word, RandomUtils.nextLong(1, 20)))
                        .sorted(Comparator.comparingLong(ImmutablePair::getRight))
                        .map(ImmutablePair::getLeft)
                        .collect(joining(" "))
                )
                .withDescription("Description for integration test " + RandomStringUtils.randomNumeric(12));
    }
}
