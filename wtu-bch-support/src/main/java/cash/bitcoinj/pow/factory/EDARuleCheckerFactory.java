package cash.bitcoinj.pow.factory;

import cash.bitcoinj.core.Block;
import cash.bitcoinj.core.NetworkParameters;
import cash.bitcoinj.core.StoredBlock;
import cash.bitcoinj.params.AbstractBitcoinNetParams;
import cash.bitcoinj.params.TestNet3Params;
import cash.bitcoinj.pow.AbstractPowRulesChecker;
import cash.bitcoinj.pow.AbstractRuleCheckerFactory;
import cash.bitcoinj.pow.RulesPoolChecker;
import cash.bitcoinj.pow.rule.DifficultyTransitionPointRuleChecker;
import cash.bitcoinj.pow.rule.EmergencyDifficultyAdjustmentRuleChecker;
import cash.bitcoinj.pow.rule.LastNonMinimalDifficultyRuleChecker;
import cash.bitcoinj.pow.rule.MinimalDifficultyNoChangedRuleChecker;

public class EDARuleCheckerFactory extends AbstractRuleCheckerFactory {

    public EDARuleCheckerFactory(NetworkParameters parameters) {
        super(parameters);
    }

    @Override
    public RulesPoolChecker getRuleChecker(StoredBlock storedPrev, Block nextBlock) {
        if (AbstractBitcoinNetParams.isDifficultyTransitionPoint(storedPrev, networkParameters)) {
            return getTransitionPointRulesChecker();
        } else {
            return getNoTransitionPointRulesChecker(storedPrev, nextBlock);
        }
    }

    private RulesPoolChecker getTransitionPointRulesChecker() {
        RulesPoolChecker rulesChecker = new RulesPoolChecker(networkParameters);
        rulesChecker.addRule(new DifficultyTransitionPointRuleChecker(networkParameters));
        return rulesChecker;
    }

    private RulesPoolChecker getNoTransitionPointRulesChecker(StoredBlock storedPrev, Block nextBlock) {
        RulesPoolChecker rulesChecker = new RulesPoolChecker(networkParameters);
        if (isTestNet() && TestNet3Params.isValidTestnetDateBlock(nextBlock)) {
            rulesChecker.addRule(new LastNonMinimalDifficultyRuleChecker(networkParameters));
        } else {
            if (AbstractPowRulesChecker.hasEqualDifficulty(
                    storedPrev.getHeader().getDifficultyTarget(), networkParameters.getMaxTarget())) {
                rulesChecker.addRule(new MinimalDifficultyNoChangedRuleChecker(networkParameters));
            } else {
                rulesChecker.addRule(new EmergencyDifficultyAdjustmentRuleChecker(networkParameters));
            }
        }
        return rulesChecker;
    }

}
