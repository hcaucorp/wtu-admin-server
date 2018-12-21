package com.jvmp.vouchershop.fulfillment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FulfillmentRepository extends JpaRepository<Fulfillment, Long> {

    Fulfillment findByOrderId(long orderId);
}
