package com.jvmp.vouchershop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.security.TestSecurityConfig;
import com.jvmp.vouchershop.utils.IAmATeapotException;
import com.jvmp.vouchershop.voucher.VoucherNotFound;
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

import static com.jvmp.vouchershop.utils.RandomUtils.*;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        Application.class, TestSecurityConfig.class
})
@AutoConfigureMockMvc
@ActiveProfiles("unit-test")
public class VoucherControllerTest {

    private final static String baseUrl = "/api";

    private ObjectMapper om = new ObjectMapper();

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VoucherService voucherService;

    @Test
    public void getAllVouchers() throws Exception {
        mvc.perform(get(baseUrl + "/vouchers")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
    }

    @Test
    public void generateVouchers() throws Exception {
        mvc.perform(post(baseUrl + "/vouchers")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(om.writeValueAsString(randomVoucherGenerationSpec())))
                .andExpect(status().isCreated());
    }

    @Test
    public void deleteById() throws Exception {
        mvc.perform(delete(baseUrl + "/vouchers/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
    }

    @Test
    public void redeemVoucher() throws Exception {
        RedemptionRequest request = randomRedemptionRequest();
        RedemptionResponse response = new RedemptionResponse(singletonList("http://trackingurl.com/" + randomString()), randomString());
        when(voucherService.redeemVoucher(eq(request))).thenReturn(response);

        mvc.perform(post(baseUrl + "/vouchers/redeem")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(om.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(om.writeValueAsString(response)))
                .andExpect(jsonPath("$.trackingUrls[0]").value(response.getTrackingUrls().get(0)))
                .andExpect(jsonPath("$.transactionId").value(response.getTransactionId()));

        verify(notificationService, times(1)).pushRedemptionNotification(any());
    }

    @Test
    public void redeemVoucher_notFound() throws Exception {
        RedemptionRequest request = randomRedemptionRequest();
        when(voucherService.redeemVoucher(any())).thenThrow(new VoucherNotFound(randomString()));

        mvc.perform(post(baseUrl + "/vouchers/redeem")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(om.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(notificationService, times(1)).pushRedemptionNotification(any());
    }

    public void redeemVoucher_totalDisaster() throws Exception {

        RedemptionRequest request = randomRedemptionRequest();
        when(voucherService.redeemVoucher(any())).thenThrow(new IAmATeapotException());

        mvc.perform(post(baseUrl + "/vouchers/redeem")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(om.writeValueAsString(request)))
                .andExpect(status().isIAmATeapot());

        verify(notificationService, times(1)).pushRedemptionNotification(any());
    }
}