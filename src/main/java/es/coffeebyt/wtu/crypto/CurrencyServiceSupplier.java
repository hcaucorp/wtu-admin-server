package es.coffeebyt.wtu.crypto;

import es.coffeebyt.wtu.exception.InvalidConfigurationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

@Component
@RequiredArgsConstructor
public class CurrencyServiceSupplier {

    private final List<CurrencyService> registeredServices;

    public CurrencyService findByCurrency(String currency) {
        Set<CurrencyService> currencyServices = registeredServices.stream()
                .filter(currencyService -> currencyService.acceptsCurrency(currency))
                .collect(toSet());

        if (currencyServices.size() > 1) {
            String message = "Found %d different services to handle %s currency. There can be only one";
            throw new InvalidConfigurationException(format(message, currencyServices.size(), currency));
        }

        return currencyServices.stream().findFirst()
                .orElseThrow(() -> new CurrencyNotSupported(currency));
    }
}
