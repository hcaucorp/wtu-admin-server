package es.coffeebyt.wtu.controller;

import es.coffeebyt.wtu.crypto.CurrencyNotSupported;
import es.coffeebyt.wtu.crypto.CurrencyServiceSupplier;
import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import es.coffeebyt.wtu.wallet.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import java.util.List;

@RequestMapping("/api/wallets")
@RestController
@AllArgsConstructor
@CrossOrigin
public class WalletController {

    private CurrencyServiceSupplier currencyServiceSupplier;
    private WalletService walletService;

    @GetMapping
    public List<Wallet> getAllWallets() {
        return walletService.findAll();
    }

    @PostMapping
    public ResponseEntity<Wallet> generateWallet(@RequestBody String currency) throws CurrencyNotSupported {

        Wallet wallet = currencyServiceSupplier.findByCurrency(currency).generateWallet();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(wallet);
    }

    @PutMapping
    public ResponseEntity<Object> importWallet(@RequestBody @Valid @Nonnull ImportWalletRequest walletDescription) throws CurrencyNotSupported {
        currencyServiceSupplier.findByCurrency(walletDescription.currency)
                .importWallet(walletDescription);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}