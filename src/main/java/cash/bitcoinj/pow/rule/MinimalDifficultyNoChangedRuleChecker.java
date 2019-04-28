package cash.bitcoinj.pow.rule;

import cash.bitcoinj.pow.AbstractPowRulesChecker;
import cash.bitcoinj.store.BlockStore;
import cash.bitcoinj.store.BlockStoreException;

import java.math.BigInteger;

public class MinimalDifficultyNoChangedRuleChecker extends AbstractPowRulesChecker {

    public MinimalDifficultyNoChangedRuleChecker(NetworkParameters networkParameters) {
        super(networkParameters);
    }

    @Override
    public void checkRules(StoredBlock storedPrev, Block nextBlock, BlockStore blockStore,
                           AbstractBlockChain blockChain) throws VerificationException, BlockStoreException {
        Block prevBlock = storedPrev.getHeader();
        BigInteger minDifficulty = networkParameters.getMaxTarget();

        if (hasEqualDifficulty(prevBlock.getDifficultyTarget(), minDifficulty)) {
            if (!hasEqualDifficulty(prevBlock, nextBlock)) {
                throw new VerificationException("Unexpected change in difficulty at height " +
                        storedPrev.getHeight() + ": " +
                        Long.toHexString(nextBlock.getDifficultyTarget()) + " vs " +
                        Long.toHexString(prevBlock.getDifficultyTarget()));
            }
        }
    }

}
