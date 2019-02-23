package com.jvmp.vouchershop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.fulfillment.Fulfillment;
import com.jvmp.vouchershop.repository.FulfillmentRepository;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.security.TestSecurityConfig;
import com.jvmp.vouchershop.system.PropertyNames;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;

import static com.jvmp.vouchershop.utils.RandomUtils.randomFulfillment;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        Application.class, TestSecurityConfig.class
})
@AutoConfigureMockMvc
@ActiveProfiles("unit-test")
public class FulfillmentControllerTest {

    private final static String baseUrl = "/api/fulfillments";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Value(PropertyNames.SHOPIFY_WEBHOOK_SHARED_SECRET)
    private String webhookSecret;

    @Autowired
    private FulfillmentRepository fulfillmentRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private MockMvc mvc;

    @Test
    public void getFulfillmentForOrder() throws Exception {
        long orderId = nextLong();
        Fulfillment ff = randomFulfillment().withOrderId(orderId);
        ff.setVouchers(new HashSet<>((voucherRepository.saveAll(ff.getVouchers()))));
        ff = fulfillmentRepository.save(ff);

        assertEquals(1, fulfillmentRepository.count());

        mvc.perform(get(baseUrl + "/" + orderId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().string(objectMapper.writeValueAsString(ff)));
    }
}