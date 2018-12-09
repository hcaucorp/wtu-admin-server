package com.jvmp.vouchershop.wallet;

import com.jvmp.vouchershop.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface WalletRepository extends JpaRepository<Wallet, Long> {

}
