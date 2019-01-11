package com.jvmp.vouchershop.security;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TokenResponse {
    public String accessToken;
    public String refreshToken;
    public String idToken;
    public String tokenType;
    public long expires_in;
}
