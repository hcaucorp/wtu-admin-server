package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.domain.Wallet;
import com.jvmp.vouchershop.exception.*;
import com.jvmp.vouchershop.repository.WalletRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("/wallets")
@AllArgsConstructor
public class WalletController {

    private WalletRepository walletRepository;

    @GetMapping
    public Page<Wallet> getQuestions(Pageable pageable) {
        return walletRepository.findAll(pageable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        return walletRepository.findById(id)
                .map(wallet -> {

                    // prevent deleting wallet with balance and loosing the money on it
                    if (wallet.getBtcWallet().getBalance().isPositive()) {
                        throw new IllegalOperationException("Wallet balance is positive and can't be deleted. Move money to different wallet before deleting this wallet " +
                                "or else ALL the funds will be lost!");
                    }

                    walletRepository.delete(wallet);
                    return ResponseEntity.ok().build();
                })
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id " + id));
    }
}
