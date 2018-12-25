package com.jvmp.vouchershop.voucher;

import com.jvmp.vouchershop.voucher.impl.VoucherGenerationDetails;
import com.jvmp.vouchershop.voucher.impl.VoucherRedemptionDetails;

import java.util.List;

public interface VoucherService {

    List<Voucher> generateVouchers(VoucherGenerationDetails details);

    List<Voucher> findAll();

    void delete(long id);

    void save(List<Voucher> vouchers);

    /**
     * Sends money represented by the voucher to a prescribed recepient (wallet address)
     *
     * @param detail
     * @return A transaction identifier, currency agnostic, a way to uniquely identify transaction and provide a tracking info or kind of proof the transaction
     * has been performed. For BTC/~BCH it would be a transaction hash which allows to see the transaciton in the blockchain (blockchain explorer link for
     * tracking information)
     */
    String redeemVoucher(VoucherRedemptionDetails detail);
}
