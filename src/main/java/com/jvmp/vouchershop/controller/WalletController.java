package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/api")
@RestController
@AllArgsConstructor
@CrossOrigin
public class WalletController {

    private WalletService walletService;

    @GetMapping("/wallets")
    public List<Wallet> getAllWallets() {
        return walletService.findAll();
    }

    @PostMapping("/wallets")
    public ResponseEntity<Wallet> generateWallet(@RequestBody String currency) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(walletService.generateWallet(currency));
    }

    @PutMapping("/wallets")
    public ResponseEntity<Object> importWallet(@RequestBody Map<String, String> walletDescription) {
        return walletService.importWallet(walletDescription)
                .map(wallet -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .build())
                .orElse(ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build());
    }
}
