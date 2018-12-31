package com.jvmp.vouchershop.voucher.impl;

import lombok.experimental.UtilityClass;

import java.util.Set;
import java.util.stream.Collectors;

import static com.jvmp.vouchershop.Collections.asSet;

@UtilityClass
public class RedemptionUtils {

    private static Set<String> blockChainExplorers = asSet(
            "https://www.blockchain.com/btc/tx/",
            "https://blockexplorer.com/tx/",
            "https://live.blockcypher.com/btc/tx/",
            "https://btc.com/"
    );

    static RedemptionResponse fromTxHash(String txHash) {
        return new RedemptionResponse(
                blockChainExplorers.stream()
                        .map(url -> url + txHash)
                        .collect(Collectors.toList()), txHash
        );
    }
}
