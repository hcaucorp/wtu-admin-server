package com.jvmp.vouchershop.voucher.impl;

import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.voucher.VoucherCodeGenerator;
import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@SuppressWarnings("FieldCanBeLocal")
@Component
@RequiredArgsConstructor
public class PaperVoucherCodeGenerator implements VoucherCodeGenerator {

    private final String PATTERN = "wtu{currency}-{uuid}";
    private final String CURRENCY = "{currency}";
    private final String UUID = "{uuid}";

    private final WalletService walletService;

    private String uuid() {
        return java.util.UUID.randomUUID().toString();
    }

    @Override
    public String apply(VoucherGenerationDetails generationDetails) {
        Wallet wallet = walletService.findById(generationDetails.walletId)
                .orElseThrow(() -> new ResourceNotFoundException(format("Wallet with id %s not found", generationDetails.walletId)));

        return PATTERN
                .replace(CURRENCY, wallet.getCurrency().toLowerCase())
                .replace(UUID, uuid());
    }
}
