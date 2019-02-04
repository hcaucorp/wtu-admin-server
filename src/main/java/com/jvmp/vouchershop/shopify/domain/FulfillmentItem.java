package com.jvmp.vouchershop.shopify.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.util.List;

/**
 * Source <a href="https://help.shopify.com/en/api/reference/shipping-and-fulfillment/fulfillment#create">https://help.shopify.com/en/api/reference/shipping-and-fulfillment/fulfillment#create</a>
 *
 * Fulfill all line items for an order and send the shipping confirmation email. Not specifying line item IDs causes all unfulfilled and partially
 * fulfilled line items for the order to be fulfilled.
 * <pre>
 * POST /admin/orders/#{order_id}/fulfillments.json
 * {
 *      "fulfillment": {
 *          "location_id": 905684977,
 *          "tracking_number": "123456789",
 *          "tracking_urls": [
 *              "https://shipping.xyz/track.php?num=123456789",
 *              "https://anothershipper.corp/track.php?code=abc"
 *          ],
 *          "notify_customer": true
 *      }
 * }
 * </pre>
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Wither
@AllArgsConstructor
@NoArgsConstructor
public class FulfillmentItem {
    private long locationId;

    private String trackingNumber;

    private List<String> trackingUrls;

    private boolean notifyCustomer = true;
}
