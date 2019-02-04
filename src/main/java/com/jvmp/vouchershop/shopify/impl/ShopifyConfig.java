package com.jvmp.vouchershop.shopify.impl;

import com.jvmp.vouchershop.shopify.ShopifyApiClient;
import com.jvmp.vouchershop.shopify.ShopifyService;
import com.jvmp.vouchershop.system.PropertyNames;
import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShopifyConfig {

    /**
     * location_id for fulfillment request. Can be found in Shopify's store configuration.
     */
    @Value(PropertyNames.SHOPIFY_LOCATION_ID)
    private long locationId;

    @Value(PropertyNames.SHOPIFY_API_URL)
    private String apiUrl;

    @Value(PropertyNames.SHOPIFY_API_KEY)
    private String apiKey;

    @Value(PropertyNames.SHOPIFY_API_PASSWORD)
    private String apiPassword;

    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor(apiKey, apiPassword);
    }

    @Bean
    public ShopifyService shopifyService(ShopifyApiClient apiClient) {
        return new DefaultShopifyService(apiClient, locationId);
    }
}
