package com.jvmp.vouchershop.crypto.bch;

import cash.bitcoinj.core.Context;
import cash.bitcoinj.core.Sha256Hash;
import cash.bitcoinj.core.Transaction;
import cash.bitcoinj.params.UnitTestParams;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.Wallet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static com.jvmp.vouchershop.utils.RandomUtils.randomBchAddress;
import static com.jvmp.vouchershop.utils.RandomUtils.randomString;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BitcoinCashServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private BitcoinCashJAdapter bitcoinCashJAdapter;

    @Mock
    private NotificationService notificationService;

    private BitcoinCashService bitcoinCashService;

    private Context context;

    @Before
    public void setUp() {
        context = new Context(UnitTestParams.get());
        bitcoinCashService = new BitcoinCashService(walletRepository, context.getParams(), bitcoinCashJAdapter,
                notificationService);
    }

    @Test
    public void sendMoneyShouldSucceed() throws Exception {
        Wallet wallet = randomBchWallet().withCurrency("BTC");
        String to = randomBchAddress(context.getParams());
        long amount = nextLong(1, 1_000);
        Transaction tx = new Transaction(context.getParams());
        Sha256Hash hash = Sha256Hash.of(randomString().getBytes());
        ReflectionTestUtils.setField(tx, "hash", hash);
        cash.bitcoinj.wallet.Wallet.SendResult sendResult = new cash.bitcoinj.wallet.Wallet.SendResult();
        sendResult.tx = tx;
        when(bitcoinCashJAdapter.sendCoins(any())).thenReturn(sendResult);
        //wtf pretty long setup

        String result = bitcoinCashService.sendMoney(wallet, to, amount);
        assertEquals(tx.getHashAsString(), result);
    }
}