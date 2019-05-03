package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.crypto.btc.BitcoinJAdapter;
import com.jvmp.vouchershop.exception.CurrencyNotSupported;
import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.security.TestSecurityConfig;
import com.jvmp.vouchershop.wallet.Wallet;
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

import static com.jvmp.vouchershop.utils.RandomUtils.randomWallet;
import static java.util.Collections.emptyList;
import static junit.framework.TestCase.fail;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        Application.class, TestSecurityConfig.class
})
@AutoConfigureMockMvc
@ActiveProfiles("unit-test")
public class WalletControllerTest {

    private final static String baseUrl = "/api";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WalletRepository walletRepository;

    @MockBean
    private BitcoinJAdapter bitcoinJAdapter;

    @Autowired
    private NetworkParameters networkParameters;

    @Before
    public void setUp() {
        when(bitcoinJAdapter.getBalance()).thenReturn(1L);
    }

    @Test
    public void getAllWallets() throws Exception {
        mvc.perform(get(baseUrl + "/wallets"))
                .andExpect(status().isOk());
    }

    @Test
    public void generateWallet() throws Exception {
        Wallet wallet = randomWallet(networkParameters)
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

        fail("apply pojo ");
        fail("test currency service resolution");
        mvc.perform(put(baseUrl + "/wallets")
                //todo: create a pojo for this
                .content("{\n" +
                        "            \"currency\": \"BTC\",\n" +
                        "            \"mnemonic\": \"olive poet gather huge museum jewel cute giant rent canvas mask lift\",\n" +
                        "            \"createdAt\": 1546175793\n" +
                        "        }")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test(expected = CurrencyNotSupported.class)
    public void importWallet_unsupportedCurrency_shouldFail() {
        fail("anyway");
    }
}