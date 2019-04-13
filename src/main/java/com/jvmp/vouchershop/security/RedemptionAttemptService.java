package com.jvmp.vouchershop.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jvmp.vouchershop.system.PropertyNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedemptionAttemptService {

    private final int maxAttempts;

    private LoadingCache<String, Integer> attemptsCache;

    public RedemptionAttemptService(
            @Value(PropertyNames.ENUMERATION_PROTECTION_COOL_DOWN_TIME) int coolDownTime,
            @Value(PropertyNames.ENUMERATION_PROTECTION_COOL_DOWN_UNIT) String coolDownUnit,
            @Value(PropertyNames.ENUMERATION_PROTECTION_MAX_ATTEMPTS) int maxAttempts
    ) {
        attemptsCache = CacheBuilder.newBuilder().
                expireAfterWrite(coolDownTime, TimeUnit.valueOf(coolDownUnit))
                .build(new CacheLoader<String, Integer>() {
                    public Integer load(@Nonnull String ipAddress) {
                        return 0;
                    }
                });
        this.maxAttempts = maxAttempts;
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
