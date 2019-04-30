package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.crypto.btc.BitcoinJAdapter;
import com.jvmp.vouchershop.crypto.btc.BitcoinJConfig;
import com.jvmp.vouchershop.crypto.btc.BitcoinService;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.security.Auth0Service;
import com.jvmp.vouchershop.system.DatabaseConfig;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;
import com.jvmp.vouchershop.voucher.impl.RedemptionResponse;
import com.jvmp.vouchershop.voucher.impl.VoucherGenerationDetails;
import com.jvmp.vouchershop.wallet.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static com.jvmp.vouchershop.utils.RandomUtils.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                Application.class,
                Auth0Service.class,
                BitcoinJConfig.class,
                DatabaseConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MockBeans({
        @MockBean(NotificationService.class)
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

    @Autowired
    private BitcoinService btcWalletService;

    @Autowired
    private NetworkParameters networkParameters;

    @Autowired
    private Auth0Service auth0Service;

    private static BitcoinJAdapter closeMe;

    private List<Voucher> testVouchers;

    private String authorizationValue;

    @Before
    public void setUpTest() throws Exception {
        base = new URL("http://localhost:" + port + "/api");
        testVouchers = asList(
                voucherRepository.save(randomVoucher()),
                voucherRepository.save(randomVoucher())
        );
        Context.propagate(new Context(networkParameters));
        authorizationValue = "Bearer " + auth0Service.getToken().accessToken;
    }

    @Autowired
    private BitcoinJAdapter bitcoinJAdapter;

    @AfterClass
    public static void tearDownClass() {
        closeMe.close();
    }

    @After
    public void tearDown() {
        walletRepository.deleteAll();
        voucherRepository.deleteAll();
        closeMe = bitcoinJAdapter;
    }

    @Test
    public void getAllVouchers() {
        testVouchers.forEach(voucher -> assertTrue(voucherRepository.findById(voucher.getId()).isPresent()));
        RequestEntity<?> requestEntity = RequestEntity
                .get(URI.create(base.toString() + "/vouchers"))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .build();
        ResponseEntity<List<Voucher>> response = template
                .exchange(base.toString() + "/vouchers", HttpMethod.GET, requestEntity, new VoucherList());
        assertEquals(testVouchers, response.getBody());
    }

    @Test
    public void deleteVoucherBySku() {
        String sku = randomSku();
        testVouchers = asList(
                voucherRepository.save(randomVoucher().withSku(sku)),
                voucherRepository.save(randomVoucher().withSku(sku))
        );

        testVouchers.forEach(voucher -> assertTrue(voucherRepository.findById(voucher.getId()).isPresent()));

        String url = base.toString() + "/vouchers/" + sku;

        RequestEntity<Void> requestEntity = RequestEntity
                .delete(URI.create(url))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .build();

        ResponseEntity<String> responseEntity = template.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        testVouchers.forEach(voucher -> assertFalse(voucherRepository.findById(voucher.getId()).isPresent()));
    }

    @Test
    public void generateVouchers() {
        Wallet wallet = walletRepository.save(randomWallet(networkParameters));

        String url = base.toString() + "/vouchers";

        RequestEntity<VoucherGenerationDetails> requestEntity = RequestEntity
                .post(URI.create(url))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .body(randomVoucherGenerationSpec()
                        .withWalletId(wallet.getId())
                        .withSku(randomString()));

        ResponseEntity<String> response = template.postForEntity(url, requestEntity, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void redeemVoucher() throws UnreadableWalletException {
        // receive address: myAUke4cumJb6fYvHAGvXVMzHbKTusrixG
        Wallet wallet = btcWalletService.importWallet(
                "defense rain auction twelve arrest guitar coast oval piano crack tattoo ordinary", 1546128000L)
                .orElseThrow(IllegalOperationException::new);

        btcWalletService.findAll(); // to start the service

        log.info("Service should be running now.");

        Voucher voucher = voucherRepository.save(randomVoucher()
                .withWalletId(wallet.getId())
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false));

        String url = base.toString() + "/vouchers/redeem";

        RequestEntity<?> requestEntity = RequestEntity
                .post(URI.create(url))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .body(new RedemptionRequest()
                        .withVoucherCode(voucher.getCode())
                        .withDestinationAddress("mqTZ5Lmt1rrgFPeGeTC8DFExAxV1UK852G"));

        RedemptionResponse response =
                template.postForEntity(url, requestEntity, RedemptionResponse.class).getBody();

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
