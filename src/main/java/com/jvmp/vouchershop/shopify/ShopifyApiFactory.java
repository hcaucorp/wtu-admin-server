package com.jvmp.vouchershop.shopify;

import com.jvmp.vouchershop.shopify.interceptors.ContentTypeRequestInterceptor;
import com.jvmp.vouchershop.shopify.interceptors.OAuthRequestInterceptor;
import feign.Feign;
import feign.RequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

import java.util.ArrayList;

public class ShopifyApiFactory
{
    public static ShopifyApiClient create(String accessToken, String myShopifyUrl, String nodeAddress)
    {

        // Prepare the request interceptors
        ArrayList<RequestInterceptor> requestInterceptors = new ArrayList<>();

        requestInterceptors.add(new OAuthRequestInterceptor(accessToken));
        requestInterceptors.add(new ContentTypeRequestInterceptor());

        return Feign.builder()
                .decoder(new JacksonDecoder())
                .encoder(new JacksonEncoder())
                .requestInterceptors(requestInterceptors)
//                .logger(new Logger.JavaLogger().appendToFile("http.log"))
//                .logLevel(Logger.Level.FULL)
                .target(ShopifyApiClient.class, myShopifyUrl);
    }

}
