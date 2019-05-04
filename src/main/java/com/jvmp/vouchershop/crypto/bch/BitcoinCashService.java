package com.jvmp.vouchershop.crypto.bch;

import cash.bitcoinj.core.*;
import cash.bitcoinj.wallet.DeterministicSeed;
import cash.bitcoinj.wallet.SendRequest;
import cash.bitcoinj.wallet.UnreadableWalletException;
import com.jvmp.vouchershop.crypto.CurrencyService;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.ImportWalletRequest;
import com.jvmp.vouchershop.wallet.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.Optional;

import static java.lang.String.format;
import static java.time.Instant.ofEpochSecond;
import static java.util.Collections.emptyList;

@Slf4j
@Component
@RequiredArgsConstructor
public class BitcoinCashService implements CurrencyService, AutoCloseable {

    public final static String BCH = "BCH";

    private final WalletRepository walletRepository;
    private final NetworkParameters networkParameters;
    private final BitcoinCashJAdapter bitcoinj;
    private final NotificationService notificationService;

    private static String walletWords(@Nonnull cash.bitcoinj.wallet.Wallet bitcoinjWallet) {
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

    public Optional<Wallet> importWallet(String mnemonic, long creationTime) throws UnreadableWalletException {
        if (walletRepository.findOneByCurrency(BCH).isPresent()) {
            log.error("BCH wallet already exists. Currently we support only single wallet per currency");
            return Optional.empty();
        }

        if (creationTime > Instant.now().getEpochSecond()) {
            log.error("Creation time is set in the future. Are you trying to pass milli seconds?");
            return Optional.empty();
        }

        DeterministicSeed deterministicSeed = new DeterministicSeed(mnemonic, null, "", creationTime);
        cash.bitcoinj.wallet.Wallet wallet = cash.bitcoinj.wallet.Wallet.fromSeed(networkParameters, deterministicSeed);

        return Optional.of(restoreWalletSaveAndStart(wallet,
                ofEpochSecond(creationTime).toEpochMilli()));
    }

    private Wallet restoreWalletSaveAndStart(cash.bitcoinj.wallet.Wallet bitcoinjWallet, long createdAtMillis) {
        bitcoinj.restoreWalletFromSeed(bitcoinjWallet.getKeyChainSeed());

        Wallet wallet = new Wallet()
                .withBalance(bitcoinj.getBalance())
                .withAddress(bitcoinjWallet.currentReceiveAddress().toString())
                .withCreatedAt(createdAtMillis)
                .withCurrency(BCH)
                .withMnemonic(walletWords(bitcoinjWallet));

        return walletRepository.save(wallet);
    }

    @Override
    public Optional<Wallet> importWallet(ImportWalletRequest walletDescription) {
        try {
            String mnemonic = walletDescription.mnemonic;
            long createdAt = walletDescription.createdAt;

            if (mnemonic == null)
                return Optional.empty();

            return importWallet(mnemonic, createdAt);
        } catch (UnreadableWalletException e) {
            log.error(e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<DeterministicSeed> readWalletFromDB() {
        return walletRepository.findOneByCurrency(BCH)
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
        if (walletRepository.findOneByCurrency(BCH).isPresent())
            throw new IllegalOperationException("BCH wallet already exists. Currently we support only single wallet per currency");

        cash.bitcoinj.wallet.Wallet cashjWallet = new cash.bitcoinj.wallet.Wallet(networkParameters);
        String walletWords = walletWords(cashjWallet);
        long creationTime = cashjWallet.getKeyChainSeed().getCreationTimeSeconds();

        log.info("Seed words are: {}", walletWords);
        log.info("Seed birthday is: {}", creationTime);

        return restoreWalletSaveAndStart(cashjWallet, Instant.ofEpochSecond(creationTime).toEpochMilli());
    }

    @Override
    public String sendMoney(Wallet from, String toAddress, long amount) {
        if (!acceptsCurrency(from.getCurrency())) {
            String message = format("Wallet %s can provide only for vouchers in %s", from.getId(), from.getCurrency());
            throw new IllegalOperationException(message);
        }

        Address targetAddress = Address.fromBase58(networkParameters, toAddress);
        try {
            //using eco fees
            SendRequest sendRequest = SendRequest.to(targetAddress, Coin.valueOf(amount));
            sendRequest.feePerKb = Transaction.REFERENCE_DEFAULT_MIN_TX_FEE;

            cash.bitcoinj.wallet.Wallet.SendResult sendResult = bitcoinj.sendCoins(sendRequest);

            return sendResult.tx.getHashAsString();
        } catch (InsufficientMoneyException e) {
            String message = format("Not enough funds %s wallet. Available %d, but requested %d", from.getId(), bitcoinj.getBalance(), amount);
            notificationService.pushRedemptionNotification(message);
            throw new IllegalOperationException(message);
        }
    }

    @Override
    public long getBalance(Wallet wallet) {
        return acceptsCurrency(wallet.getCurrency()) ? bitcoinj.getBalance() : 0;
    }

    @Override
    public boolean acceptsCurrency(String currency) {
        return BCH.equals(currency);
    }
}
