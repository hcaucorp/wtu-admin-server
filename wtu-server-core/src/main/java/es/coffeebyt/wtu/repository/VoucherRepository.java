package es.coffeebyt.wtu.repository;

import es.coffeebyt.wtu.voucher.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    Optional<Voucher> findByCode(String code);

    List<Voucher> findBySoldTrue();
    List<Voucher> findBySoldFalse();
    List<Voucher> findBySoldFalseAndSku(String sku);

    List<Voucher> findByPublishedFalse();
    List<Voucher> findByPublishedFalseAndSku(String sku);
    List<Voucher> findByPublishedTrue();
    List<Voucher> findByPublishedTrueAndRedeemedFalse();
    List<Voucher> findByPublishedTrueAndSoldFalseAndSku(String sku);

    List<Voucher> findByRedeemedFalse();
    List<Voucher> findByRedeemedFalseAndSku(String sku);

    List<Voucher> findByRedeemedTrue();
    List<Voucher> findByRedeemedTrueAndSku(String sku);

}
