package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.domain.Wallet;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.wallet.WalletService;
import org.apache.commons.lang3.RandomUtils;
import org.bitcoinj.core.Coin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by Hubert Czerpak on 2018-12-08
 */
@RunWith(MockitoJUnitRunner.class)
public class WalletControllerTest {

    @Mock
    private WalletService walletService;

    @Mock
    private org.bitcoinj.wallet.Wallet btcWallet;

    private WalletController walletController;

    private Wallet testWallet;

    @Before
    public void setUp() {
        walletController = new WalletController(walletService);
        testWallet = new Wallet()
                .withAddress(UUID.randomUUID().toString())
                .withBtcWallet(btcWallet);
    }

    @Test
    public void getAllWallets() {
        when(walletService.findAll()).thenReturn(singletonList(testWallet));

        assertEquals(singletonList(testWallet), walletController.getAllWallets());

        verify(walletService, times(1)).findAll();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void return404IfNowWalletOnDeleteWallet() {
        final long id = RandomUtils.nextLong(0, 1000);

        walletController.deleteWallet(id);
    }

    @Test(expected = IllegalOperationException.class)
    public void dontLetRemoveWalletWithBalance() {
        final long id = RandomUtils.nextLong(0, 1000);
        when(walletService.findById(eq(id))).thenReturn(Optional.of(testWallet));
        when(btcWallet.getBalance()).thenReturn(Coin.valueOf(RandomUtils.nextLong(1_000, 2_000)));

        walletController.deleteWallet(id);
    }

    @Test
    public void deleteWalletSuccessfully() {
        final long id = RandomUtils.nextLong(0, 1000);
        when(walletService.findById(eq(id))).thenReturn(Optional.of(testWallet));
        when(btcWallet.getBalance()).thenReturn(Coin.ZERO);

        walletController.deleteWallet(id);

        verify(walletService, times(1)).delete(eq(id));
    }

    @Test
    public void generateWallet() {
    }
}