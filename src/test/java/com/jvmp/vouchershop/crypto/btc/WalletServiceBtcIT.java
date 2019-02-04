package com.jvmp.vouchershop.crypto.btc;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.Wallet;
import org.bitcoinj.core.Context;
import org.bitcoinj.params.UnitTestParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;

import static com.jvmp.vouchershop.utils.RandomUtils.randomWallet;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class WalletServiceBtcIT {

    @Autowired
    private WalletRepository walletRepository;

    @MockBean
    private BitcoinJAdapter bitcoinJAdapter;

    private WalletServiceBtc walletService;

    @Before
    public void setUp() {
        Context btcContext = new Context(UnitTestParams.get());
        walletService = new WalletServiceBtc(walletRepository, btcContext.getParams(), bitcoinJAdapter);
    }

    @Test
    public void save() {
        Wallet savedWallet = walletService.save(randomWallet());

        assertNotNull(savedWallet.getId());
        assertTrue(Instant.ofEpochMilli(savedWallet.getCreatedAt()).isBefore(Instant.now()));
    }
}