package es.coffeebyt.wtu.fulfillment;

import java.util.Set;

@Deprecated
public interface FulfillmentService {

    Set<Fulfillment> findAll();

    Fulfillment findByOrderId(long orderId);
}