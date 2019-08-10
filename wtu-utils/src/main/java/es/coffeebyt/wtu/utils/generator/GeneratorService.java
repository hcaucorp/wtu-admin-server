package es.coffeebyt.wtu.utils.generator;

import es.coffeebyt.wtu.voucher.VoucherCodeGenerator;
import es.coffeebyt.wtu.voucher.impl.VoucherGenerationSpec;
import lombok.RequiredArgsConstructor;

import static es.coffeebyt.wtu.voucher.impl.DefaultVoucherCodeGenerator.CURRENCY;
import static es.coffeebyt.wtu.voucher.impl.DefaultVoucherCodeGenerator.PATTERN;
import static es.coffeebyt.wtu.voucher.impl.DefaultVoucherCodeGenerator.UUID;
import static es.coffeebyt.wtu.voucher.impl.DefaultVoucherCodeGenerator.uuid;

@RequiredArgsConstructor
public class GeneratorService implements VoucherCodeGenerator {

    private final String currency;

    @Override public String apply(VoucherGenerationSpec voucherGenerationSpec) {
            return PATTERN
                    .replace(CURRENCY, currency)
                    .replace(UUID, uuid());
    }

}
