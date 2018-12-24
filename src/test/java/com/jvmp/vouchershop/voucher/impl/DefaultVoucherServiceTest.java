package com.jvmp.vouchershop.voucher.impl;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static com.jvmp.vouchershop.RandomUtils.randomWallet;
import static com.jvmp.vouchershop.voucher.VoucherRandomUtils.voucher;
import static com.jvmp.vouchershop.voucher.VoucherRandomUtils.voucherGenerationSpec;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        VoucherGenerationDetails spec = voucherGenerationSpec()
                .withCount(10)
                .withTotalAmount(99);

        subject.generateVouchers(spec);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void generateVouchersButWalletDoesntExist() {
        VoucherGenerationDetails spec = voucherGenerationSpec();

        subject.generateVouchers(spec);
    }

    @Test
    public void generateVouchers() {
        Wallet wallet = randomWallet().withId(1L);
        VoucherGenerationDetails spec = voucherGenerationSpec().withWalletId(wallet.getId());
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
        Voucher publishedVoucher = voucher().withPublished(true);
        when(voucherRepository.findById(any())).thenReturn(Optional.of(publishedVoucher));

        subject.delete(1);
    }

    @Test
    public void deleteVoucherById() {
        Voucher voucher = voucher();
        when(voucherRepository.findById(any())).thenReturn(Optional.of(voucher));

        subject.delete(voucher.getId());

        verify(voucherRepository, times(1)).delete(eq(voucher));
    }

    @Test
    public void redeemVoucher() {
        fail("not implemented");
    }
}