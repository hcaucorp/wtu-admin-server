package es.coffeebyt.wtu.utils;

import static es.coffeebyt.wtu.crypto.libra.LibraService.LIBRA;
import static es.coffeebyt.wtu.utils.RandomUtils.randomWallet;
import static java.lang.System.lineSeparator;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import es.coffeebyt.wtu.voucher.impl.DefaultVoucherCodeGenerator;
import es.coffeebyt.wtu.voucher.impl.VoucherGenerationSpec;
import es.coffeebyt.wtu.wallet.WalletService;

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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DefaultVoucherCodeGenerator.class)
@ActiveProfiles("it")
public class PaperVoucherGenerator {

    private static final int voucherCount = 8800;
    private static final String currency = LIBRA;

    @MockBean
    private WalletService walletService;

    @Autowired
    private DefaultVoucherCodeGenerator voucherCodeGenerator;

    /**
     * Generates voucher codes to file.
     */
    @Test
    @Ignore
    public void generatePaperVouchers() throws Exception {
        when(walletService.findById(any())).thenReturn(Optional.of(randomWallet(TestNet3Params.get()).withCurrency(currency)));

        String fileContent = "Voucher code\n" + IntStream.range(0, voucherCount)
                .mapToObj(value -> voucherCodeGenerator
                        .apply(new VoucherGenerationSpec(voucherCount, 1L, 1L, 1L, "GBP", "sku", null)))
                .collect(Collectors.joining(lineSeparator()));

        Files.write(Paths.get("PaperVouchesCodes-" + currency +
                ".txt"), fileContent.getBytes());
    }
}
