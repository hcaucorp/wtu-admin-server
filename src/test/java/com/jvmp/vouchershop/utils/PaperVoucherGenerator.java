package com.jvmp.vouchershop.utils;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.voucher.impl.PaperVoucherCodeGenerator;
import com.jvmp.vouchershop.voucher.impl.VoucherGenerationDetails;
import com.jvmp.vouchershop.wallet.WalletService;
import org.bitcoinj.params.TestNet3Params;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.jvmp.vouchershop.crypto.bch.BitcoinCashService.BCH;
import static com.jvmp.vouchershop.utils.RandomUtils.randomWallet;
import static java.lang.System.lineSeparator;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("it")
public class PaperVoucherGenerator {

    private static final int voucherCount = 10_000;
    private static final String currency = BCH;

    @MockBean
    private WalletService walletService;

    @Autowired
    private PaperVoucherCodeGenerator voucherCodeGenerator;

    @Test
    @Ignore("I know I shouldn't do this but it's really easy to run small program like this")
    public void generatePaperVouchers() throws Exception {
        when(walletService.findById(any())).thenReturn(Optional.of(randomWallet(TestNet3Params.get()).withCurrency(currency)));

        String fileContent = "Voucher code\n" + IntStream.range(0, voucherCount).mapToObj(value -> voucherCodeGenerator
                .apply(new VoucherGenerationDetails(voucherCount, 1L, 1L, 1L, "GBP", "sku")))
                .collect(Collectors.joining(lineSeparator()));

        Files.write(Paths.get("voucherCodes.csv"), fileContent.getBytes());
    }
}
