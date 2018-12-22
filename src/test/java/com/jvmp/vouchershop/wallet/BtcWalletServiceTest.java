package com.jvmp.vouchershop.wallet;

import com.jvmp.vouchershop.crypto.btc.BtcWalletService;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.repository.WalletRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.bitcoinj.core.Context;
import org.bitcoinj.params.UnitTestParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.jvmp.vouchershop.RandomUtils.wallet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Hubert Czerpak on 2018-12-08
 */
@RunWith(MockitoJUnitRunner.class)
public class BtcWalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    private BtcWalletService walletService;

//    @Mock
//    private org.bitcoinj.wallet.Wallet btcWallet;

    private Wallet testWallet;

    @Before
    public void setUp() {
        Context btcContext = new Context(UnitTestParams.get());
        walletService = new BtcWalletService(walletRepository, btcContext.getParams());
        testWallet = wallet();
    }

    @Test
    public void generateWallet() {
        String strongPassword = RandomStringUtils.randomAlphabetic(32);
        String description = RandomStringUtils.randomAlphabetic(32);
        Wallet generatedWallet = walletService.generateWallet(strongPassword, description);

        assertNotNull(generatedWallet);
        assertNotNull(generatedWallet.getAddress());
        assertNotNull(generatedWallet.getMnemonic());
        assertEquals(description, generatedWallet.getDescription());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void return404IfNowWalletOnDeleteWallet() {
        final long id = RandomUtils.nextLong(0, 1000);

        walletService.delete(id);
    }

// TODO have to convert restoreWallet() function into "wallet restoration service"
//    @Test(expected = IllegalOperationException.class)
//    public void dontLetRemoveWalletWithBalance() {
//        final long id = RandomUtils.nextLong(0, 1000);
//        when(walletRepository.findById(eq(id))).thenReturn(Optional.of(testWallet));
//        when(btcWallet.getBalance()).thenReturn(Coin.valueOf(RandomUtils.nextLong(1_000, 2_000)));
//
//        walletService.delete(id);
//    }

    @Test
    public void deleteWalletSuccessfully() {
        final long id = RandomUtils.nextLong(0, 1000);
        when(walletRepository.findById(eq(id))).thenReturn(Optional.of(testWallet));

        walletService.delete(id);

        verify(walletRepository, times(1)).deleteById(id);
    }
}