package com.jvmp.vouchershop.voucher;

import com.jvmp.vouchershop.voucher.impl.VoucherGenerationDetails;

import java.util.function.Function;

public interface VoucherCodeGenerator extends Function<VoucherGenerationDetails, String> {
}
