package com.jvmp.vouchershop.crypto.btc;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.KeyChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Slf4j
@Component
public class BtcWalletService implements WalletService {

    private final WalletRepository walletRepository;

    private final NetworkParameters networkParameters;

    private final WalletAppKit bitcoinj;

    @Autowired
    public BtcWalletService(WalletRepository walletRepository, NetworkParameters networkParameters) {
        this(walletRepository, networkParameters, "wallet.storage");
    }

    public BtcWalletService(WalletRepository walletRepository, NetworkParameters networkParameters, String fileSuffix) {
        this.walletRepository = walletRepository;
        this.networkParameters = networkParameters;
        this.bitcoinj = new WalletAppKit(networkParameters, new File("."), fileSuffix);
    }

    @PostConstruct
    public void init() {
        try {

            bitcoinj.startAsync();
            bitcoinj.awaitRunning();

            org.bitcoinj.wallet.Wallet bitcoinjWallet = bitcoinj.wallet();
            Wallet wallet = walletRepository.findAll().stream() //if there is a wallet in db then it should be the same as bitcoinj one
                    .findFirst()
                    .orElseGet(() -> fromBitcoinjWallet(bitcoinjWallet)); //if there is no wallet in db, then take one from bitcoinj

            // check if these are the same
            if (!wallet.getMnemonic().equals(walletWords(bitcoinjWallet))) {

                if (walletRepository.count() > 0)
                    log.error("Found other wallet in the database: \n{}\n but bitcoinj is configured with yet another one \n{}\n",
                            wallet.getMnemonic(),
                            walletWords(bitcoinjWallet));
                // this "save" should happen only once on system setup, every other time bitcoinj and db wallet should be in sync
                // and there should be only one wallet per currency in DB
                walletRepository.save(wallet);
            }
        } catch (IllegalStateException ise) {
            log.error("BitcoinJ has failed to start", ise);
        }
    }

    @PreDestroy
    public void close() {
        bitcoinj.stopAsync();
        bitcoinj.awaitRunning();
    }

    private Wallet fromBitcoinjWallet(org.bitcoinj.wallet.Wallet bitcoinjWallet) {
        String walletWords = walletWords(bitcoinjWallet);
        long creationTime = bitcoinjWallet.getKeyChainSeed().getCreationTimeSeconds();

        Wallet wallet = new Wallet()
                .withAddress(bitcoinjWallet
                        .getActiveKeyChain()
                        .getKey(KeyChain.KeyPurpose.AUTHENTICATION)
                        .serializePubB58(networkParameters)
                )
                .withCreatedAt(Instant.ofEpochSecond(creationTime).toEpochMilli())
                .withCurrency("BTC")
                .withMnemonic(walletWords);

        log.info("Seed words are: {}", walletWords);
        log.info("Seed birthday is: {}", creationTime);

        return wallet;
    }

    private String walletWords(@Nonnull org.bitcoinj.wallet.Wallet bitcoinjWallet) {
        return String.join(
                " ",
                Optional.ofNullable(bitcoinjWallet.getKeyChainSeed().getMnemonicCode())
                        .orElse(emptyList()));
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
            result = bitcoinj.wallet()
                    .sendCoins(bitcoinj.peerGroup(), targetAddress, Coin.COIN);

            return Observable
                    .fromFuture(result.broadcastComplete)
                    .map(Transaction::getHashAsString);

        } catch (InsufficientMoneyException e) {
            log.error("Well, sending money has failed. Not enough funds on the wallet {}", from);
            log.error("Stack trace: not needed lol");
            return Observable.error(e);
        }
    }
}
