package cash.bitcoinj.pow.factory;

import cash.bitcoinj.core.Block;
import cash.bitcoinj.core.NetworkParameters;
import cash.bitcoinj.core.StoredBlock;
import cash.bitcoinj.pow.AbstractRuleCheckerFactory;
import cash.bitcoinj.pow.RulesPoolChecker;

public class RuleCheckerFactory extends AbstractRuleCheckerFactory {

    private AbstractRuleCheckerFactory daaRulesFactory;
    private AbstractRuleCheckerFactory edaRulesFactory;

    private RuleCheckerFactory(NetworkParameters parameters) {
        super(parameters);
        this.daaRulesFactory = new DAARuleCheckerFactory(parameters);
        this.edaRulesFactory = new EDARuleCheckerFactory(parameters);
    }

    public static RuleCheckerFactory create(NetworkParameters parameters) {
        return new RuleCheckerFactory(parameters);
    }

    @Override
    public RulesPoolChecker getRuleChecker(StoredBlock storedPrev, Block nextBlock) {
        if (isNewDaaActivated(storedPrev, networkParameters)) {
            return daaRulesFactory.getRuleChecker(storedPrev, nextBlock);
        } else {
            return edaRulesFactory.getRuleChecker(storedPrev, nextBlock);
        }
    }

    private boolean isNewDaaActivated(StoredBlock storedPrev, NetworkParameters parameters) {
        return storedPrev.getHeight() >= parameters.getDAAUpdateHeight();
    }

}
