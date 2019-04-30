package com.jvmp.vouchershop.repository;

import com.jvmp.vouchershop.wallet.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findOneByCurrency(String currency);

}
