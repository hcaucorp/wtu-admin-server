package es.coffeebyt.wtu.controller;

import static es.coffeebyt.wtu.crypto.bch.BitcoinCashService.BCH;
import static es.coffeebyt.wtu.voucher.listeners.OnePerCustomerForMaltaPromotion.MALTA_VOUCHER_SKU;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
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
import es.coffeebyt.wtu.security.TestSecurityConfig;
import es.coffeebyt.wtu.system.DatabaseConfig;
import es.coffeebyt.wtu.utils.RandomUtils;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;
import es.coffeebyt.wtu.voucher.listeners.OnePerCustomerForMaltaPromotion;
import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import es.coffeebyt.wtu.wallet.WalletService;
import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        Application.class, TestSecurityConfig.class
})
@AutoConfigureMockMvc
@ActiveProfiles("unit-test")
public class WalletControllerTest {

    private final static String baseUrl = "/api";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WalletRepository walletRepository;

    @MockBean
    private WalletService walletService;

    @MockBean
    private BitcoinJFacade bitcoinJFacade;

    @Autowired
    private NetworkParameters networkParameters;

    @Before
    public void setUp() {
        when(bitcoinJFacade.getBalance()).thenReturn(1L);
    }

    @Test
    public void getAllWallets() throws Exception {
        mvc.perform(get(baseUrl + "/wallets"))
                .andExpect(status().isOk());

        verify(walletService, times(1)).findAll();
    }

    @Test
    public void generateWallet() throws Exception {
        Wallet wallet = RandomUtils.randomWallet(networkParameters)
                .withCurrency("BTC");
        when(walletRepository.save(ArgumentMatchers.any(Wallet.class))).thenReturn(wallet);

        mvc.perform(post(baseUrl + "/wallets")
                .content("BTC"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(empty())))
                .andExpect(jsonPath("$.address", not(empty())))
                .andExpect(jsonPath("$.balance", notNullValue()))
                .andExpect(jsonPath("$.currency", is("BTC")))
                .andExpect(jsonPath("$.createdAt", greaterThan(1322697600000L)))
                .andExpect(jsonPath("$.mnemonic", not(empty())));

        mvc.perform(post(baseUrl + "/wallets")
                .content("ETH"))
                .andExpect(status().isBadRequest());

        mvc.perform(post(baseUrl + "/wallets")
                .content("XYZ"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void importWallet() throws Exception {
        when(walletRepository.findAll()).thenReturn(emptyList());
        when(walletRepository.save(ArgumentMatchers.any(Wallet.class))).thenReturn(new Wallet());

        mvc.perform(put(baseUrl + "/wallets")
                .content(objectMapper.writeValueAsString(new ImportWalletRequest(
                        "BTC",
                        "olive poet gather huge museum jewel cute giant rent canvas mask lift",
                        1546175793)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void importWallet_unsupportedCurrency_shouldFail() throws Exception {
        when(walletRepository.findAll()).thenReturn(emptyList());

        mvc.perform(put(baseUrl + "/wallets")
                .content(objectMapper.writeValueAsString(new ImportWalletRequest(
                        "ZZZ",
                        "olive poet gather huge museum jewel cute giant rent canvas mask lift",
                        1546175793)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

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
    public static class VoucherController_seasonal_Malta_IT {


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
        private OnePerCustomerForMaltaPromotion onePerCustomerForMaltaPromotion;

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
            onePerCustomerForMaltaPromotion.cachePut(destinationAddress);

            ResponseEntity<ApiError> responseEntity = requestRedemption(voucher, destinationAddress);
            Assert.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

            ApiError response = responseEntity.getBody();
            Assert.assertNotNull(response);
            Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
            Assert.assertTrue(response.getMessage().startsWith("You've already used one voucher"));

            Optional<Voucher> byId = voucherRepository.findById(voucher.getId());
            Assert.assertTrue(byId.isPresent());
            Assert.assertFalse(byId.get().isRedeemed());
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
}