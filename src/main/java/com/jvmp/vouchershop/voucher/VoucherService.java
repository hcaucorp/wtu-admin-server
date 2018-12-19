package com.jvmp.vouchershop.voucher;

import com.jvmp.vouchershop.domain.Voucher;

import java.util.List;

public interface VoucherService {

    List<Voucher> generateVouchers(VoucherGenerationSpec details);

    List<Voucher> findAll();

    void delete(long id);

    void save(List<Voucher> vouchers);
}
