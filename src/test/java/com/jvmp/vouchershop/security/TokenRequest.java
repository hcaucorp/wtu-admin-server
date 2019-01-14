package com.jvmp.vouchershop.security;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TokenRequest {
    public String grantType;
    public String clientId;
    public String clientSecret;
    public String audience;
}
