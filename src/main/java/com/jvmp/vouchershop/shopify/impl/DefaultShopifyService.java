package com.jvmp.vouchershop.shopify.impl;

import com.jvmp.vouchershop.shopify.ShopifyApiClient;
import com.jvmp.vouchershop.shopify.ShopifyApiFactory;
import com.jvmp.vouchershop.shopify.ShopifyService;
import com.jvmp.vouchershop.shopify.domain.Fulfillment;
import com.jvmp.vouchershop.shopify.domain.FulfillmentResource;
import org.apache.commons.lang3.NotImplementedException;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomUtils.nextLong;


class DefaultShopifyService implements ShopifyService {

    private final String shopUrl;
    private final ShopifyApiClient apiClient;

    public DefaultShopifyService(String shopName, String apiKey, String apiPassword) {
        shopUrl = "https://" + shopName + ".myshopify.com/";
        apiClient = ShopifyApiFactory.create(apiKey, apiPassword, shopUrl);
    }

    public Fulfillment create(long orderId, long locationId) {
        return new Fulfillment(
                nextLong(0, Long.MAX_VALUE),
                orderId,
                locationId,
                "" + orderId, // use orderId as tracking number
                singletonList(shopUrl + "orders/" + orderId),
                true
        );
    }

    @Override
    public void markOrderFulfilled(Fulfillment fulfillment) {

        apiClient.fulfillAllItems(fulfillment.getOrderId(), new FulfillmentResource(fulfillment));

        /*
        * Fulfill all line items for an order and send the shipping confirmation email. Not specifying line item IDs causes all unfulfilled and partially
         * fulfilled line items for the order to be fulfilled.

POST /admin/orders/#{order_id}/fulfillments.json
{
  "fulfillment": {
    "location_id": 905684977,
    "tracking_number": "123456789",
    "tracking_urls": [
      "https://shipping.xyz/track.php?num=123456789",
      "https://anothershipper.corp/track.php?code=abc"
    ],
    "notify_customer": true
  }
}
*/


        // TODO Note
        //
        //If you are using this endpoint with a Partner development store or a trial store, then you can create no more than 5 new fulfillments per minute.

        // TODO tracking urls: provide endpoint to check delivery status/info based on orderId from shopify

        throw new NotImplementedException(" kevin! ");
    }
}
