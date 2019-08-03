package cash.bitcoinj.core.listeners;

import cash.bitcoinj.core.EmptyMessage;
import cash.bitcoinj.core.NetworkParameters;

public class FeeFilterMessage extends EmptyMessage {
    public FeeFilterMessage(NetworkParameters params) {
        super(params);
    }
}
