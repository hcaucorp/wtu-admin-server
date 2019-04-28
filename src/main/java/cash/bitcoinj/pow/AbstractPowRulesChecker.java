package cash.bitcoinj.pow;

import cash.bitcoinj.core.*;
import cash.bitcoinj.store.BlockStore;
import cash.bitcoinj.store.BlockStoreException;

import java.math.BigInteger;

public abstract class AbstractPowRulesChecker {

    protected NetworkParameters networkParameters;

    public AbstractPowRulesChecker(NetworkParameters networkParameters) {
        this.networkParameters = networkParameters;
    }

    public static boolean hasEqualDifficulty(Block prevBlock, Block nextBlock) {
        return prevBlock.getDifficultyTarget() == nextBlock.getDifficultyTarget();
    }

    public static boolean hasEqualDifficulty(long a, BigInteger b) {
        return a == Utils.encodeCompactBits(b);
    }

    public abstract void checkRules(StoredBlock storedPrev, Block nextBlock, BlockStore blockStore,
                                    AbstractBlockChain blockChain) throws VerificationException, BlockStoreException;

}
