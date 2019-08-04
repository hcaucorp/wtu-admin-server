package es.coffeebyt.wtu.controller;

import static es.coffeebyt.wtu.metrics.ActuatorConfig.COUNTER_REDEMPTION_FAILURE;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

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

import java.util.Optional;

import es.coffeebyt.wtu.Application;
import es.coffeebyt.wtu.api.ApiErrorValues;
import es.coffeebyt.wtu.notifications.NotificationService;
import es.coffeebyt.wtu.security.EnumerationProtectionService;
import es.coffeebyt.wtu.security.TestSecurityConfig;
import es.coffeebyt.wtu.utils.IAmATeapotException;
import es.coffeebyt.wtu.utils.RandomUtils;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.voucher.VoucherInfoResponse;
import es.coffeebyt.wtu.voucher.VoucherNotFoundException;
import es.coffeebyt.wtu.voucher.VoucherService;
import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;
import es.coffeebyt.wtu.voucher.impl.RedemptionResponse;
import es.coffeebyt.wtu.voucher.listeners.RedemptionSuccessCounter;
import io.micrometer.core.instrument.MeterRegistry;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        Application.class, TestSecurityConfig.class, RedemptionSuccessCounter.class
})
@AutoConfigureMockMvc
@ActiveProfiles("unit-test")
public class VoucherControllerTest {

    private final static String baseUrl = "/api";

    private ObjectMapper om = new ObjectMapper();

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private EnumerationProtectionService enumerationProtectionService;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VoucherService voucherService;

    @Autowired
    private MeterRegistry meterRegistry;

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
                .content(om.writeValueAsString(RandomUtils.randomVoucherGenerationSpec())))
                .andExpect(status().isCreated());
    }

    @Test
    public void deleteBySku() throws Exception {
        String sku = "sku-" + RandomUtils.randomString();
        mvc.perform(delete(baseUrl + "/vouchers/" + sku)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        verify(voucherService, times(1)).deleteBySku(sku);
    }

    @Test
    public void redeemVoucher() throws Exception {
        RedemptionRequest request = RandomUtils.randomRedemptionRequest();
        RedemptionResponse response = new RedemptionResponse(singletonList("http://trackingurl.com/" + RandomUtils.randomString()), RandomUtils.randomString());
        when(voucherService.redeemVoucher(eq(request))).thenReturn(response);

        mvc.perform(post(baseUrl + "/vouchers/redeem")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(om.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(om.writeValueAsString(response)))
                .andExpect(jsonPath("$.trackingUrls[0]").value(response.getTrackingUrls().get(0)))
                .andExpect(jsonPath("$.transactionId").value(response.getTransactionId()));
    }

    @Test
    public void redeemVoucherVerifyApiErrorValue() {
        String onePerCustomerErrorCode = ApiErrorValues.MALTA_GIFT_CODE_FAILING_WITH_ONE_PER_CUSTOMER_ERROR;
    }

    static RequestPostProcessor remoteHost(final String remoteHost) {
        return request -> {
            request.setRemoteAddr(remoteHost);
            return request;
        };
    }

    @Test
    public void redeemVoucher_notFound() throws Exception {
        double currentCount = meterRegistry.counter(COUNTER_REDEMPTION_FAILURE).count();

        RedemptionRequest request = RandomUtils.randomRedemptionRequest();
        when(voucherService.redeemVoucher(any())).thenThrow(new VoucherNotFoundException(RandomUtils.randomString()));

        mvc.perform(post(baseUrl + "/vouchers/redeem")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(om.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertEquals(currentCount + 1, meterRegistry.counter(COUNTER_REDEMPTION_FAILURE).count(), 0);
    }

    @Test
    public void redeemVoucher_totalDisaster() throws Exception {
        double currentCount = meterRegistry.counter(COUNTER_REDEMPTION_FAILURE).count();

        RedemptionRequest request = RandomUtils.randomRedemptionRequest();
        when(voucherService.redeemVoucher(any())).thenThrow(new IAmATeapotException());

        mvc.perform(post(baseUrl + "/vouchers/redeem")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(om.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertEquals(currentCount + 1, meterRegistry.counter(COUNTER_REDEMPTION_FAILURE).count(), 0);
    }

    @Test
    public void redeemVoucher_butBlockedIp() throws Exception {
        RedemptionRequest request = RandomUtils.randomRedemptionRequest();
        String ip = RandomUtils.randomIp();
        when(voucherService.redeemVoucher(any())).thenThrow(new VoucherNotFoundException(""));
        when(enumerationProtectionService.isBlocked(any())).thenReturn(true);

        mvc.perform(post(baseUrl + "/vouchers/redeem")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(om.writeValueAsString(request)).with(remoteHost(ip)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void redeemVoucher_butBlockedProxiedIp() throws Exception {
        String ip = RandomUtils.randomIp();
        RedemptionRequest request = RandomUtils.randomRedemptionRequest();
        when(voucherService.redeemVoucher(eq(request))).thenThrow(new VoucherNotFoundException(""));
        when(enumerationProtectionService.isBlocked(eq(ip))).thenReturn(true);

        mvc.perform(post(baseUrl + "/vouchers/redeem")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("X-Forwarder-For", ip + ", " + RandomUtils.randomIp() + ", " + RandomUtils.randomIp()) //clientIpAddress, proxy1, proxy2
                .content(om.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getVoucherInfo() throws Exception {
        Voucher voucher = RandomUtils.randomValidVoucher();

        VoucherInfoResponse voucherInfoResponse = VoucherInfoResponse.from(voucher);

        when(voucherService.findByCode(eq(voucher.getCode()))).thenReturn(Optional.of(voucher));

        mvc.perform(get(baseUrl + "/vouchers/" + voucher.getCode())
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().json(om.writeValueAsString(voucherInfoResponse)));
    }

    @Test
    public void getVoucherInfoUnsoldVoucher() throws Exception {
        Voucher voucher = RandomUtils.randomVoucher().withSold(false);

        when(voucherService.findByCode(eq(voucher.getCode()))).thenReturn(Optional.of(voucher));

        mvc.perform(get(baseUrl + "/vouchers/" + voucher.getCode())
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").doesNotExist()); //security: important to not give away error details
    }
}