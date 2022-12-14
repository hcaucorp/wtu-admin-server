package es.coffeebyt.wtu.crypto.bch;

import cash.bitcoinj.core.NetworkParameters;
import cash.bitcoinj.kits.WalletAppKit;
import cash.bitcoinj.params.MainNetParams;
import cash.bitcoinj.params.RegTestParams;
import cash.bitcoinj.params.TestNet3Params;
import es.coffeebyt.wtu.exception.InvalidConfigurationException;
import es.coffeebyt.wtu.system.PropertyNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Slf4j
@Configuration
public class BitcoinCashJConfig {

    @Value(PropertyNames.BITCOIN_NETWORK)
    private String networkType;

    @Bean("BitcoinCashNetworkProperties")
    public NetworkParameters networkParameters() {
        switch (networkType) {
            case "testnet":
                return TestNet3Params.get();
            case "regtest":
                return RegTestParams.get();
            case "mainnet":
                return MainNetParams.get();
            default:
                throw new InvalidConfigurationException("Correct value for " + PropertyNames.BITCOIN_NETWORK + "property is [regtest|testnet|mainnet]");
        }
    }

    @Bean("BitcoinCashWalletAppKit")
    public WalletAppKit walletAppKit(NetworkParameters networkParameters) {
        String fileSuffix = networkType + "wallet.storage";

        return new WalletAppKit(networkParameters, new File(".bch"), fileSuffix);
    }
}
