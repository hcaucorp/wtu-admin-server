package com.jvmp.vouchershop;

import com.jvmp.vouchershop.shopify.domain.CollectList;
import com.jvmp.vouchershop.shopify.domain.Count;
import com.jvmp.vouchershop.shopify.domain.CustomCollectionList;
import com.jvmp.vouchershop.shopify.domain.CustomerList;
import com.jvmp.vouchershop.shopify.domain.OrderList;
import com.jvmp.vouchershop.shopify.domain.ProductList;
import com.jvmp.vouchershop.shopify.domain.RecurringApplicationChargeRequest;
import com.jvmp.vouchershop.shopify.domain.RecurringApplicationChargeResponse;
import com.jvmp.vouchershop.shopify.domain.SmartCollectionList;
import com.jvmp.vouchershop.shopify.domain.TransactionList;
import com.jvmp.vouchershop.shopify.domain.Webhook;
import com.jvmp.vouchershop.shopify.domain.WebhookList;
import feign.Param;
import feign.RequestLine;

public interface ShopifyApiClient
{
    @RequestLine("GET /admin/customers.json?limit={limit}&since_id={since-id}&page={page}&fields={fields}")
    CustomerList getCustomers(@Param("limit") Integer limit, @Param("since-id") String sinceId, @Param("page") Integer page, @Param("fields") String fields);

    @RequestLine("GET /admin/customers/count.json")
    Count getCustomersCount();

    @RequestLine("GET /admin/custom_collections.json?limit={limit}&since_id={since-id}&page={page}&fields={fields}")
    CustomCollectionList getCustomCollections(@Param("limit") Integer limit, @Param("since-id") String sinceId, @Param("page") Integer page, @Param("fields") String fields);

    @RequestLine("GET /admin/custom_collections/count.json")
    Count getCustomCollectionsCount();

    @RequestLine("GET /admin/smart_collections.json?limit={limit}&since_id={since-id}&page={page}&fields={fields}")
    SmartCollectionList getSmartCollections(@Param("limit") Integer limit, @Param("since-id") String sinceId, @Param("page") Integer page, @Param("fields") String fields);

    @RequestLine("GET /admin/smart_collections/count.json")
    Count getSmartCollectionsCount();

    @RequestLine("GET /admin/products.json?limit={limit}&since_id={since-id}&page={page}&fields={fields}")
    ProductList getProducts(@Param("limit") Integer limit, @Param("since-id") String sinceId, @Param("page") Integer page, @Param("fields") String fields);

    @RequestLine("GET /admin/products/count.json")
    Count getProductsCount();

    @RequestLine("GET /admin/collects.json?limit={limit}&page={page}&fields={fields}")
    CollectList getCollects(@Param("limit") Integer limit, @Param("page") Integer page, @Param("fields") String fields);

    @RequestLine("GET /admin/collects/count.json")
    Count getCollectsCount();

    @RequestLine("GET /admin/orders.json?limit={limit}&since_id={since-id}&page={page}&fields={fields}&status=any")
    OrderList getOrders(@Param("limit") Integer limit, @Param("since-id") String sinceId, @Param("page") Integer page, @Param("fields") String fields);

    @RequestLine("GET /admin/orders/count.json?status=any")
    Count getOrdersCount();

    @RequestLine("GET /admin/webhooks.json?limit={limit}&since_id={since-id}&page={page}&fields={fields}")
    WebhookList getWebhooks(@Param("limit") Integer limit, @Param("since-id") String sinceId, @Param("page") Integer page, @Param("fields") String fields);

    @RequestLine("GET /admin/webhooks/count.json")
    Count getWebhooksCount();

    @RequestLine("GET /admin/orders/{orderId}/transactions.json?limit={limit}&since_id={since-id}")
    TransactionList getTransactions(@Param("orderId") String orderId, @Param("limit") Integer limit, @Param("since-id") String sinceId);

    @RequestLine("GET /admin/orders/{orderId}/transactions/count.json")
    Count getTransactionsCount(@Param("orderId") String orderId);

    @RequestLine("POST /admin/webhooks.json")
    Webhook createWebhook(Webhook webhook);

    @RequestLine("POST /admin/recurring_application_charges.json")
    RecurringApplicationChargeResponse createRecurringApplicationCharge(RecurringApplicationChargeRequest request);

    @RequestLine("DELETE /admin/recurring_application_charges/{chargeId}.json")
    void cancelRecurringApplicationCharge(@Param("chargeId") String chargeId);

    @RequestLine("POST /admin/recurring_application_charges/{chargeId}/activate.json")
    RecurringApplicationChargeResponse activateRecurringApplicationCharge(@Param("chargeId") String chargeId);

    @RequestLine("GET /admin/recurring_application_charges/{chargeId}.json")
    RecurringApplicationChargeResponse getRecurringApplicationCharge(@Param("chargeId") String chargeId);
}
