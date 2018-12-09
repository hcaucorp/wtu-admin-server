package com.jvmp.vouchershop.wallet;

import com.jvmp.vouchershop.domain.Wallet;
import com.jvmp.vouchershop.system.DatabaseConfig;
import org.apache.commons.lang3.RandomStringUtils;
import org.bitcoinj.params.UnitTestParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.Assert.*;

/**
 * Created by Hubert Czerpak on 2018-12-08
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = {DatabaseConfig.class, WalletRepository.class},
        loader = AnnotationConfigContextLoader.class)
@Transactional
public class WalletServiceImplIT {

    @Autowired
    private WalletRepository walletRepository;

    private WalletServiceImpl walletService;

    @Before
    public void setUp() {
        walletService = new WalletServiceImpl(walletRepository, new UnitTestParams());
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
        Wallet generatedWallet = walletService.generateWallet(strongPassword);
        Wallet savedWallet = walletService.save(generatedWallet);

        assertNotNull(generatedWallet);
        assertNull(generatedWallet.getId());
        assertNull(generatedWallet.getCreatedAt());

        assertNotNull(savedWallet.getId());
        assertNotNull(savedWallet.getCreatedAt());
        assertTrue(savedWallet.getCreatedAt().toInstant().isBefore(Instant.now()));
    }
}