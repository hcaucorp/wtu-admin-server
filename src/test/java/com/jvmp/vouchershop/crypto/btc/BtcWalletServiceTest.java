package com.jvmp.vouchershop.crypto.btc;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.repository.WalletRepository;
import org.bitcoinj.core.Context;
import org.bitcoinj.params.UnitTestParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.jvmp.vouchershop.RandomUtils.randomString;
import static com.jvmp.vouchershop.RandomUtils.randomWallet;

@RunWith(MockitoJUnitRunner.class)
public class BtcWalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    private BtcWalletService walletService;

    @Before
    public void setUp() {
        Context btcContext = new Context(UnitTestParams.get());
        walletService = new BtcWalletService(walletRepository, btcContext.getParams());
    }

    @Test(expected = IllegalOperationException.class)
    public void sendMoneyWrongWalletCurrency() {

        walletService.sendMoney(randomWallet().withCurrency("BSV"), randomString(), 10);
    }

    @Test
    public void sendMoney() {
    }
}