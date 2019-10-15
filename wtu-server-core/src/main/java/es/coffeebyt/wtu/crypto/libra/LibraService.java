package es.coffeebyt.wtu.crypto.libra;

import javax.annotation.PreDestroy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Security;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.protobuf.ByteString;

import dev.jlibra.LibraHelper;
import dev.jlibra.admissioncontrol.AdmissionControl;
import dev.jlibra.admissioncontrol.query.AccountResource;
import dev.jlibra.admissioncontrol.query.ImmutableGetAccountState;
import dev.jlibra.admissioncontrol.query.ImmutableQuery;
import dev.jlibra.admissioncontrol.query.UpdateToLatestLedgerResult;
import dev.jlibra.admissioncontrol.transaction.AccountAddressArgument;
import dev.jlibra.admissioncontrol.transaction.ImmutableProgram;
import dev.jlibra.admissioncontrol.transaction.ImmutableSignedTransaction;
import dev.jlibra.admissioncontrol.transaction.ImmutableTransaction;
import dev.jlibra.admissioncontrol.transaction.SignedTransaction;
import dev.jlibra.admissioncontrol.transaction.SubmitTransactionResult;
import dev.jlibra.admissioncontrol.transaction.Transaction;
import dev.jlibra.admissioncontrol.transaction.U64Argument;
import dev.jlibra.mnemonic.ExtendedPrivKey;
import dev.jlibra.mnemonic.Mnemonic;
import dev.jlibra.move.Move;
import es.coffeebyt.wtu.crypto.CurrencyService;
import es.coffeebyt.wtu.exception.Thrower;
import es.coffeebyt.wtu.repository.WalletRepository;
import es.coffeebyt.wtu.system.PropertyNames;
import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import es.coffeebyt.wtu.wallet.WalletStatus;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static dev.jlibra.mnemonic.Mnemonic.WORDS;
import static java.lang.String.format;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Component
public class LibraService implements CurrencyService {

    public static final String LIBRA = "LIBRA";

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
                .withBalance(balanceOf(wallet));
    }

    @Override
    public Wallet generateWallet() {
        Optional<Wallet> wallet = walletRepository.findOneByCurrency(LIBRA);
        if (wallet.isPresent())
            Thrower.logAndThrowIllegalOperationException(
                    "LIBRA wallet already exists. Currently we support only single wallet per currency");

        Mnemonic mnemonic = generateMnemonic();
        LibraWallet libraWallet = new LibraWallet(mnemonic);

        return restoreWalletSaveAndStart(libraWallet);
    }

    private Wallet restoreWalletSaveAndStart(LibraWallet libraWallet) {
        Wallet wallet = new Wallet()
                .withMnemonic(libraWallet.mnemonic.toString())
                .withCurrency(LIBRA);

        return walletRepository.save(wallet.withBalance(balanceOf(wallet)));
    }

    @Override
    public String sendMoney(Wallet from, String toAddress, long amount) {

        if (!acceptsCurrency(from.getCurrency()))
            Thrower.logAndThrowIllegalOperationException(
                    format("Wallet's %s currency (%s) doesn't match supported currency: LIBRA", from.getId(),
                            from.getCurrency()));

        LibraWallet sourceWallet = new LibraWallet(from);
        AccountResource accountState = getAccountState(sourceWallet.account0);

        long sequenceNumber = accountState != null ? accountState.getSequenceNumber() : 0;

        // Arguments for the peer to peer transaction
        U64Argument amountArgument = new U64Argument(amount * 1000000);
        AccountAddressArgument addressArgument = new AccountAddressArgument(Hex.decode(toAddress));

        Transaction transaction = ImmutableTransaction.builder()
                .sequenceNumber(sequenceNumber)
                .maxGasAmount(240000)
                .gasUnitPrice(1)
                .expirationTime(Instant.now().getEpochSecond() + 60)
                .program(
                        ImmutableProgram.builder()
                                .code(ByteString.copyFrom(Move.peerToPeerTransferAsBytes()))
                                .addArguments(addressArgument, amountArgument)
                                .build())
                .build();

        SignedTransaction signedTransaction = ImmutableSignedTransaction.builder()
                .publicKey(sourceWallet.account0.publicKey.getEncoded())
                .transaction(transaction)
                .signature(LibraHelper.signTransaction(transaction, sourceWallet.account0.privateKey))
                .build();

        SubmitTransactionResult result = admissionControl.submitTransaction(signedTransaction);

        log.info("Status type: {}", result.getStatusCase());
        log.info("Admission control status: {}", result.getAdmissionControlStatus());
        log.info("Mempool status: {}", result.getMempoolStatus());
        log.info("VM status: {}", result.getVmStatus());

        return sourceWallet.account0.getAddress() + ":" + sequenceNumber;
    }

    private AccountResource getAccountState(ExtendedPrivKey account0) {
        UpdateToLatestLedgerResult result = admissionControl
                .updateToLatestLedger(ImmutableQuery.builder()
                        .addAccountStateQueries(ImmutableGetAccountState.builder()
                                .address(Hex.decode(account0.getAddress()))
                                .build())
                        .build());

        if (isEmpty(result.getAccountStates())) {
            return null;
        }

        if (result.getAccountStates().size() > 1) {
            log.error("Found {} states for the account {}. Aborting...", result.getAccountStates().size(), account0);
            result.getAccountStates().forEach(accountState -> {
                log.error("Account State: ");
                log.error("Authentication Key: {}", Hex.toHexString(accountState.getAuthenticationKey()));
                log.error("Address: {}", Hex.toHexString(accountState.getAuthenticationKey()));
                log.error("Received events: {}", accountState.getReceivedEvents());
                log.error("Sent events: {}", accountState.getSentEvents());
                log.error("Balance (microLibras): {}", accountState.getBalanceInMicroLibras());
                log.error("Balance (Libras): {}",
                        new BigDecimal(accountState.getBalanceInMicroLibras())
                                .divide(BigDecimal.valueOf(1000000), RoundingMode.DOWN));
                log.error("Sequence number: {}", accountState.getSequenceNumber());
                log.error("Delegated withdrawal capability: {}", accountState.getDelegatedWithdrawalCapability());
            });
            throw new IllegalStateException("Multiple account states found for provided wallet.");
        }

        return result.getAccountStates().get(0);
    }

    @Override
    public long balanceOf(Wallet wallet) {

        LibraWallet libraWallet = new LibraWallet(Mnemonic.fromString(wallet.getMnemonic()));
        String forAddress = libraWallet.account0.getAddress();

        UpdateToLatestLedgerResult result = admissionControl.updateToLatestLedger(
                ImmutableQuery.builder().addAccountStateQueries(
                        ImmutableGetAccountState.builder().address(Hex.decode(forAddress)).build()
                ).build());

        return result.getAccountStates()
                .stream()
                .filter(accountState -> Arrays.equals(
                        accountState.getAuthenticationKey(),
                        Hex.decode(forAddress)
                ))
                .map(AccountResource::getBalanceInMicroLibras)
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

    @Override public WalletStatus statusOf(Wallet wallet) {
        return WalletStatus.NEW;
    }
}
