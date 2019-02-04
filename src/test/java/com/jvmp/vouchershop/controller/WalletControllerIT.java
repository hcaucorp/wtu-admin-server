package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.security.Auth0Service;
import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URL;
import java.util.List;

import static com.jvmp.vouchershop.utils.RandomUtils.randomWallet;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {
                Application.class,
                Auth0Service.class
        }
)
public class WalletControllerIT {

    @LocalServerPort
    private int port;

    private URL base;

    @Autowired
    private TestRestTemplate template;

    @MockBean
    private WalletService walletService;

    @Autowired
    private NetworkParameters networkParameters;

    @Autowired
    private Auth0Service auth0Service;

    private String authorizationValue;

    @Before
    public void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port + "/api");
        Context.propagate(new Context(networkParameters));
        authorizationValue = "Bearer " + auth0Service.getToken().accessToken;
    }

    @Test
    public void getAllWallets() {
        Wallet testWallet = randomWallet(networkParameters);
        when(walletService.findAll()).thenReturn(singletonList(testWallet));

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
    public void getAllWalletsNoAuth() {
        Wallet testWallet = randomWallet(networkParameters);
        when(walletService.findAll()).thenReturn(singletonList(testWallet));

        String url = base.toString() + "/wallets";

        RequestEntity<?> requestEntity = RequestEntity
                .get(URI.create(url))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        ResponseEntity<String> noAuth = template
                .exchange(url, HttpMethod.GET, requestEntity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, noAuth.getStatusCode());
    }

    private static class WalletList extends ParameterizedTypeReference<List<Wallet>> {
        //
    }
}
