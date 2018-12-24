package com.jvmp.vouchershop.shopify.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Wither
@AllArgsConstructor
@NoArgsConstructor
public class Fulfillment {

    //only needed are:
//    "location_id": 905684977,
//            "tracking_number": "123456789",
//            "tracking_urls": [
//            "https://shipping.xyz/track.php?num=123456789",
//            "https://anothershipper.corp/track.php?code=abc"
//            ],
//            "notify_customer": true
    private long id;

    private long orderId;

    private long locationId;

    private String trackingNumber;

    private List<String> trackingUrls;

    private boolean notifyCustomer = true;

//
//    "id": 1022782919,
//            "order_id": 450789469,
//            "status": "success",
//            "created_at": "2018-11-14T16:27:44-05:00",
//            "service": "manual",
//            "updated_at": "2018-11-14T16:27:44-05:00",
//            "tracking_company": "Bluedart",
//            "shipment_status": null,
//            "location_id": 905684977,
//            "tracking_number": "123456789",
//            "tracking_numbers": [
//    "tracking_url": "https://shipping.xyz/track.php?num=123456789",
//            "tracking_urls": [
//            "https://shipping.xyz/track.php?num=123456789",
//            "https://anothershipper.corp/track.php?code=abc"
//            ],
//            "receipt": {},
//        "name": "#1001.1",
//        "admin_graphql_api_id": "gid://shopify/Fulfillment/1022782919",
}
