package com.jvmp.vouchershop.crypto.btc;

import com.jvmp.vouchershop.domain.VWallet;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@RequiredArgsConstructor
@ThreadSafe
public class WalletServiceImpl implements WalletService {

    private final Map<Long, Wallet> restoredWallets = new ConcurrentHashMap<>();

    private final WalletRepository walletRepository;

    private final NetworkParameters networkParameters;

    @Override
    public VWallet generateWallet(String password, String description) {

//        VWallet btcWallet = new VWallet(networkParameters);

//
//        BlockChain chain = null;
//        try {
//            chain = new BlockChain(networkParameters, btcWallet, blockStore);
//        } catch (BlockStoreException e) {
//            log.error("Cannot create VWallet. See stacktrace below for more info", e);
//            throw new InternalServerException("Cause by BlockStoreException, original message: " + e.getMessage(), e);
//        }
//        PeerGroup peerGroup = new PeerGroup(networkParameters, chain);
//        peerGroup.addWallet(btcWallet);
//        peerGroup.start();

        org.bitcoinj.wallet.Wallet bitcoinjWallet = new org.bitcoinj.wallet.Wallet(networkParameters);
        String walletWords = walletWords(bitcoinjWallet);
        long creationTime = bitcoinjWallet.getKeyChainSeed().getCreationTimeSeconds();

        VWallet vWallet = new VWallet()
                .withAddress(bitcoinjWallet
                        .getActiveKeyChain()
                        .getKey(KeyChain.KeyPurpose.AUTHENTICATION)
                        .serializePubB58(networkParameters)
                )
                .withCreationTime(creationTime)
                .withDescription(description)
                .withMnemonic(walletWords);

        log.info("Seed words are: {}", walletWords);
        log.info("Seed birthday is: {}", creationTime);

        return vWallet;
    }

    private String walletWords(@Nonnull org.bitcoinj.wallet.Wallet bitcoinjWallet) {
        return String.join(
                " ",
                Optional.ofNullable(bitcoinjWallet.getKeyChainSeed().getMnemonicCode())
                        .orElse(emptyList()));
    }

    @Override
    public List<VWallet> findAll() {
        return walletRepository.findAll()
                .stream()
                .map(vWallet -> findBalance(vWallet)
                        .map(balance -> vWallet.withBalance(balance.value))
                        .orElse(vWallet))
                .collect(toList());
    }

    @Override
    public Optional<VWallet> findById(Long id) {
        return walletRepository.findById(id);
    }

    @Override
    public void delete(long id) {
        VWallet vWallet = findById(id).orElseThrow(() -> new ResourceNotFoundException("VWallet not found with id " + id));

        // prevent deleting wallet with balance and loosing the money on it
        findBalance(vWallet).ifPresent(
                coin -> {
                    if (coin.isPositive())
                        throw new IllegalOperationException("VWallet balance is positive and can't be deleted. Move money to different wallet before deleting this wallet " +
                                "or else ALL the funds will be lost!");

                    walletRepository.deleteById(id);
                });
    }

    @Override
    public VWallet save(VWallet VWallet) {
        return walletRepository.save(VWallet);
    }

    @Override
    public Optional<Coin> findBalance(VWallet vWallet) {
        long id = vWallet.getId();

        if (!restoredWallets.containsKey(id))
            restoreWallet(vWallet).ifPresent(wallet -> restoredWallets.put(id, wallet));


        return restoredWallets.containsKey(vWallet.getId()) ?
                Optional.of(restoredWallets.get(vWallet.getId()).getBalance()) :
                Optional.empty();
    }

    private Optional<Wallet> restoreWallet(VWallet vWallet) {
        try {
            DeterministicSeed seed = new DeterministicSeed(vWallet.getMnemonic(), null, "", vWallet.getCreationTime());
            return Optional.of(Wallet.fromSeed(networkParameters, seed));
        } catch (UnreadableWalletException e) {
            log.error("Wallet id={} can't be restored by bitcoinj library. Who the fuck knows why...", vWallet.getId());
            log.error("Cause: ", e);
            return Optional.empty();
        }
    }
}
