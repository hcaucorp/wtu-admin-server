package com.jvmp.vouchershop.security;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "auth0")
public interface Auth0Client {

    @Headers("Content-Type: application/json")
    @RequestLine("POST https://{auth0_issuer}/oauth/token")
    TokenResponse getToken(@Param("auth0_issuer") String issuer, TokenRequest tokenRequest);
}
