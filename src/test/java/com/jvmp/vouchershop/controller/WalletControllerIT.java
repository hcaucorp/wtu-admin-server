package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;
import java.util.List;

import static com.jvmp.vouchershop.RandomUtils.randomWallet;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

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

        ResponseEntity<List<Wallet>> response = template
                .withBasicAuth(ControllerUtils.USER_NAME, ControllerUtils.USER_PASS)
                .exchange(base.toString() + "/wallets", HttpMethod.GET, null, new WalletList());

        assertEquals(singletonList(testWallet), response.getBody());
    }

    private static class WalletList extends ParameterizedTypeReference<List<Wallet>> {
        //
    }
}
