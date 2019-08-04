package es.coffeebyt.wtu.utils;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class TryUtils {

    private static final Supplier<AssertionError> exceptionExpected = () -> new AssertionError("Expected an exception");

    /**
     * run the lambda and retrieve the exception if there was anything thrown during the run
     **/
    public static Throwable expectingException(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            return throwable;
        }

        throw exceptionExpected.get();
    }
}
