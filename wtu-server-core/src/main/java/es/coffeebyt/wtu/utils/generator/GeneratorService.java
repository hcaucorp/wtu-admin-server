package es.coffeebyt.wtu.utils.generator;

import es.coffeebyt.wtu.voucher.VoucherCodeGenerator;
import es.coffeebyt.wtu.voucher.impl.VoucherGenerationSpec;
import lombok.RequiredArgsConstructor;

import static es.coffeebyt.wtu.voucher.impl.DefaultVoucherCodeGenerator.CURRENCY_PATTERN;
import static es.coffeebyt.wtu.voucher.impl.DefaultVoucherCodeGenerator.VOUCHER_CODE_PATTERN;
import static es.coffeebyt.wtu.voucher.impl.DefaultVoucherCodeGenerator.UUID_PATTERN;
import static es.coffeebyt.wtu.voucher.impl.DefaultVoucherCodeGenerator.uuid;

@RequiredArgsConstructor
public class GeneratorService implements VoucherCodeGenerator {

    private final String currency;

    @Override public String apply(VoucherGenerationSpec voucherGenerationSpec) {
            return VOUCHER_CODE_PATTERN
                    .replace(CURRENCY_PATTERN, currency)
                    .replace(UUID_PATTERN, uuid());
    }

}
