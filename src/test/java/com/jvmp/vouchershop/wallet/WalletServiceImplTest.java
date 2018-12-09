package com.jvmp.vouchershop.wallet;

import com.jvmp.vouchershop.domain.Wallet;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.repository.WalletRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.params.UnitTestParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by Hubert Czerpak on 2018-12-08
 */
@RunWith(MockitoJUnitRunner.class)
public class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    private WalletServiceImpl walletService;

    @Mock
    private org.bitcoinj.wallet.Wallet btcWallet;
    private Wallet testWallet;

    @Before
    public void setUp() {
        Context btcContext = new Context(UnitTestParams.get());
        walletService = new WalletServiceImpl(walletRepository, btcContext.getParams());
        testWallet = new Wallet()
                .withAddress(UUID.randomUUID().toString())
                .withBtcWallet(btcWallet);
    }

    @Test
    public void generateWallet() {
        String strongPassword = RandomStringUtils.randomAlphabetic(32);
        Wallet generatedWallet = walletService.generateWallet(strongPassword);

        assertNotNull(generatedWallet);
        assertNotNull(generatedWallet.getAddress());
        assertNotNull(generatedWallet.getExtendedPrivateKey());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void return404IfNowWalletOnDeleteWallet() {
        final long id = RandomUtils.nextLong(0, 1000);

        walletService.delete(id);
    }

    @Test
    public void findAllWallets() {
        walletService.findAll();
        verify(walletRepository, times(1)).findAll();
    }

    @Test
    public void findById() {
        final long id = RandomUtils.nextLong(0, 1000);
        walletService.findById(id);
        verify(walletRepository, times(1)).findById(id);
    }


    @Test(expected = IllegalOperationException.class)
    public void dontLetRemoveWalletWithBalance() {
        final long id = RandomUtils.nextLong(0, 1000);
        when(walletRepository.findById(eq(id))).thenReturn(Optional.of(testWallet));
        when(btcWallet.getBalance()).thenReturn(Coin.valueOf(RandomUtils.nextLong(1_000, 2_000)));

        walletService.delete(id);
    }

    @Test
    public void deleteWalletSuccessfully() {
        final long id = RandomUtils.nextLong(0, 1000);
        when(walletRepository.findById(eq(id))).thenReturn(Optional.of(testWallet));
        when(btcWallet.getBalance()).thenReturn(Coin.ZERO);

        walletService.delete(id);

        verify(walletRepository, times(1)).deleteById(id);
    }
}