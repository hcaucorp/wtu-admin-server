package com.jvmp.vouchershop.voucher.impl;

import com.jvmp.vouchershop.voucher.VoucherCodeGenerator;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

public class FromSpecCodeGenerator implements VoucherCodeGenerator {

    private final Deque<String> voucherCodes;

    public FromSpecCodeGenerator(Collection<String> codes) {
        voucherCodes = new LinkedList<>(codes);
    }

    @Override
    public String apply(VoucherGenerationSpec voucherGenerationSpec) {
        return voucherCodes.poll();
    }
}
