package es.coffeebyt.wtu.crypto.libra;

import es.coffeebyt.wtu.crypto.CurrencyService;
import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.springframework.stereotype.Component;

import java.security.Security;
import java.time.Instant;

@Slf4j
@Component
public class LibraService implements CurrencyService {

    private final static String LIBRA = "libra";
    private final static SHA3.DigestSHA3 SHA3 = new SHA3.Digest256();

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    @Override
    public Wallet importWallet(ImportWalletRequest walletDescription) {

        return new Wallet()
//                .withBalance(bitcoinj.getBalance())
//                .withAddress(addressString)
                .withCreatedAt(Instant.now().toEpochMilli())
                .withCurrency(LIBRA)
                .withMnemonic(walletDescription.mnemonic);
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
        return LIBRA.equalsIgnoreCase(currency);
    }
}
