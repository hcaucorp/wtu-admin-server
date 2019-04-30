package com.jvmp.vouchershop.wallet;

import com.jvmp.vouchershop.crypto.CurrencyService;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class CurrencyServiceSupplier implements Function<String, CurrencyService> {

    private final List<CurrencyService> registeredServices;

    public Optional<CurrencyService> findByCurrency(String currency) {
        return registeredServices.stream()
                .filter(currencyService -> currencyService.worksWith(currency))
                .findFirst();
    }

    @Override
    public CurrencyService apply(String currency) {
        return findByCurrency(currency)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Currency %s not supported", currency)));
    }
}
