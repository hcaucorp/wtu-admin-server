package com.jvmp.vouchershop.crypto.bch;

import cash.bitcoinj.core.InsufficientMoneyException;
import cash.bitcoinj.kits.WalletAppKit;
import cash.bitcoinj.wallet.DeterministicSeed;
import cash.bitcoinj.wallet.SendRequest;
import cash.bitcoinj.wallet.Wallet;
import com.google.common.util.concurrent.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@SuppressWarnings("UnstableApiUsage")
@Slf4j
@Component
@RequiredArgsConstructor
public class BitcoinCashJAdapter implements AutoCloseable {

    private final WalletAppKit bitcoinj;

    public void close() {
        bitcoinj.stopAsync();
        bitcoinj.awaitTerminated();
    }

    void restoreWalletFromSeed(DeterministicSeed seed) {
        bitcoinj.restoreWalletFromSeed(seed);
    }

    private void startSilently() {
        if (bitcoinj.state() != Service.State.NEW) return;

        bitcoinj.startAsync();
        bitcoinj.awaitRunning();
    }

    public long getBalance() {
        startSilently();
        return bitcoinj.wallet().getBalance(Wallet.BalanceType.ESTIMATED_SPENDABLE).value;
    }

    Wallet.SendResult sendCoins(SendRequest sendRequest) throws InsufficientMoneyException {
        startSilently();

        return bitcoinj.wallet().sendCoins(bitcoinj.peerGroup(), sendRequest);
    }
}
