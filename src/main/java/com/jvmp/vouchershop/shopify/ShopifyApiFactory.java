package com.jvmp.vouchershop.shopify;

import feign.Feign;
import feign.Logger;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ShopifyApiFactory
{
    public ShopifyApiClient create(String apiKey, String apiPassword, String apiUrl)
    {
        BasicAuthRequestInterceptor basicAuthRequestInterceptor = new BasicAuthRequestInterceptor(apiKey, apiPassword);

        return Feign.builder()
                .client(new OkHttpClient())
                .decoder(new JacksonDecoder())
                .encoder(new JacksonEncoder())
                .logger(new Slf4jLogger(ShopifyApiClient.class))
                .logLevel(Logger.Level.FULL)
                .requestInterceptor(basicAuthRequestInterceptor)
                .target(ShopifyApiClient.class, apiUrl);
    }
}
