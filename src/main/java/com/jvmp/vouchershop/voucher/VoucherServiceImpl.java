package com.jvmp.vouchershop.voucher;

import com.jvmp.vouchershop.domain.Voucher;
import com.jvmp.vouchershop.domain.Wallet;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final WalletService walletService;
    private final VoucherRepository voucherRepository;

    private final Supplier<String> DEFAULT_ID_GENERATOR = () -> UUID.randomUUID().toString();

    @Override
    public List<Voucher> generateVouchers(VoucherGenerationSpec spec) {
        if (spec.totalAmount % spec.count != 0)
            throw new IllegalOperationException("Total amount must be divisible by count because all vouchers must be identical. Current specification is " +
                    "incorrect: can't split amount of: " + spec.totalAmount + " into " + spec.count + " equal pieces.");

        String currency = walletService.findById(spec.walletId)
                .map(Wallet::getCurrency)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet with id " + spec.walletId + " not found."));

        long amount = spec.totalAmount / spec.count;

        return IntStream.range(0, spec.count)
                .mapToObj(next -> new Voucher(DEFAULT_ID_GENERATOR.get(), amount, currency, spec.walletId))
                .collect(toList());
    }

    @Override
    public List<Voucher> findAll() {
        return voucherRepository.findAll();
    }

    @Override
    public Optional<Voucher> findById(long id) {
        return voucherRepository.findById(id);
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
    public Voucher save(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    @Override
    public void save(List<Voucher> vouchers) {
        voucherRepository.saveAll(vouchers);
    }
}
