package com.jvmp.vouchershop.crypto.btc;

import com.jvmp.vouchershop.repository.WalletRepository;
import com.jvmp.vouchershop.wallet.WalletService;
import org.bitcoinj.core.NetworkParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.jvmp.vouchershop.RandomUtils.randomString;

@Configuration
public class BitcoinJConfigForTests {

    @Bean
    public WalletService walletService(WalletRepository walletRepository, NetworkParameters networkParameters) {
        return new BtcWalletService(walletRepository, networkParameters, randomString());
    }
}
