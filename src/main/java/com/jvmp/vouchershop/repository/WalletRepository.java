package com.jvmp.vouchershop.repository;

import com.jvmp.vouchershop.domain.VWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<VWallet, Long> {

}
