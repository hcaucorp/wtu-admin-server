package com.jvmp.vouchershop.wallet;

import com.jvmp.vouchershop.crypto.CurrencyService;
import com.jvmp.vouchershop.exception.CurrencyNotSupported;
import com.jvmp.vouchershop.exception.InvalidConfigurationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
@Component
public class CurrencyServiceSupplier {

    private final List<CurrencyService> registeredServices;

    public CurrencyService findByCurrency(String currency) {
        Set<CurrencyService> currencyServices = registeredServices.stream()
                .filter(currencyService -> currencyService.worksWith(currency))
                .collect(toSet());

        if (currencyServices.size() > 1) {
            String message = "Found %d different services to handle %s currency. There can be only one";
            throw new InvalidConfigurationException(format(message, currencyServices.size(), currency));
        }

        return currencyServices.stream().findFirst()
                .orElseThrow(() -> new CurrencyNotSupported(currency));
    }
}
