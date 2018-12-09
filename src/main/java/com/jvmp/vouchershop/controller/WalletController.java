package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.domain.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class WalletController {

    private WalletService walletService;

    @GetMapping("/wallets")
    public List<Wallet> getAllWallets() {
        return walletService.findAll();
    }

    @DeleteMapping("/wallets/{id}")
    public ResponseEntity<?> deleteWallet(@PathVariable Long id) {
        walletService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/wallets/new")
    public ResponseEntity<Wallet> generateWallet(@RequestParam("password") String password) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(walletService.save(walletService.generateWallet(password)));
    }
}
