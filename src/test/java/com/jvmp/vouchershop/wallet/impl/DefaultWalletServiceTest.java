package com.jvmp.vouchershop.wallet.impl;

import com.jvmp.vouchershop.wallet.Wallet;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static com.jvmp.vouchershop.utils.RandomUtils.randomWallet;
import static org.junit.Assert.*;

public class DefaultWalletServiceTest {

    private DefaultWalletService subject;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void importWallet() {
        fail();
    }

    @Test
    public void generateWallet() {
        fail();
    }

    @Test
    public void findAll() {
        fail();
    }

    @Test
    public void findById() {
        fail();
    }

    @Test
    public void save() {
        Wallet savedWallet = subject.save(randomWallet());
        assertNotNull(savedWallet.getId());
        Instant now = Instant.now(), createdAt = Instant.ofEpochMilli(savedWallet.getCreatedAt());
        assertFalse(createdAt.isAfter(now));
    }
}