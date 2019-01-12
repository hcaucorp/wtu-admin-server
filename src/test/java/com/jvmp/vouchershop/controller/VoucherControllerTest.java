package com.jvmp.vouchershop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.security.NoSecurityConfig;
import com.jvmp.vouchershop.voucher.VoucherService;
import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;
import com.jvmp.vouchershop.voucher.impl.RedemptionResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.jvmp.vouchershop.RandomUtils.*;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        Application.class, NoSecurityConfig.class
})
@AutoConfigureMockMvc
@ActiveProfiles("unit-test")
public class VoucherControllerTest {

    private ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VoucherService voucherService;

    @Test
    public void getAllVouchers() throws Exception {
        mvc.perform(get("/vouchers")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
    }

    @Test
    public void generateVouchers() throws Exception {
        mvc.perform(post("/vouchers")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(om.writeValueAsString(randomVoucherGenerationSpec())))
                .andExpect(status().isCreated());
    }

    @Test
    public void deleteById() throws Exception {
        mvc.perform(delete("/vouchers/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
    }

    @Test
    public void redeemVoucher() throws Exception {
        RedemptionRequest request = randomRedemptionRequest();
        RedemptionResponse response = new RedemptionResponse(singletonList("http://trackingurl.com/" + randomString()), randomString());
        when(voucherService.redeemVoucher(eq(request))).thenReturn(response);

        mvc.perform(post("/vouchers/redeem")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(om.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(om.writeValueAsString(response)))
                .andExpect(jsonPath("$.trackingUrls[0]").value(response.getTrackingUrls().get(0)))
                .andExpect(jsonPath("$.transactionId").value(response.getTransactionId()));

    }
}