package com.jvmp.vouchershop.wallet;

import java.util.List;
import java.util.Optional;

/**
 * Entity responsible for dealing with {@link com.jvmp.vouchershop.wallet.Wallet} objects. Nothing to do with money
 * nor currencies.
 * <p>
 * Not to be confused with {@link com.jvmp.vouchershop.crypto.CurrencyService} responsible for money related actions.
 */
public interface WalletService {

    Optional<Wallet> importWallet(ImportWalletRequest walletDescription);

    Wallet generateWallet(String currency);

    List<Wallet> findAll();

    Optional<Wallet> findById(Long id);

    Wallet save(Wallet wallet);
}
