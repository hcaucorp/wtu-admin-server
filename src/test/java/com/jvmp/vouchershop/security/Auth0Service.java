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

    public TokenResponse getToken() {
        TokenRequest body = new TokenRequest("client_credentials", clientId, clientIdSecret, audience);
        return httpClient.getToken(body);
    }
}
