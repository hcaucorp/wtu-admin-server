package es.coffeebyt.wtu.time;

import java.time.Duration;
import java.time.ZonedDateTime;

import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneOffset.UTC;

public class TimeStamp {

    private TimeStamp() {}

    private static final long oneDayMillis = Duration.ofDays(1).toMillis();

    public static long clearTimeInformation(long epochMillis) {
        return ZonedDateTime
                .ofInstant(ofEpochMilli(epochMillis), UTC)
                .toLocalDate()
                .toEpochDay()
                * oneDayMillis;
    }

}
