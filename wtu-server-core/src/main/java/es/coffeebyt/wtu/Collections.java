package es.coffeebyt.wtu;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Collections {

    @SafeVarargs
    public static <T> Set<T> asSet(T... ts) {
        return new HashSet<>(asList(ts));
    }
}
