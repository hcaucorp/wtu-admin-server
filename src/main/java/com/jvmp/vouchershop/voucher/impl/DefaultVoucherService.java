package com.jvmp.vouchershop.voucher.impl;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.VoucherService;
import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class DefaultVoucherService implements VoucherService {

    private final WalletService walletService;
    private final VoucherRepository voucherRepository;

    public static final Supplier<String> DEFAULT_VOUCHER_CODE_GENERATOR = () -> UUID.randomUUID().toString();

    @Override
    public List<Voucher> generateVouchers(VoucherGenerationDetails spec) {
        if (spec.totalAmount % spec.count != 0)
            throw new IllegalOperationException("Total amount must be divisible by count because all vouchers must be identical. Current specification is " +
                    "incorrect: can't split amount of: " + spec.totalAmount + " into " + spec.count + " equal pieces.");

        String currency = walletService.findById(spec.walletId)
                .map(Wallet::getCurrency)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet with id " + spec.walletId + " not found."));

        long amount = spec.totalAmount / spec.count;

        return IntStream.range(0, spec.count)
                .mapToObj(next -> new Voucher()
                        .withAmount(amount)
                        .withCode(DEFAULT_VOUCHER_CODE_GENERATOR.get())
                        .withCurrency(currency)
                        .withWalletId(spec.walletId)
                        .withSku(spec.getSku()))
                // TODO add more info?
                .collect(toList());
    }

    @Override
    public List<Voucher> findAll() {
        return voucherRepository.findAll();
    }

    @Override
    public void delete(long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher with id " + id + "not found."));

        if (voucher.isPublished())
            throw new IllegalOperationException("Voucher has been published for sale and cannot be deleted");

        voucherRepository.delete(voucher);
    }

    @Override
    public void save(List<Voucher> vouchers) {
        voucherRepository.saveAll(vouchers);
    }
}
