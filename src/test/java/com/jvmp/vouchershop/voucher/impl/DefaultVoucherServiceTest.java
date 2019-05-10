package com.jvmp.vouchershop.voucher.impl;

import com.jvmp.vouchershop.crypto.CurrencyService;
import com.jvmp.vouchershop.crypto.CurrencyServiceSupplier;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.VoucherCodeGenerator;
import com.jvmp.vouchershop.voucher.VoucherNotFoundException;
import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import org.bitcoinj.core.Context;
import org.bitcoinj.params.UnitTestParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.jvmp.vouchershop.utils.RandomUtils.*;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.Assert.assertEquals;
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
    private CurrencyService currencyService;

    @Mock
    private VoucherCodeGenerator voucherCodeGenerator;

    private DefaultVoucherService subject;

    @Before
    public void setUp() {
        Context btcContext = new Context(UnitTestParams.get());
        Context.propagate(btcContext);
        when(currencyServiceSupplier.findByCurrency(any())).thenReturn(currencyService);
        subject = new DefaultVoucherService(voucherCodeGenerator, walletService, voucherRepository, currencyServiceSupplier);
    }

    @Test(expected = IllegalOperationException.class)
    public void generateVouchersWithIndivisibleInput() {
        VoucherGenerationDetails spec = randomVoucherGenerationSpec()
                .withCount(10)
                .withTotalAmount(99);

        subject.generateVouchers(spec);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void generateVouchersButWalletDoesntExist() {
        VoucherGenerationDetails spec = randomVoucherGenerationSpec();

        subject.generateVouchers(spec);
    }

    @Test
    public void generateVouchers() {
        Wallet wallet = randomWallet().withId(1L);
        VoucherGenerationDetails spec = randomVoucherGenerationSpec().withWalletId(wallet.getId());
        when(walletService.findById(wallet.getId())).thenReturn(Optional.of(wallet));

        List<Voucher> vouchers = subject.generateVouchers(spec);

        assertEquals(spec.getCount(), vouchers.size());
    }

    @Test(expected = VoucherNotFoundException.class)
    public void redeemVoucher_noVoucher() throws VoucherNotFoundException {
        String code = randomString();

        when(voucherRepository.findByCode(eq(code))).thenReturn(Optional.empty());

        subject.redeemVoucher(new RedemptionRequest(randomString(), code));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void redeemVoucher_noWallet() throws VoucherNotFoundException {
        Voucher voucher = randomVoucher()
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false);
        String code = voucher.getCode();
        String destinationAddress = randomString();

        when(voucherRepository.findByCode(eq(code))).thenReturn(Optional.of(voucher));

        subject.redeemVoucher(new RedemptionRequest(destinationAddress, code));
    }

    @Test(expected = IllegalOperationException.class)
    public void redeemVoucher_notEnoughMoney() {
        Wallet wallet = randomWallet(UnitTestParams.get());
        Voucher voucher = randomVoucher()
                .withWalletId(wallet.getId())
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false);
        String code = voucher.getCode();
        String destinationAddress = randomString();

        when(voucherRepository.findByCode(eq(code))).thenReturn(Optional.of(voucher));
        when(walletService.findById(eq(wallet.getId()))).thenReturn(Optional.of(wallet));
        when(currencyService.sendMoney(eq(wallet), eq(destinationAddress), eq(voucher.getAmount()))).thenThrow(new IllegalOperationException());

        subject.redeemVoucher(new RedemptionRequest(destinationAddress, code));
    }

    @Test
    public void redeemVoucher_happyEnding() throws VoucherNotFoundException {
        Wallet wallet = randomWallet(UnitTestParams.get());
        Voucher voucher = randomVoucher()
                .withWalletId(wallet.getId())
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false);
        String code = voucher.getCode();
        String destinationAddress = randomString();

        when(voucherRepository.findByCode(eq(code))).thenReturn(Optional.of(voucher));
        when(walletService.findById(eq(wallet.getId()))).thenReturn(Optional.of(wallet));
        when(currencyService.sendMoney(eq(wallet), eq(destinationAddress), eq(voucher.getAmount()))).thenReturn(randomString());

        subject.redeemVoucher(new RedemptionRequest(destinationAddress, code));

        verify(currencyService, times(1)).sendMoney(eq(wallet), eq(destinationAddress), eq(voucher.getAmount()));
        verify(voucherRepository, times(1)).save(eq(voucher.withRedeemed(true)));
    }

    @Test
    public void refund_happyEnding() {
        Voucher voucher = randomVoucher()
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false);

        when(voucherRepository.findByCode(eq(voucher.getCode()))).thenReturn(Optional.of(voucher));

        subject.refund(voucher.getCode());
    }

    @Test(expected = VoucherNotFoundException.class)
    public void refund_NotFound() {
        Voucher voucher = randomVoucher();
        when(voucherRepository.findByCode(any())).thenReturn(Optional.empty());

        subject.refund(voucher.getCode());
    }

    @Test(expected = IllegalOperationException.class)
    public void refund_NotRefundable() {
        Voucher voucher = randomVoucher()
                .withRedeemed(true);

        when(voucherRepository.findByCode(eq(voucher.getCode()))).thenReturn(Optional.of(voucher));

        subject.refund(voucher.getCode());
    }

    @Test
    public void redeemVoucher_requestingCorrectRedemptionCurrency() throws VoucherNotFoundException {
        Wallet wallet = randomWallet(UnitTestParams.get());
        Voucher voucher = randomVoucher()
                .withWalletId(wallet.getId())
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false);
        String code = voucher.getCode();
        String destinationAddress = randomString();

        when(voucherRepository.findByCode(eq(code))).thenReturn(Optional.of(voucher));
        when(walletService.findById(eq(wallet.getId()))).thenReturn(Optional.of(wallet));
        when(currencyService.sendMoney(eq(wallet), eq(destinationAddress), eq(voucher.getAmount()))).thenReturn(randomString());

        subject.redeemVoucher(new RedemptionRequest(destinationAddress, code));

        verify(currencyService, times(1)).sendMoney(eq(wallet), eq(destinationAddress), eq(voucher.getAmount()));
        verify(voucherRepository, times(1)).save(eq(voucher.withRedeemed(true)));
    }

    @Test
    public void publishVouchersBySku_noVoucherFound() {
        String sku = randomSku();
        when(voucherRepository.findByPublishedFalseAndSku(eq(sku))).thenReturn(emptyList());

        subject.publishBySku(sku);

        verify(voucherRepository, never()).saveAll(any());
    }

    @Test
    public void publishVouchersBySku_shouldSucceedForMultipleVouchers() {
        int counter = nextInt(10, 20);

        String sku = randomSku();
        List<Voucher> vouchers = randomVouchers(counter).stream()
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
        String sku = randomSku();
        when(voucherRepository.findByPublishedTrueAndSku(eq(sku))).thenReturn(emptyList());

        subject.unPublishBySku(sku);

        verify(voucherRepository, never()).saveAll(captor.capture());
    }

    @Captor
    private ArgumentCaptor<List<Voucher>> captor;

    @Test
    public void unPublishVouchersBySku_shouldSucceedForMultipleVouchers() {
        int counter = nextInt(10, 20);

        String sku = randomSku();
        List<Voucher> vouchers = randomVouchers(counter).stream()
                .map(voucher -> voucher
                        .withSku(sku)
                        .withPublished(true)
                )
                .collect(toList());

        when(voucherRepository.findByPublishedTrueAndSku(eq(sku))).thenReturn(vouchers);

        subject.unPublishBySku(sku);

        verify(voucherRepository, times(1)).saveAll(captor.capture());

        Set<Voucher> expected = vouchers.stream()
                .map(voucher -> voucher.withPublished(false))
                .collect(toSet());
        Set<Voucher> actual = new HashSet<>(captor.getValue());

        assertEquals(expected, actual);
    }

    @Test
    public void unPublishVouchersBySku_shouldNotUnPublishSoldVouchers() {

        int allCount = nextInt(10, 20);
        int soldCount = nextInt(1, allCount - 1);

        String sku = randomSku();
        List<Voucher> vouchers = randomVouchers(allCount).stream()
                .map(voucher -> voucher
                        .withSku(sku)
                        .withPublished(true)
                )
                .collect(toList());

        for (int i = 0; i < soldCount; i++) {
            vouchers.get(i).setSold(true);
        }

        when(voucherRepository.findByPublishedTrueAndSku(eq(sku))).thenReturn(vouchers);

        subject.unPublishBySku(sku);

        verify(voucherRepository, times(1)).saveAll(captor.capture());

        Set<Voucher> expected = vouchers.stream()
                .filter(voucher -> !voucher.isSold())
                .map(voucher -> voucher.withPublished(false))
                .collect(toSet());
        Set<Voucher> actual = new HashSet<>(captor.getValue());

        assertEquals(expected, actual);
    }
}