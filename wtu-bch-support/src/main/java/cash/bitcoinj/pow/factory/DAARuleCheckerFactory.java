package cash.bitcoinj.pow.factory;

import cash.bitcoinj.core.Block;
import cash.bitcoinj.core.NetworkParameters;
import cash.bitcoinj.core.StoredBlock;
import cash.bitcoinj.params.TestNet3Params;
import cash.bitcoinj.pow.AbstractRuleCheckerFactory;
import cash.bitcoinj.pow.RulesPoolChecker;
import cash.bitcoinj.pow.rule.MinimalDifficultyRuleChecker;
import cash.bitcoinj.pow.rule.NewDifficultyAdjustmentAlgorithmRulesChecker;

public class DAARuleCheckerFactory extends AbstractRuleCheckerFactory {

    public DAARuleCheckerFactory(NetworkParameters parameters) {
        super(parameters);
    }

    @Override
    public RulesPoolChecker getRuleChecker(StoredBlock storedPrev, Block nextBlock) {
        RulesPoolChecker rulesChecker = new RulesPoolChecker(networkParameters);
        if (isTestNet() && TestNet3Params.isValidTestnetDateBlock(nextBlock)) {
            rulesChecker.addRule(new MinimalDifficultyRuleChecker(networkParameters));
        } else {
            rulesChecker.addRule(new NewDifficultyAdjustmentAlgorithmRulesChecker(networkParameters));
        }
        return rulesChecker;
    }

}
