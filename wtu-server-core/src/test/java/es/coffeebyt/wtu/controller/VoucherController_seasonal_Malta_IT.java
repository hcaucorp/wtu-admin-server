package es.coffeebyt.wtu.controller;

import static es.coffeebyt.wtu.crypto.bch.BitcoinCashService.BCH;
import static es.coffeebyt.wtu.voucher.listeners.MaltaPromotion.MALTA_VOUCHER_SKU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import es.coffeebyt.wtu.Application;
import es.coffeebyt.wtu.api.ApiError;
import es.coffeebyt.wtu.crypto.CurrencyService;
import es.coffeebyt.wtu.crypto.CurrencyServiceSupplier;
import es.coffeebyt.wtu.crypto.bch.BitcoinCashJFacade;
import es.coffeebyt.wtu.crypto.btc.BitcoinJConfig;
import es.coffeebyt.wtu.crypto.btc.BitcoinJFacade;
import es.coffeebyt.wtu.notifications.NotificationService;
import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.repository.WalletRepository;
import es.coffeebyt.wtu.security.Auth0Service;
import es.coffeebyt.wtu.system.DatabaseConfig;
import es.coffeebyt.wtu.utils.RandomUtils;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;
import es.coffeebyt.wtu.voucher.listeners.MaltaPromotion;
import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import lombok.extern.slf4j.Slf4j;

@Ignore // temporarily, remove this test later
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
public class VoucherController_seasonal_Malta_IT {


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
    private BitcoinJFacade bitcoinJFacade;

    @Autowired
    private BitcoinCashJFacade bitcoinCashJFacade;

    @Autowired
    private MaltaPromotion maltaPromotion;

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
        Context.propagate(new Context(networkParameters));
        authorizationValue = "Bearer " + auth0Service.getToken().accessToken;
    }

    @After
    public void tearDown() {
        walletRepository.deleteAll();
        voucherRepository.deleteAll();
    }

    @Test
    public void attemptToRedeemAnotherVoucherToTheSameAddress() {
        String currency = BCH;
        String destinationAddress = "mqTZ5Lmt1rrgFPeGeTC8DFExAxV1UK852G";
        CurrencyService currencyService = currencyServiceSupplier.findByCurrency(currency);

        // receive address: myAUke4cumJb6fYvHAGvXVMzHbKTusrixG
        // for bch: bchtest:qrqe9azt6mdt3r04sct7l7mm2d3znp7daqlzv4zrfp
        Wallet wallet = currencyService.importWallet(new ImportWalletRequest(
                currency,
                "defense rain auction twelve arrest guitar coast oval piano crack tattoo ordinary",
                Instant.now().getEpochSecond() // for this test we don't need balance visible, so don't download the chain
        ));

        Voucher voucher = voucherRepository.save(RandomUtils.randomVoucher()
                .withSku(MALTA_VOUCHER_SKU)
                .withWalletId(wallet.getId())
                .withSold(true)
                .withPublished(true)
                .withRedeemed(false));

        //this should prevent consecutive redemptions to destinationAddress
        maltaPromotion.cachePut(destinationAddress);

        ResponseEntity<ApiError> responseEntity = requestRedemption(voucher, destinationAddress);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        ApiError response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertTrue(response.getMessage().startsWith("You've already used one voucher"));

        Optional<Voucher> byId = voucherRepository.findById(voucher.getId());
        assertTrue(byId.isPresent());
        assertFalse(byId.get().isRedeemed());
    }

    private ResponseEntity<ApiError> requestRedemption(Voucher voucher, String destinationAddress) {
        String url = base.toString() + "/vouchers/redeem";
        RequestEntity<?> requestEntity = RequestEntity
                .post(URI.create(url))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .body(new RedemptionRequest()
                        .withVoucherCode(voucher.getCode())
                        .withDestinationAddress(destinationAddress));

        return template.postForEntity(url, requestEntity, ApiError.class);
    }

}