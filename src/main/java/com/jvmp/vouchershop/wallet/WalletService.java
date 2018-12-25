package com.jvmp.vouchershop.wallet;

import java.util.List;
import java.util.Optional;

public interface WalletService {

    Wallet generateWallet(String password, String description);

    List<Wallet> findAll();

    Optional<Wallet> findById(Long id);

    void delete(long id);

    Wallet save(Wallet Wallet);

    String sendMoney(Wallet from, String toAddress, long amount);
}
