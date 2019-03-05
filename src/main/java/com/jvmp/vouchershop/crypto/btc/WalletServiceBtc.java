package com.jvmp.vouchershop.crypto.btc;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.ImportWalletRequest;
import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet.SendResult;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import java.time.Instant;
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
    private final BitcoinJAdapter bitcoinj;
    private final NotificationService notificationService;

    public static String walletWords(@Nonnull org.bitcoinj.wallet.Wallet bitcoinjWallet) {
        return String.join(" ", Optional.ofNullable(bitcoinjWallet.getKeyChainSeed().getMnemonicCode())
                .orElse(emptyList()));
    }

    @PreDestroy
    public void close() {
        bitcoinj.close();
    }

    public Optional<Wallet> importWallet(String mnemonic, long creationTime) throws UnreadableWalletException {
        if (!walletRepository.findAll().isEmpty()) {
            log.error("BTC wallet already exists. Currently we support only single wallet per currency");
            return Optional.empty();
        }

        if (creationTime > Instant.now().getEpochSecond()) {
            log.error("Creation time is set in the future. Are you trying to pass milli seconds?");
            return Optional.empty();
        }

        DeterministicSeed deterministicSeed = new DeterministicSeed(mnemonic, null, "", creationTime);
        org.bitcoinj.wallet.Wallet wallet = fromSeed(networkParameters, deterministicSeed);

        return Optional.of(restoreWalletAndStart(wallet,
                Instant.ofEpochSecond(creationTime).toEpochMilli()));
    }

    private Wallet restoreWalletAndStart(org.bitcoinj.wallet.Wallet bitcoinjWallet, long createdAtMillis) {
        bitcoinj.restoreWalletFromSeed(bitcoinjWallet.getKeyChainSeed());

        // order of withers matters here
        return save(new Wallet()
                .withBalance(bitcoinj.getBalance())
                .withAddress(bitcoinjWallet.currentReceiveAddress().toString())
                .withCreatedAt(createdAtMillis)
                .withCurrency("BTC")
                .withMnemonic(walletWords(bitcoinjWallet)));
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

        return restoreWalletAndStart(bitcoinjWallet, creationTime);
    }

    @Override
    public List<Wallet> findAll() {
        return walletRepository.findAll().stream()
                .map(wallet -> wallet.withBalance(bitcoinj.getBalance()))
                .collect(toList());
    }

    @Override
    public Optional<Wallet> findById(Long id) {
        return walletRepository.findById(id);
    }

    @Override
    public Wallet save(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    @Override
    public String sendMoney(Wallet from, String toAddress, long amount) {
        if (!"BTC".equals(from.getCurrency())) {
            log.error("Wallet {} can provide only for vouchers in BTC", from.toString());
            throw new IllegalOperationException("Wallet " + from.toString() + " can provide only for vouchers in BTC");
        }

        Address targetAddress = Address.fromBase58(networkParameters, toAddress);
        try {
            //using eco fees
            SendRequest sendRequest = SendRequest.to(targetAddress, Coin.valueOf(amount));
            sendRequest.feePerKb = Transaction.REFERENCE_DEFAULT_MIN_TX_FEE;

            SendResult sendResult = bitcoinj.sendCoins(sendRequest);

            return sendResult.tx.getHashAsString();
        } catch (InsufficientMoneyException e) {
            String message = "Not enough funds " + bitcoinj.getBalance() + " on the wallet " + from.getId();
            log.error(message);
            notificationService.pushRedemptionNotification(message);
            throw new IllegalOperationException(message);
        }
    }
}
