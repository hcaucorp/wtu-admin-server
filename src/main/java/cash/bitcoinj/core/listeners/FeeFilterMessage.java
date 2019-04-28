package cash.bitcoinj.core.listeners;

import cash.bitcoinj.core.EmptyMessage;
import cash.bitcoinj.core.NetworkParameters;

/**
 * Created by HashEngineering on 8/11/2017.
 */
public class FeeFilterMessage extends EmptyMessage {
    public FeeFilterMessage(NetworkParameters params) {
        super(params);
    }
}
