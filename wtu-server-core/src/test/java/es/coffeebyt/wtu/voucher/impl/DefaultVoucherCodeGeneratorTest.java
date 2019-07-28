package es.coffeebyt.wtu.voucher.impl;

import es.coffeebyt.wtu.wallet.Wallet;
import es.coffeebyt.wtu.wallet.WalletService;
import org.bitcoinj.core.Context;
import org.bitcoinj.params.UnitTestParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static es.coffeebyt.wtu.crypto.btc.BitcoinService.BTC;
import static es.coffeebyt.wtu.utils.RandomUtils.randomVoucherGenerationSpec;
import static es.coffeebyt.wtu.utils.RandomUtils.randomWallet;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultVoucherCodeGeneratorTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private DefaultVoucherCodeGenerator subject;

    private Context btcContext;

    @Before
    public void setUp() {
        btcContext = new Context(UnitTestParams.get());
    }

    @Test
    public void testGenerationFormat() {
        VoucherGenerationSpec spec = randomVoucherGenerationSpec();
        Wallet wallet = randomWallet(btcContext.getParams())
                .withCurrency(BTC);

        when(walletService.findById(any())).thenReturn(Optional.of(wallet));

        String actual = subject.apply(spec);

        assertTrue(actual.startsWith("wtubtc-"));
    }
}