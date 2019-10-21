package es.coffeebyt.wtu.system.patching.tasks.deprecated;

import java.util.ArrayList;
import java.util.List;

import cash.bitcoinj.core.AddressFormatException;
import cash.bitcoinj.core.CashAddressFactory;
import cash.bitcoinj.core.NetworkParameters;
import es.coffeebyt.wtu.repository.WalletRepository;
import es.coffeebyt.wtu.system.patching.PatchingResult;
import es.coffeebyt.wtu.system.patching.PatchingTask;
import lombok.RequiredArgsConstructor;

import static es.coffeebyt.wtu.crypto.bch.BitcoinCashService.BCH;
import static java.lang.String.format;

//@Component
@RequiredArgsConstructor
public class MigrateBitcoinCashWalletAddress implements PatchingTask {

    private final WalletRepository walletRepository;
    private final NetworkParameters params;

    @Override public List<PatchingResult> call() {

        CashAddressFactory cashAddressFactory = new CashAddressFactory();
        List<PatchingResult> results = new ArrayList<>();

        walletRepository.findOneByCurrency(BCH)
                .map(wallet -> {
                    String walletAddress = wallet.getAddress();
                    try {
                        String cashAddr = cashAddressFactory.getFromBase58(params, walletAddress).toString();
                        wallet.setAddress(cashAddr);
                        results.add(new PatchingResult(
                                "Wallet: " + wallet.toString(),
                                wallet.getId().toString(),
                                format("Address migrated from %s to %s", walletAddress, cashAddr)
                        ));
                        return wallet;
                    } catch (AddressFormatException e) {
                        return null;
                    }
                })
                .ifPresent(walletRepository::save);

        return results;
    }
}
