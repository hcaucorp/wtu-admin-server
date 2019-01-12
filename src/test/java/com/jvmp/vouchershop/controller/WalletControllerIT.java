package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;
import java.util.List;

import static com.jvmp.vouchershop.RandomUtils.randomString;
import static com.jvmp.vouchershop.RandomUtils.randomWallet;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = Application.class
)
public class WalletControllerIT {

    @LocalServerPort
    private int port;

    private URL base;

    @Autowired
    private TestRestTemplate template;

    @MockBean
    private WalletService walletService;


    @Before
    public void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port);
    }

    @Test
    public void getAllWallets() {
        Wallet testWallet = randomWallet();
        when(walletService.findAll()).thenReturn(singletonList(testWallet));

        String responseString = template
                .getForObject(base.toString() + "/wallets", String.class);

        log.info("Response content: \n{}\n", responseString);

        ResponseEntity<List<Wallet>> response = template
                .exchange(base.toString() + "/wallets", HttpMethod.GET, null, new WalletList());


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(singletonList(testWallet), response.getBody());
    }

    @Test
    public void getAllWalletsNoAuth() {
        Wallet testWallet = randomWallet();
        when(walletService.findAll()).thenReturn(singletonList(testWallet));
        ResponseEntity<List<Wallet>> noAuth = template
                .withBasicAuth(randomString(), randomString())
                .exchange(base.toString() + "/wallets", HttpMethod.GET, null, new WalletList());

        assertEquals(HttpStatus.UNAUTHORIZED, noAuth.getStatusCode());
    }

    private static class WalletList extends ParameterizedTypeReference<List<Wallet>> {
        //
    }
}
