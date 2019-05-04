package com.jvmp.vouchershop.wallet;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.crypto.bch.BitcoinCashService;
import com.jvmp.vouchershop.crypto.btc.BitcoinService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.jvmp.vouchershop.crypto.bch.BitcoinCashService.BCH;
import static com.jvmp.vouchershop.crypto.btc.BitcoinService.BTC;
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
        assertEquals(bitcoinService, subject.findByCurrency(BTC));
        assertEquals(bitcoinCashService, subject.findByCurrency(BCH));
    }
}