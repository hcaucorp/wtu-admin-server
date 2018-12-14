package com.jvmp.vouchershop.voucher;

import com.jvmp.vouchershop.domain.Voucher;

import java.util.List;
import java.util.Optional;

public interface VoucherService {

    List<Voucher> generateVouchers(VoucherGenerationSpec details);

    List<Voucher> findAll();

    Optional<Voucher> findById(long id);

    void delete(long id);

    Voucher save(Voucher voucher);

    void save(List<Voucher> vouchers);
}
