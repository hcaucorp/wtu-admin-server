package com.jvmp.vouchershop.wallet;

import io.reactivex.Observable;

import java.util.List;
import java.util.Optional;

public interface WalletService {

    List<Wallet> findAll();

    Optional<Wallet> findById(Long id);

    Wallet save(Wallet Wallet);

    Observable<String> sendMoney(Wallet from, String toAddress, long amount);
}
