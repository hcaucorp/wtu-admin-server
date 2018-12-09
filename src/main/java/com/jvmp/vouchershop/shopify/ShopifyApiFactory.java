package com.jvmp.vouchershop.shopify;

import com.jvmp.vouchershop.shopify.interceptors.ContentTypeRequestInterceptor;
import com.jvmp.vouchershop.shopify.interceptors.OAuthRequestInterceptor;
import com.jvmp.vouchershop.shopify.interceptors.RequestLimitInterceptor;
import com.jvmp.vouchershop.shopify.jackson.ShopifyJacksonDecoder;
import com.jvmp.vouchershop.shopify.redisson.ShopifyRedissonManager;
import feign.Feign;
import feign.RequestInterceptor;
import feign.jackson.JacksonEncoder;

import java.util.ArrayList;

public class ShopifyApiFactory
{
    public static ShopifyApiClient create(String accessToken, String myShopifyUrl, String nodeAddress)
    {
        ShopifyRedissonManager shopifyRedissonManager = new ShopifyRedissonManager(nodeAddress, myShopifyUrl);

        // Prepare the request interceptors
        ArrayList<RequestInterceptor> requestInterceptors = new ArrayList<>();

        requestInterceptors.add(new OAuthRequestInterceptor(accessToken));
        requestInterceptors.add(new ContentTypeRequestInterceptor());
        requestInterceptors.add(new RequestLimitInterceptor(shopifyRedissonManager));

        return Feign.builder()
                .decoder(new ShopifyJacksonDecoder(shopifyRedissonManager))
                .encoder(new JacksonEncoder())
                .requestInterceptors(requestInterceptors)
//                .logger(new Logger.JavaLogger().appendToFile("http.log"))
//                .logLevel(Logger.Level.FULL)
                .target(ShopifyApiClient.class, myShopifyUrl);
    }
}
