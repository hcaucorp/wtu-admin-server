package com.jvmp.vouchershop.voucher;

import com.jvmp.vouchershop.voucher.impl.VoucherGenerationSpec;

import java.util.function.Function;

public interface VoucherCodeGenerator extends Function<VoucherGenerationSpec, String> {
}
