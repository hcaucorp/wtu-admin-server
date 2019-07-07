package com.jvmp.vouchershop.crypto;

import com.jvmp.vouchershop.wallet.ImportWalletRequest;
import com.jvmp.vouchershop.wallet.Wallet;

/**
 * Responsible for money related actions like sending, getting balances
 */
public interface CurrencyService {

    Wallet importWallet(ImportWalletRequest walletDescription);

    Wallet generateWallet();

    String sendMoney(Wallet from, String toAddress, long amount);

    long getBalance(Wallet wallet);

    boolean acceptsCurrency(String currency);
}
