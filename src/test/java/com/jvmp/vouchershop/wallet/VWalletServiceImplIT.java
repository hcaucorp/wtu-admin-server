package com.jvmp.vouchershop.wallet;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.crypto.btc.WalletServiceImpl;
import com.jvmp.vouchershop.domain.VWallet;
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class VWalletServiceImplIT {

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
        VWallet generatedVWallet = walletService.generateWallet(
                strongPassword,
                RandomStringUtils.randomAlphabetic(32));

        assertNotNull(generatedVWallet);
        assertNull(generatedVWallet.getId());
        assertTrue(0 < generatedVWallet.getCreationTime());

        VWallet savedVWallet = walletService.save(generatedVWallet);

        assertNotNull(savedVWallet.getId());
        assertTrue(0 < savedVWallet.getCreationTime());
    }

}