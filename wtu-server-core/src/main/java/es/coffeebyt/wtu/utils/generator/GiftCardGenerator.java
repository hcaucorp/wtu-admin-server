package es.coffeebyt.wtu.utils.generator;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.List;

import static es.coffeebyt.wtu.crypto.bch.BitcoinCashService.BCH;
import static es.coffeebyt.wtu.crypto.btc.BitcoinService.BTC;
import static es.coffeebyt.wtu.crypto.libra.LibraService.LIBRA;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.write;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;

/**
 * Generates voucher codes to file.
 */
public class GiftCardGenerator {

    public static void main(String[] args) throws Exception {

        List<GenerationSpec> specs = asList(
                new GenerationSpec(1000, "BSV"),
                new GenerationSpec(2000, BTC),
                new GenerationSpec(2000, BCH),
                new GenerationSpec(6000, LIBRA)
        );

        String fileContent =
                specs.stream()
                        .map(spec -> {
                            GeneratorService generator = new GeneratorService(spec.currency.toLowerCase());
                            return range(0, spec.count)
                                    .mapToObj(ignored -> generator.apply(null))
                                    .collect(joining(lineSeparator()));
                        })
                        .collect(joining(lineSeparator()));

        pushToFile("gift-card-generator-output.txt", fileContent);
    }

    private static void pushToFile(String file, String content) throws IOException {
        try {
            write(get(file), content.getBytes(), APPEND);
        } catch (NoSuchFileException e) {
            write(get("paper-codes-vol-II.txt"), content.getBytes(), CREATE_NEW);
        }
    }
}
