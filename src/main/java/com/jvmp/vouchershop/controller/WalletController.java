package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.crypto.CurrencyService;
import com.jvmp.vouchershop.exception.CurrencyNotSupported;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.CurrencyServiceSupplier;
import com.jvmp.vouchershop.wallet.ImportWalletRequest;
import com.jvmp.vouchershop.wallet.Wallet;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Wallet> generateWallet(@RequestBody String currency) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(currencyServiceSupplier.findByCurrency(currency)
                        .map(CurrencyService::generateWallet)
                        .orElseThrow(() -> new CurrencyNotSupported(currency)));
    }

    @PutMapping("/wallets")
    public ResponseEntity<Object> importWallet(@RequestBody ImportWalletRequest walletDescription) {
        currencyServiceSupplier.findByCurrency(walletDescription.)

        return walletService.importWallet(walletDescription)
                .map(wallet -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .build())
                .orElse(ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build());
    }
}
