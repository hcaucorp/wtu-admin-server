package com.jvmp.vouchershop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvmp.vouchershop.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.jvmp.vouchershop.RandomUtils.randomVoucherGenerationSpec;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class VoucherControllerTest {


    @Autowired
    private MockMvc mvc;

    @Test
    public void getAllVouchers() throws Exception {
        mvc.perform(get("/vouchers"))
                .andExpect(status().isOk());
    }

    @Test
    public void generateVouchers() throws Exception {
        mvc.perform(post("/vouchers")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(randomVoucherGenerationSpec())))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteById() throws Exception {
        mvc.perform(delete("/vouchers/1"))
                .andExpect(status().isNotFound());
    }
}