package com.jvmp.vouchershop.crypto.btc;

import com.google.common.annotations.VisibleForTesting;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import io.reactivex.Observable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.bitcoinj.wallet.Wallet.fromSeed;

@Slf4j
@Component
@RequiredArgsConstructor
public class BtcWalletService implements WalletService, AutoCloseable {

    private final WalletRepository walletRepository;

    private final NetworkParameters networkParameters;

    private final WalletAppKit bitcoinj;

    private static String walletWords(@Nonnull org.bitcoinj.wallet.Wallet bitcoinjWallet) {
        return String.join(" ", Optional.ofNullable(bitcoinjWallet.getKeyChainSeed().getMnemonicCode())
                .orElse(emptyList()));
    }

    @PostConstruct
    public void init() {
        if (!findAll().isEmpty())
            start();
    }

    @PreDestroy
    public void close() {
        bitcoinj.stopAsync();
        bitcoinj.awaitRunning();
    }

    @VisibleForTesting
    void start() {
        if (findAll().isEmpty())
            throw new IllegalOperationException("There is no wallet found in the system. Generate a wallet first before attempting to use Bitcoin (BTC) wallet library.");

        if (bitcoinj.isRunning())
            return;

        try {
            bitcoinj.startAsync();
            bitcoinj.awaitRunning();
        } catch (IllegalStateException ise) {
            log.error("BitcoinJ has failed to start", ise);
        }
    }

    @VisibleForTesting
    public Wallet importWallet(String mnemonics, long creationTime) throws UnreadableWalletException {
        if (!findAll().isEmpty())
            throw new IllegalOperationException("BTC wallet already exists. Currently we support only single wallet per currency");

        return restoreWalletAndStart(fromSeed(networkParameters, new DeterministicSeed(mnemonics, null, "", creationTime)));
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

    public Wallet generateWallet() {
        if (!findAll().isEmpty())
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
        return walletRepository.findAll();
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
