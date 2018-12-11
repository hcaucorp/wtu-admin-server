package com.jvmp.vouchershop.wallet;

import com.jvmp.vouchershop.domain.Wallet;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.wallet.KeyChain;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    private final NetworkParameters networkParameters;

    @Override
    public Wallet generateWallet(String password, String description) {

//        Wallet btcWallet = new Wallet(networkParameters);

//
//        BlockChain chain = null;
//        try {
//            chain = new BlockChain(networkParameters, btcWallet, blockStore);
//        } catch (BlockStoreException e) {
//            log.error("Cannot create wallet. See stacktrace below for more info", e);
//            throw new InternalServerException("Cause by BlockStoreException, original message: " + e.getMessage(), e);
//        }
//        PeerGroup peerGroup = new PeerGroup(networkParameters, chain);
//        peerGroup.addWallet(btcWallet);
//        peerGroup.start();

        org.bitcoinj.wallet.Wallet bitcoinjWallet = new org.bitcoinj.wallet.Wallet(networkParameters);
        String walletWords = walletWords(bitcoinjWallet);

        Wallet wallet = new Wallet()
                .withAddress(bitcoinjWallet
                        .getActiveKeyChain()
                        .getKey(KeyChain.KeyPurpose.AUTHENTICATION)
                        .serializePubB58(networkParameters)
                )
                .withDescription(description)
                .withExtendedPrivateKey(walletWords)
                .withBtcWallet(bitcoinjWallet);

        log.info("Seed words are: {}", walletWords);
        log.info("Seed birthday is: {}", bitcoinjWallet.getKeyChainSeed().getCreationTimeSeconds());

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
        findById(id)
                .map(wallet -> {
                    // prevent deleting wallet with balance and loosing the money on it
                    if (wallet.getBtcWallet().getBalance().isPositive()) {
                        throw new IllegalOperationException("Wallet balance is positive and can't be deleted. Move money to different wallet before deleting this wallet " +
                                "or else ALL the funds will be lost!");
                    }

                    return ResponseEntity.ok().build();
                })
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id " + id));

        walletRepository.deleteById(id);
    }

    @Override
    public Wallet save(Wallet wallet) {
        return walletRepository.save(wallet);
    }
}
