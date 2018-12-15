package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.domain.Voucher;
import com.jvmp.vouchershop.domain.Wallet;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.repository.WalletRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.List;

import static com.jvmp.vouchershop.voucher.VoucherRandomUtils.voucher;
import static com.jvmp.vouchershop.voucher.VoucherRandomUtils.voucherGenerationSpec;
import static com.jvmp.vouchershop.voucher.WalletRandomUtils.wallet;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {
        VoucherRepository.class, Voucher.class,
        WalletRepository.class, Wallet.class
})
public class VoucherControllerIT {

    @LocalServerPort
    private int port;

    private URL base;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private WalletRepository walletRepository;

    private Voucher testVoucher;

    @Before
    public void setUp() throws Exception {
        base = new URL("http://localhost:" + port + "/");
        testVoucher = voucherRepository.save(voucher());
    }

    @Test
    public void getAllVouchers() {
        assertTrue(voucherRepository.findById(testVoucher.getId()).isPresent());

        ResponseEntity<List> response = template.getForEntity(base.toString() + "/vouchers", List.class);
        assertEquals(singleton(testVoucher), response.getBody());
    }

    @Test
    public void deleteVoucherById() {
        assertTrue(voucherRepository.findById(testVoucher.getId()).isPresent());

        template.delete(base.toString() + "/vouchers/" + testVoucher.getId());

        assertFalse(voucherRepository.findById(testVoucher.getId()).isPresent());
    }

    @Test
    public void generateVouchers() {
        Wallet wallet = walletRepository.save(wallet());

        URI location = template.exchange(
                base.toString() + "/vouchers",
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<List<Voucher>>() {},
                voucherGenerationSpec().withWalletId(wallet.getId()))
                .getHeaders()
                .getLocation();

        assertNotNull(location);
    }
}