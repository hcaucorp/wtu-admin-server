package com.jvmp.vouchershop.crypto.btc;

import com.jvmp.vouchershop.domain.Wallet;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyList;

@Slf4j
@Component
@RequiredArgsConstructor
@ThreadSafe
public class BtcWalletService implements WalletService {

    private final Map<Long, org.bitcoinj.wallet.Wallet> restoredWallets = new ConcurrentHashMap<>();

    private final WalletRepository walletRepository;

    private final NetworkParameters networkParameters;

    @Override
    public Wallet generateWallet(String password, String description) {
        org.bitcoinj.wallet.Wallet bitcoinjWallet = new org.bitcoinj.wallet.Wallet(networkParameters);
        String walletWords = walletWords(bitcoinjWallet);
        long creationTime = bitcoinjWallet.getKeyChainSeed().getCreationTimeSeconds();

        Wallet wallet = new Wallet()
                .withAddress(bitcoinjWallet
                        .getActiveKeyChain()
                        .getKey(KeyChain.KeyPurpose.AUTHENTICATION)
                        .serializePubB58(networkParameters)
                )
                .withCreatedAt(new Date(Instant.ofEpochSecond(creationTime).toEpochMilli()))
                .withCurrency("BTC")
                .withDescription(description)
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
    public void delete(long id) {
        findById(id).orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id " + id));

        // prevent deleting wallet with balance and loosing the money on it
//        findBalance(wallet).ifPresent(
//                coin -> {
//                    if (coin.isPositive())
//                        throw new IllegalOperationException("Wallet balance is positive and can't be deleted. Move money to different wallet before deleting this wallet " +
//                                "or else ALL the funds will be lost!");
//
//                    walletRepository.deleteById(id);
//                });
//

        walletRepository.deleteById(id);
    }

    @Override
    public Wallet save(Wallet Wallet) {
        return walletRepository.save(Wallet);
    }

    public Optional<Coin> findBalance(Wallet w) {
        long id = w.getId();

        if (!restoredWallets.containsKey(id))
            restoreWallet(w).ifPresent(wallet -> restoredWallets.put(id, wallet));


        return restoredWallets.containsKey(w.getId()) ?
                Optional.of(restoredWallets.get(w.getId()).getBalance()) :
                Optional.empty();
    }

    private Optional<org.bitcoinj.wallet.Wallet> restoreWallet(Wallet wallet) {
        try {
            long creationTime = wallet.getCreatedAt().toInstant().getEpochSecond();
            DeterministicSeed seed = new DeterministicSeed(wallet.getMnemonic(), null, "", creationTime);
            return Optional.of(org.bitcoinj.wallet.Wallet.fromSeed(networkParameters, seed));
        } catch (UnreadableWalletException e) {
            log.error("Wallet id={} can't be restored by bitcoinj library. Who the fuck knows why...", wallet.getId());
            log.error("Cause: ", e);
            return Optional.empty();
        }
    }
}
