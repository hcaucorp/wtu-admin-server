package com.jvmp.vouchershop.voucher;

import com.jvmp.vouchershop.voucher.impl.VoucherGenerationDetails;

import java.util.List;

public interface VoucherService {

    List<Voucher> generateVouchers(VoucherGenerationDetails details);

    List<Voucher> findAll();

    void delete(long id);

    void save(List<Voucher> vouchers);
}
