package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.crypto.btc.BitcoinJAdapter;
import com.jvmp.vouchershop.crypto.btc.BitcoinJConfig;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.security.Auth0Service;
import com.jvmp.vouchershop.system.DatabaseConfig;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;
import com.jvmp.vouchershop.voucher.impl.RedemptionResponse;
import com.jvmp.vouchershop.wallet.Wallet;
import com.jvmp.vouchershop.wallet.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.params.TestNet3Params;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.jvmp.vouchershop.utils.RandomUtils.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                Application.class,
                Auth0Service.class,
                BitcoinJConfig.class,
                DatabaseConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MockBeans({
        @MockBean(NotificationService.class),
        @MockBean(BitcoinJAdapter.class)
})
public class VoucherControllerStressTest {

    @LocalServerPort
    private int port;

    private URL base;

    @Autowired
    private Auth0Service auth0Service;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private VoucherRepository voucherRepository;

    @MockBean
    private WalletService walletService;

    private String authorizationValue;

    private CountDownLatch countDownLatch;
    private List<ResponseEntity<RedemptionResponse>> results = Collections.synchronizedList(new ArrayList<>());
    private Voucher voucher;
    private Wallet wallet;
    private String destinationAddress;

    @Before
    public void setUpTest() throws Exception {
        base = new URL("http://localhost:" + port + "/api");
        authorizationValue = "Bearer " + auth0Service.getToken().accessToken;
        destinationAddress = "mqTZ5Lmt1rrgFPeGeTC8DFExAxV1UK852G";

        voucher = voucherRepository.save(randomVoucher()
                .withSold(true)
                .withRedeemed(false));

        wallet = randomWallet(TestNet3Params.get());
        when(walletService.findById(any())).thenReturn(Optional.of(wallet));
        when(walletService.sendMoney(any(), any(), anyLong())).thenReturn(randomString());
    }

    @Test
    public void doubleRedemptionTest() throws InterruptedException {

        runSimulation(500);

        assertFalse(results.isEmpty());

        long okCount = results.stream()
                .filter(response -> response.getStatusCode() == HttpStatus.OK)
                .count();

        assertEquals(1, okCount);

        Optional<Voucher> byId = voucherRepository.findById(voucher.getId());

        assertTrue(byId.isPresent());
        assertTrue(byId.get().isRedeemed());

        verify(walletService, times(1)).sendMoney(wallet, destinationAddress, voucher.getAmount());
    }

    public void runSimulation(int numWorkers) throws InterruptedException {
        countDownLatch = new CountDownLatch(numWorkers);

        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < numWorkers; i++) {
            Thread worker = new Thread(new RequestSenderThread());
            worker.start();
            workers.add(worker);
        }

        for (Thread thread : workers) {
            thread.join(60_000);
        }

        countDownLatch.await(1, TimeUnit.MINUTES);
    }

    class RequestSenderThread implements Runnable {

        @Override
        public void run() {
            String url = base.toString() + "/vouchers/redeem";

            RequestEntity<?> requestEntity = RequestEntity
                    .post(URI.create(url))
                    .header(HttpHeaders.AUTHORIZATION, authorizationValue)
                    .body(new RedemptionRequest()
                            .withVoucherCode(voucher.getCode())
                            .withDestinationAddress(destinationAddress));

            try {
                countDownLatch.countDown();
                countDownLatch.await();
            } catch (InterruptedException e) {
                fail(e.getMessage());
            }

            ResponseEntity<RedemptionResponse> response =
                    template.postForEntity(url, requestEntity, RedemptionResponse.class);

            results.add(response);
        }
    }
}
