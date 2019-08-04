package es.coffeebyt.wtu.crypto.btc;

import static es.coffeebyt.wtu.utils.TryUtils.expectingException;
import static java.lang.String.format;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.UnitTestParams;
import org.bitcoinj.wallet.Wallet.SendResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import es.coffeebyt.wtu.exception.IllegalOperationException;
import es.coffeebyt.wtu.repository.WalletRepository;
import es.coffeebyt.wtu.utils.RandomUtils;
import es.coffeebyt.wtu.wallet.Wallet;
import lombok.val;

@RunWith(MockitoJUnitRunner.class)
public class BitcoinServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private BitcoinJFacade bitcoinJFacade;

    private BitcoinService bitcoinService;

    private Context btcContext;

    @Before
    public void setUp() {
        btcContext = new Context(UnitTestParams.get());
        bitcoinService = new BitcoinService(walletRepository, btcContext.getParams(), bitcoinJFacade);
    }

    @Test
    public void sendMoneyShouldFailForNotBTC() {
        Wallet wallet = RandomUtils.randomWallet().withCurrency("BSV");
        String to = RandomUtils.randomString();
        long amount = nextLong(1, Long.MAX_VALUE);
        String expectedMessage = format("Wallet's %s currency (BSV) doesn't match supported currency: BTC", wallet.getId());

        Throwable throwable = expectingException(() -> bitcoinService.sendMoney(wallet, to, amount));
        assertEquals(IllegalOperationException.class, throwable.getClass());
        assertEquals(expectedMessage, throwable.getMessage());
    }

    @Test(expected = IllegalOperationException.class)
    public void sendMoneyShouldFailWhenInsufficientMoney() throws Exception {
        Wallet wallet = RandomUtils.randomWallet().withCurrency("BTC");
        String to = RandomUtils.randomBtcAddress();
        long amount = nextLong(1, 1_000);
        when(bitcoinJFacade.sendCoins(any())).thenThrow(new InsufficientMoneyException(Coin.valueOf(amount)));

        bitcoinService.sendMoney(wallet, to, amount);
    }

    @Test
    public void sendMoneyShouldSucceed() throws Exception {
        Wallet wallet = RandomUtils.randomWallet().withCurrency("BTC");
        String to = RandomUtils.randomBtcAddress();
        long amount = nextLong(1, 1_000);
        Transaction tx = new Transaction(btcContext.getParams());
        Sha256Hash hash = Sha256Hash.of(RandomUtils.randomString().getBytes());
        ReflectionTestUtils.setField(tx, "cachedTxId", hash);
        SendResult sendResult = new SendResult();
        sendResult.tx = tx;
        when(bitcoinJFacade.sendCoins(any())).thenReturn(sendResult);
        //wtf pretty long setup

        String result = bitcoinService.sendMoney(wallet, to, amount);
        assertEquals(tx.getTxId().toString(), result);
    }

    @Test
    public void importWallet() {
        long createdAt = 1546175793;
        Wallet testWallet = RandomUtils.randomWallet()
                .withCreatedAt(createdAt * 1_000);

        when(walletRepository.save(any())).thenReturn(testWallet.withId(nextLong(1, Long.MAX_VALUE)));

        Wallet wallet = bitcoinService.importWallet(testWallet.getMnemonic(), createdAt);

        assertEquals(testWallet.getCreatedAt(), wallet.getCreatedAt());
    }

    @Test
    public void generateWalletShouldFailIfOneAlreadyExists() {
        String expectedMessage = "BTC wallet already exists. Currently we support only single wallet per currency";
        when(walletRepository.findOneByCurrency(BitcoinService.BTC)).thenReturn(Optional.of(RandomUtils.randomWallet()));

        Throwable t = expectingException(() -> bitcoinService.generateWallet());

        assertNotNull(t);
        assertEquals(IllegalOperationException.class, t.getClass());
        assertEquals(expectedMessage, t.getMessage());
    }

    @Test
    public void generateWalletShouldSucced() {
        val testWallet = RandomUtils.randomWallet();
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet.withId(nextLong(1, Long.MAX_VALUE)));
        when(walletRepository.findOneByCurrency(BitcoinService.BTC)).thenReturn(Optional.empty());

        Wallet wallet = bitcoinService.generateWallet();

        assertNotNull(wallet);

        verify(walletRepository, times(1)).save(any(Wallet.class));
    }
}