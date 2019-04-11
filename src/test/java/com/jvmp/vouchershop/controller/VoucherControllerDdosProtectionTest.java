package com.jvmp.vouchershop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.security.TestSecurityConfig;
import com.jvmp.vouchershop.voucher.VoucherNotFoundException;
import com.jvmp.vouchershop.voucher.ddos.RedemptionAttemptService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        Application.class, TestSecurityConfig.class
})
@AutoConfigureMockMvc
@ActiveProfiles("unit-test")
@MockBeans({
        @MockBean(NotificationService.class)
})
public class VoucherControllerDdosProtectionTest {

    private final static String baseUrl = "/api";

    private ObjectMapper om = new ObjectMapper();

    @Autowired
    private RedemptionAttemptService redemptionAttemptService;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VoucherRepository voucherRepository;

    @Test
    public void triggerIpBlacklisting() throws Exception {
        when(voucherRepository.findByCode(any())).thenThrow(new VoucherNotFoundException(""));

        mvc.perform(get(baseUrl + "/vouchers")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
    }
}
