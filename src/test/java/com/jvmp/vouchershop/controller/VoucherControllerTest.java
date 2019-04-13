package com.jvmp.vouchershop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.security.RedemptionAttemptService;
import com.jvmp.vouchershop.security.TestSecurityConfig;
import com.jvmp.vouchershop.utils.IAmATeapotException;
import com.jvmp.vouchershop.voucher.VoucherNotFoundException;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

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

    @MockBean
    private RedemptionAttemptService redemptionAttemptService;

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
    public void deleteBySku() throws Exception {
        String sku = "sku-" + randomString();
        mvc.perform(delete(baseUrl + "/vouchers/" + sku)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        verify(voucherService, times(1)).deleteBySku(sku);
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

    static RequestPostProcessor remoteHost(final String remoteHost) {
        return request -> {
            request.setRemoteAddr(remoteHost);
            return request;
        };
    }

    @Test
    public void redeemVoucher_notFound() throws Exception {
        RedemptionRequest request = randomRedemptionRequest();
        when(voucherService.redeemVoucher(any())).thenThrow(new VoucherNotFoundException(randomString()));

        mvc.perform(post(baseUrl + "/vouchers/redeem")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(om.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(notificationService, times(1)).pushRedemptionNotification(any());
    }

    @Test
    public void redeemVoucher_totalDisaster() throws Exception {

        RedemptionRequest request = randomRedemptionRequest();
        when(voucherService.redeemVoucher(any())).thenThrow(new IAmATeapotException());

        mvc.perform(post(baseUrl + "/vouchers/redeem")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(om.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(notificationService, times(1)).pushRedemptionNotification(any());
    }

    @Test
    public void redeemVoucher_butBlockedIp() throws Exception {
        RedemptionRequest request = randomRedemptionRequest();
        String ip = randomIp();
        when(voucherService.redeemVoucher(any())).thenThrow(new VoucherNotFoundException(""));
        when(redemptionAttemptService.isBlocked(any())).thenReturn(true);

        mvc.perform(post(baseUrl + "/vouchers/redeem")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(om.writeValueAsString(request)).with(remoteHost(ip)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void redeemVoucher_butBlockedProxiedIp() throws Exception {
        String ip = randomIp();
        RedemptionRequest request = randomRedemptionRequest();
        when(voucherService.redeemVoucher(eq(request))).thenThrow(new VoucherNotFoundException(""));
        when(redemptionAttemptService.isBlocked(eq(ip))).thenReturn(true);

        mvc.perform(post(baseUrl + "/vouchers/redeem")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("X-Forwarder-For", ip + ", " + randomIp() + ", " + randomIp()) //clientIpAddress, proxy1, proxy2
                .content(om.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}