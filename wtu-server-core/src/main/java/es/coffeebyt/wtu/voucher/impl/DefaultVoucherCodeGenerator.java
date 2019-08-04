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

    private final String PATTERN = "wtu{currency}-{uuid}";
    private final String CURRENCY = "{currency}";
    private final String UUID = "{uuid}";

    private final WalletService walletService;

    private String uuid() {
        return java.util.UUID.randomUUID().toString();
    }

    @Override
    public String apply(VoucherGenerationSpec generationDetails) {
        Wallet wallet = walletService.findById(generationDetails.walletId)
                .orElseThrow(() -> new ResourceNotFoundException(format("Wallet with id %s not found", generationDetails.walletId)));

        return PATTERN
                .replace(CURRENCY, wallet.getCurrency().toLowerCase())
                .replace(UUID, uuid());
    }
}
