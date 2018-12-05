package com.jvmp.vouchershop.wallet;

import com.google.common.base.Joiner;
import com.jvmp.vouchershop.exception.InternalServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.store.*;
import org.bitcoinj.wallet.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final NetworkParameters networkParameters;
    private final BlockStore blockStore;

    @Override
    public Wallet generateWallet(String password) {

        Wallet btcWallet = new Wallet(networkParameters);
        BlockChain chain = null;
        try {
            chain = new BlockChain(networkParameters, btcWallet, blockStore);
        } catch (BlockStoreException e) {
            log.error("Cannot create wallet. See stacktrace below for more info", e);
            throw new InternalServerException("Cause by BlockStoreException, original message: " + e.getMessage(), e);
        }
        PeerGroup peerGroup = new PeerGroup(networkParameters, chain);
        peerGroup.addWallet(btcWallet);
        peerGroup.start();

        DeterministicSeed seed = btcWallet.getKeyChainSeed();
        List<String> mnemonicCode = seed.getMnemonicCode();

        log.info("Seed words are: " + (mnemonicCode == null ? "" : Joiner.on(" ").join(mnemonicCode)));
        log.info("Seed birthday is: " + seed.getCreationTimeSeconds());

        return null;
    }
}
