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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static com.jvmp.vouchershop.utils.RandomUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
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
        fail();
    }

    @Test
    public void publishVouchersBySku_shouldSuceedForMultipleVouchers() {
        fail();
    }

    @Test
    public void unPublishVouchersBySku_noSku() {
        fail();
    }


    @Test
    public void unPublishVouchersBySku_shouldSucceedForMultipleVouchers() {
        fail();
    }

    @Test
    public void unPublishVouchersBySku_shouldNotUnpublishSoldVouchers() {
        fail();
    }
}