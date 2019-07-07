package com.jvmp.vouchershop.crypto.libra;

import com.jvmp.vouchershop.crypto.CurrencyService;
import com.jvmp.vouchershop.wallet.ImportWalletRequest;
import com.jvmp.vouchershop.wallet.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class LibraService implements CurrencyService {

    private final static String LIBRA = "LIBRA";

    @Override
    public Wallet importWallet(ImportWalletRequest walletDescription) {

        String mnemonic = requireNonNull(walletDescription.mnemonic);
        long createdAt = walletDescription.createdAt;

        return null;
    }

    @Override
    public Wallet generateWallet() {
        return null;
    }

    @Override
    public String sendMoney(Wallet from, String toAddress, long amount) {
        return null;
    }

    @Override
    public long getBalance(Wallet wallet) {
        return 0;
    }

    @Override
    public boolean acceptsCurrency(String currency) {
        return LIBRA.equals(currency);
    }
}
