package es.coffeebyt.wtu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.coffeebyt.wtu.fulfillment.Fulfillment;

@Deprecated
@Repository
public interface FulfillmentRepository extends JpaRepository<Fulfillment, Long> {

    Fulfillment findByOrderId(long orderId);
}
