package cash.bitcoinj.pow;

import cash.bitcoinj.core.Block;
import cash.bitcoinj.core.NetworkParameters;
import cash.bitcoinj.core.StoredBlock;

public abstract class AbstractRuleCheckerFactory {

    protected NetworkParameters networkParameters;

    public AbstractRuleCheckerFactory(NetworkParameters networkParameters) {
        this.networkParameters = networkParameters;
    }

    public abstract RulesPoolChecker getRuleChecker(StoredBlock storedPrev, Block nextBlock);

    protected boolean isTestNet() {
        return NetworkParameters.ID_TESTNET.equals(networkParameters.getId());
    }

}
