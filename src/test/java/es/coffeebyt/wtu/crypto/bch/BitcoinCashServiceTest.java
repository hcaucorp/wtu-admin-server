package es.coffeebyt.wtu.crypto.bch;

import cash.bitcoinj.core.Context;
import cash.bitcoinj.core.Sha256Hash;
import cash.bitcoinj.core.Transaction;
import cash.bitcoinj.params.MainNetParams;
import cash.bitcoinj.params.UnitTestParams;
import es.coffeebyt.wtu.repository.WalletRepository;
import es.coffeebyt.wtu.utils.RandomUtils;
import es.coffeebyt.wtu.wallet.Wallet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static es.coffeebyt.Collections.asSet;
import static es.coffeebyt.wtu.crypto.bch.BitcoinCashService.BCH;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BitcoinCashServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private BitcoinCashJAdapter bitcoinCashJAdapter;

    private BitcoinCashService subject;

    private Context context;

    @Before
    public void setUp() {
        context = new Context(UnitTestParams.get());
        subject = new BitcoinCashService(walletRepository, context.getParams(), bitcoinCashJAdapter);
    }

    @Test
    public void sendMoneyToCashAddress() throws Exception {
        Wallet wallet = RandomUtils.randomWallet(context.getParams()).withCurrency(BCH);

        Set<String> addressExamples = asSet("bchtest:qzepp6dn7czmu5t64uewj35mm8lcj2375sqt4ac8er");
        String to = addressExamples.iterator().next();

        long amount = nextLong(1, 1_000);
        Transaction tx = new Transaction(context.getParams());
        Sha256Hash hash = Sha256Hash.of(RandomUtils.randomString().getBytes());
        ReflectionTestUtils.setField(tx, "hash", hash);
        cash.bitcoinj.wallet.Wallet.SendResult sendResult = new cash.bitcoinj.wallet.Wallet.SendResult();
        sendResult.tx = tx;
        when(bitcoinCashJAdapter.sendCoins(any())).thenReturn(sendResult);
        //wtf pretty long setup

        String result = subject.sendMoney(wallet, to, amount);
        assertEquals(tx.getHashAsString(), result);
    }

    @Test
    public void readAddress() {
        assertNotNull(subject.readAddress(UnitTestParams.get(), "bchtest:qzepp6dn7czmu5t64uewj35mm8lcj2375sqt4ac8er"));
        assertNotNull(subject.readAddress(MainNetParams.get(), "bitcoincash:qpm2qsznhks23z7629mms6s4cwef74vcwvy22gdx6a"));
        assertNotNull(subject.readAddress(MainNetParams.get(), "1BpEi6DfDAUFd7GtittLSdBeYJvcoaVggu"));
    }
}