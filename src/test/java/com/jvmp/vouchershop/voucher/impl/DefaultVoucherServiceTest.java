package com.jvmp.vouchershop.voucher.impl;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import io.reactivex.Observable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static com.jvmp.vouchershop.RandomUtils.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultVoucherServiceTest {

    @Mock
    private WalletService walletService;

    @Mock
    private VoucherRepository voucherRepository;

    private DefaultVoucherService subject;

    @Before
    public void setUp() {
        subject = new DefaultVoucherService(walletService, voucherRepository);
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

    @Test(expected = ResourceNotFoundException.class)
    public void deleteVoucherByIdNotFound() {
        subject.delete(1);
    }

    @Test(expected = IllegalOperationException.class)
    public void deletePublishedVoucher() {
        Voucher publishedVoucher = randomVoucher().withPublished(true);
        when(voucherRepository.findById(any())).thenReturn(Optional.of(publishedVoucher));

        subject.delete(1);
    }

    @Test
    public void deleteVoucherById() {
        Voucher voucher = randomVoucher();
        when(voucherRepository.findById(any())).thenReturn(Optional.of(voucher));

        subject.delete(voucher.getId());

        verify(voucherRepository, times(1)).delete(eq(voucher));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void redeemVoucher_noVoucher() {
        String code = randomString();

        when(voucherRepository.findByCode(eq(code))).thenReturn(Optional.empty());

        subject.redeemVoucher(new VoucherRedemptionDetails(randomString(), code));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void redeemVoucher_noWallet() {
        Wallet wallet = randomWallet();
        Voucher voucher = randomVoucher()
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false);
        String code = voucher.getCode();
        String destinationAddress = randomString();

        when(voucherRepository.findByCode(eq(code))).thenReturn(Optional.of(voucher));

        subject.redeemVoucher(new VoucherRedemptionDetails(destinationAddress, code));
    }

    @Test
    public void redeemVoucher_happyEnding() {
        Wallet wallet = randomWallet();
        Voucher voucher = randomVoucher()
                .withWalletId(wallet.getId())
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false);
        String code = voucher.getCode();
        String destinationAddress = randomString();

        when(voucherRepository.findByCode(eq(code))).thenReturn(Optional.of(voucher));
        when(walletService.findById(eq(wallet.getId()))).thenReturn(Optional.of(wallet));
        when(walletService.sendMoney(eq(wallet), eq(destinationAddress), eq(voucher.getAmount()))).thenReturn(Observable.just(randomString()));

        subject.redeemVoucher(new VoucherRedemptionDetails(destinationAddress, code));

        verify(walletService, times(1)).sendMoney(eq(wallet), eq(destinationAddress), eq(voucher.getAmount()));
        verify(voucherRepository, times(1)).save(eq(voucher.withRedeemed(true)));
    }

    @Test(expected = IllegalOperationException.class)
    public void checkVoucher_expired() {
        DefaultVoucherService.checkVoucher(randomVoucher()
                .withCreatedAt(LocalDateTime.of(2015, 1, 1, 1, 1).toInstant(ZoneOffset.UTC).toEpochMilli())
                .withExpiresAt(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()));
    }

    @Test(expected = IllegalOperationException.class)
    public void checkVoucher_alreadyRedeemed() {
        DefaultVoucherService.checkVoucher(randomVoucher()
                .withPublished(true)
                .withSold(true)
                .withRedeemed(true)
        );
    }

    @Test(expected = IllegalOperationException.class)
    public void checkVoucher_notSoldYet() {
        DefaultVoucherService.checkVoucher(randomVoucher()
                .withPublished(true)
        );
    }

    @Test(expected = IllegalOperationException.class)
    public void checkVoucher_notPublished() {
        DefaultVoucherService.checkVoucher(randomVoucher());
    }

    @Test
    public void isExpired() {
        assertFalse(DefaultVoucherService.isExpired(randomVoucher()
                .withCreatedAt(Instant.now().toEpochMilli())
                .withExpiresAt(LocalDateTime.now().plusYears(1).toInstant(ZoneOffset.UTC).toEpochMilli())));

        assertTrue(DefaultVoucherService.isExpired(randomVoucher()
                .withCreatedAt(LocalDateTime.of(2015, 1, 1, 1, 1).toInstant(ZoneOffset.UTC).toEpochMilli())
                .withExpiresAt(LocalDateTime.now().minusYears(1).toInstant(ZoneOffset.UTC).toEpochMilli())));
    }
}