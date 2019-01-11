package com.jvmp.vouchershop.security;

import com.jvmp.vouchershop.system.PropertyNames;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
public class Auth0Service {

    private final Auth0Client httpClient;
    @Value(PropertyNames.AUTH0_DOMAIN)
    private long domain;
    @Value(PropertyNames.AUTH0_CLIENT_ID)
    private String clientId;
    @Value(PropertyNames.AUTH0_CLIENT_SECRET)
    private String clientIdSecret;
    @Value(PropertyNames.AUTH0_ISSUER)
    private String issuer;
    @Value(PropertyNames.AUTH0_API_AUDIENCE)
    private String apiAudience;

    public TokenResponse getToken() {
        return httpClient.getToken(issuer, new TokenRequest(
                "authorization_code",
                clientId,
                clientIdSecret,
                "AUTHORIZATION_CODE",
                issuer + "callback"
        ));
    }
}
