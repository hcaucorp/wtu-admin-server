package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.crypto.CurrencyService;
import com.jvmp.vouchershop.crypto.CurrencyServiceSupplier;
import com.jvmp.vouchershop.crypto.bch.BitcoinCashJAdapter;
import com.jvmp.vouchershop.crypto.bch.BitcoinCashService;
import com.jvmp.vouchershop.crypto.btc.BitcoinJAdapter;
import com.jvmp.vouchershop.crypto.btc.BitcoinJConfig;
import com.jvmp.vouchershop.crypto.btc.BitcoinService;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.security.Auth0Service;
import com.jvmp.vouchershop.system.DatabaseConfig;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.VoucherInfoResponse;
import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;
import com.jvmp.vouchershop.voucher.impl.RedemptionResponse;
import com.jvmp.vouchershop.voucher.impl.VoucherGenerationDetails;
import com.jvmp.vouchershop.wallet.ImportWalletRequest;
import com.jvmp.vouchershop.wallet.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.junit.*;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static com.jvmp.Collections.asSet;
import static com.jvmp.vouchershop.crypto.bch.BitcoinCashService.BCH;
import static com.jvmp.vouchershop.crypto.btc.BitcoinService.BTC;
import static com.jvmp.vouchershop.utils.RandomUtils.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
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

    private static final Set<AutoCloseable> closeUs = new HashSet<>();
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
    private CurrencyServiceSupplier currencyServiceSupplier;
    @Autowired
    private NetworkParameters networkParameters;

    @Autowired
    private Auth0Service auth0Service;

    @Autowired
    private BitcoinJAdapter bitcoinJAdapter;

    @Autowired
    private BitcoinCashJAdapter bitcoinCashJAdapter;

    private List<Voucher> testVouchers;

    private String authorizationValue;

    @AfterClass
    public static void tearDownClass() throws Exception {
        for (AutoCloseable autoCloseable : closeUs) {
            autoCloseable.close();
        }
    }

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

    @After
    public void tearDown() {
        walletRepository.deleteAll();
        voucherRepository.deleteAll();
        closeUs.add(bitcoinJAdapter);
        closeUs.add(bitcoinCashJAdapter);
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

        List<Voucher> body = response.getBody();
        assertNotNull(body);

        Set<Voucher> expected = new HashSet<>(testVouchers), actual = new HashSet<>(body);

        assertEquals(expected, actual);
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
    public void redeemBtcVoucher() {
        redeemVoucher(BTC, "mqTZ5Lmt1rrgFPeGeTC8DFExAxV1UK852G");
    }

    @Test
    public void redeemBchVoucher_toLegacyDestinationAddress() {
        redeemVoucher(BCH, "mqTZ5Lmt1rrgFPeGeTC8DFExAxV1UK852G");
    }

    @Test
    @Ignore
    public void redeemMultipleVouchersUsingOneCoinInTheSameBlock_BTC() {
        redeemMultipleVouchersUsingOneCoinInTheSameBlock(BTC, "mqTZ5Lmt1rrgFPeGeTC8DFExAxV1UK852G");
    }

    @Test
    @Ignore
    public void redeemMultipleVouchersUsingOneCoinInTheSameBlock_BCH() {
        redeemMultipleVouchersUsingOneCoinInTheSameBlock(BCH, "mqTZ5Lmt1rrgFPeGeTC8DFExAxV1UK852G");
    }

    public void redeemMultipleVouchersUsingOneCoinInTheSameBlock(String currency, String destinationAddress) {
        CurrencyService currencyService = startCurrencyService(currency);

        // receive address: myAUke4cumJb6fYvHAGvXVMzHbKTusrixG
        // in cash address: bchtest:qrqe9azt6mdt3r04sct7l7mm2d3znp7daqlzv4zrfp
        Wallet wallet = currencyService.importWallet(new ImportWalletRequest(
                currency,
                "defense rain auction twelve arrest guitar coast oval piano crack tattoo ordinary",
                1546128000L));

        List<Voucher> vouchers = IntStream.range(0, 10).mapToObj(i -> voucherRepository.save(randomVoucher()
                .withWalletId(wallet.getId())
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false)))
                .collect(toList());

        for (int i = 0; i < vouchers.size(); i++) {

            Voucher voucher = vouchers.get(i);
            ResponseEntity<RedemptionResponse> responseEntity = requestRedemption(voucher, destinationAddress);
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

            RedemptionResponse response = responseEntity.getBody();
            assertNotNull(response);
            assertNotNull(response.getTransactionId());
            assertFalse(response.getTrackingUrls().isEmpty());

            Optional<Voucher> byId = voucherRepository.findById(voucher.getId());
            assertTrue(byId.isPresent());
            assertTrue(byId.get().isRedeemed());

            log.info("Redeemed transaction no. {} of {}", i, vouchers.size());
        }
    }

    private CurrencyService startCurrencyService(String currency) {
        CurrencyService currencyService = currencyServiceSupplier.findByCurrency(currency);

        if (currencyService instanceof BitcoinService)
            ((BitcoinService) currencyService).start();

        if (currencyService instanceof BitcoinCashService)
            ((BitcoinCashService) currencyService).start();

        log.debug("Service should be running now.");

        return currencyService;
    }

    private ResponseEntity<RedemptionResponse> requestRedemption(Voucher voucher, String destinationAddress) {
        String url = base.toString() + "/vouchers/redeem";
        RequestEntity<?> requestEntity = RequestEntity
                .post(URI.create(url))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .body(new RedemptionRequest()
                        .withVoucherCode(voucher.getCode())
                        .withDestinationAddress(destinationAddress));

        return template.postForEntity(url, requestEntity, RedemptionResponse.class);
    }

    public void redeemVoucher(String currency, String destinationAddress) {
        CurrencyService currencyService = startCurrencyService(currency);

        // receive address: myAUke4cumJb6fYvHAGvXVMzHbKTusrixG
        // for bch: bchtest:qrqe9azt6mdt3r04sct7l7mm2d3znp7daqlzv4zrfp
        Wallet wallet = currencyService.importWallet(new ImportWalletRequest(
                currency,
                "defense rain auction twelve arrest guitar coast oval piano crack tattoo ordinary",
                1546128000L));

        Voucher voucher = voucherRepository.save(randomVoucher()
                .withWalletId(wallet.getId())
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false));

        ResponseEntity<RedemptionResponse> responseEntity = requestRedemption(voucher, destinationAddress);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        RedemptionResponse response = responseEntity.getBody();
        assertNotNull(response);
        assertNotNull(response.getTransactionId());
        assertFalse(response.getTrackingUrls().isEmpty());

        Optional<Voucher> byId = voucherRepository.findById(voucher.getId());
        assertTrue(byId.isPresent());
        assertTrue(byId.get().isRedeemed());
    }

    @Test
    public void publishVouchersBySku() {
        String sku = randomSku();
        voucherRepository.deleteAll();
        Voucher published = randomVoucher().withSku(sku).withPublished(true),
                unpublished = randomVoucher().withSku(sku).withPublished(false);
        testVouchers = voucherRepository.saveAll(asSet(published, unpublished));

        String url = base.toString() + format("/vouchers/%s/publish", sku);
        RequestEntity<?> requestEntity = RequestEntity
                .post(URI.create(url))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .build();

        ResponseEntity<String> response =
                template.postForEntity(url, requestEntity, String.class);

        Set<Voucher> expected = testVouchers.stream()
                .map(voucher -> voucher.withPublished(true))
                .collect(toSet());
        Set<Voucher> actual = new HashSet<>(voucherRepository.findAll());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, actual);
    }

    @Test
    public void unpublishVouchersBySku() {
        String sku = randomSku();
        voucherRepository.deleteAll();
        Voucher published = randomVoucher().withSku(sku).withPublished(true),
                unpublished = randomVoucher().withSku(sku).withPublished(false);
        testVouchers = voucherRepository.saveAll(asSet(published, unpublished));

        String url = base.toString() + format("/vouchers/%s/unpublish", sku);
        RequestEntity<?> requestEntity = RequestEntity
                .post(URI.create(url))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .build();

        ResponseEntity<String> response =
                template.postForEntity(url, requestEntity, String.class);

        Set<Voucher> expected = testVouchers.stream()
                .map(voucher -> voucher.withPublished(false))
                .collect(toSet());
        Set<Voucher> actual = new HashSet<>(voucherRepository.findAll());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, actual);
    }

    @Test
    public void voucherInfoByCodeShouldBePublic() {
        Voucher voucher = voucherRepository.save(randomValidVoucher());

        String url = base.toString() + "/vouchers/" + voucher.getCode();

        ResponseEntity<VoucherInfoResponse> response = template.getForEntity(url, VoucherInfoResponse.class);

        VoucherInfoResponse expected = VoucherInfoResponse.from(voucher);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    private static class VoucherList extends ParameterizedTypeReference<List<Voucher>> {
        //
    }
}
