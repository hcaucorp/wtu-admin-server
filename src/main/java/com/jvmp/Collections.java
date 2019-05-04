package com.jvmp;

import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

@UtilityClass
public class Collections {

    @SafeVarargs
    public static <T> Set<T> asSet(T... ts) {
        return new HashSet<>(asList(ts));
    }
}
