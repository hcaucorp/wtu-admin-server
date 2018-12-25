package com.jvmp.vouchershop.wallet;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.crypto.btc.BtcWalletService;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.repository.WalletRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.bitcoinj.core.Context;
import org.bitcoinj.params.UnitTestParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;

import static com.jvmp.vouchershop.RandomUtils.randomWallet;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class BtcWalletServiceIT {

    @Autowired
    private WalletRepository walletRepository;

    private BtcWalletService walletService;

    @Before
    public void setUp() {
        Context btcContext = new Context(UnitTestParams.get());
        walletService = new BtcWalletService(walletRepository, btcContext.getParams());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void deleteVoucherByIdNotFound() {
        walletService.delete(1);
    }

    @Test
    public void delete() {
        Wallet wallet = walletRepository.save(randomWallet());
        assertNotNull(walletRepository.findById(wallet.getId()));

        walletService.delete(wallet.getId());

        assertFalse(walletRepository.findById(wallet.getId()).isPresent());
    }

    @Test
    public void save() {
        String strongPassword = RandomStringUtils.randomAlphabetic(32);
        Wallet generatedWallet = walletService.generateWallet(
                strongPassword,
                RandomStringUtils.randomAlphabetic(32));

        assertNotNull(generatedWallet);
        assertNull(generatedWallet.getId());
        assertTrue(generatedWallet.getCreatedAt() > 0);

        Wallet savedWallet = walletService.save(generatedWallet);

        assertNotNull(savedWallet.getId());
        assertTrue(Instant.ofEpochMilli(savedWallet.getCreatedAt()).isBefore(Instant.now()));
    }
}