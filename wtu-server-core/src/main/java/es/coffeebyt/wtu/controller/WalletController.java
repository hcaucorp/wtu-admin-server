package es.coffeebyt.wtu.controller;

import javax.annotation.Nonnull;
import javax.validation.Valid;

import java.util.List;

import es.coffeebyt.wtu.crypto.CurrencyServiceSupplier;
import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import es.coffeebyt.wtu.wallet.WalletReport;
import es.coffeebyt.wtu.wallet.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/wallets")
@RestController
@AllArgsConstructor
@CrossOrigin
public class WalletController {

    private CurrencyServiceSupplier currencyServiceSupplier;
    private WalletService walletService;

    @GetMapping
    public List<WalletReport> walletsReport() {
        return walletService.walletStats();
    }

    @PostMapping
    public ResponseEntity<Wallet> generateWallet(@RequestBody String currency) {

        Wallet wallet = currencyServiceSupplier.findByCurrency(currency).generateWallet();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(wallet);
    }

    @PutMapping
    public ResponseEntity<Object> importWallet(@RequestBody @Valid @Nonnull ImportWalletRequest walletDescription) {
        currencyServiceSupplier.findByCurrency(walletDescription.currency)
                .importWallet(walletDescription);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}
