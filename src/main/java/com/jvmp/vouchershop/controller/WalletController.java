package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
