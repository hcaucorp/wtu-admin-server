package es.coffeebyt.wtu.crypto.bch;

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
import es.coffeebyt.wtu.exception.Thrower;
import es.coffeebyt.wtu.repository.WalletRepository;
import es.coffeebyt.wtu.system.PropertyNames;
import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import es.coffeebyt.wtu.wallet.WalletStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static java.time.Instant.ofEpochSecond;
import static java.util.Collections.emptyList;

@Slf4j
@Component
@RequiredArgsConstructor
public class BitcoinCashService implements CurrencyService, AutoCloseable {

    public static final String BCH = "BCH";

    @Value(PropertyNames.BITCOINJ_AUTOSTART)
    private boolean autoStart;

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
            bitcoinj.startAsync(); //force service start
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
            throw new BitcoinCashException(e.getMessage());
        }
    }

    private Wallet restoreWalletSaveAndStart(cash.bitcoinj.wallet.Wallet bitcoinjWallet, long createdAtMillis) {
        bitcoinj.restoreWalletFromSeed(bitcoinjWallet.getKeyChainSeed());

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
        long creationTime = cashjWallet.getKeyChainSeed().getCreationTimeSeconds();

        return restoreWalletSaveAndStart(cashjWallet, Instant.ofEpochSecond(creationTime).toEpochMilli());
    }

    CashAddress readAddress(NetworkParameters params, String input) {
        try {
            return addressFactory.getFromFormattedAddress(params, input);
        } catch (AddressFormatException e) {
            log.error(String.format("Input: %s could not be decoded from cash address format. Exception: %s, message %s",
                    input, e.getClass().getSimpleName(), e.getMessage()));

            throw new BitcoinCashAddressFormatException();
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
            throw new BitcoinCashException(message);
        }
    }

    @Override public WalletStatus statusOf(Wallet wallet) {
        return bitcoinj.getBalance() == -1 ? WalletStatus.STARTING : WalletStatus.RUNNING;
    }

    @Override
    public long balanceOf(Wallet wallet) {
        return acceptsCurrency(wallet.getCurrency()) ? bitcoinj.getBalance() : 0;
    }

    @Override
    public boolean acceptsCurrency(String currency) {
        return BCH.equals(currency);
    }
}
