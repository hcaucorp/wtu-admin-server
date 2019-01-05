package com.jvmp.vouchershop.crypto.btc;

import com.google.common.util.concurrent.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BitcoinJAdapter {

    private final WalletAppKit bitcoinj;

    public void start() {
        //noinspection UnstableApiUsage
        if (bitcoinj.state() != Service.State.NEW) {
            log.error("BitcoinJ is in state: {}. It cannot be started.", bitcoinj.state());
            return;
        }

        bitcoinj.startAsync();
        bitcoinj.awaitRunning();
    }

    public void close() {
        bitcoinj.stopAsync();
        bitcoinj.awaitRunning();
    }

    public void restoreWalletFromSeed(DeterministicSeed seed) {
        bitcoinj.restoreWalletFromSeed(seed);
    }

    public long getBalance() {
        return bitcoinj.wallet().getBalance().value;
    }

    public Wallet.SendResult sendCoins(SendRequest sendRequest) throws InsufficientMoneyException {
        return bitcoinj.wallet().sendCoins(bitcoinj.peerGroup(), sendRequest);
    }
}
