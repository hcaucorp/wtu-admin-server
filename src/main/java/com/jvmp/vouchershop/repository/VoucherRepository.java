package com.jvmp.vouchershop.repository;

import com.jvmp.vouchershop.voucher.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    List<Voucher> findBySoldFalseAndSku(String sku);
}
