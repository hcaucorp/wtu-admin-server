package com.jvmp.vouchershop.system;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PropertyNames {
    public final String BITCOIN_NETWORK = "${bitcoinj.network.type}";

    public final String SHOPIFY_SHOP_NAME = "${shopify.shop-name}";
    public final String SHOPIFY_API_KEY = "${shopify.api-key}";
    public final String SHOPIFY_API_PASSWORD = "${shopify.api-password}";
    public final String SHOPIFY_WEBHOOK_SHARED_SECRET = "${shopify.secret}";
    public final String SHOPIFY_LOCATION_ID = "${shopify.location-id}";

    public final String AUTH0_DOMAIN = "${auth0.domain}";
    public final String AUTH0_CLIENT_ID = "${auth0.clientId}";
    public final String AUTH0_CLIENT_SECRET = "${auth0.clientSecret}";
    public final String AUTH0_API_AUDIENCE = "${auth0.apiAudience}";

    public final String AWS_SNS_TOPIC_ORDERS = "${es.coffeebyt.notification.orders-topic-arn}";
    public final String AWS_SNS_TOPIC_REDEMPTIONS = "${es.coffeebyt.notification.redemptions-topic-arn}";
    public final String AWS_SNS_ACCESS_KEY_ID = "${es.coffeebyt.notification.access-key-id}";
    public final String AWS_SNS_SECRET_KEY_ID = "${es.coffeebyt.notification.secret-key-id}";
}
