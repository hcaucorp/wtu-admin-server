package es.coffeebyt.wtu.wallet.impl;

import es.coffeebyt.wtu.crypto.CurrencyService;
import es.coffeebyt.wtu.crypto.CurrencyServiceSupplier;
import es.coffeebyt.wtu.repository.WalletRepository;
import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static es.coffeebyt.wtu.crypto.btc.BitcoinService.BTC;
import static es.coffeebyt.wtu.utils.RandomUtils.*;
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
    public void importWallet() {
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