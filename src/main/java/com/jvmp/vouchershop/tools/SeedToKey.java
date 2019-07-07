package com.jvmp.vouchershop.tools;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;

import static java.lang.Long.parseLong;
import static org.bitcoinj.wallet.Wallet.fromSeed;

@Slf4j
public class SeedToKey {

    public static void main(String[] args) throws UnreadableWalletException {

        if (args.length != 2) {
            log.error("Usage: SeedToKey birthdaySeconds csvMnemonic");
        }

        long creationTime = parseLong(args[0]);
        String mnemonic = String.join(" ", args[1].split(","));

        NetworkParameters params = MainNetParams.get();

        DeterministicSeed deterministicSeed = new DeterministicSeed(mnemonic, null, "", creationTime);
        Wallet wallet = fromSeed(params, deterministicSeed);

        DeterministicKey watchingKey = wallet.getWatchingKey();

        // Get the standardised base58 encoded serialization
        log.info("Watching key data: " + watchingKey.serializePubB58(params));
        log.info("Master key data: " + watchingKey.serializePrivB58(params));
    }
}
