package com.jvmp.vouchershop.wallet;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.domain.Wallet;
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

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class WalletServiceImplIT {

    @Autowired
    private WalletRepository walletRepository;

    private WalletServiceImpl walletService;

    @Before
    public void setUp() {
        Context btcContext = new Context(UnitTestParams.get());
        walletService = new WalletServiceImpl(walletRepository, btcContext.getParams());
    }


    @Test
    public void findAll() {
    }

    @Test
    public void findById() {
    }

    @Test
    public void delete() {
    }

    @Test
    public void save() {
        String strongPassword = RandomStringUtils.randomAlphabetic(32);
        Wallet generatedWallet = walletService.generateWallet(
                strongPassword,
                RandomStringUtils.randomAlphabetic(32));

        assertNotNull(generatedWallet);
        assertNull(generatedWallet.getId());
        assertNull(generatedWallet.getCreatedAt());

        Wallet savedWallet = walletService.save(generatedWallet);

        assertNotNull(savedWallet.getId());
        assertNotNull(savedWallet.getCreatedAt());
        assertTrue(savedWallet.getCreatedAt().toInstant().isBefore(Instant.now()));

        assertEquals(1, walletRepository.count());
    }

}