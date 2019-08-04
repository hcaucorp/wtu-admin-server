package es.coffeebyt.wtu.crypto.bch;

import com.google.common.util.concurrent.Service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import cash.bitcoinj.core.InsufficientMoneyException;
import cash.bitcoinj.kits.WalletAppKit;
import cash.bitcoinj.wallet.DeterministicSeed;
import cash.bitcoinj.wallet.SendRequest;
import cash.bitcoinj.wallet.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("UnstableApiUsage")
@Slf4j
@Component
@RequiredArgsConstructor
public class BitcoinCashJFacade implements AutoCloseable {

    @Qualifier("BitcoinCashWalletAppKit")
    private final WalletAppKit bitcoinj;

    public void close() {
        bitcoinj.stopAsync();
        bitcoinj.awaitTerminated();
    }

    void restoreWalletFromSeed(DeterministicSeed seed) {
        bitcoinj.restoreWalletFromSeed(seed);
    }

    void startSilently() {
        if (bitcoinj.state() != Service.State.NEW) return;

        bitcoinj.startAsync();
        bitcoinj.awaitRunning();
    }

    long getBalance() {
        startSilently();
        return bitcoinj.wallet().getBalance(Wallet.BalanceType.ESTIMATED_SPENDABLE).value;
    }

    Wallet.SendResult sendCoins(SendRequest sendRequest) throws InsufficientMoneyException {
        startSilently();

        return bitcoinj.wallet().sendCoins(bitcoinj.peerGroup(), sendRequest);
    }
}
