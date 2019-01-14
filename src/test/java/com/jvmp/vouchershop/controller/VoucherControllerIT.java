package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.crypto.btc.BitcoinJConfig;
import com.jvmp.vouchershop.crypto.btc.WalletServiceBtc;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.security.Auth0Service;
import com.jvmp.vouchershop.system.DatabaseConfig;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;
import com.jvmp.vouchershop.voucher.impl.RedemptionResponse;
import com.jvmp.vouchershop.voucher.impl.VoucherGenerationDetails;
import com.jvmp.vouchershop.wallet.Wallet;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static com.jvmp.vouchershop.RandomUtils.randomSku;
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
        classes = {
                Application.class,
                Auth0Service.class
        },
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

    @Autowired
    private Auth0Service auth0Service;

    private List<Voucher> testVouchers;

    private String authorizationValue;

    @Before
    public void setUpTest() throws Exception {
        base = new URL("http://localhost:" + port + "/");
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
//    @Ignore("can't make this retarded library work") // TODO really need this to work properly!
    public void redeemVoucher() throws UnreadableWalletException {
        // receive address: myAUke4cumJb6fYvHAGvXVMzHbKTusrixG
        Wallet wallet = btcWalletService.importWallet(
                "defense rain auction twelve arrest guitar coast oval piano crack tattoo ordinary", 1546128000L)
                .orElseThrow(IllegalOperationException::new);

        Voucher voucher = voucherRepository.save(randomVoucher()
                .withWalletId(wallet.getId())
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false));

        String url = "/vouchers/redeem";

        RequestEntity<?> requestEntity = RequestEntity
                .post(URI.create(url))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .body(new RedemptionRequest()
                        .withVoucherCode(voucher.getCode())
                        .withDestinationAddress("mqTZ5Lmt1rrgFPeGeTC8DFExAxV1UK852G"));

        RedemptionResponse response = template.postForEntity(url, requestEntity, RedemptionResponse.class).getBody();

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

    @Test
    @Ignore("can't make this retarded library work")
    // TODO redemption page will hash request body using a "secret" known only for 'redemption' and this 'server'
    public void redeemVoucher_HmacHashing() {

    }
}
