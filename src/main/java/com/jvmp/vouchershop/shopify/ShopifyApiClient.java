package com.jvmp.vouchershop.shopify;

import com.jvmp.vouchershop.shopify.domain.FulfillmentResource;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "shopify")
public interface ShopifyApiClient {
//    @RequestLine("GET /admin/customers.json?limit={limit}&since_id={since-id}&page={page}&fields={fields}")
//    CustomerList getCustomers(@Param("limit") Integer limit, @Param("since-id") String sinceId, @Param("page") Integer page, @Param("fields") String fields);
//
//    @RequestLine("GET /admin/customers/count.json")
//    Count getCustomersCount();
//
//    @RequestLine("GET /admin/custom_collections.json?limit={limit}&since_id={since-id}&page={page}&fields={fields}")
//    CustomCollectionList getCustomCollections(@Param("limit") Integer limit, @Param("since-id") String sinceId, @Param("page") Integer page, @Param("fields") String fields);
//
//    @RequestLine("GET /admin/custom_collections/count.json")
//    Count getCustomCollectionsCount();
//
//    @RequestLine("GET /admin/smart_collections.json?limit={limit}&since_id={since-id}&page={page}&fields={fields}")
//    SmartCollectionList getSmartCollections(@Param("limit") Integer limit, @Param("since-id") String sinceId, @Param("page") Integer page, @Param("fields") String fields);
//
//    @RequestLine("GET /admin/smart_collections/count.json")
//    Count getSmartCollectionsCount();
//
//    @RequestLine("GET /admin/products.json?limit={limit}&since_id={since-id}&page={page}&fields={fields}")
//    ProductList getProducts(@Param("limit") Integer limit, @Param("since-id") String sinceId, @Param("page") Integer page, @Param("fields") String fields);
//
//    @RequestLine("GET /admin/products/count.json")
//    Count getProductsCount();
//
//    @RequestLine("GET /admin/collects.json?limit={limit}&page={page}&fields={fields}")
//    CollectList getCollects(@Param("limit") Integer limit, @Param("page") Integer page, @Param("fields") String fields);
//
//    @RequestLine("GET /admin/collects/count.json")
//    Count getCollectsCount();
//
//    @RequestLine("GET /admin/orders.json?limit={limit}&since_id={since-id}&page={page}&fields={fields}&status=any")
//    OrderList getOrders(@Param("limit") Integer limit, @Param("since-id") String sinceId, @Param("page") Integer page, @Param("fields") String fields);
//
//    @RequestLine("GET /admin/orders/count.json?status=any")
//    Count getOrdersCount();
//
//    @RequestLine("GET /admin/webhooks.json?limit={limit}&since_id={since-id}&page={page}&fields={fields}")
//    WebhookList getWebhooks(@Param("limit") Integer limit, @Param("since-id") String sinceId, @Param("page") Integer page, @Param("fields") String fields);
//
//    @RequestLine("GET /admin/webhooks/count.json")
//    Count getWebhooksCount();
//
//    @RequestLine("GET /admin/orders/{orderId}/transactions.json?limit={limit}&since_id={since-id}")
//    TransactionList getTransactions(@Param("orderId") String orderId, @Param("limit") Integer limit, @Param("since-id") String sinceId);
//
//    @RequestLine("GET /admin/orders/{orderId}/transactions/count.json")
//    Count getTransactionsCount(@Param("orderId") String orderId);
//
//    @RequestLine("POST /admin/webhooks.json")
//    Webhook createWebhook(Webhook webhook);
//
//    @RequestLine("POST /admin/recurring_application_charges.json")
//    RecurringApplicationChargeResponse createRecurringApplicationCharge(RecurringApplicationChargeRequest request);
//
//    @RequestLine("DELETE /admin/recurring_application_charges/{chargeId}.json")
//    void cancelRecurringApplicationCharge(@Param("chargeId") String chargeId);
//
//    @RequestLine("POST /admin/recurring_application_charges/{chargeId}/activate.json")
//    RecurringApplicationChargeResponse activateRecurringApplicationCharge(@Param("chargeId") String chargeId);
//
//    @RequestLine("GET /admin/recurring_application_charges/{chargeId}.json")
//    RecurringApplicationChargeResponse getRecurringApplicationCharge(@Param("chargeId") String chargeId);

    @RequestMapping(method = RequestMethod.GET,
            value = "/admin/orders/#{order_id}/fulfillments.json",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    FulfillmentResource fulfillAllItems(@PathVariable("order_id") long order_id, FulfillmentResource request);
}
