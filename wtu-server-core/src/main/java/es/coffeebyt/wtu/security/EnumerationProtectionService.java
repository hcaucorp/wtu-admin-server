package es.coffeebyt.wtu.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import es.coffeebyt.wtu.exception.IllegalOperationException;
import es.coffeebyt.wtu.system.PropertyNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class EnumerationProtectionService {

    private final int maxAttempts;

    private LoadingCache<String, Integer> attemptsCache;

    public EnumerationProtectionService(
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

    private static String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    public void succeeded(HttpServletRequest request) {
        attemptsCache.invalidate(getClientIP(request));
    }

    public boolean isBlocked(String ipAddress) {
        try {
            return attemptsCache.get(ipAddress) >= maxAttempts;
        } catch (ExecutionException e) {
            return false;
        }
    }

    public void failed(HttpServletRequest request) {
        String ip = getClientIP(request);

        int attempts = 0;
        try {
            attempts = attemptsCache.get(ip);
        } catch (ExecutionException ignored) {
            // ignore
        }
        attempts++;
        attemptsCache.put(ip, attempts);

        if (attempts % 100_000 == 0)
            log.warn("{} attempts made from ip {}", attempts, ip);
    }

    public void checkIfBlocked(HttpServletRequest request) {
        String ip = getClientIP(request);

        if (isBlocked(ip)) {
            throw new IllegalOperationException("IP is blocked: " + ip);
        }
    }
}
