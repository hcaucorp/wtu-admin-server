package es.coffeebyt.wtu.repository;

import es.coffeebyt.wtu.wallet.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findOneByCurrency(String currency);

}
