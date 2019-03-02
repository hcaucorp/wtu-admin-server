package com.jvmp.vouchershop.wallet;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WalletService {

    Optional<Wallet> importWallet(Map<String, String> walletDescription);

    Wallet generateWallet(String currency);

    List<Wallet> findAll();

    Optional<Wallet> findById(Long id);

    Wallet save(Wallet Wallet);

    String sendMoney(Wallet from, String toAddress, long amount);
}
