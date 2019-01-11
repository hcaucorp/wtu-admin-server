package com.jvmp.vouchershop.security;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TokenRequest {
    public String grantType;
    public String clientId;
    public String clientSecret;
    public String code;
    public String redirectUri;
}
