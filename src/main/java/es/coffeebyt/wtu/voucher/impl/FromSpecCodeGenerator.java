package es.coffeebyt.wtu.voucher.impl;

import es.coffeebyt.wtu.voucher.VoucherCodeGenerator;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

public class FromSpecCodeGenerator implements VoucherCodeGenerator {

    static String splitter = "[\\s,]+";
    private final Deque<String> voucherCodes;

    public FromSpecCodeGenerator(@Nonnull VoucherGenerationSpec spec) {
        String[] codes = spec.voucherCodes.split(splitter);

        voucherCodes = new LinkedList<>(Arrays.asList(codes));
    }

    @Override
    public String apply(VoucherGenerationSpec ignored) {
        return voucherCodes.poll();
    }
}
