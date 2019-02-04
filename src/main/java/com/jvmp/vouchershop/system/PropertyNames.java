package com.jvmp.vouchershop.system;

public class PropertyNames {
    public static final String BITCOIN_NETWORK = "${bitcoinj.network.type}";

    public static final String SHOPIFY_API_URL = "${shopify.api-url}";
    public static final String SHOPIFY_API_KEY = "${shopify.api-key}";
    public static final String SHOPIFY_API_PASSWORD = "${shopify.api-password}";
    public static final String SHOPIFY_WEBHOOK_SHARED_SECRET = "${shopify.secret}";
    public static final String SHOPIFY_LOCATION_ID = "${shopify.location-id}";

    public static final String AUTH0_DOMAIN = "${auth0.domain}";
    public static final String AUTH0_CLIENT_ID = "${auth0.clientId}";
    public static final String AUTH0_CLIENT_SECRET = "${auth0.clientSecret}";
    public static final String AUTH0_API_AUDIENCE = "${auth0.apiAudience}";

    public static final String AWS_SNS_TOPIC_ORDERS = "${es.coffeebyt.notification.orders-topic-arn}";
    public static final String AWS_SNS_TOPIC_REDEMPTIONS = "${es.coffeebyt.notification.redemptions-topic-arn}";
    public static final String AWS_SNS_ACCESS_KEY_ID = "${es.coffeebyt.notification.access-key-id}";
    public static final String AWS_SNS_SECRET_KEY_ID = "${es.coffeebyt.notification.secret-key-id}";
}
