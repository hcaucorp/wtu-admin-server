package com.jvmp.vouchershop.crypto.btc;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.Wallet;
import lombok.val;
import org.bitcoinj.core.*;
import org.bitcoinj.params.UnitTestParams;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet.SendResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.jvmp.vouchershop.utils.RandomUtils.*;
import static com.jvmp.vouchershop.utils.TryUtils.expectingException;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BitcoinServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private BitcoinJAdapter bitcoinJAdapter;

    @Mock
    private NotificationService notificationService;

    private BitcoinService bitcoinService;

    private Context btcContext;

    @Before
    public void setUp() {
        btcContext = new Context(UnitTestParams.get());
        bitcoinService = new BitcoinService(walletRepository, btcContext.getParams(), bitcoinJAdapter,
                notificationService);
    }

    @Test
    public void sendMoneyShouldFailForNotBTC() {
        Wallet wallet = randomWallet().withCurrency("BSV");
        String to = randomString();
        long amount = nextLong(1, Long.MAX_VALUE);
        String expectedMessage = "Wallet " + wallet.toString() + " can provide only for vouchers in BTC";

        Throwable throwable = expectingException(() -> bitcoinService.sendMoney(wallet, to, amount));
        assertEquals(IllegalOperationException.class, throwable.getClass());
        assertEquals(expectedMessage, throwable.getMessage());
    }

    @Test(expected = IllegalOperationException.class)
    public void sendMoneyShouldFailWhenInsufficientMoney() throws Exception {
        Wallet wallet = randomWallet().withCurrency("BTC");
        String to = randomBtcAddress();
        long amount = nextLong(1, 1_000);
        when(bitcoinJAdapter.sendCoins(any())).thenThrow(new InsufficientMoneyException(Coin.valueOf(amount)));

        bitcoinService.sendMoney(wallet, to, amount);
    }

    @Test
    public void sendMoneyShouldSucceed() throws Exception {
        Wallet wallet = randomWallet().withCurrency("BTC");
        String to = randomBtcAddress();
        long amount = nextLong(1, 1_000);
        Transaction tx = new Transaction(btcContext.getParams());
        Sha256Hash hash = Sha256Hash.of(randomString().getBytes());
        ReflectionTestUtils.setField(tx, "hash", hash);
        SendResult sendResult = new SendResult();
        sendResult.tx = tx;
        when(bitcoinJAdapter.sendCoins(any())).thenReturn(sendResult);
        //wtf pretty long setup

        String result = bitcoinService.sendMoney(wallet, to, amount);
        assertEquals(tx.getHashAsString(), result);
    }

    @Test
    public void importWallet() throws UnreadableWalletException {
        long createdAt = 1546175793;
        Wallet testWallet = randomWallet()
                .withCreatedAt(createdAt * 1_000);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        when(walletRepository.save(walletCaptor.capture())).thenReturn(testWallet.withId(nextLong(1, Long.MAX_VALUE)));

        Optional<Wallet> optionalWallet = bitcoinService.importWallet(testWallet.getMnemonic(), createdAt);

        assertTrue(optionalWallet.isPresent());

        Wallet result = walletCaptor.getValue();
        assertEquals(testWallet.getCreatedAt(), result.getCreatedAt());
    }

    @Test
    public void generateWalletShouldFailForNotBTC() {
        String expectedMessage = "Currency BSV is not supported.";

        Throwable t = expectingException(() -> bitcoinService.generateWallet("BSV"));

        assertNotNull(t);
        assertEquals(IllegalOperationException.class, t.getClass());
        assertEquals(expectedMessage, t.getMessage());
    }

    @Test
    public void generateWalletShouldFailIfOneAlreadyExists() {
        String expectedMessage = "BTC wallet already exists. Currently we support only single wallet per currency";
        when(walletRepository.findAll()).thenReturn(singletonList(randomWallet()));

        Throwable t = expectingException(() -> bitcoinService.generateWallet("BTC"));

        assertNotNull(t);
        assertEquals(IllegalOperationException.class, t.getClass());
        assertEquals(expectedMessage, t.getMessage());
    }

    @Test
    public void generateWalletShouldSucced() {
        val testWallet = randomWallet();
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet.withId(nextLong(1, Long.MAX_VALUE)));
        when(walletRepository.findAll()).thenReturn(emptyList());

        Wallet wallet = bitcoinService.generateWallet("BTC");

        assertNotNull(wallet);

        verify(walletRepository, times(1)).save(any(Wallet.class));
    }
}