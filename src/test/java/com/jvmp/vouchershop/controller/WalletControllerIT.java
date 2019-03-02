package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.crypto.btc.BitcoinJAdapter;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.security.Auth0Service;
import com.jvmp.vouchershop.wallet.Wallet;
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
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.List;

import static com.jvmp.vouchershop.utils.RandomUtils.randomWallet;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
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

    private static BitcoinJAdapter closeMe;

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
    private BitcoinJAdapter bitcoinJAdapter;

    @AfterClass
    public static void tearDownClass() {
        closeMe.close();
    }

    @After
    public void tearDown() {
        walletRepository.deleteAll();
        closeMe = bitcoinJAdapter;
    }

    @Test
    public void getAllWallets() {
        Wallet testWallet = walletRepository.save(randomWallet(networkParameters));

        String url = base.toString() + "/wallets";

        RequestEntity<?> requestEntity = RequestEntity
                .get(URI.create(url))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .build();

        ResponseEntity<List<Wallet>> response = template
                .exchange(url, HttpMethod.GET, requestEntity, new WalletList());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(singletonList(testWallet), response.getBody());
    }

    @Test
    public void importWallet() {
        String url = base.toString() + "/wallets";

        RequestEntity<?> requestEntity = RequestEntity
                .put(URI.create(url))
                .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                .contentType(MediaType.APPLICATION_JSON)
                .body(randomWallet(networkParameters)
                        .withCreatedAt(Instant.now().getEpochSecond())); // todo replace wallet with new request dto because dto should contain seconds but wallet unfortunatelly must stay with millis

        ResponseEntity<String> response = template.exchange(url, HttpMethod.PUT, requestEntity, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNull(response.getBody());
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
                .body(randomWallet(networkParameters));

        ResponseEntity<String> noAuth = template
                .exchange(url, HttpMethod.PUT, requestEntity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, noAuth.getStatusCode());
    }

    private static class WalletList extends ParameterizedTypeReference<List<Wallet>> {
        //
    }
}
