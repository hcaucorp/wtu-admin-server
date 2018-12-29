package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin
public class WalletController {

    private WalletService walletService;

    @GetMapping("/wallets")
    public List<Wallet> getAllWallets() {
        return walletService.findAll();
    }

    @GetMapping("/wallets/new")
    public ResponseEntity<Wallet> generateWallet() {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(walletService.generateWallet());
    }
}
