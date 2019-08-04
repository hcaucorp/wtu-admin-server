package es.coffeebyt.wtu.crypto.libra;

import static dev.jlibra.mnemonic.Mnemonic.WORDS;

import dev.jlibra.AccountState;
import dev.jlibra.admissioncontrol.AdmissionControl;
import dev.jlibra.admissioncontrol.query.ImmutableGetAccountState;
import dev.jlibra.admissioncontrol.query.ImmutableQuery;
import dev.jlibra.admissioncontrol.query.UpdateToLatestLedgerResult;
import dev.jlibra.mnemonic.Mnemonic;
import es.coffeebyt.wtu.crypto.CurrencyService;
import es.coffeebyt.wtu.exception.Thrower;
import es.coffeebyt.wtu.repository.WalletRepository;
import es.coffeebyt.wtu.system.PropertyNames;
import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.RandomUtils;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

import java.security.Security;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
public class LibraService implements CurrencyService {

    public final static String LIBRA = "LIBRA";

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private final ManagedChannel channel;
    private final AdmissionControl admissionControl;
    private final WalletRepository walletRepository;

    public LibraService(@Value(PropertyNames.LIBRA_NETWORK_ADDRESS) String networkAddress,
                        @Value(PropertyNames.LIBRA_NETWORK_PORT) int networkPort,
                        WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
        channel = ManagedChannelBuilder.forAddress(networkAddress, networkPort)
                .usePlaintext()
                .build();

        admissionControl = new AdmissionControl(channel);
    }

    @PreDestroy
    public void beforeDestroy() {
        channel.shutdown();
    }

    @Override
    public Wallet importWallet(ImportWalletRequest walletDescription) {
        Wallet wallet = new Wallet()
                .withMnemonic(walletDescription.mnemonic)
                .withCurrency(LIBRA);

        return wallet
                .withBalance(getBalance(wallet));
    }

    @Override
    public Wallet generateWallet() {
        Optional<Wallet> wallet = walletRepository.findOneByCurrency(LIBRA);
        if (wallet.isPresent())
            Thrower.logAndThrowIllegalOperationException("LIBRA wallet already exists. Currently we support only single wallet per currency");

        Mnemonic mnemonic = generateMnemonic();
        LibraWallet libraWallet = new LibraWallet(mnemonic);

        log.info("Seed words are: {}", mnemonic.toString());

        return restoreWalletSaveAndStart(libraWallet);
    }


    private Wallet restoreWalletSaveAndStart(LibraWallet libraWallet) {
        Wallet wallet = new Wallet()
                .withMnemonic(libraWallet.mnemonic.toString())
                .withCurrency(LIBRA);

        return walletRepository.save(wallet.withBalance(getBalance(wallet)));
    }

    @Override
    public String sendMoney(Wallet from, String toAddress, long amount) {
        return null;
    }

    @Override
    public long getBalance(Wallet wallet) {

        LibraWallet libraWallet = new LibraWallet(Mnemonic.fromString(wallet.getMnemonic()));
        String forAddress = libraWallet.account0.getAddress();

        UpdateToLatestLedgerResult result = admissionControl.updateToLatestLedger(
                ImmutableQuery.builder().addAccountStateQueries(
                        ImmutableGetAccountState.builder().address(Hex.decode(forAddress)).build()
                ).build());

        return result.getAccountStates()
                .stream()
                .filter(accountState -> Arrays.equals(
                        accountState.getAddress(),
                        Hex.decode(forAddress)
                ))
                .map(AccountState::getBalanceInMicroLibras)
                .findFirst()
                .orElse(0L);
    }

    @Override
    public boolean acceptsCurrency(String currency) {
        return LIBRA.equalsIgnoreCase(currency);
    }

    private Mnemonic generateMnemonic() {
        String words = IntStream.range(0, 18)
                .map(ignored -> RandomUtils.nextInt(0, WORDS.size()))
                .mapToObj(WORDS::get)
                .collect(Collectors.joining(" "));

        log.info("Generated seed: {}", words);

        return Mnemonic.fromString(words);
    }
}
