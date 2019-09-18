package es.coffeebyt.wtu.crypto.bch;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class BitcoinCashException extends RuntimeException {

    public BitcoinCashException(String message) {
        super(message);
    }
}
