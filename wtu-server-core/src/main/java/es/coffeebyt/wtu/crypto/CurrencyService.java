package es.coffeebyt.wtu.crypto;

import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import es.coffeebyt.wtu.wallet.WalletStatus;

/**
 * Responsible for money related actions like sending, getting balances
 */
public interface CurrencyService {

    boolean acceptsCurrency(String currency);

    long balanceOf(Wallet wallet);

    /**
     * Create fully functioning Wallet instance
     */
    Wallet importWallet(ImportWalletRequest walletDescription);


    Wallet generateWallet();

    String sendMoney(Wallet from, String toAddress, long amount);

    WalletStatus statusOf(Wallet wallet);

}
