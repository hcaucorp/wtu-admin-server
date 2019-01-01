package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.crypto.btc.BitcoinJConfig;
import com.jvmp.vouchershop.crypto.btc.WalletServiceBtc;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.system.DatabaseConfig;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;
import com.jvmp.vouchershop.voucher.impl.RedemptionResponse;
import com.jvmp.vouchershop.wallet.Wallet;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.junit.Before;
import org.junit.Ignore;
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

import static com.jvmp.vouchershop.RandomUtils.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {DatabaseConfig.class, BitcoinJConfig.class})
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

    @Autowired
    private WalletServiceBtc btcWalletService;

    @Autowired
    private NetworkParameters networkParameters;

    private List<Voucher> testVouchers;

    @Before
    public void setUp() throws Exception {
        base = new URL("http://localhost:" + port + "/");
        testVouchers = asList(
                voucherRepository.save(randomVoucher()),
                voucherRepository.save(randomVoucher())
        );
        Context.propagate(new Context(networkParameters));
    }

    @Test
    public void getAllVouchers() {
        testVouchers.forEach(voucher -> assertTrue(voucherRepository.findById(voucher.getId()).isPresent()));
        ResponseEntity<List<Voucher>> response = template
                .withBasicAuth(ControllerUtils.USER_NAME, ControllerUtils.USER_PASS)
                .exchange(base.toString() + "/vouchers", HttpMethod.GET, null, new VoucherList());
        assertEquals(testVouchers, response.getBody());
    }

    @Test
    public void deleteVoucherById() {
        testVouchers.stream().map(Voucher::getId).forEach(id -> {
            assertTrue(voucherRepository.findById(id).isPresent());
            template
                    .withBasicAuth(ControllerUtils.USER_NAME, ControllerUtils.USER_PASS)
                    .delete(base.toString() + "/vouchers/" + id);
            assertFalse(voucherRepository.findById(id).isPresent());
        });
    }

    @Test
    public void generateVouchers() {
        Wallet wallet = walletRepository.save(randomWallet());

        URI location = template
                .withBasicAuth(ControllerUtils.USER_NAME, ControllerUtils.USER_PASS)
                .postForLocation(base.toString() + "/vouchers",
                        randomVoucherGenerationSpec()
                                .withWalletId(wallet.getId())
                                .withSku(randomString()),
                        String.class
                );

        assertNotNull(location);

        walletRepository.delete(wallet);
    }

    @Test
    @Ignore("can't make this retarded library work")
    public void redeemVoucher() throws UnreadableWalletException {
        // receive address: myAUke4cumJb6fYvHAGvXVMzHbKTusrixG
        Wallet wallet = btcWalletService.importWallet(
                "defense rain auction twelve arrest guitar coast oval piano crack tattoo ordinary", 1546105372517L);

        Voucher voucher = voucherRepository.save(randomVoucher()
                .withWalletId(wallet.getId())
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false));

        RedemptionResponse response = template
                .withBasicAuth(ControllerUtils.USER_NAME, ControllerUtils.USER_PASS)
                .postForEntity("/vouchers/redeem", new RedemptionRequest()
                                .withVoucherCode(voucher.getCode())
                                .withDestinationAddress("mqTZ5Lmt1rrgFPeGeTC8DFExAxV1UK852G"),
                        RedemptionResponse.class)
                .getBody();

        assertNotNull(response);
        assertNotNull(response.getTransactionId());
        assertFalse(response.getTrackingUrls().isEmpty());

        Optional<Voucher> byId = voucherRepository.findById(voucher.getId());

        assertTrue(byId.isPresent());
        assertTrue(byId.get().isRedeemed());
    }

    private static class VoucherList extends ParameterizedTypeReference<List<Voucher>> {
        //
    }
}
