package es.coffeebyt.wtu.crypto.btc;

import es.coffeebyt.wtu.crypto.CurrencyService;
import es.coffeebyt.wtu.exception.IllegalOperationException;
import es.coffeebyt.wtu.exception.Thrower;
import es.coffeebyt.wtu.repository.WalletRepository;
import es.coffeebyt.wtu.system.PropertyNames;
import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet.SendResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.Optional;

import static java.lang.String.format;
import static java.time.Instant.ofEpochSecond;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.bitcoinj.script.Script.ScriptType.P2PKH;
import static org.bitcoinj.wallet.Wallet.fromSeed;

@Slf4j
@Component
@RequiredArgsConstructor
public class BitcoinService implements CurrencyService, AutoCloseable {

    public static final String BTC = "BTC";

    @Value(PropertyNames.BITCOINJ_AUTOSTART)
    private boolean autoStart;

    private final WalletRepository walletRepository;
    private final NetworkParameters networkParameters;
    private final BitcoinJFacade bitcoinj;

    public static String walletWords(@Nonnull org.bitcoinj.wallet.Wallet bitcoinjWallet) {
        return String.join(" ", Optional.ofNullable(bitcoinjWallet.getKeyChainSeed().getMnemonicCode())
                .orElse(emptyList()));
    }

    @PostConstruct
    public void start() {
        readWalletFromDB()
                .ifPresent(bitcoinj::restoreWalletFromSeed);

        if (autoStart) {
            bitcoinj.startSilently(); //force service start
        }
    }

    @PreDestroy
    public void close() {
        bitcoinj.close();
    }

    public Wallet importWallet(String mnemonic, long epochSeconds) {
        if (walletRepository.findOneByCurrency(BTC).isPresent())
            Thrower.logAndThrowIllegalOperationException("BTC wallet already exists. Currently we support only single wallet per currency");

        if (epochSeconds > Instant.now().getEpochSecond())
            Thrower.logAndThrowIllegalOperationException("Creation time is set in the future. Are you trying to pass milli seconds?");

        try {
            DeterministicSeed deterministicSeed = new DeterministicSeed(mnemonic, null, "", epochSeconds);
            org.bitcoinj.wallet.Wallet wallet = fromSeed(networkParameters, deterministicSeed, P2PKH);
            return restoreWalletSaveAndStart(wallet, ofEpochSecond(epochSeconds).toEpochMilli());
        } catch (UnreadableWalletException e) {
            String message = format("Can't read wallet (mnemonic: %s, created at: %s) because: %s",
                    mnemonic, ofEpochSecond(epochSeconds).toEpochMilli(), e.getMessage());
            log.error(message);
            throw new BitcoinException(message);
        }
    }

    private Wallet restoreWalletSaveAndStart(org.bitcoinj.wallet.Wallet bitcoinjWallet, long createdAtMillis) {
        bitcoinj.restoreWalletFromSeed(bitcoinjWallet.getKeyChainSeed());

        Wallet wallet = new Wallet()
                .withBalance(bitcoinj.getBalance())
                .withAddress(bitcoinjWallet.currentReceiveAddress().toString())
                .withCreatedAt(createdAtMillis)
                .withCurrency(BTC)
                .withMnemonic(walletWords(bitcoinjWallet));

        return walletRepository.save(wallet);
    }

    @Override
    public Wallet importWallet(ImportWalletRequest walletDescription) {
        String mnemonic = requireNonNull(walletDescription.mnemonic);
        long createdAt = walletDescription.createdAt;
        return importWallet(mnemonic, createdAt);
    }

    private Optional<DeterministicSeed> readWalletFromDB() {
        return walletRepository.findOneByCurrency(BTC)
                .flatMap(wallet -> from(wallet.getMnemonic(), wallet.getCreatedAt()));
    }

    private Optional<DeterministicSeed> from(String mnemonic, long createdAtMillis) {
        try {
            long createdAtSeconds = Instant.ofEpochMilli(createdAtMillis).getEpochSecond();
            return Optional.of(new DeterministicSeed(mnemonic, null, "", createdAtSeconds));
        } catch (UnreadableWalletException e) {
            String message = format("Can't read wallet (mnemonic: %s, created at: %s) because: %s", mnemonic, createdAtMillis, e.getMessage());
            log.error(message);
            throw new BitcoinException(message);
        }
    }

    public Wallet generateWallet() {
        Optional<Wallet> wallet = walletRepository.findOneByCurrency(BTC);
        if (wallet.isPresent())
            Thrower.logAndThrowIllegalOperationException("BTC wallet already exists. Currently we support only single wallet per currency");

        org.bitcoinj.wallet.Wallet bitcoinjWallet = new org.bitcoinj.wallet.Wallet(
                networkParameters,
                KeyChainGroup.builder(networkParameters).fromRandom(Script.ScriptType.P2PKH).build()
        );
        String walletWords = walletWords(bitcoinjWallet);
        long creationTime = bitcoinjWallet.getKeyChainSeed().getCreationTimeSeconds();

        log.info("Seed words are: {}", walletWords);
        log.info("Seed birthday is: {}", creationTime);

        return restoreWalletSaveAndStart(bitcoinjWallet, Instant.ofEpochSecond(creationTime).toEpochMilli());
    }

    @Override
    public String sendMoney(Wallet from, String toAddress, long amount) {
        if (!acceptsCurrency(from.getCurrency()))
            Thrower.logAndThrowIllegalOperationException(format("Wallet's %s currency (%s) doesn't match supported currency: BTC", from.getId(), from.getCurrency()));

        Address targetAddress = Address.fromString(networkParameters, toAddress);
        try {
            //using eco fees
            SendRequest sendRequest = SendRequest.to(targetAddress, Coin.valueOf(amount));
            sendRequest.feePerKb = Transaction.REFERENCE_DEFAULT_MIN_TX_FEE;

            SendResult sendResult = bitcoinj.sendCoins(sendRequest);

            return sendResult.tx.getTxId().toString();
        } catch (InsufficientMoneyException e) {
            String message = format("Not enough funds on wallet #%s. Available %.8f, but requested %.8f. Exception message: %s",
                    from.getId(), (double) bitcoinj.getBalance() / 10_000_000, (double) amount / 10_000_000, e.getMessage());

            throw new IllegalOperationException(message);
        }
    }

    @Override
    public long getBalance(Wallet wallet) {
        return acceptsCurrency(wallet.getCurrency()) ? bitcoinj.getBalance() : 0;
    }

    @Override
    public boolean acceptsCurrency(String currency) {
        return BTC.equals(currency);
    }
}
