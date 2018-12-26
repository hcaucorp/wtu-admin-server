package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.Wallet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Autowired
    private WalletRepository walletRepository;

    private Wallet testWallet;

    @Before
    public void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port);

        testWallet = walletRepository.save(randomWallet());
    }

    @Test
    public void getAllWallets() {
        ResponseEntity<List<Wallet>> response =
                template.exchange(base.toString() + "/wallets", HttpMethod.GET, null, new WalletList());
        assertEquals(singletonList(testWallet), response.getBody());
    }

    private static class WalletList extends ParameterizedTypeReference<List<Wallet>> {
        //
    }
}
