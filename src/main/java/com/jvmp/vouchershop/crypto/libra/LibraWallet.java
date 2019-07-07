package com.jvmp.vouchershop.crypto.libra;

import com.jvmp.vouchershop.wallet.Wallet;

public class LibraWallet {

    private LibraWallet() {}

    public static LibraWallet forWallet(Wallet wallet) {
        return new LibraWallet();
    }
}
