package es.coffeebyt.wtu.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bitcoinj.params.TestNet3Params;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import es.coffeebyt.wtu.Application;
import es.coffeebyt.wtu.crypto.CurrencyService;
import es.coffeebyt.wtu.crypto.CurrencyServiceSupplier;
import es.coffeebyt.wtu.crypto.btc.BitcoinJConfig;
import es.coffeebyt.wtu.notifications.NotificationService;
import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.security.Auth0Service;
import es.coffeebyt.wtu.system.DatabaseConfig;
import es.coffeebyt.wtu.utils.RandomUtils;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;
import es.coffeebyt.wtu.voucher.impl.RedemptionResponse;
import es.coffeebyt.wtu.wallet.Wallet;
import es.coffeebyt.wtu.wallet.WalletService;
import lombok.extern.slf4j.Slf4j;

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
@MockBean(NotificationService.class)
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

    @MockBean
    private CurrencyServiceSupplier currencyServiceSupplier;

    @MockBean(name = "bitcoinService")
    private CurrencyService currencyService;

    private String authorizationValue;

    private CountDownLatch countDownLatch;
    private List<ResponseEntity<RedemptionResponse>> results = Collections.synchronizedList(new ArrayList<>());
    private Voucher voucher;
    private Wallet wallet;
    private String destinationAddress;

    @Before
    public void setUpTest() throws MalformedURLException {
        base = new URL("http://localhost:" + port + "/api");
        authorizationValue = "Bearer " + auth0Service.getToken().accessToken;
        destinationAddress = "mqTZ5Lmt1rrgFPeGeTC8DFExAxV1UK852G";

        voucher = voucherRepository.save(RandomUtils.randomValidVoucher());

        wallet = RandomUtils.randomWallet(TestNet3Params.get());
        when(walletService.findById(any())).thenReturn(Optional.of(wallet));
        when(currencyService.sendMoney(any(), any(), anyLong())).thenReturn(RandomUtils.randomString());
        when(currencyServiceSupplier.findByCurrency(any())).thenReturn(currencyService);
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

        assertTrue(byId.isPresent() && byId.get().isRedeemed());

        verify(currencyService, times(1)).sendMoney(wallet, destinationAddress, voucher.getAmount());
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

        assertTrue(countDownLatch.await(1, TimeUnit.MINUTES));
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
                Thread.currentThread().interrupt();
            }

            ResponseEntity<RedemptionResponse> response =
                    template.postForEntity(url, requestEntity, RedemptionResponse.class);

            results.add(response);
        }
    }
}
