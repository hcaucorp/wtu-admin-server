package com.jvmp.vouchershop.crypto.btc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.Service;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import io.reactivex.Observable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.bitcoinj.wallet.Wallet.fromSeed;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletServiceBtc implements WalletService, AutoCloseable {

    private final WalletRepository walletRepository;

    private final NetworkParameters networkParameters;

    private final WalletAppKit bitcoinj;

    private static String walletWords(@Nonnull org.bitcoinj.wallet.Wallet bitcoinjWallet) {
        return String.join(" ", Optional.ofNullable(bitcoinjWallet.getKeyChainSeed().getMnemonicCode())
                .orElse(emptyList()));
    }

    @PreDestroy
    public void close() {
        bitcoinj.stopAsync();
        bitcoinj.awaitRunning();
    }

    @VisibleForTesting
    void start() {
        try {
            if (walletRepository.findAll().isEmpty()) {
                log.error("There is no wallet found in the system. Generate a wallet first before attempting to use BitcoinJ.");
                return;
            }

            if (bitcoinj.state() != Service.State.NEW) {
                log.error("BitcoinJ is in state: {}. It cannot be started.", bitcoinj.state());
                return;
            }

            bitcoinj.startAsync();
            bitcoinj.awaitRunning();
        } catch (IllegalStateException | NullPointerException e) {
            // null pointer is thrown during tests, fucking piece of shit library
            log.error("BitcoinJ has failed to start", e);
        }
    }

    @VisibleForTesting
    public Optional<Wallet> importWallet(String mnemonics, long creationTime) throws UnreadableWalletException {
        if (!walletRepository.findAll().isEmpty()) {
            log.error("BTC wallet already exists. Currently we support only single wallet per currency");
            return Optional.empty();
        }

        return Optional.of(
                restoreWalletAndStart(
                        fromSeed(networkParameters, new DeterministicSeed(mnemonics, null, "", creationTime))));
    }

    private Wallet restoreWalletAndStart(org.bitcoinj.wallet.Wallet bitcoinjWallet) {
        bitcoinj.restoreWalletFromSeed(bitcoinjWallet.getKeyChainSeed());

        Wallet wallet = save(new Wallet()
                .withAddress(bitcoinjWallet.currentReceiveAddress().toString())
                .withCreatedAt(bitcoinjWallet.getEarliestKeyCreationTime())
                .withCurrency("BTC")
                .withMnemonic(walletWords(bitcoinjWallet)));

        start();

        return wallet;
    }

    public Wallet generateWallet(String currency) {
        if (!"BTC".equals(currency))
            throw new IllegalOperationException("Currency " + currency + " is not supported.");

        if (!walletRepository.findAll().isEmpty())
            throw new IllegalOperationException("BTC wallet already exists. Currently we support only single wallet per currency");

        org.bitcoinj.wallet.Wallet bitcoinjWallet = new org.bitcoinj.wallet.Wallet(networkParameters);
        String walletWords = walletWords(bitcoinjWallet);
        long creationTime = bitcoinjWallet.getKeyChainSeed().getCreationTimeSeconds();

        log.info("Seed words are: {}", walletWords);
        log.info("Seed birthday is: {}", creationTime);

        return restoreWalletAndStart(bitcoinjWallet);
    }

    @Override
    public List<Wallet> findAll() {
        return walletRepository.findAll().stream()
                .peek(wallet -> start())
                .map(wallet -> wallet.withBalance(bitcoinj.wallet().getBalance().value))
                .collect(toList());
    }

    @Override
    public Optional<Wallet> findById(Long id) {
        return walletRepository.findById(id);
    }

    @Override
    public Wallet save(Wallet Wallet) {
        return walletRepository.save(Wallet);
    }

    @Override
    public Observable<String> sendMoney(Wallet from, String toAddress, long amount) {
        if (!"BTC".equals(from.getCurrency()))
            throw new IllegalOperationException("Wallet " + from.toString() + " can provide only for vouchers in BTC");

        Address targetAddress = Address.fromBase58(networkParameters, toAddress);
        org.bitcoinj.wallet.Wallet.SendResult result;
        try {

            //using eco fees
            SendRequest sendRequest = SendRequest.to(targetAddress, Coin.valueOf(amount));
            sendRequest.feePerKb = Transaction.REFERENCE_DEFAULT_MIN_TX_FEE;

            result = bitcoinj.wallet()
                    .sendCoins(bitcoinj.peerGroup(), sendRequest);

            return Observable
                    .fromFuture(result.broadcastComplete)
                    .map(Transaction::getHashAsString);

        } catch (InsufficientMoneyException e) {
            log.error("Not enough funds {} on the wallet {}", bitcoinj.wallet().getBalance(), from);
            return Observable.error(e);
        }
    }
}