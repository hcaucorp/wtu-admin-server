package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.crypto.btc.BitcoinJConfig;
import com.jvmp.vouchershop.crypto.btc.BitcoinJConfigForTests;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.system.DatabaseConfig;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.impl.VoucherRedemptionDetails;
import com.jvmp.vouchershop.wallet.Wallet;
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

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static com.jvmp.vouchershop.RandomUtils.randomString;
import static com.jvmp.vouchershop.RandomUtils.randomVoucher;
import static com.jvmp.vouchershop.RandomUtils.randomVoucherGenerationSpec;
import static com.jvmp.vouchershop.RandomUtils.randomWallet;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {DatabaseConfig.class, BitcoinJConfig.class, BitcoinJConfigForTests.class})
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

    private List<Voucher> testVouchers;

    @Before
    public void setUp() throws Exception {
        base = new URL("http://localhost:" + port + "/");
        testVouchers = asList(
                voucherRepository.save(randomVoucher()),
                voucherRepository.save(randomVoucher())
        );
    }

    @Test
    public void getAllVouchers() {
        testVouchers.forEach(voucher -> assertTrue(voucherRepository.findById(voucher.getId()).isPresent()));
        ResponseEntity<List<Voucher>> response = template.exchange(base.toString() + "/vouchers", HttpMethod.GET, null, new VoucherList());
        assertEquals(testVouchers, response.getBody());
    }

    @Test
    public void deleteVoucherById() {
        testVouchers.stream().map(Voucher::getId).forEach(id -> {
            assertTrue(voucherRepository.findById(id).isPresent());
            template.delete(base.toString() + "/vouchers/" + id);
            assertFalse(voucherRepository.findById(id).isPresent());
        });
    }

    @Test
    public void generateVouchers() {
        Wallet wallet = walletRepository.save(randomWallet());

        URI location = template.postForLocation(
                base.toString() + "/vouchers",
                randomVoucherGenerationSpec()
                        .withWalletId(wallet.getId())
                        .withSku(randomString()),
                String.class
        );

        assertNotNull(location);
    }

    @Test
    public void redeemVoucher() {
        Wallet wallet = walletRepository.save(randomWallet()
                .withCurrency("BTC"));
        Voucher voucher = voucherRepository.save(randomVoucher()
                .withWalletId(wallet.getId())
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false));

        template.put("/vouchers/redeem", new VoucherRedemptionDetails()
                .withVoucherCode(voucher.getCode())
                .withDestinationAddress("mqTZ5Lmt1rrgFPeGeTC8DFExAxV1UK852G"));

        Optional<Voucher> byId = voucherRepository.findById(voucher.getId());

        assertTrue(byId.isPresent());
        assertTrue(byId.get().isRedeemed());
    }

    private static class VoucherList extends ParameterizedTypeReference<List<Voucher>> {
        //
    }
}
