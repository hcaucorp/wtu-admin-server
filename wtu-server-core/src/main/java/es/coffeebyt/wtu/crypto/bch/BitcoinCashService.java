package es.coffeebyt.wtu.crypto.bch;

import static java.lang.String.format;
import static java.time.Instant.ofEpochSecond;
import static java.util.Collections.emptyList;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.time.Instant;
import java.util.Optional;

import cash.bitcoinj.core.Address;
import cash.bitcoinj.core.AddressFormatException;
import cash.bitcoinj.core.CashAddress;
import cash.bitcoinj.core.CashAddressFactory;
import cash.bitcoinj.core.Coin;
import cash.bitcoinj.core.InsufficientMoneyException;
import cash.bitcoinj.core.NetworkParameters;
import cash.bitcoinj.core.Transaction;
import cash.bitcoinj.wallet.DeterministicSeed;
import cash.bitcoinj.wallet.SendRequest;
import cash.bitcoinj.wallet.UnreadableWalletException;
import es.coffeebyt.wtu.crypto.CurrencyService;
import es.coffeebyt.wtu.exception.IllegalOperationException;
import es.coffeebyt.wtu.exception.Thrower;
import es.coffeebyt.wtu.repository.WalletRepository;
import es.coffeebyt.wtu.system.PropertyNames;
import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BitcoinCashService implements CurrencyService, AutoCloseable {

    @Value(PropertyNames.BITCOINJ_AUTOSTART)
    private boolean autoStart;

    public final static String BCH = "BCH";

    private final WalletRepository walletRepository;

    @Qualifier("BitcoinCashNetworkProperties")
    private final NetworkParameters networkParameters;

    private final BitcoinCashJFacade bitcoinj;

    private final CashAddressFactory addressFactory = new CashAddressFactory();

    public static String walletWords(@Nonnull cash.bitcoinj.wallet.Wallet bitcoinjWallet) {
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

    public Wallet importWallet(String mnemonic, long creationTime) {
        if (walletRepository.findOneByCurrency(BCH).isPresent())
            Thrower.logAndThrowIllegalOperationException("BCH wallet already exists. Currently we support only single wallet per currency");

        if (creationTime > Instant.now().getEpochSecond())
            Thrower.logAndThrowIllegalOperationException("Creation time is set in the future. Are you trying to pass milli seconds?");

        try {
            DeterministicSeed deterministicSeed = new DeterministicSeed(mnemonic, null, "", creationTime);
            cash.bitcoinj.wallet.Wallet wallet = cash.bitcoinj.wallet.Wallet.fromSeed(networkParameters, deterministicSeed);
            return restoreWalletSaveAndStart(wallet, ofEpochSecond(creationTime).toEpochMilli());
        } catch (UnreadableWalletException e) {
            log.error(e.getMessage());
            throw new IllegalOperationException(e.getMessage());
        }
    }

    private Wallet restoreWalletSaveAndStart(cash.bitcoinj.wallet.Wallet bitcoinjWallet, long createdAtMillis) {
        // TODO if file storage not found then restore wallet

        // code from bitcoinj:
//        log.info("Starting up with directory = {}", directory);
//        try {
//            File chainFile = new File(directory, filePrefix + ".spvchain");
//            boolean chainFileExists = chainFile.exists();
//            vWalletFile = new File(directory, filePrefix + ".wallet");

//        bitcoinj.restoreWalletFromSeed(bitcoinjWallet.getKeyChainSeed());

        CashAddress receivingAddress = addressFactory.getFromBase58(networkParameters, bitcoinjWallet.currentReceiveAddress().toString());

        Wallet wallet = new Wallet()
                .withBalance(bitcoinj.getBalance())
                .withAddress(receivingAddress.toString())
                .withCreatedAt(createdAtMillis)
                .withCurrency(BCH)
                .withMnemonic(walletWords(bitcoinjWallet));

        return walletRepository.save(wallet);
    }

    @Override
    public Wallet importWallet(ImportWalletRequest walletDescription) {
        String mnemonic = walletDescription.mnemonic;
        long createdAt = walletDescription.createdAt;

        return importWallet(mnemonic, createdAt);
    }

    private Optional<DeterministicSeed> readWalletFromDB() {
        return walletRepository.findOneByCurrency(BCH)
                .flatMap(wallet -> from(wallet.getMnemonic(), wallet.getCreatedAt()));
    }

    private Optional<DeterministicSeed> from(String mnemonic, long createdAtMillis) {
        try {
            long createdAtSeconds = Instant.ofEpochMilli(createdAtMillis).getEpochSecond();
            return Optional.of(new DeterministicSeed(mnemonic, null, "", createdAtSeconds));
        } catch (UnreadableWalletException e) {
            return Optional.empty();
        }
    }

    public Wallet generateWallet() {
        if (walletRepository.findOneByCurrency(BCH).isPresent())
            Thrower.logAndThrowIllegalOperationException("BCH wallet already exists. Currently we support only single wallet per currency");

        cash.bitcoinj.wallet.Wallet cashjWallet = new cash.bitcoinj.wallet.Wallet(networkParameters);
        String walletWords = walletWords(cashjWallet);
        long creationTime = cashjWallet.getKeyChainSeed().getCreationTimeSeconds();

        log.info("Seed words are: {}", walletWords);
        log.info("Seed birthday is: {}", creationTime);

        return restoreWalletSaveAndStart(cashjWallet, Instant.ofEpochSecond(creationTime).toEpochMilli());
    }

    CashAddress readAddress(NetworkParameters params, String input) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            return addressFactory.getFromFormattedAddress(params, input);
        } catch (AddressFormatException e) {
            stringBuilder
                    .append("Input: ")
                    .append(input)
                    .append("could not be decoded from cash address format. Exception: ")
                    .append(e.getClass().getSimpleName())
                    .append(e.getMessage());
        }

        try {
            return addressFactory.getFromBase58(params, input);
        } catch (AddressFormatException e) {
            stringBuilder
                    .append("Input: ")
                    .append(input)
                    .append("could not be decoded from Base58 address format. Exception: ")
                    .append(e.getClass().getSimpleName())
                    .append(e.getMessage());
            log.error(stringBuilder.toString());

            throw e;
        }
    }

    @Override
    public String sendMoney(Wallet from, String toAddress, long amount) {
        if (!acceptsCurrency(from.getCurrency()))
            Thrower.logAndThrowIllegalOperationException(format("Wallet %s doesn't work with %s", from.getId(), from.getCurrency()));

        Address targetAddress = readAddress(networkParameters, toAddress);
        try {
            //using eco fees
            SendRequest sendRequest = SendRequest.to(targetAddress, Coin.valueOf(amount));
            sendRequest.feePerKb = Transaction.REFERENCE_DEFAULT_MIN_TX_FEE;
            sendRequest.setUseForkId(true);

            cash.bitcoinj.wallet.Wallet.SendResult sendResult = bitcoinj.sendCoins(sendRequest);

            return sendResult.tx.getHashAsString();
        } catch (InsufficientMoneyException e) {
            String message = format("Not enough funds on wallet #%s. Available %.8f, but requested %.8f. Exception message: %s",
                    from.getId(), (double) bitcoinj.getBalance() / 10_000_000, (double) amount / 10_000_000, e.getMessage());

            if (log.isInfoEnabled()) {
                // re-convert to cash address because it may still be in Base58 in DB
                CashAddress receivingAddress = readAddress(networkParameters, from.getAddress());
                log.info("Current receiving address: {}", receivingAddress);
            }
            throw new IllegalOperationException(message);
        }
    }

    @Override
    public long getBalance(Wallet wallet) {
        return acceptsCurrency(wallet.getCurrency()) ? bitcoinj.getBalance() : 0;
    }

    @Override
    public boolean acceptsCurrency(String currency) {
        return BCH.equals(currency);
    }
}
