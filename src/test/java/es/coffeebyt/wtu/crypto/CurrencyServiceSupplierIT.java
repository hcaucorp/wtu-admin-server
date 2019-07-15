package es.coffeebyt.wtu.crypto;

import es.coffeebyt.wtu.Application;
import es.coffeebyt.wtu.crypto.bch.BitcoinCashService;
import es.coffeebyt.wtu.crypto.btc.BitcoinService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class CurrencyServiceSupplierIT {

    @Autowired
    private CurrencyServiceSupplier subject;

    @Autowired
    private BitcoinService bitcoinService;

    @Autowired
    private BitcoinCashService bitcoinCashService;

    @Test
    public void testCorrectSpringContextAndServiceResolution() {
        assertEquals(bitcoinService, subject.findByCurrency(BitcoinService.BTC));
        assertEquals(bitcoinCashService, subject.findByCurrency(BitcoinCashService.BCH));
    }
}