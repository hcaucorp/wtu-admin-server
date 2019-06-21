package com.jvmp.vouchershop.voucher.impl;

import com.google.common.collect.ImmutableMap;
import lombok.experimental.UtilityClass;
import org.bitcoinj.core.NetworkParameters;

import java.util.Map;
import java.util.Set;

import static com.jvmp.Collections.asSet;

@UtilityClass
public class RedemptionUtils {

    public static final Map<String, Set<String>> blockExploresByNetworkId = ImmutableMap.<String, Set<String>>builder()
            .put(NetworkParameters.ID_MAINNET, asSet(
                    "https://www.blockchain.com/btc/tx/%s",
                    "https://blockexplorer.com/tx/%s",
                    "https://live.blockcypher.com/btc/tx/%s",
                    "https://btc.com/%s"
            ))
            .put(NetworkParameters.ID_TESTNET, asSet(
                    "https://live.blockcypher.com/btc-testnet/tx/%s"
            ))
            .put(cash.bitcoinj.core.NetworkParameters.ID_MAINNET, asSet(
                    "https://www.blockchain.com/bch/tx/%s"
            ))
            .put(cash.bitcoinj.core.NetworkParameters.ID_TESTNET, asSet(
                    "https://blockexplorer.one/bitcoin-cash/testnet/tx/%s"
            ))
            .build();
}
