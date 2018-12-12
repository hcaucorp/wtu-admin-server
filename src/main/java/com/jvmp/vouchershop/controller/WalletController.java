package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.domain.VWallet;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.crypto.btc.WalletService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin
public class WalletController {

    private WalletService walletService;

    @GetMapping("/wallets")
    public List<VWallet> getAllWallets() {
        return walletService.findAll();
    }

    @GetMapping("/wallets/{id}")
    public ResponseEntity<?> getWallet(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VWallet not found")));
    }

    @DeleteMapping("/wallets/{id}")
    public ResponseEntity<?> deleteWallet(@PathVariable Long id) {
        walletService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/wallets/generate")
    public ResponseEntity<VWallet> generateWallet(@RequestBody GenerateWalletPayload payload) {
        VWallet VWallet = walletService.save(walletService.generateWallet(payload.password, payload.description));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(URI.create("/wallets/" + VWallet.getId()))
                .build();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class GenerateWalletPayload {
        public String password;
        public String description;
    }
}
