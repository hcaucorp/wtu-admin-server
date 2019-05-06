package com.jvmp.vouchershop.wallet.impl;

import com.jvmp.vouchershop.crypto.CurrencyService;
import com.jvmp.vouchershop.crypto.CurrencyServiceSupplier;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.ImportWalletRequest;
import com.jvmp.vouchershop.wallet.Wallet;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static com.jvmp.vouchershop.crypto.btc.BitcoinService.BTC;
import static com.jvmp.vouchershop.utils.RandomUtils.*;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang.math.RandomUtils.nextLong;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultWalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private CurrencyServiceSupplier currencyServiceSupplier;

    @Mock
    private CurrencyService currencyService;

    private DefaultWalletService subject;

    @Before
    public void setUp() {
        subject = new DefaultWalletService(walletRepository, currencyServiceSupplier);

        when(currencyServiceSupplier.findByCurrency(any())).thenReturn(currencyService);
    }

    @Test
    public void importWallet() throws UnreadableWalletException {
        ImportWalletRequest importDescription = new ImportWalletRequest(BTC, randomString(), 1L);
        subject.importWallet(importDescription);

        verify(currencyService, times(1)).importWallet(eq(importDescription));
    }

    @Test
    public void generateWallet() {
        String currency = randomCurrency();
        subject.generateWallet(currency);

        verify(currencyService, times(1)).generateWallet();
    }

    @Test
    public void findAll() {
        String currency = randomCurrency();
        Wallet wallet = randomWallet().withCurrency(currency);
        long balance = nextLong();
        when(walletRepository.findAll()).thenReturn(singletonList(wallet));
        when(currencyService.getBalance(eq(wallet))).thenReturn(balance);

        List<Wallet> wallets = subject.findAll();

        verify(walletRepository, times(1)).findAll();
        verify(currencyService, times(1)).getBalance(wallet);

        assertEquals(singletonList(wallet.withBalance(balance)), wallets);
    }

    @Test
    public void findById() {
        long id = 5;
        subject.findById(id);
        verify(walletRepository, times(1)).findById(id);
    }

    @Test
    public void save() {
        Wallet wallet = randomWallet();
        subject.save(wallet);
        verify(walletRepository, times(1)).save(wallet);
    }
}