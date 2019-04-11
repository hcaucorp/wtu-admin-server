package com.jvmp.vouchershop.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jvmp.vouchershop.system.PropertyNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedemptionAttemptService {

    @Value(PropertyNames.ENUMERATION_PROTECTION_MAX_ATTEMPTS)
    public static int maxAttempts;
    @Value(PropertyNames.ENUMERATION_PROTECTION_COOL_DOWN_TIME)
    public static int coolDownTime = 10;
    @Value(PropertyNames.ENUMERATION_PROTECTION_COOL_DOWN_UNIT)
    public static String coolDownUnit;

    @SuppressWarnings("UnstableApiUsage")
    private LoadingCache<String, Integer> attemptsCache;

    @SuppressWarnings("WeakerAccess")
    public RedemptionAttemptService() {
        //noinspection NullableProblems
        attemptsCache = CacheBuilder.newBuilder().
                expireAfterWrite(1, TimeUnit.valueOf(coolDownUnit))
                .build(new CacheLoader<String, Integer>() {
            public Integer load(String ipAddress) {
                return 0;
            }
        });
    }

    public void succeeded(String ipAddress) {
        attemptsCache.invalidate(ipAddress);
    }

    public void failed(String ipAddress) {
        int attempts = 0;
        try {
            attempts = attemptsCache.get(ipAddress);
        } catch (ExecutionException ignored) {
        }
        attempts++;
        attemptsCache.put(ipAddress, attempts);

        if (attempts % 100_000 == 0)
            log.warn("{} attempts made from ip {}", attempts, ipAddress);
    }

    public boolean isBlocked(String ipAddress) {
        try {
            return attemptsCache.get(ipAddress) >= maxAttempts;
        } catch (ExecutionException e) {
            return false;
        }
    }
}
