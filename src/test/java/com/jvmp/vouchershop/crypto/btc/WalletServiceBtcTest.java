package com.jvmp.vouchershop.crypto.btc;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.Wallet;
import lombok.val;
import org.bitcoinj.core.Context;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.UnitTestParams;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Optional;

import static com.google.common.util.concurrent.Service.State.*;
import static com.jvmp.vouchershop.Collections.asSet;
import static com.jvmp.vouchershop.RandomUtils.randomString;
import static com.jvmp.vouchershop.RandomUtils.randomWallet;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WalletServiceBtcTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletAppKit walletAppKit;

    private WalletServiceBtc serviceBtc;

    @Before
    public void setUp() {
        Context btcContext = new Context(UnitTestParams.get());
        serviceBtc = new WalletServiceBtc(walletRepository, btcContext.getParams(), walletAppKit);
    }

    @Test(expected = IllegalOperationException.class)
    public void sendMoneyWrongWalletCurrency() {
        serviceBtc.sendMoney(randomWallet().withCurrency("BSV"), randomString(), 10);
    }

    @Test
    public void sendMoney() {
        fail("not implemented");
    }

    @Test
    public void start_doNothingIfThereIsNoWallets() {
        when(walletRepository.findAll()).thenReturn(emptyList());

        serviceBtc.start();
        verifyZeroInteractions(walletAppKit);
    }

    @Test
    public void start_doNothingIfStatusIsNotNEW() {
        when(walletRepository.findAll()).thenReturn(singletonList(randomWallet()));

        asSet(FAILED, RUNNING, STARTING, STOPPING, TERMINATED).forEach(state -> {
            when(walletAppKit.state()).thenReturn(state);
            serviceBtc.start();
        });

        verify(walletAppKit, never()).startAsync();
    }

    @Test
    public void startShouldSucceed() {
        when(walletRepository.findAll()).thenReturn(singletonList(randomWallet()));
        when(walletAppKit.state()).thenReturn(NEW);

        serviceBtc.start();

        verify(walletAppKit, times(1)).startAsync();
        verify(walletAppKit, times(1)).awaitRunning();
    }

    @Test
    public void importWallet() throws UnreadableWalletException {
        val testWallet = randomWallet();

        Optional<Wallet> wallet = serviceBtc.importWallet(testWallet.getMnemonic(), Instant.now().toEpochMilli());

        assertNotNull(wallet);
    }

    @Test
    public void generateWallet() {
        Wallet wallet = serviceBtc.generateWallet("BTC");

        assertNotNull(wallet);
    }
}