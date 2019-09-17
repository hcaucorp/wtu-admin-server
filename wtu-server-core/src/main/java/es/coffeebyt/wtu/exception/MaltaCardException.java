package es.coffeebyt.wtu.exception;

import static es.coffeebyt.wtu.exception.WtuErrorCodes.ONE_PER_CUSTOMER;

public class MaltaCardException extends IllegalOperationException {

    public MaltaCardException() {
        super(ONE_PER_CUSTOMER.name());
    }

}
