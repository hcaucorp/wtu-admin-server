package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.crypto.btc.BitcoinJAdapter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class WalletControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BitcoinJAdapter bitcoinJAdapter;

    @Before
    public void setUp() {
        when(bitcoinJAdapter.getBalance()).thenReturn(1L);
    }

    @WithMockUser(ControllerUtils.USER_NAME)
    @Test
    public void getAllWallets() throws Exception {
        mvc.perform(get("/wallets"))
                .andExpect(status().isOk());
    }

    @WithMockUser(ControllerUtils.USER_NAME)
    @Test
    public void generateWallet() throws Exception {

        mvc.perform(post("/wallets")
                .content("BTC"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(empty())))
                .andExpect(jsonPath("$.address", not(empty())))
                .andExpect(jsonPath("$.balance", notNullValue()))
                .andExpect(jsonPath("$.currency", is("BTC")))
                .andExpect(jsonPath("$.createdAt", greaterThan(1322697600L)))
                .andExpect(jsonPath("$.mnemonic", not(empty())));

        mvc.perform(post("/wallets")
                .content("ETH"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/wallets")
                .content("XYZ"))
                .andExpect(status().isBadRequest());
    }
}