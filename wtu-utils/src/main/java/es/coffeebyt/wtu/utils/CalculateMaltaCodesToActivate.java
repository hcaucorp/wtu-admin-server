package es.coffeebyt.wtu.utils;

import static java.lang.System.lineSeparator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CalculateMaltaCodesToActivate {

    private static final String pathPrefix = "wtu-utils/src/main/resources/bch-codes/";

    public static void main(String[] args) throws Exception {

        log.info("Current dir: {}", Paths.get("").toAbsolutePath().toString());

        Set<String> allCodes = readCodesFromFile("001-to-print-set.txt");
        Set<String> neverPrinted = readCodesFromFile("002-never-printed.txt");
        Set<String> alreadyActive = readCodesFromFile("003-already-active-codes.txt");

        Set<String> maltaCodes = allCodes.stream()
                .filter(code -> !neverPrinted.contains(code))
                .filter(code -> !alreadyActive.contains(code))
                .collect(Collectors.toSet());

        String fileContent = maltaCodes.stream()
                .collect(Collectors.joining(lineSeparator()));

        Files.write(Paths.get(pathPrefix + "004-codes-for-malta.txt"), fileContent.getBytes());
    }

    private static Set<String> readCodesFromFile(String path) throws FileNotFoundException {
        return new BufferedReader(new FileReader(Paths.get(pathPrefix + path).toFile()))
                .lines()
                .collect(Collectors.toSet());

    }
}
