package es.coffeebyt.wtu.wallet;

import es.coffeebyt.wtu.crypto.CurrencyService;
import org.bitcoinj.wallet.UnreadableWalletException;

import java.util.List;
import java.util.Optional;

/**
 * Entity responsible for dealing with {@link Wallet} objects. Nothing to do with money
 * nor currencies.
 * <p>
 * Not to be confused with {@link CurrencyService} responsible for money related actions.
 */
public interface WalletService {

    Wallet importWallet(ImportWalletRequest walletDescription) throws UnreadableWalletException;

    Wallet generateWallet(String currency);

    List<Wallet> findAll();

    Optional<Wallet> findById(Long id);

    Wallet save(Wallet wallet);
}
