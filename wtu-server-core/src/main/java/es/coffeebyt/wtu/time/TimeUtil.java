package es.coffeebyt.wtu.time;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import lombok.experimental.UtilityClass;

import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneOffset.UTC;

@UtilityClass
public class TimeUtil {
    
    private static final long oneDayMillis = Duration.ofDays(1).toMillis();

    public static long clearTimeInformation(long epochMillis) {
        return ZonedDateTime
                .ofInstant(ofEpochMilli(epochMillis), UTC)
                .toLocalDate()
                .toEpochDay()
                * oneDayMillis;
    }

    /**
     * Creates time in millis but hour/minutes/seconds is gone, 0.
     */
    public static long twoYearsFromNowMillis() {
        return clearTimeInformation(
                ZonedDateTime.now(ZoneOffset.UTC).plusYears(2).toInstant().toEpochMilli()
        );
    }
}
