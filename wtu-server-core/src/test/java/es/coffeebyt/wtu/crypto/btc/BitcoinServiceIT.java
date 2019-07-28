package es.coffeebyt.wtu.crypto.btc;

import es.coffeebyt.wtu.Application;
import es.coffeebyt.wtu.repository.WalletRepository;
import es.coffeebyt.wtu.wallet.Wallet;
import org.bitcoinj.core.Context;
import org.bitcoinj.params.UnitTestParams;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("it")
public class BitcoinServiceIT {

    @Autowired
    private WalletRepository walletRepository;

    @MockBean
    private BitcoinJFacade bitcoinJFacade;

    private BitcoinService bitcoinService;

    @Before
    public void setUp() {
        Context btcContext = new Context(UnitTestParams.get());
        bitcoinService = new BitcoinService(walletRepository, btcContext.getParams(), bitcoinJFacade);
    }

    @After
    public void tearDown() {
        walletRepository.deleteAll();
    }

    @Test
    public void generateWallet() {
        Wallet savedWallet = bitcoinService.generateWallet();
        assertNotNull(savedWallet.getId());
    }
}