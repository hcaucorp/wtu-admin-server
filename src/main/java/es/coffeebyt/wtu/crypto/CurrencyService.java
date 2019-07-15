package es.coffeebyt.wtu.crypto;

import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;

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
