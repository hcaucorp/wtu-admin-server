package es.coffeebyt.wtu.voucher.impl;

import es.coffeebyt.wtu.utils.RandomUtils;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class FromSpecCodeGeneratorTest {

    private FromSpecCodeGenerator subject;

    @Test
    public void shouldSplitByCommaAndWhiteChars() {
        asList(
                "a a",
                "a,a",
                "a ,a",
                "a , a",
                "a  a",
                "a, a",
                "a\na",
                "a,\na",
                "a\n,a",
                "a\n a",
                "a \na",
                "a \n a",
                "a\n,a",
                "a ,\na",
                "a \n, a"
        ).forEach(s -> {
            String[] split = s.split(FromSpecCodeGenerator.splitter);

            assertEquals("candidate: " + s, 2, split.length);
            assertEquals("a", split[0]);
            assertEquals("a", split[1]);
        });
    }

    @Test
    public void shouldReturnUniqueVoucherCodes() {
        VoucherGenerationSpec spec = RandomUtils.randomVoucherGenerationSpec();
        subject = new FromSpecCodeGenerator(spec);

        Set<String> codes = new HashSet<>();

        for (int i = 0; i < spec.count; i++) {
            codes.add(subject.apply(spec));
        }

        assertEquals(spec.count, codes.size());
    }
}