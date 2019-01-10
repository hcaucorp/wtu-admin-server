package com.jvmp.vouchershop.crypto.btc;

import com.google.common.util.concurrent.ListenableFuture;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.Wallet;
import lombok.val;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.UnitTestParams;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet.SendResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static com.jvmp.vouchershop.RandomUtils.randomString;
import static com.jvmp.vouchershop.RandomUtils.randomWallet;
import static com.jvmp.vouchershop.TryUtils.expectingException;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"ResultOfMethodCallIgnored"})
@RunWith(MockitoJUnitRunner.class)
public class WalletServiceBtcTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private BitcoinJAdapter bitcoinJAdapter;

    private WalletServiceBtc walletServiceBtc;

    private Context btcContext;
    @Mock
    private ListenableFuture<Transaction> listenableFuture;

    @Before
    public void setUp() {
        btcContext = new Context(UnitTestParams.get());
        walletServiceBtc = new WalletServiceBtc(walletRepository, btcContext.getParams(), bitcoinJAdapter);
    }

    @Test
    public void sendMoneyShouldFailForNotBTC() {
        Wallet wallet = randomWallet().withCurrency("BSV");
        String to = randomString();
        long amount = nextLong(1, Long.MAX_VALUE);
        String expectedMessage = "Wallet " + wallet.toString() + " can provide only for vouchers in BTC";

        walletServiceBtc.sendMoney(wallet, to, amount)
                .subscribe(s -> fail("was expecting an exception"),
                        throwable -> {
                            assertEquals(IllegalOperationException.class, throwable.getClass());
                            assertEquals(expectedMessage, throwable.getMessage());
                        });
    }

    @Test
    public void sendMoneyShouldFailWhenIssuficientMoney() {
        Wallet wallet = randomWallet();
        String to = randomString();
        long amount = nextLong(1, Long.MAX_VALUE);

        walletServiceBtc.sendMoney(wallet, to, amount)
                .subscribe(s -> fail("was expecting an exception"),
                        throwable -> assertEquals(InsufficientMoneyException.class, throwable.getClass()));
    }

    @Test
    public void sendMoneyShouldSucceed() throws Exception {
        Wallet wallet = randomWallet();
        String to = randomString();
        long amount = nextLong(1, Long.MAX_VALUE);
        Transaction tx = new Transaction(btcContext.getParams());
        Sha256Hash hash = Sha256Hash.of(randomString().getBytes());
        ReflectionTestUtils.setField(tx, "hash", hash);
        SendResult sendResult = new SendResult();
        sendResult.broadcastComplete = listenableFuture;
        when(listenableFuture.get()).thenReturn(tx);
        when(bitcoinJAdapter.sendCoins(any())).thenReturn(new SendResult());
        //wtf pretty long setup

        walletServiceBtc.sendMoney(wallet, to, amount)
                .subscribe(s -> assertEquals(hash.toString(), s),
                        throwable -> {
                            throw new RuntimeException(throwable);
                        });
    }

    @Test
    public void importWallet() throws UnreadableWalletException {
        val testWallet = randomWallet();
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet.withId(nextLong(1, Long.MAX_VALUE)));

        Optional<Wallet> wallet = walletServiceBtc.importWallet(testWallet.getMnemonic(), Instant.now().toEpochMilli());

        assertNotNull(wallet);
    }

    @Test
    public void generateWalletShouldFailForNotBTC() {
        String expectedMessage = "Currency BSV is not supported.";
        when(walletRepository.findAll()).thenReturn(singletonList(randomWallet()));

        Throwable t = expectingException(() -> walletServiceBtc.generateWallet("BSV"));

        assertNotNull(t);
        assertEquals(IllegalOperationException.class, t.getClass());
        assertEquals(expectedMessage, t.getMessage());
    }

    @Test
    public void generateWalletShouldFailIfOneAlreadyExists() {
        String expectedMessage = "BTC wallet already exists. Currently we support only single wallet per currency";
        when(walletRepository.findAll()).thenReturn(singletonList(randomWallet()));

        Throwable t = expectingException(() -> walletServiceBtc.generateWallet("BTC"));

        assertNotNull(t);
        assertEquals(IllegalOperationException.class, t.getClass());
        assertEquals(expectedMessage, t.getMessage());
    }

    @Test
    public void generateWalletShouldSucced() {
        val testWallet = randomWallet();
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet.withId(nextLong(1, Long.MAX_VALUE)));
        when(walletRepository.findAll()).thenReturn(emptyList());

        Wallet wallet = walletServiceBtc.generateWallet("BTC");

        assertNotNull(wallet);

        verify(walletRepository, times(1)).save(any(Wallet.class));
    }
}