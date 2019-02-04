package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.crypto.btc.BitcoinJAdapter;
import com.jvmp.vouchershop.security.TestSecurityConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        Application.class, TestSecurityConfig.class
})
@AutoConfigureMockMvc
@ActiveProfiles("unit-test")
public class WalletControllerTest {

    private final static String baseUrl = "/api";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BitcoinJAdapter bitcoinJAdapter;

    @Before
    public void setUp() {
        when(bitcoinJAdapter.getBalance()).thenReturn(1L);
    }

    @Test
    public void getAllWallets() throws Exception {
        mvc.perform(get(baseUrl + "/wallets"))
                .andExpect(status().isOk());
    }

    @Test
    public void generateWallet() throws Exception {

        mvc.perform(post(baseUrl + "/wallets")
                .content("BTC"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(empty())))
                .andExpect(jsonPath("$.address", not(empty())))
                .andExpect(jsonPath("$.balance", notNullValue()))
                .andExpect(jsonPath("$.currency", is("BTC")))
                .andExpect(jsonPath("$.createdAt", greaterThan(1322697600L)))
                .andExpect(jsonPath("$.mnemonic", not(empty())));

        mvc.perform(post(baseUrl + "/wallets")
                .content("ETH"))
                .andExpect(status().isBadRequest());

        mvc.perform(post(baseUrl + "/wallets")
                .content("XYZ"))
                .andExpect(status().isBadRequest());
    }
}