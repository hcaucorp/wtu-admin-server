package com.jvmp.vouchershop.crypto.btc;

import com.jvmp.vouchershop.crypto.CurrencyService;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.ImportWalletRequest;
import com.jvmp.vouchershop.wallet.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet.SendResult;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.Optional;

import static com.jvmp.vouchershop.exception.Thrower.logAndThrowIllegalOperationException;
import static java.lang.String.format;
import static java.time.Instant.ofEpochSecond;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.bitcoinj.wallet.Wallet.fromSeed;

@Slf4j
@Component
@RequiredArgsConstructor
public class BitcoinService implements CurrencyService, AutoCloseable {

    public final static String BTC = "BTC";

    private final WalletRepository walletRepository;
    private final NetworkParameters networkParameters;
    private final BitcoinJAdapter bitcoinj;

    public static String walletWords(@Nonnull org.bitcoinj.wallet.Wallet bitcoinjWallet) {
        return String.join(" ", Optional.ofNullable(bitcoinjWallet.getKeyChainSeed().getMnemonicCode())
                .orElse(emptyList()));
    }

    @PostConstruct
    public void start() {
        readWalletFromDB()
                .ifPresent(bitcoinj::restoreWalletFromSeed);
    }

    @PreDestroy
    public void close() {
        bitcoinj.close();
    }

    public Wallet importWallet(String mnemonic, long creationTime) {
        if (!walletRepository.findAll().isEmpty())
            logAndThrowIllegalOperationException("BTC wallet already exists. Currently we support only single wallet per currency");

        if (creationTime > Instant.now().getEpochSecond())
            logAndThrowIllegalOperationException("Creation time is set in the future. Are you trying to pass milli seconds?");

        try {
            DeterministicSeed deterministicSeed = new DeterministicSeed(mnemonic, null, "", creationTime);
            org.bitcoinj.wallet.Wallet wallet = fromSeed(networkParameters, deterministicSeed);
            return restoreWalletSaveAndStart(wallet, ofEpochSecond(creationTime).toEpochMilli());
        } catch (UnreadableWalletException e) {
            log.error(e.getMessage());
            throw new IllegalOperationException(e.getMessage());
        }
    }

    private Wallet restoreWalletSaveAndStart(org.bitcoinj.wallet.Wallet bitcoinjWallet, long createdAtMillis) {
        bitcoinj.restoreWalletFromSeed(bitcoinjWallet.getKeyChainSeed());

        Wallet wallet = new Wallet()
                .withBalance(bitcoinj.getBalance())
                .withAddress(bitcoinjWallet.currentReceiveAddress().toString())
                .withCreatedAt(createdAtMillis)
                .withCurrency(BTC)
                .withMnemonic(walletWords(bitcoinjWallet));

        return walletRepository.save(wallet);
    }

    @Override
    public Wallet importWallet(ImportWalletRequest walletDescription) {
        String mnemonic = requireNonNull(walletDescription.mnemonic);
        long createdAt = walletDescription.createdAt;
        return importWallet(mnemonic, createdAt);
    }

    private Optional<DeterministicSeed> readWalletFromDB() {
        return walletRepository.findOneByCurrency(BTC)
                .flatMap(wallet -> from(wallet.getMnemonic(), wallet.getCreatedAt()));
    }

    private Optional<DeterministicSeed> from(String mnemonic, long createdAtMillis) {
        try {
            long createdAtSeconds = Instant.ofEpochMilli(createdAtMillis).getEpochSecond();
            return Optional.of(new DeterministicSeed(mnemonic, null, "", createdAtSeconds));
        } catch (UnreadableWalletException e) {
            return Optional.empty();
        }
    }

    public Wallet generateWallet() {
        Optional<Wallet> wallet = walletRepository.findOneByCurrency(BTC);
        if (wallet.isPresent())
            logAndThrowIllegalOperationException("BTC wallet already exists. Currently we support only single wallet per currency");

        org.bitcoinj.wallet.Wallet bitcoinjWallet = new org.bitcoinj.wallet.Wallet(networkParameters);
        String walletWords = walletWords(bitcoinjWallet);
        long creationTime = bitcoinjWallet.getKeyChainSeed().getCreationTimeSeconds();

        log.info("Seed words are: {}", walletWords);
        log.info("Seed birthday is: {}", creationTime);

        return restoreWalletSaveAndStart(bitcoinjWallet, Instant.ofEpochSecond(creationTime).toEpochMilli());
    }

    @Override
    public String sendMoney(Wallet from, String toAddress, long amount) {
        if (!acceptsCurrency(from.getCurrency()))
            logAndThrowIllegalOperationException(format("Wallet's %s currency (%s) doesn't match supported currency: BTC", from.getId(), from.getCurrency()));

        Address targetAddress = Address.fromBase58(networkParameters, toAddress);
        try {
            //using eco fees
            SendRequest sendRequest = SendRequest.to(targetAddress, Coin.valueOf(amount));
            sendRequest.feePerKb = Transaction.REFERENCE_DEFAULT_MIN_TX_FEE;

            SendResult sendResult = bitcoinj.sendCoins(sendRequest);

            return sendResult.tx.getHashAsString();
        } catch (InsufficientMoneyException e) {
            String message = format("Not enough funds %s wallet. Available %d, but requested %d. Exception message: %s",
                    from.getId(), bitcoinj.getBalance(), amount, e.getMessage());
            throw new IllegalOperationException(message);
        }
    }

    @Override
    public long getBalance(Wallet wallet) {
        return acceptsCurrency(wallet.getCurrency()) ? bitcoinj.getBalance() : 0;
    }

    @Override
    public boolean acceptsCurrency(String currency) {
        return BTC.equals(currency);
    }
}
