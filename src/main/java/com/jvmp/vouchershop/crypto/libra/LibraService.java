package com.jvmp.vouchershop.crypto.libra;

import admission_control.AdmissionControlGrpc;
import admission_control.AdmissionControlGrpc.AdmissionControlBlockingStub;
import admission_control.AdmissionControlOuterClass.SubmitTransactionRequest;
import admission_control.AdmissionControlOuterClass.SubmitTransactionResponse;
import com.google.protobuf.ByteString;
import com.jvmp.vouchershop.crypto.CurrencyService;
import com.jvmp.vouchershop.exception.InvalidConfigurationException;
import com.jvmp.vouchershop.wallet.ImportWalletRequest;
import com.jvmp.vouchershop.wallet.Wallet;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.asymmetric.edec.BCEdDSAPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.edec.BCEdDSAPublicKey;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;
import types.Transaction.Program;
import types.Transaction.RawTransaction;
import types.Transaction.SignedTransaction;
import types.Transaction.TransactionArgument;

import java.nio.ByteBuffer;
import java.security.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static types.Transaction.TransactionArgument.ArgType.ADDRESS;
import static types.Transaction.TransactionArgument.ArgType.U64;

@Slf4j
@Component
public class LibraService implements CurrencyService {

    private final static String LIBRA = "LIBRA";
    private final static SHA3.DigestSHA3 SHA3 = new SHA3.Digest256();

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    @Override
    public Wallet importWallet(ImportWalletRequest walletDescription) {

//        String privateKeyString = requireNonNull(walletDescription.mnemonic).split(" ")[0];
        String publicKeyString = requireNonNull(walletDescription.mnemonic).split(" ")[1];
//        PrivateKey privateKey = LibraHelper.privateKeyFromHexString(privateKeyString);
        PublicKey publicKey = LibraHelper.publicKeyFromHexString(publicKeyString);
        String addressString = new String(Hex.encode(SHA3.digest(LibraHelper.stripPrefix(publicKey))));

        return new Wallet()
//                .withBalance(bitcoinj.getBalance())
                .withAddress(addressString)
                .withCreatedAt(Instant.now().toEpochMilli())
                .withCurrency(LIBRA)
                .withMnemonic(walletDescription.mnemonic);
    }

    @Override
    public Wallet generateWallet() {
        KeyPairGenerator kpGen;
        try {
            kpGen = KeyPairGenerator.getInstance("Ed25519", "BC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new InvalidConfigurationException(e.getMessage());
        }

        KeyPair keyPair = kpGen.generateKeyPair();
        BCEdDSAPrivateKey privateKey = (BCEdDSAPrivateKey) keyPair.getPrivate();
        BCEdDSAPublicKey publicKey = (BCEdDSAPublicKey) keyPair.getPublic();

        String addressString = new String(Hex.encode(SHA3.digest(LibraHelper.stripPrefix(publicKey))));
        String publicKeyString = new String(Hex.encode(publicKey.getEncoded()));
        String privateKeyString = new String(Hex.encode(privateKey.getEncoded()));


        log.info("Libra address: {}", addressString);
        log.info("Public key: {}", publicKeyString);
        log.info("Private key: {}", privateKeyString);

        Wallet wallet = new Wallet()
//                .withBalance(bitcoinj.getBalance())
                .withAddress(addressString)
                .withCreatedAt(Instant.now().toEpochMilli())
                .withCurrency(LIBRA)
                .withMnemonic(privateKeyString + " " + publicKeyString);

        return wallet;
    }

    @Override
    public String sendMoney(Wallet from, String toAddress, long amount) {
        PrivateKey privateKey = LibraHelper.privateKeyFromHexString(from.getMnemonic().split(" ")[0]);
        PublicKey publicKey = LibraHelper.publicKeyFromHexString(from.getMnemonic().split(" ")[1]);
        String fromAddress = from.getAddress();

        ManagedChannel channel = ManagedChannelBuilder.forAddress("ac.testnet.libra.org", 8000)
                .usePlaintext()
                .build();

        AdmissionControlBlockingStub stub = AdmissionControlGrpc.newBlockingStub(channel);

        TransactionArgument arg = TransactionArgument.newBuilder()
                .setType(ADDRESS)
                .setData(ByteString.copyFrom(Hex.decode(toAddress)))
                .build();

        TransactionArgument arg2 = TransactionArgument.newBuilder()
                .setType(U64)
                .setData(ByteString
                        .copyFrom(ByteBuffer.allocate(Long.BYTES).putLong(amount).array()))
                .build();

        Program program = Program.newBuilder()
                .addAllArguments(Arrays.asList(arg, arg2))
                .setCode(ByteString.copyFrom(LibraHelper.transferMoveScript()))
                .addAllModules(new ArrayList<>())
                .build();

        RawTransaction rawTransaction = RawTransaction.newBuilder()
                .setProgram(program)
                .setExpirationTime(600)
                .setGasUnitPrice(1)
                .setMaxGasAmount(6000)
                .setSenderAccount(ByteString.copyFrom(Hex.decode(fromAddress)))
                .setSequenceNumber(2)
                .build();

        SignedTransaction signedTransaction = SignedTransaction.newBuilder()
                .setRawTxnBytes(rawTransaction.toByteString())
                .setSenderPublicKey(ByteString.copyFrom(LibraHelper.stripPrefix(publicKey)))
                .setSenderSignature(ByteString.copyFrom(LibraHelper.signTransaction(rawTransaction, privateKey)))
                .build();

        SubmitTransactionRequest submitTransactionRequest = SubmitTransactionRequest.newBuilder()
                .setSignedTxn(signedTransaction)
                .build();

        SubmitTransactionResponse response = stub.submitTransaction(submitTransactionRequest);

        log.info("response: " + response);

        channel.shutdown();

        return "";
    }

    @Override
    public long getBalance(Wallet wallet) {
        return 0;
    }

    @Override
    public boolean acceptsCurrency(String currency) {
        return LIBRA.equals(currency);
    }
}
