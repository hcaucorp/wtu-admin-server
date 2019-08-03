package cash.bitcoinj.core.listeners;

import cash.bitcoinj.core.EmptyMessage;
import cash.bitcoinj.core.NetworkParameters;

public class SendHeadersMessage extends EmptyMessage {

    // this is needed by the BitcoinSerializer
    public SendHeadersMessage(NetworkParameters params, byte[] payload) {
        super(params, payload, 0);
    }
}
