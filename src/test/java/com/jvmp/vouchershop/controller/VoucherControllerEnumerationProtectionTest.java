package com.jvmp.vouchershop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.security.EnumerationProtectionService;
import com.jvmp.vouchershop.security.TestSecurityConfig;
import com.jvmp.vouchershop.system.PropertyNames;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.VoucherNotFoundException;
import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;
import org.apache.commons.lang3.NotImplementedException;
import org.bitcoinj.params.TestNet3Params;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.jvmp.vouchershop.controller.VoucherControllerTest.remoteHost;
import static com.jvmp.vouchershop.utils.RandomUtils.*;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class VoucherControllerEnumerationProtectionTest {

    private final static String baseUrl = "/api";

    private ObjectMapper om = new ObjectMapper();

    @Value(PropertyNames.ENUMERATION_PROTECTION_MAX_ATTEMPTS)
    public int maxAttempts;
    @Value(PropertyNames.ENUMERATION_PROTECTION_COOL_DOWN_TIME)
    public int coolDownTime;
    @Value(PropertyNames.ENUMERATION_PROTECTION_COOL_DOWN_UNIT)
    public String coolDownUnit;

    @Autowired
    private EnumerationProtectionService enumerationProtectionService;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VoucherRepository voucherRepository;

    private String testIp;

    @Before
    public void setUp() {
        testIp = randomIp();
    }

    @Test
    public void triggerIpBlacklisting_byRedemption() throws Exception {
        Voucher notExistingVoucher = randomVoucher();
        when(voucherRepository.findByCode(eq(notExistingVoucher.getCode()))).thenReturn(Optional.empty());
        int attemptsAfterBlacklisted = nextInt(2, 5);

        requestRedemption(notExistingVoucher, maxAttempts + attemptsAfterBlacklisted);

        assertTrue(enumerationProtectionService.isBlocked(testIp));
    }

    @Test
    public void triggerIpBlacklisting_byVoucherInfo() throws Exception {
        Voucher notExistingVoucher = randomVoucher();
        when(voucherRepository.findByCode(eq(notExistingVoucher.getCode()))).thenReturn(Optional.empty());
        int attemptsAfterBlacklisted = nextInt(2, 5);

        requestVoucherInfo(notExistingVoucher, maxAttempts + attemptsAfterBlacklisted);

        assertTrue(enumerationProtectionService.isBlocked(testIp));
    }

    private void requestRedemption(Voucher v, int times) throws Exception {
        RedemptionRequest redemptionRequest = new RedemptionRequest(randomBtcAddress(TestNet3Params.get()), v.getCode());
        String url = baseUrl + "/vouchers/redeem";

        for (int i = 0; i < times; i++)
            mvc.perform(post(url, redemptionRequest)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(om.writeValueAsString(redemptionRequest))
                    .with(remoteHost(testIp)))
                    .andExpect(status().isBadRequest());
    }

    private void requestVoucherInfo(Voucher v, int times) throws Exception {
        RedemptionRequest redemptionRequest = new RedemptionRequest(randomBtcAddress(TestNet3Params.get()), v.getCode());
        String url = baseUrl + "/vouchers/" + v.getCode();

        for (int i = 0; i < times; i++)
            mvc.perform(get(url, redemptionRequest)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .with(remoteHost(testIp)))
                    .andExpect(status().isBadRequest());
    }

    @Test
    public void evictIpFromBlackList() throws Exception {
        Voucher notExistingVoucher = randomVoucher();
        when(voucherRepository.findByCode(eq(notExistingVoucher.getCode()))).thenThrow(new VoucherNotFoundException(""));

        requestRedemption(notExistingVoucher, maxAttempts + 1);
        assertTrue(enumerationProtectionService.isBlocked(testIp));

        Thread.sleep(Duration.of(coolDownTime, toChronoUnit(TimeUnit.valueOf(coolDownUnit))).toMillis() + 1);
        assertFalse(enumerationProtectionService.isBlocked(testIp));
    }

    private ChronoUnit toChronoUnit(TimeUnit timeUnit) {
        if (timeUnit == TimeUnit.MILLISECONDS) {
            return ChronoUnit.MILLIS;
        }
        throw new NotImplementedException("add another case for " + timeUnit);
    }
}
