package com.jvmp.vouchershop.wallet;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class WalletServiceResolver {

    private final List<WalletService> registeredServices;

    public Optional<WalletService> findByCurrency(String currency) {
        return registeredServices.stream()
                .filter(walletService -> walletService.canHandle(currency))
                .findFirst();
    }
}
