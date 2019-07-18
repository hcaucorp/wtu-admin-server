package es.coffeebyt.wtu.system;

public class PropertyNames {
    public static final String BITCOIN_NETWORK = "${bitcoinj.network.type}";
    public static final String BITCOINJ_AUTOSTART = "${es.coffeebyt.bitcoinj.auto-start}";

    public static final String AUTH0_DOMAIN = "${auth0.domain}";
    public static final String AUTH0_CLIENT_ID = "${auth0.clientId}";
    public static final String AUTH0_CLIENT_SECRET = "${auth0.clientSecret}";
    public static final String AUTH0_API_AUDIENCE = "${auth0.apiAudience}";

    public static final String AWS_SNS_TOPIC_REDEMPTIONS = "${es.coffeebyt.notification.redemptions-topic-arn}";
    public static final String AWS_SNS_ACCESS_KEY_ID = "${es.coffeebyt.notification.access-key-id}";
    public static final String AWS_SNS_SECRET_KEY_ID = "${es.coffeebyt.notification.secret-key-id}";

    public static final String ENUMERATION_PROTECTION_MAX_ATTEMPTS = "${es.coffeebyt.security.enumeration-protection.max-attempts}";
    public static final String ENUMERATION_PROTECTION_COOL_DOWN_TIME = "${es.coffeebyt.security.enumeration-protection.cool-down-time}";
    /**
     * Any String value of java.util.concurrent.TimeUnit
     **/
    public static final String ENUMERATION_PROTECTION_COOL_DOWN_UNIT = "${es.coffeebyt.security.enumeration-protection.cool-down-unit}";
}
