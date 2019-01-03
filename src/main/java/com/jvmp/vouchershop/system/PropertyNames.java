package com.jvmp.vouchershop.system;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PropertyNames {
    public final String BITCOIN_NETWORK = "${bitcoinj.network.type}";

    public final String SHOPIFY_SHOP_NAME = "${shopify.shop-name}";
    public final String SHOPIFY_API_KEY = "${shopify.api-key}";
    public final String SHOPIFY_API_PASSWORD = "${shopify.api-password}";
    public final String SHOPIFY_WEBHOOK_SHARED_SECRET = "${shopify.secret}";
    public final String SHIPIFY_LOCATION_ID = "${shopify.location-id}";
}
