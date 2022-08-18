package es.coffeebyt.wtu.crypto.btc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class BitcoinException extends RuntimeException {

    public BitcoinException(String message) {
        super(message);
    }
}
