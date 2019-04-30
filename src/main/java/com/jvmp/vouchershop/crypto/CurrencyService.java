package com.jvmp.vouchershop.crypto;

import com.jvmp.vouchershop.wallet.ImportWalletRequest;
import com.jvmp.vouchershop.wallet.Wallet;

import java.util.Optional;

public interface CurrencyService {

    Optional<Wallet> importWallet(ImportWalletRequest walletDescription);

    Wallet generateWallet();

    String sendMoney(Wallet from, String toAddress, long amount);

    long getBalance(Wallet wallet);

    boolean worksWith(String currency);
}
