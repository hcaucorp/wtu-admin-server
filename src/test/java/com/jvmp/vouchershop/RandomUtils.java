package com.jvmp.vouchershop;

import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.wallet.Wallet;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@UtilityClass
public class RandomUtils {

    public static Wallet wallet() {
        return new Wallet()
                .withAddress("Test wallet address #" + RandomStringUtils.randomNumeric(12))
                .withCreatedAt(Date.from(Instant.now()))
                .withCurrency(RandomStringUtils.randomAlphabetic(3).toUpperCase())
                .withMnemonic(Stream.of(
                        "behave snap girl enforce sadness boil fine during use anchor screen sample".split(" "))
                        .map(word -> ImmutablePair.of(word, org.apache.commons.lang3.RandomUtils.nextLong(1, 20)))
                        .sorted(Comparator.comparingLong(ImmutablePair::getRight))
                        .map(ImmutablePair::getLeft)
                        .collect(joining(" "))
                )
                .withDescription("Description for integration test " + RandomStringUtils.randomNumeric(12));
    }

    public static Order order() {
        return new Order();
    }
}
