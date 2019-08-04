package es.coffeebyt.wtu.exception;

import java.util.function.Function;
import java.util.function.Supplier;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class Thrower {

    public static void logAndThrowIllegalOperationException(String message) {
        logAndThrow(message, () -> new IllegalOperationException(message));
    }

    public static void logAndThrow(String message, Function<String, RuntimeException> exceptionFactory) {
        log.error(message);
        throw exceptionFactory.apply(message);
    }

    public static void logAndThrow(String message, Supplier<RuntimeException> exceptionSupplier) {
        log.error(message);
        throw exceptionSupplier.get();
    }
}
