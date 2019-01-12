package com.jvmp.vouchershop.security;

import com.jvmp.vouchershop.system.PropertyNames;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
public class Auth0Service {

    private final Auth0Client httpClient;

    @Value(PropertyNames.AUTH0_API_AUDIENCE)
    private String audience;
    @Value(PropertyNames.AUTH0_CLIENT_ID)
    private String clientId;
    @Value(PropertyNames.AUTH0_CLIENT_SECRET)
    private String clientIdSecret;
    @Value(PropertyNames.AUTH0_ISSUER)
    private String issuer;

    public TokenResponse getToken() {
        String authorize = httpClient.authorize(audience, "read:orders", clientId, "test");
        TokenRequest body = new TokenRequest("authorization_code", clientId, clientIdSecret, "AUTHORIZATION_CODE", issuer + "callback");
        return httpClient.getToken(body);
    }
}
