/*
 * Copyright 2014 the bitcoinj authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cash.bitcoinj.wallet;

import java.util.HashSet;
import java.util.List;

import cash.bitcoinj.core.Coin;
import cash.bitcoinj.core.Transaction;
import cash.bitcoinj.core.TransactionInput;
import cash.bitcoinj.core.TransactionOutPoint;
import cash.bitcoinj.core.TransactionOutput;

/**
 * A filtering coin selector delegates to another coin selector, but won't select outputs spent by the given transactions.
 */
public class FilteringCoinSelector implements CoinSelector {
    protected CoinSelector delegate;
    protected HashSet<TransactionOutPoint> spent = new HashSet<>();

    public FilteringCoinSelector(CoinSelector delegate) {
        this.delegate = delegate;
    }

    public void excludeOutputsSpentBy(Transaction tx) {
        for (TransactionInput input : tx.getInputs()) {
            spent.add(input.getOutpoint());
        }
    }

    @Override
    public CoinSelection select(Coin target, List<TransactionOutput> candidates) {
        candidates.removeIf(output -> spent.contains(output.getOutPointFor()));
        return delegate.select(target, candidates);
    }
}
