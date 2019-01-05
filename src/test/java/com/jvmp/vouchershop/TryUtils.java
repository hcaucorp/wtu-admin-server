package com.jvmp.vouchershop;

import io.reactivex.Observable;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@UtilityClass
public class TryUtils {

    /**
     * run the lambda and retreive the exception if there was anything thrown during the run
     **/
    public static Optional<Throwable> tryy(Runnable runnable) {
        AtomicReference<Throwable> result = new AtomicReference<>();
        Observable.fromCallable(() -> {
            runnable.run();
            return "ignored";
        })
                .subscribe(ignored -> {
                }, result::set);
        return Optional.ofNullable(result.get());
    }
}
