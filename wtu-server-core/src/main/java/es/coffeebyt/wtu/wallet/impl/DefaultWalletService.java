package es.coffeebyt.wtu.wallet.impl;

import java.util.List;
import java.util.Optional;

import es.coffeebyt.wtu.crypto.CurrencyServiceSupplier;
import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.repository.WalletRepository;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.wallet.ImportWalletRequest;
import es.coffeebyt.wtu.wallet.Wallet;
import es.coffeebyt.wtu.wallet.WalletReport;
import es.coffeebyt.wtu.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultWalletService implements WalletService {

    private final WalletRepository walletRepository;
    private final CurrencyServiceSupplier currencyServiceSupplier;
    private final VoucherRepository voucherRepository;

    @Override
    public Wallet importWallet(ImportWalletRequest walletDescription) {
        return currencyServiceSupplier.findByCurrency(walletDescription.currency)
                .importWallet(walletDescription)
                .withMnemonic(null); // remove pk :D
    }

    @Override
    public Wallet generateWallet(String currency) {
        return currencyServiceSupplier.findByCurrency(currency)
                .generateWallet()
                .withMnemonic(null); // remove pk :D
    }

    @Override
    public List<Wallet> findAll() {
        return walletRepository.findAll().parallelStream()
                .map(wallet -> wallet
                        .withBalance(currencyServiceSupplier
                                .findByCurrency(wallet.getCurrency())
                                .getBalance(wallet))
                        .withMnemonic(null) // remove pk :D
                )
                .collect(toList());
    }

    @Override
    public Optional<Wallet> findById(Long id) {
        return walletRepository.findById(id)
                .map(wallet -> wallet
                        .withMnemonic(null) // remove pk
                );
    }

    @Override
    public Wallet save(Wallet w) {
        return walletRepository.save(w)
                .withMnemonic(null); // remove pk
    }

    @Override
    public List<WalletReport> walletStats() {
        List<Voucher> redeemable = voucherRepository.findByPublishedTrueAndRedeemedFalse();
        return findAll().stream()
                .map(wallet -> new WalletReport(
                        wallet,
                        redeemable.stream()
                                .filter(voucher -> voucher.getWalletId() == wallet.getId())
                                .map(Voucher::getAmount)
                                .reduce(0L, Long::sum)
                ))
                .collect(toList());
    }
}
