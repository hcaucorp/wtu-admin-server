package es.coffeebyt.wtu.controller;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.bitcoinj.core.NetworkParameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import es.coffeebyt.wtu.Application;
import es.coffeebyt.wtu.crypto.btc.BitcoinJFacade;
import es.coffeebyt.wtu.repository.WalletRepository;
import es.coffeebyt.wtu.security.TestSecurityConfig;
import es.coffeebyt.wtu.utils.RandomUtils;
import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import es.coffeebyt.wtu.wallet.WalletService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        Application.class, TestSecurityConfig.class
})
@AutoConfigureMockMvc
@ActiveProfiles("unit-test")
public class WalletControllerTest {

    private final static String baseUrl = "/api";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WalletRepository walletRepository;

    @MockBean
    private WalletService walletService;

    @MockBean
    private BitcoinJFacade bitcoinJFacade;

    @Autowired
    private NetworkParameters networkParameters;

    @Before
    public void setUp() {
        when(bitcoinJFacade.getBalance()).thenReturn(1L);
    }

    @Test
    public void getAllWallets() throws Exception {
        mvc.perform(get(baseUrl + "/wallets"))
                .andExpect(status().isOk());

        verify(walletService, times(1)).findAll();
    }

    @Test
    public void generateWallet() throws Exception {
        Wallet wallet = RandomUtils.randomWallet(networkParameters)
                .withCurrency("BTC");
        when(walletRepository.save(ArgumentMatchers.any(Wallet.class))).thenReturn(wallet);

        mvc.perform(post(baseUrl + "/wallets")
                .content("BTC"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(empty())))
                .andExpect(jsonPath("$.address", not(empty())))
                .andExpect(jsonPath("$.balance", notNullValue()))
                .andExpect(jsonPath("$.currency", is("BTC")))
                .andExpect(jsonPath("$.createdAt", greaterThan(1322697600000L)))
                .andExpect(jsonPath("$.mnemonic", not(empty())));

        mvc.perform(post(baseUrl + "/wallets")
                .content("ETH"))
                .andExpect(status().isBadRequest());

        mvc.perform(post(baseUrl + "/wallets")
                .content("XYZ"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void importWallet() throws Exception {
        when(walletRepository.findAll()).thenReturn(emptyList());
        when(walletRepository.save(ArgumentMatchers.any(Wallet.class))).thenReturn(new Wallet());

        mvc.perform(put(baseUrl + "/wallets")
                .content(objectMapper.writeValueAsString(new ImportWalletRequest(
                        "BTC",
                        "olive poet gather huge museum jewel cute giant rent canvas mask lift",
                        1546175793)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void importWallet_unsupportedCurrency_shouldFail() throws Exception {
        when(walletRepository.findAll()).thenReturn(emptyList());

        mvc.perform(put(baseUrl + "/wallets")
                .content(objectMapper.writeValueAsString(new ImportWalletRequest(
                        "ZZZ",
                        "olive poet gather huge museum jewel cute giant rent canvas mask lift",
                        1546175793)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}