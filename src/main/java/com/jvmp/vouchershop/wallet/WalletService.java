package com.jvmp.vouchershop.wallet;

import java.util.List;
import java.util.Optional;

public interface WalletService {

    Optional<Wallet> importWallet(ImportWalletRequest walletDescription);

    Wallet generateWallet(String currency);

    List<Wallet> findAll();

    Optional<Wallet> findById(Long id);

    Wallet save(Wallet Wallet);

    String sendMoney(Wallet from, String toAddress, long amount);

    boolean canHandle(String currency);
}
