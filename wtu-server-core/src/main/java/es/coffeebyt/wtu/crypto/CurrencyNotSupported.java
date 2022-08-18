package es.coffeebyt.wtu.crypto;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CurrencyNotSupported extends RuntimeException {

    public CurrencyNotSupported(String currency) {
        super("Currency " + currency + " not supported");
    }
}
