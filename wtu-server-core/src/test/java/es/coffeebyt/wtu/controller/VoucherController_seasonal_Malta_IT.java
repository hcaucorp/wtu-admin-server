package es.coffeebyt.wtu.controller;

import static es.coffeebyt.wtu.crypto.bch.BitcoinCashService.BCH;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import es.coffeebyt.wtu.Application;
import es.coffeebyt.wtu.crypto.CurrencyService;
import es.coffeebyt.wtu.crypto.CurrencyServiceSupplier;
import es.coffeebyt.wtu.crypto.bch.BitcoinCashJFacade;
import es.coffeebyt.wtu.crypto.bch.BitcoinCashService;
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
import es.coffeebyt.wtu.voucher.impl.RedemptionResponse;
import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import lombok.extern.slf4j.Slf4j;

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
                voucherRepository.save(RandomUtils.randomVoucher()),
                voucherRepository.save(RandomUtils.randomVoucher())
        );
        Context.propagate(new Context(networkParameters));
        authorizationValue = "Bearer " + auth0Service.getToken().accessToken;
    }

    @After
    public void tearDown() {
        walletRepository.deleteAll();
        voucherRepository.deleteAll();
        closeUs.add(bitcoinJFacade);
        closeUs.add(bitcoinCashJFacade);
    }

    @Test
    public void redeemVoucher() {
        String currency = BCH;
        String destinationAddress = "mqTZ5Lmt1rrgFPeGeTC8DFExAxV1UK852G";
        CurrencyService currencyService = startCurrencyService(currency);

        // receive address: myAUke4cumJb6fYvHAGvXVMzHbKTusrixG
        // for bch: bchtest:qrqe9azt6mdt3r04sct7l7mm2d3znp7daqlzv4zrfp
        Wallet wallet = currencyService.importWallet(new ImportWalletRequest(
                currency,
                "defense rain auction twelve arrest guitar coast oval piano crack tattoo ordinary",
                1546128000L));

        Voucher voucher = voucherRepository.save(RandomUtils.randomVoucher()
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


    private CurrencyService startCurrencyService(String currency) {
        CurrencyService currencyService = currencyServiceSupplier.findByCurrency(currency);

        if (currencyService instanceof BitcoinCashService)
            ((BitcoinCashService) currencyService).start();

        log.debug("Service should be running now.");

        return currencyService;
    }
}
