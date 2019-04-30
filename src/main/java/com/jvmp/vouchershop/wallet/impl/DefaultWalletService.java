package com.jvmp.vouchershop.wallet.impl;

import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.CurrencyServiceSupplier;
import com.jvmp.vouchershop.wallet.ImportWalletRequest;
import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultWalletService implements WalletService {

    private final WalletRepository walletRepository;
    private final CurrencyServiceSupplier currencyServiceSupplier;

    @Override
    public Optional<Wallet> importWallet(ImportWalletRequest walletDescription) {
        return Optional.empty();
    }

    @Override
    public Wallet generateWallet(String currency) {
        return null;
    }

    @Override
    public List<Wallet> findAll() {
        return walletRepository.findAll().stream()
                .map(wallet -> currencyServiceSupplier.findByCurrency(wallet.getCurrency())
                        .map(currencyService -> wallet.withBalance(currencyService.getBalance(wallet)))
                        .orElse(wallet))
                .collect(toList());
    }

    @Override
    public Optional<Wallet> findById(Long id) {
        return walletRepository.findById(id);
    }

    @Override
    public Wallet save(Wallet w) {
        return walletRepository.save(w);
    }
}
