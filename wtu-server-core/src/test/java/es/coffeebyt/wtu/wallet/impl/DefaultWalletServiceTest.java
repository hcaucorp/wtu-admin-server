package es.coffeebyt.wtu.wallet.impl;

import es.coffeebyt.wtu.crypto.CurrencyService;
import es.coffeebyt.wtu.crypto.CurrencyServiceSupplier;
import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.repository.WalletRepository;
import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static es.coffeebyt.wtu.crypto.btc.BitcoinService.BTC;
import static es.coffeebyt.wtu.utils.RandomUtils.randomCurrency;
import static es.coffeebyt.wtu.utils.RandomUtils.randomString;
import static es.coffeebyt.wtu.utils.RandomUtils.randomWallet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultWalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private CurrencyServiceSupplier currencyServiceSupplier;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private DefaultWalletService subject;

    @Before
    public void setUp() {
        when(currencyServiceSupplier.findByCurrency(any())).thenReturn(currencyService);
    }

    @Test
    public void importWallet() {
        when(currencyService.importWallet(any())).thenReturn(randomWallet());

        ImportWalletRequest importDescription = new ImportWalletRequest(BTC, randomString(), 1L);
        subject.importWallet(importDescription);

        verify(currencyService, times(1)).importWallet(eq(importDescription));
    }

    @Test
    public void generateWallet() {
        String currency = randomCurrency();
        when(currencyService.generateWallet()).thenReturn(randomWallet());

        subject.generateWallet(currency);

        verify(currencyService, times(1)).generateWallet();
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
        when(walletRepository.save(any())).thenReturn(wallet);

        subject.save(wallet);
        verify(walletRepository, times(1)).save(wallet);
    }
}
