package com.jvmp.vouchershop.shopify.impl;

import com.jvmp.vouchershop.shopify.ShopifyService;
import com.jvmp.vouchershop.system.PropertyNames;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ShopifyConfig {

    /**
     * location_id for fulfillment request. Can be found in Shopify's store configuration.
     */
    @Value(PropertyNames.SHIPIFY_LOCATION_ID)
    private long locationId;

    @Value(PropertyNames.SHOPIFY_SHOP_NAME)
    private String shopName;

    @Value(PropertyNames.SHOPIFY_API_KEY)
    private String apiKey;

    @Value(PropertyNames.SHOPIFY_API_PASSWORD)
    private String apiPassword;

    @Bean
    public ShopifyService shopifyService() {
        return new ThrottlingShopifyService(new DefaultShopifyService(shopName, apiKey, apiPassword, locationId), Duration.ofSeconds(12));
    }
}
