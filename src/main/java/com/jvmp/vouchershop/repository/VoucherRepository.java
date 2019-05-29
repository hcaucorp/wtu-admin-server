package com.jvmp.vouchershop.repository;

import com.jvmp.vouchershop.voucher.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    List<Voucher> findBySoldFalseAndSku(String sku);

    List<Voucher> findByPublishedFalseAndSku(String sku);

    List<Voucher> findByPublishedTrueAndSoldFalseAndSku(String sku);

    List<Voucher> findByRedeemedFalse();

    List<Voucher> findByRedeemedTrue();

    List<Voucher> findByRedeemedFalseAndSku(String sku);

    List<Voucher> findByRedeemedTrueAndSku(String sku);

    Optional<Voucher> findByCode(String code);
}
