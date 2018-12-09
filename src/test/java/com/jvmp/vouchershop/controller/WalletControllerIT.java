package com.jvmp.vouchershop.controller;

import com.google.common.collect.ImmutableMap;
import com.jvmp.vouchershop.domain.Wallet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WalletControllerIT {

    @LocalServerPort
    private int port;

    private URL base;

    @Autowired
    private TestRestTemplate template;

    @Before
    public void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port + "/wallets");
    }

    @Test
    public void getAllWallets() {
        ResponseEntity<List> response = template.getForEntity(base.toString(), List.class);
        assertEquals(emptyList(), response.getBody());
    }

    @Test
    public void generateWalletPersistsItImmediately() {
        String strongPassword = UUID.randomUUID().toString();
        Wallet generatedWallet = template
                .getForEntity(
                        base.toString() + "/generate",
                        Wallet.class,
                        ImmutableMap.builder().put("password", strongPassword).build()
                )
                .getBody();

        assertNotNull(generatedWallet);
        assertNotNull(generatedWallet.getId());

        List wallets = template.getForEntity(base.toString(), List.class).getBody();
        assertNotNull(wallets);
        assertEquals(singletonList(generatedWallet), wallets);
    }
}
