package es.coffeebyt.wtu.utils;

import static java.lang.System.lineSeparator;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import es.coffeebyt.wtu.crypto.libra.LibraService;
import es.coffeebyt.wtu.voucher.VoucherCodeGenerator;
import es.coffeebyt.wtu.voucher.impl.DefaultVoucherCodeGenerator;
import es.coffeebyt.wtu.voucher.impl.VoucherGenerationSpec;
import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import es.coffeebyt.wtu.wallet.WalletService;

/**
 * Generates voucher codes to file.
 */
public class PaperVoucherGenerator {

    private static final int voucherCount = 8800;
    private static final String currency = LibraService.LIBRA;

    public static void main(String[] args) throws Exception {
        VoucherCodeGenerator voucherCodeGenerator = new DefaultVoucherCodeGenerator(new WalletServiceInternal());

        String fileContent = "Voucher code\n" + IntStream.range(0, voucherCount)
                .mapToObj(value -> voucherCodeGenerator
                        .apply(new VoucherGenerationSpec(voucherCount, 1L, 1L, 1L, "GBP", "sku", null)))
                .collect(Collectors.joining(lineSeparator()));

        Files.write(Paths.get("paper-codes-" + currency + ".txt"), fileContent.getBytes());
    }

    private static class WalletServiceInternal implements WalletService {

        @Override
        public Wallet importWallet(ImportWalletRequest walletDescription) {
            return null;
        }

        @Override
        public Wallet generateWallet(String currency) {
            return null;
        }

        @Override
        public List<Wallet> findAll() {
            return null;
        }

        @Override
        public Optional<Wallet> findById(Long id) {
            return Optional.of(new Wallet()
                    .withCurrency(currency));
        }

        @Override
        public Wallet save(Wallet wallet) {
            return null;
        }
    }
}
