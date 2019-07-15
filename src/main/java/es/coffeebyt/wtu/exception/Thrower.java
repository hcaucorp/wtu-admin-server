package es.coffeebyt.wtu.exception;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
@UtilityClass
public class Thrower {

    public static void logAndThrowIllegalOperationException(String message) {
        logAndThrow(message, IllegalOperationException::new);
    }

    public static void logAndThrow(String message, Function<String, RuntimeException> exceptionFactory) {
        log.error(message);
        throw exceptionFactory.apply(message);
    }
}
