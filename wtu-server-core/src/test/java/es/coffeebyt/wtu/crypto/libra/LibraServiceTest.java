package es.coffeebyt.wtu.crypto.libra;

import es.coffeebyt.wtu.repository.WalletRepository;
import es.coffeebyt.wtu.wallet.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class LibraServiceTest {

    @Mock
    private WalletRepository walletRepository;

    private LibraService subject;

    @Before
    public void setUp() {
        subject = new LibraService("localhost", 666, walletRepository);
    }

    @Test
    public void importWallet() {
    }

    @Ignore
    @Test
    public void generateWallet() {
        Wallet generatedWallet = subject.generateWallet();

        assertNotNull(generatedWallet);

        log.debug(generatedWallet.toString());
    }

    @Test
    public void sendMoney() {
    }

    @Test
    public void getBalance() {
    }

    @Test
    public void acceptsCurrency() {
    }
}
