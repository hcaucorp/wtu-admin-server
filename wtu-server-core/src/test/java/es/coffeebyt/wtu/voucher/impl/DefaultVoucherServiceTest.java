package es.coffeebyt.wtu.voucher.impl;

import es.coffeebyt.wtu.crypto.CurrencyService;
import es.coffeebyt.wtu.crypto.CurrencyServiceSupplier;
import es.coffeebyt.wtu.exception.IllegalOperationException;
import es.coffeebyt.wtu.exception.ResourceNotFoundException;
import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.utils.RandomUtils;
import es.coffeebyt.wtu.voucher.RedemptionListener;
import es.coffeebyt.wtu.voucher.RedemptionValidator;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.voucher.VoucherNotFoundException;
import es.coffeebyt.wtu.wallet.Wallet;
import es.coffeebyt.wtu.wallet.WalletService;
import org.bitcoinj.core.Context;
import org.bitcoinj.params.UnitTestParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static es.coffeebyt.wtu.utils.TryUtils.expectingException;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultVoucherServiceTest {

    @Mock
    private WalletService walletService;

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private CurrencyServiceSupplier currencyServiceSupplier;

    @Mock
    private List<RedemptionValidator> redemptionValidators;

    @Mock
    private List<RedemptionListener> redemptionListeners;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private DefaultVoucherService subject;

    @Before
    public void setUp() {
        Context btcContext = new Context(UnitTestParams.get());
        Context.propagate(btcContext);
        when(currencyServiceSupplier.findByCurrency(any())).thenReturn(currencyService);
    }

    @Test(expected = IllegalOperationException.class)
    public void generateVouchersWithIndivisibleInput() {
        VoucherGenerationSpec spec = RandomUtils.randomVoucherGenerationSpec()
                .withCount(10)
                .withTotalAmount(99);

        subject.generateVouchers(spec);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void generateVouchersButWalletDoesntExist() {
        VoucherGenerationSpec spec = RandomUtils.randomVoucherGenerationSpec();

        subject.generateVouchers(spec);
    }

    @Test
    public void generateVouchers() {
        Wallet wallet = RandomUtils.randomWallet().withId(1L);
        Voucher v1 = RandomUtils.randomVoucher(),
                v2 = RandomUtils.randomVoucher();
        VoucherGenerationSpec spec = RandomUtils.randomVoucherGenerationSpec()
                .withWalletId(wallet.getId())
                .withVoucherCodes(v1.getCode() + " " + v2.getCode())
                .withCount(2)
                .withTotalAmount(100_000);

        when(walletService.findById(wallet.getId())).thenReturn(Optional.of(wallet));

        List<Voucher> vouchers = subject.generateVouchers(spec);
        Set<String> generatedCodes = vouchers.stream().map(Voucher::getCode).collect(toSet());

        assertEquals(2, vouchers.size());
        assertTrue(generatedCodes.contains(v1.getCode()));
        assertTrue(generatedCodes.contains(v2.getCode()));
    }

    @Test(expected = VoucherNotFoundException.class)
    public void redeemVoucher_noVoucher() throws VoucherNotFoundException {
        String code = RandomUtils.randomString();

        when(voucherRepository.findByCode(eq(code))).thenReturn(Optional.empty());

        subject.redeemVoucher(new RedemptionRequest(RandomUtils.randomString(), code));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void redeemVoucher_noWallet() throws VoucherNotFoundException {
        Voucher voucher = RandomUtils.randomVoucher()
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false);
        String code = voucher.getCode();
        String destinationAddress = RandomUtils.randomString();

        when(voucherRepository.findByCode(eq(code))).thenReturn(Optional.of(voucher));

        subject.redeemVoucher(new RedemptionRequest(destinationAddress, code));
    }

    @Test(expected = IllegalOperationException.class)
    public void redeemVoucher_notEnoughMoney() {
        Wallet wallet = RandomUtils.randomWallet(UnitTestParams.get());
        Voucher voucher = RandomUtils.randomVoucher()
                .withWalletId(wallet.getId())
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false);
        String code = voucher.getCode();
        String destinationAddress = RandomUtils.randomString();

        when(voucherRepository.findByCode(eq(code))).thenReturn(Optional.of(voucher));
        when(walletService.findById(eq(wallet.getId()))).thenReturn(Optional.of(wallet));
        when(currencyService.sendMoney(eq(wallet), eq(destinationAddress), eq(voucher.getAmount()))).thenThrow(new IllegalOperationException());

        subject.redeemVoucher(new RedemptionRequest(destinationAddress, code));
    }

    @Test
    public void redeemVoucher_happyEnding() throws VoucherNotFoundException {
        Wallet wallet = RandomUtils.randomWallet(UnitTestParams.get());
        Voucher voucher = RandomUtils.randomVoucher()
                .withWalletId(wallet.getId())
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false);
        String code = voucher.getCode();
        String destinationAddress = RandomUtils.randomString();

        when(voucherRepository.findByCode(eq(code))).thenReturn(Optional.of(voucher));
        when(walletService.findById(eq(wallet.getId()))).thenReturn(Optional.of(wallet));
        when(currencyService.sendMoney(eq(wallet), eq(destinationAddress), eq(voucher.getAmount()))).thenReturn(RandomUtils.randomString());

        subject.redeemVoucher(new RedemptionRequest(destinationAddress, code));

        verify(currencyService, times(1)).sendMoney(eq(wallet), eq(destinationAddress), eq(voucher.getAmount()));
        verify(voucherRepository, times(1)).save(eq(voucher.withRedeemed(true)));
    }

    @Test
    public void redeemVoucher_requestingCorrectRedemptionCurrency() throws VoucherNotFoundException {
        Wallet wallet = RandomUtils.randomWallet(UnitTestParams.get());
        Voucher voucher = RandomUtils.randomVoucher()
                .withWalletId(wallet.getId())
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false);
        String code = voucher.getCode();
        String destinationAddress = RandomUtils.randomString();

        when(voucherRepository.findByCode(eq(code))).thenReturn(Optional.of(voucher));
        when(walletService.findById(eq(wallet.getId()))).thenReturn(Optional.of(wallet));
        when(currencyService.sendMoney(eq(wallet), eq(destinationAddress), eq(voucher.getAmount()))).thenReturn(RandomUtils.randomString());

        subject.redeemVoucher(new RedemptionRequest(destinationAddress, code));

        verify(currencyService, times(1)).sendMoney(eq(wallet), eq(destinationAddress), eq(voucher.getAmount()));
        verify(voucherRepository, times(1)).save(eq(voucher.withRedeemed(true)));
    }

    @Test
    public void publishVouchersBySku_noVoucherFound() {
        String sku = RandomUtils.randomSku();
        when(voucherRepository.findByPublishedFalseAndSku(eq(sku))).thenReturn(emptyList());

        subject.publishBySku(sku);

        verify(voucherRepository, never()).saveAll(any());
    }

    @Test
    public void publishVouchersBySku_shouldSucceedForMultipleVouchers() {
        int counter = nextInt(10, 20);

        String sku = RandomUtils.randomSku();
        List<Voucher> vouchers = RandomUtils.randomVouchers(counter).stream()
                .map(voucher -> voucher
                        .withSku(sku)
                        .withPublished(false)
                )
                .collect(toList());

        when(voucherRepository.findByPublishedFalseAndSku(eq(sku))).thenReturn(vouchers);

        subject.publishBySku(sku);

        verify(voucherRepository, times(1)).saveAll(captor.capture());

        Set<Voucher> expected = vouchers.stream()
                .map(voucher -> voucher.withPublished(true))
                .collect(toSet());

        Set<Voucher> actual = new HashSet<>(captor.getValue());

        assertEquals(expected, actual);
    }

    @Test
    public void unPublishVouchersBySku_noSku() {
        String sku = RandomUtils.randomSku();
        when(voucherRepository.findByPublishedTrueAndSoldFalseAndSku(eq(sku))).thenReturn(emptyList());

        subject.unPublishBySku(sku);

        verify(voucherRepository, never()).saveAll(captor.capture());
    }

    @Captor
    private ArgumentCaptor<List<Voucher>> captor;

    @Test
    public void unPublishVouchersBySku_shouldSucceedForMultipleVouchers() {
        int counter = nextInt(10, 20);

        String sku = RandomUtils.randomSku();
        List<Voucher> vouchers = RandomUtils.randomVouchers(counter).stream()
                .map(voucher -> voucher
                        .withSku(sku)
                        .withPublished(true)
                )
                .collect(toList());

        when(voucherRepository.findByPublishedTrueAndSoldFalseAndSku(eq(sku))).thenReturn(vouchers);

        subject.unPublishBySku(sku);

        verify(voucherRepository, times(1)).saveAll(captor.capture());

        Set<Voucher> expected = vouchers.stream()
                .map(voucher -> voucher.withPublished(false))
                .collect(toSet());
        Set<Voucher> actual = new HashSet<>(captor.getValue());

        assertEquals(expected, actual);
    }

    @Test
    public void voucherAmountMustBeGreaterThanDust() {
        Wallet wallet = RandomUtils.randomWallet().withId(1L);
        VoucherGenerationSpec spec = RandomUtils.randomVoucherGenerationSpec()
                .withWalletId(wallet.getId())
                .withCount(1)
                .withTotalAmount(546);

        when(walletService.findById(wallet.getId())).thenReturn(Optional.of(wallet));
        Throwable t = expectingException(() -> subject.generateVouchers(spec));

        assertNotNull(t);
        assertEquals(IllegalOperationException.class, t.getClass());
        Assert.assertEquals("Voucher value is too low. Must be greater than " + DefaultVoucherService.DUST_AMOUNT, t.getMessage());
    }

    @Test
    public void findByCode() {
        Voucher voucher = RandomUtils.randomVoucher();

        subject.findByCode(voucher.getCode());

        verify(voucherRepository, times(1)).findByCode(eq(voucher.getCode()));
    }
}