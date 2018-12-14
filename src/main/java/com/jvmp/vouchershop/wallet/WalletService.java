package com.jvmp.vouchershop.wallet;

import com.jvmp.vouchershop.domain.Wallet;
import org.bitcoinj.core.Coin;
import org.bitcoinj.wallet.UnreadableWalletException;

import java.util.List;
import java.util.Optional;

public interface WalletService {

    Wallet generateWallet(String password, String description);

    List<Wallet> findAll();

    Optional<Wallet> findById(Long id);

    void delete(long id);

    Wallet save(Wallet Wallet);
}
