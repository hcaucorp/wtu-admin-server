package com.jvmp.vouchershop.wallet;

import com.jvmp.vouchershop.domain.Wallet;

import java.util.List;
import java.util.Optional;

public interface WalletService {

    Wallet generateWallet(String password);

    List<Wallet> findAll();

    Optional<Wallet> findById(Long id);

    void delete(long id);

    Wallet save(Wallet wallet);
}
