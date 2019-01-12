package com.jvmp.vouchershop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.security.HmacUtil;
import com.jvmp.vouchershop.security.NoSecurityConfig;
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

import static com.jvmp.vouchershop.RandomUtils.randomOrder;
import static com.jvmp.vouchershop.RandomUtils.randomString;
import static com.jvmp.vouchershop.controller.ShopifyController.HTTP_HEADER_X_SHOPIFY_HMAC_SHA256;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        Application.class, NoSecurityConfig.class
})
@AutoConfigureMockMvc
@ActiveProfiles("unit-test")
public class ShopifyControllerTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Value(PropertyNames.SHOPIFY_WEBHOOK_SHARED_SECRET)
    private String webhookSecret;

    @Autowired
    private MockMvc mvc;

    @Test
    public void fullFillmentHookSuccess() throws Exception {
        String order = objectMapper.writeValueAsString(randomOrder());
        String bodyHash = HmacUtil.encode(webhookSecret, order.getBytes());

        mvc.perform(post("/shopify/webhook/fulfill")
                .content(order)
                .header(HTTP_HEADER_X_SHOPIFY_HMAC_SHA256, bodyHash))
                .andExpect(status().isAccepted());
    }

    @Test
    public void fullFillmentHookWrongHeader() throws Exception {
        String order = objectMapper.writeValueAsString(randomOrder());
        String bodyHash = randomString(); //wrong

        mvc.perform(post("/shopify/webhook/fulfill", order)
                .header(HTTP_HEADER_X_SHOPIFY_HMAC_SHA256, bodyHash)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest());
    }
}