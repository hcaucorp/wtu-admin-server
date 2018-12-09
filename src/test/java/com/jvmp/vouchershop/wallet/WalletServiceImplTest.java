package com.jvmp.vouchershop.wallet;

import com.jvmp.vouchershop.domain.Wallet;
import org.apache.commons.lang3.RandomStringUtils;
import org.bitcoinj.params.UnitTestParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by Hubert Czerpak on 2018-12-08
 */
@RunWith(MockitoJUnitRunner.class)
public class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    private WalletServiceImpl walletService;

    @Before
    public void setUp() {
        walletService = new WalletServiceImpl(walletRepository, new UnitTestParams());
    }

    @Test
    public void generateWallet() {
        String strongPassword = RandomStringUtils.randomAlphabetic(32);
        Wallet generatedWallet = walletService.generateWallet(strongPassword);

        assertNotNull(generatedWallet);
        assertNotNull(generatedWallet.getAddress());
        assertNotNull(generatedWallet.getExtendedPrivateKey());

        verify(walletRepository, times(1)).save(eq(generatedWallet));
    }
}