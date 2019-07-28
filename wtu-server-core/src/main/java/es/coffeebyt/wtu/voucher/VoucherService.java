package es.coffeebyt.wtu.voucher;

import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;
import es.coffeebyt.wtu.voucher.impl.RedemptionResponse;
import es.coffeebyt.wtu.voucher.impl.VoucherGenerationSpec;

import java.util.List;
import java.util.Optional;

public interface VoucherService {
    List<Voucher> generateVouchers(VoucherGenerationSpec details);

    List<Voucher> findAll();

    void deleteBySku(String sku);

    void publishBySku(String sku);

    void unPublishBySku(String sku);

    void save(List<Voucher> vouchers);

    /**
     * Sends money represented by the voucher to a prescribed recepient (wallet address)
     *
     * @param detail Form data from redemption ui
     * @return A transaction identifier, currency agnostic, a way to uniquely identify transaction and provide a tracking info or kind of proof the transaction
     * has been performed. For BTC/~BCH it would be a transaction hash which allows to see the transaciton in the blockchain (blockchain explorer link for
     * tracking information)
     */
    RedemptionResponse redeemVoucher(RedemptionRequest detail);

    Optional<Voucher> findByCode(String voucherCode);

    List<Voucher> findBy(boolean showRedeemed, String sku);
}
