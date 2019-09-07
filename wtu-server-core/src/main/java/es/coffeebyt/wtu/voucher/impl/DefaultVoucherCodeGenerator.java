package es.coffeebyt.wtu.voucher.impl;

import es.coffeebyt.wtu.exception.ResourceNotFoundException;
import es.coffeebyt.wtu.voucher.VoucherCodeGenerator;
import es.coffeebyt.wtu.wallet.Wallet;
import es.coffeebyt.wtu.wallet.WalletService;
import lombok.RequiredArgsConstructor;

import static java.lang.String.format;

@SuppressWarnings("FieldCanBeLocal")
@RequiredArgsConstructor
public class DefaultVoucherCodeGenerator implements VoucherCodeGenerator {

    public static final String VOUCHER_CODE_PATTERN = "wtu{currency}-{uuid}";
    public static final String CURRENCY_PATTERN = "{currency}";
    public static final String UUID_PATTERN = "{uuid}";

    private final WalletService walletService;

    public static String uuid() {
        return java.util.UUID.randomUUID().toString();
    }

    @Override
    public String apply(VoucherGenerationSpec generationDetails) {
        Wallet wallet = walletService.findById(generationDetails.walletId)
                .orElseThrow(() -> new ResourceNotFoundException(format("Wallet with id %s not found", generationDetails.walletId)));

        return VOUCHER_CODE_PATTERN
                .replace(CURRENCY_PATTERN, wallet.getCurrency().toLowerCase())
                .replace(UUID_PATTERN, uuid());
    }
}
