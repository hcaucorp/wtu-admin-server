package es.coffeebyt.wtu.voucher;

import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

public class VoucherTest {

    private final List<String> sample = asList(
            "wtubtc-1ca47211-686a-4d32-9180-b9a8895f97ac",
            "wtubch-924eb4a2-c943-49e7-aa10-5eeceb6afcaf",
            "wtulibra-fd015f96-4e0c-47fc-812a-57e414e02acb"
    );

    @Test
    public void testCodePattern() {
        Pattern pattern = Pattern.compile(Voucher.CODE_PATTERN);
        sample.forEach(code -> assertTrue(pattern.matcher(code).matches()));
    }
}
