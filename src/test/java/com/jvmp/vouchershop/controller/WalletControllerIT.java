package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.domain.Wallet;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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


    @Before
    public void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port);
    }

    @Test
    public void getAllWallets() {
        ResponseEntity<List> response = template.getForEntity(base.toString() + "/wallets", List.class);
        assertEquals(emptyList(), response.getBody());
    }

    @Test
    public void generateWalletPersistsItImmediately() {
        String strongPassword = UUID.randomUUID().toString();
        String description = "Description with number: " + RandomStringUtils.randomNumeric(12);
        URI location = generateWallet(strongPassword, description);

        assertNotNull(location);


        ResponseEntity<Wallet> entity = template.getForEntity(base.toString() + location, Wallet.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        Wallet generatedWallet = entity.getBody();

        assertNotNull(generatedWallet);
        assertNotNull(generatedWallet.getId());
        assertEquals(description, generatedWallet.getDescription());
        assertNotNull(generatedWallet.getCreatedAt());
    }

    private URI generateWallet(String strongPassword, String description) {
        return template.postForLocation(
                base.toString() + "/wallets/generate",
                new WalletController.GenerateWalletPayload(strongPassword, description));
    }

    @Test
    public void getWalletById() {
        // done in #generateWalletPersistsItImmediately()
    }

    @Test
    public void deleteWalletById() {
        String strongPassword = UUID.randomUUID().toString();
        String description = "Description with number: " + RandomStringUtils.randomNumeric(12);
        URI location = generateWallet(strongPassword, description);
        ResponseEntity<Wallet> entity;

        entity = template.getForEntity(base.toString() + location, Wallet.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());

        template.delete(base.toString() + location);

        entity = template.getForEntity(base.toString() + location, Wallet.class);
        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    }
}
