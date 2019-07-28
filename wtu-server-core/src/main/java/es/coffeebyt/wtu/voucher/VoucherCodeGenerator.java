package es.coffeebyt.wtu.voucher;

import es.coffeebyt.wtu.voucher.impl.VoucherGenerationSpec;

import java.util.function.Function;

public interface VoucherCodeGenerator extends Function<VoucherGenerationSpec, String> {
}
