package com.jvmp.vouchershop.crypto.btc;

import com.jvmp.vouchershop.exception.InvalidConfigurationException;
import com.jvmp.vouchershop.system.PropertyNames;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class BitcoinJConfig {

    @Bean
    public NetworkParameters networkParameters(@Value(PropertyNames.BITCOIN_NETWORK) String networkType) {
        switch (networkType) {
            case "testnet":
                return TestNet3Params.get();
            case "regtest":
                return RegTestParams.get();
            case "mainnet":
                return MainNetParams.get();
            default:
                throw new InvalidConfigurationException("Correct value for " + PropertyNames.BITCOIN_NETWORK +
                        "property is [regtest|testnet|mainnet]");
        }
    }
}
