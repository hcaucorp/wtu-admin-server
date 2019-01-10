package com.jvmp.vouchershop;

import io.reactivex.Observable;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@UtilityClass
public class TryUtils {

    private static final Supplier<AssertionError> exceptionExpected = () -> new AssertionError("Expected an exception");

    /**
     * run the lambda and retrieve the exception if there was anything thrown during the run
     **/
    public static Throwable expectingException(Runnable runnable) {
        AtomicReference<Throwable> result = new AtomicReference<>();
        //noinspection ResultOfMethodCallIgnored
        Observable
                .fromCallable(() -> {
                    runnable.run();
                    return "ignored";
                })
                .subscribe(ignored -> {}, result::set);
        return Optional.ofNullable(result.get()).orElseThrow(exceptionExpected);
    }

}
