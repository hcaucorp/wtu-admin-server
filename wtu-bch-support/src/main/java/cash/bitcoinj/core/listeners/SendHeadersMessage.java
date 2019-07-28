package cash.bitcoinj.core.listeners;

import cash.bitcoinj.core.EmptyMessage;
import cash.bitcoinj.core.NetworkParameters;

/**
 * Created by HashEngineering on 8/11/2017.
 */
public class SendHeadersMessage extends EmptyMessage {
    public SendHeadersMessage(NetworkParameters params) {
        super(params);
    }
}
