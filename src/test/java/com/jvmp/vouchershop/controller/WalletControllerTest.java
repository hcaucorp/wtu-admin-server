package com.jvmp.vouchershop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvmp.vouchershop.Application;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class WalletControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void getAllWallets() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/wallets"))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteWallet() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete("/wallets/" + 44))
                .andExpect(status().isNotFound());
    }

    @Test
    public void generateWallet() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String password = RandomStringUtils.randomAlphabetic(32);
        String description = "Wallet Description (" + RandomStringUtils.randomAlphabetic(32) + ")";
        String payload = objectMapper.writeValueAsString(new WalletController.GenerateWalletPayload(password, description));
        mvc.perform(MockMvcRequestBuilders.post("/wallets/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        )
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, notNullValue()));
    }

}