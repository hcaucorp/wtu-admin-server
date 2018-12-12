package com.jvmp.vouchershop.crypto.btc;

import com.jvmp.vouchershop.domain.VWallet;
import org.bitcoinj.core.Coin;
import org.bitcoinj.wallet.UnreadableWalletException;

import java.util.List;
import java.util.Optional;

public interface WalletService {

    VWallet generateWallet(String password, String description);

    List<VWallet> findAll();

    Optional<VWallet> findById(Long id);

    void delete(long id);

    VWallet save(VWallet VWallet);

    Optional<Coin> findBalance(VWallet VWallet) throws UnreadableWalletException;
}
