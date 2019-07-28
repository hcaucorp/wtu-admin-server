package cash.bitcoinj.pow;

import cash.bitcoinj.core.*;
import cash.bitcoinj.store.BlockStore;
import cash.bitcoinj.store.BlockStoreException;

import java.util.ArrayList;
import java.util.List;

public class RulesPoolChecker extends AbstractPowRulesChecker {

    private List<AbstractPowRulesChecker> rules;

    public RulesPoolChecker(NetworkParameters networkParameters) {
        super(networkParameters);
        this.rules = new ArrayList<>();
    }

    @Override
    public void checkRules(StoredBlock storedPrev, Block nextBlock, BlockStore blockStore, AbstractBlockChain blockChain) throws VerificationException, BlockStoreException {
        for (AbstractPowRulesChecker rule : rules) {
            rule.checkRules(storedPrev, nextBlock, blockStore, blockChain);
        }
    }

    public void addRule(AbstractPowRulesChecker rule) {
        this.rules.add(rule);
    }

}
