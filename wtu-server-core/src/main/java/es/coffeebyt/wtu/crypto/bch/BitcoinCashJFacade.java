package es.coffeebyt.wtu.crypto.bch;

import com.google.common.util.concurrent.Service.State;

import cash.bitcoinj.core.InsufficientMoneyException;
import cash.bitcoinj.kits.WalletAppKit;
import cash.bitcoinj.wallet.DeterministicSeed;
import cash.bitcoinj.wallet.SendRequest;
import cash.bitcoinj.wallet.Wallet;
import es.coffeebyt.wtu.crypto.btc.BitcoinException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static cash.bitcoinj.wallet.Wallet.BalanceType.ESTIMATED_SPENDABLE;
import static com.google.common.util.concurrent.Service.State.RUNNING;
import static java.lang.String.format;

@SuppressWarnings("UnstableApiUsage")
@Slf4j
@Component
@RequiredArgsConstructor
public class BitcoinCashJFacade implements AutoCloseable {

    @Qualifier("BitcoinCashWalletAppKit")
    private final WalletAppKit bitcoinj;

    public synchronized void close() {
        State state = bitcoinj.state();

        switch (state) {
        case STARTING:
            bitcoinj.awaitRunning();
            bitcoinj.stopAsync();
            bitcoinj.awaitTerminated();
            break;
        case RUNNING:
            bitcoinj.stopAsync();
            bitcoinj.awaitTerminated();
            break;
        case STOPPING:
            bitcoinj.awaitTerminated();
            break;
        default:
            // not running
            break;
        }
    }

    void restoreWalletFromSeed(DeterministicSeed seed) {
        bitcoinj.restoreWalletFromSeed(seed);
    }

    private synchronized void startSilently() {
        State state = bitcoinj.state();

        switch (state) {
        case NEW:
            bitcoinj.startAsync();
            bitcoinj.awaitRunning();
            break;
        case STARTING:
            bitcoinj.awaitRunning();
            break;
        case RUNNING:
            break;
        default:
            throw new BitcoinCashException(format("Can't start BitcoinCashJ service because it's in %s state.", state));
        }
    }

    synchronized void startAsync() {
        State state = bitcoinj.state();

        switch (state) {
        case NEW:
            bitcoinj.startAsync();
            break;
        case STARTING:
        case RUNNING:
            break;
        default:
            throw new BitcoinException(format("Can't start bitcoinj service because it's in %s state.", state));
        }
    }

    long getBalance() {
        if (bitcoinj.state() == RUNNING) {
            return bitcoinj.wallet().getBalance(ESTIMATED_SPENDABLE).value;
        }

        startAsync();
        return -1;
    }

    Wallet.SendResult sendCoins(SendRequest sendRequest) throws InsufficientMoneyException {
        startSilently(); // service must be up to send

        return bitcoinj.wallet().sendCoins(bitcoinj.peerGroup(), sendRequest);
    }
}
