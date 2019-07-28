package es.coffeebyt.wtu.crypto;

import es.coffeebyt.wtu.utils.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CurrencyServiceSupplierTest {

    @Mock
    private CurrencyService currencyService;

    private CurrencyServiceSupplier subject;

    @Before
    public void setUp() {
        subject = new CurrencyServiceSupplier(singletonList(currencyService));
    }

    @Test
    public void findByCurrency() {
        String currency = RandomUtils.randomCurrency();
        when(currencyService.acceptsCurrency(eq(currency))).thenReturn(true);

        assertEquals(currencyService, subject.findByCurrency(currency));
    }

    @Test(expected = CurrencyNotSupported.class)
    public void findByCurrency_notSupportedCurrency() {
        subject.findByCurrency(RandomUtils.randomCurrency());
    }
}