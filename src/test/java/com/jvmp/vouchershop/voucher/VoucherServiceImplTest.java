package com.jvmp.vouchershop.voucher;

import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.wallet.WalletService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class VoucherServiceImplTest {

    @Mock
    private WalletService walletService;

    @Mock
    private VoucherRepository voucherRepository;

    private VoucherServiceImpl subject;

    @Before
    public void setUp() throws Exception {
        subject = new VoucherServiceImpl(walletService, voucherRepository);
    }

    @Test
    public void generateVouchers() {
        fail("not implemented");
    }

    @Test
    public void findAll() {
        fail("not implemented");
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
        fail("not implemented");
    }

    @Test
    public void saveAll() {
        fail("not implemented");
    }
}