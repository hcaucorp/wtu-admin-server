package com.jvmp.vouchershop.wallet;

import com.jvmp.vouchershop.domain.Wallet;

public interface WalletService {

    Wallet generateWallet(String password);
}
