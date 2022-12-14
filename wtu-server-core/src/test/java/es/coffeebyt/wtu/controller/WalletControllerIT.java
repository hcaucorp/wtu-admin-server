package es.coffeebyt.wtu.controller;

import es.coffeebyt.wtu.Application;
import es.coffeebyt.wtu.crypto.btc.BitcoinJFacade;
import es.coffeebyt.wtu.repository.WalletRepository;
import es.coffeebyt.wtu.security.Auth0Service;
import es.coffeebyt.wtu.utils.RandomUtils;
import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import es.coffeebyt.wtu.wallet.WalletReport;
import es.coffeebyt.wtu.wallet.WalletStatus;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import java.net.URI;
import java.net.URL;
import java.util.List;

import static es.coffeebyt.wtu.crypto.btc.BitcoinService.BTC;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {
                Application.class,
                Auth0Service.class
        }
)
@ActiveProfiles("it")
public class WalletControllerIT {

    @LocalServerPort
    private int port;

    private URL base;

    @Autowired
    private TestRestTemplate template;

    private static BitcoinJFacade closeMe;

    @Autowired
    private NetworkParameters networkParameters;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private Auth0Service auth0Service;

    private String authorizationValue;

    @Before
    public void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port + "/api");
        Context.propagate(new Context(networkParameters));
        authorizationValue = "Bearer " + auth0Service.getToken().accessToken;
    }

    @Autowired
    private BitcoinJFacade bitcoinJFacade;

    @AfterClass
    public static void tearDownClass() {
        closeMe.close();
    }

    @After
    public void tearDown() {
        walletRepository.deleteAll();
        closeMe = bitcoinJFacade;
    }

    @Test
    public void getAllWallets() {
        Wallet testWallet = walletRepository.save(new Wallet()
                .withCurrency(BTC)
                .withMnemonic("defense rain auction twelve arrest guitar coast oval piano crack tattoo ordinary")
                .withCreatedAt(1546128000000L));

        String url = base.toString() + "/wallets";

        RequestEntity<?> requestEntity = RequestEntity
                .get(URI.create(url))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .build();

        ResponseEntity<List<WalletReport>> response = template
                .exchange(url, HttpMethod.GET, requestEntity, new WalletReportList());

        assertEquals(HttpStatus.OK, response.getStatusCode());

        // mnemonic should be empty
        assertEquals(singletonList(new WalletReport(testWallet.withMnemonic(null), 0, WalletStatus.STARTING)), response.getBody());
    }

    @Test
    public void importWallet() {
        String url = base.toString() + "/wallets";
        long createdAt = 1546175793;
        String mnemonic = RandomUtils.randomWallet(networkParameters).getMnemonic();

        RequestEntity<?> requestEntity = RequestEntity
                .put(URI.create(url))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ImportWalletRequest(BTC, mnemonic, createdAt));

        ResponseEntity<String> response = template.exchange(url, HttpMethod.PUT, requestEntity, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNull(response.getBody());

        Wallet w = walletRepository.findAll().get(0);
        assertNotNull(w);
        assertEquals(createdAt * 1_000, w.getCreatedAt());
    }

    @Test
    public void importWallet_shouldFailForUnknownCurrency() {
        String url = base.toString() + "/wallets";
        long createdAt = 1546175793;
        String mnemonic = RandomUtils.randomWallet(networkParameters).getMnemonic();

        RequestEntity<?> requestEntity = RequestEntity
                .put(URI.create(url))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .contentType(MediaType.APPLICATION_JSON)
                // unknown currency code
                .body(new ImportWalletRequest("ZZZ", mnemonic, createdAt));

        ResponseEntity<String> response = template.exchange(url, HttpMethod.PUT, requestEntity, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void getAllWalletsNoAuth() {
        String url = base.toString() + "/wallets";
        RequestEntity<?> requestEntity = RequestEntity
                .get(URI.create(url))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        ResponseEntity<String> noAuth = template
                .exchange(url, HttpMethod.GET, requestEntity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, noAuth.getStatusCode());
    }

    @Test
    public void importWalletNoAuth() {
        String url = base.toString() + "/wallets";
        RequestEntity<?> requestEntity = RequestEntity
                .put(URI.create(url))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(RandomUtils.randomWallet(networkParameters));

        ResponseEntity<String> noAuth = template
                .exchange(url, HttpMethod.PUT, requestEntity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, noAuth.getStatusCode());
    }

    private static class WalletReportList extends ParameterizedTypeReference<List<WalletReport>> {
        //
    }
}
