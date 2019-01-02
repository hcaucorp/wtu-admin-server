package com.jvmp.vouchershop.crypto.btc;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.repository.WalletRepository;
import org.bitcoinj.core.Context;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.UnitTestParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.jvmp.vouchershop.RandomUtils.randomString;
import static com.jvmp.vouchershop.RandomUtils.randomWallet;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class WalletServiceBtcTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletAppKit walletAppKit;

    private WalletServiceBtc walletService;

    @Before
    public void setUp() {
        Context btcContext = new Context(UnitTestParams.get());
        walletService = new WalletServiceBtc(walletRepository, btcContext.getParams(), walletAppKit);
    }

    @Test(expected = IllegalOperationException.class)
    public void sendMoneyWrongWalletCurrency() {

        walletService.sendMoney(randomWallet().withCurrency("BSV"), randomString(), 10);
    }

    @Test
    public void sendMoney() {
        fail("not implemented");
    }

    @Test
    public void start() {
        fail("not implemented");
    }

    @Test
    public void importWallet() {
        fail("not implemented");
    }

    @Test
    public void generateWallet() {
        fail("not implemented");
    }

    @Test
    public void sendMoney1() {
        fail("not implemented");
    }
}