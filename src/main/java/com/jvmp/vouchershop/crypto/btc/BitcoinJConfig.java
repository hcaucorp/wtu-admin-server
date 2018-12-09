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

/**
 * Created by Hubert Czerpak on 2018-12-08
 */
@Slf4j
@Configuration
public class BitcoinJConfig {

    private final static String BLOCK_CHAIN_FILE_PREFIX = "voucher-shop-";

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
//
//    @Bean
//    public WalletAppKit walletAppKit(NetworkParameters networkParameters) {
//        // Start up a basic app using a class that automates some boilerplate. Ensure we always have at least one key.
//        WalletAppKit kit = new WalletAppKit(networkParameters, new File("bitcoin-blockchain"), BLOCK_CHAIN_FILE_PREFIX) {
//            @Override
//            protected void onSetupCompleted() {
//                // This is called in a background thread after startAndWait is called, as setting up various objects
//                // can do disk and network IO that may cause UI jank/stuttering in wallet apps if it were to be done
//                // on the main thread.
//                if (wallet().getKeyChainGroupSize() < 1)
//                    wallet().importKey(new ECKey());
//            }
//        };
//
//        if (networkParameters == RegTestParams.get()) {
//            // Regression test mode is designed for testing and development only, so there's no public network for it.
//            // If you pick this mode, you're expected to be running a local "bitcoind -regtest" instance.
//            kit.connectToLocalHost();
//        }
//
//        // Download the block chain and wait until it's done.
//        log.info("Beginning to download block chain.");
//        kit.startAsync();
//        kit.awaitRunning();
//        log.info("Block chain download complete.");
//
//        return kit;
//    }
}
