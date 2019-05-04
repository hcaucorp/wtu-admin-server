package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.crypto.CurrencyNotSupported;
import com.jvmp.vouchershop.crypto.CurrencyService;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.CurrencyServiceSupplier;
import com.jvmp.vouchershop.wallet.ImportWalletRequest;
import com.jvmp.vouchershop.wallet.Wallet;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import java.util.List;

@RequestMapping("/api")
@RestController
@AllArgsConstructor
@CrossOrigin
public class WalletController {

    private CurrencyServiceSupplier currencyServiceSupplier;
    private WalletRepository walletRepository;

    @GetMapping("/wallets")
    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    @PostMapping("/wallets")
    public ResponseEntity<Wallet> generateWallet(@RequestBody String currency) throws CurrencyNotSupported {

        Wallet wallet = currencyServiceSupplier.findByCurrency(currency).generateWallet();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(wallet);
    }

    @PutMapping("/wallets")
    public ResponseEntity<Object> importWallet(@RequestBody @Valid @Nonnull ImportWalletRequest walletDescription) throws CurrencyNotSupported {

        CurrencyService currencyService = currencyServiceSupplier.findByCurrency(walletDescription.currency);

        return currencyService.importWallet(walletDescription)
                .map(wallet -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .build())
                .orElse(ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build());
    }
}
