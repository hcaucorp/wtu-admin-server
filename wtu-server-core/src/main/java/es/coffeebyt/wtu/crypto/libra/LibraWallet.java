package es.coffeebyt.wtu.crypto.libra;

import dev.jlibra.mnemonic.*;
import es.coffeebyt.wtu.wallet.Wallet;
import lombok.Value;

import javax.annotation.Nonnull;

@Value
class LibraWallet {

    public final static String SALT = "I love it when you call me señorita\n" +
            "I wish I could pretend I didn't need ya\n" +
            "But every touch is ooh la la la\n" +
            "It's true, la la la\n" +
            "Ooh, I should be running\n" +
            "Ooh, you keep me coming for you";

    public final Mnemonic mnemonic;
    public final Seed seed;
    public final LibraKeyFactory libraKeyFactory;
    public final ExtendedPrivKey account0;

    LibraWallet(Mnemonic mnemonic) {
        this.mnemonic = mnemonic;
        this.seed = new Seed(mnemonic, SALT);
        this.libraKeyFactory = new LibraKeyFactory(seed);
        this.account0 = libraKeyFactory.privateChild(new ChildNumber(0));
    }

    LibraWallet(@Nonnull Wallet wallet) {
        this(Mnemonic.fromString(wallet.getMnemonic()));
    }
}
