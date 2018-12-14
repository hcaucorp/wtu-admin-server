package com.jvmp.vouchershop.wallet;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.crypto.btc.BtcWalletService;
import com.jvmp.vouchershop.domain.Wallet;
import com.jvmp.vouchershop.repository.WalletRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.bitcoinj.core.Context;
import org.bitcoinj.params.UnitTestParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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


    @Test
    public void findAll() {
        List<Wallet> expected = IntStream.range(0, 100)
                .mapToObj(value -> walletService.generateWallet(RandomStringUtils.randomAlphabetic(12), "Integration Test wallet #" + value))
                .collect(toList());
        walletRepository.saveAll(expected);

        List<Wallet> result = walletService.findAll();
        assertEquals(expected, result);
    }

    @Test
    public void findById() {
        fail("not implemented");
    }

    @Test
    public void delete() {
        fail("not implemented");
    }

    @Test
    public void save() {
        String strongPassword = RandomStringUtils.randomAlphabetic(32);
        Wallet generatedWallet = walletService.generateWallet(
                strongPassword,
                RandomStringUtils.randomAlphabetic(32));

        assertNotNull(generatedWallet);
        assertNull(generatedWallet.getId());
        assertNotNull(generatedWallet.getCreatedAt());

        Wallet savedWallet = walletService.save(generatedWallet);

        assertNotNull(savedWallet.getId());
        assertTrue(savedWallet.getCreatedAt().toInstant().isBefore(Instant.now()));
    }

}