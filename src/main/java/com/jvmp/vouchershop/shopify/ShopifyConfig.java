package com.jvmp.vouchershop.shopify;

import com.jvmp.vouchershop.shopify.filter.WebHookFilter;
import com.jvmp.vouchershop.system.PropertyNames;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShopifyConfig {

//    @Bean
//    public ShopifyApiClient shopifyApiClient(
//            @Value(PropertyNames.SHOPIFY_SHOP_NAME) String shopName,
//            @Value(PropertyNames.SHOPIFY_API_KEY) String apiKey,
//            @Value(PropertyNames.SHOPIFY_ACCESS_TOKEN) String accessToken) {
//        return ShopifyApiFactory.create(shopName, apiKey, accessToken);
//    }

    @Bean
    public FilterRegistrationBean<WebHookFilter> loggingFilter(@Value(PropertyNames.SHOPIFY_WEBHOOK_SHARED_SECRET) String secret) {
        FilterRegistrationBean<WebHookFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new WebHookFilter(secret));
        registrationBean.addUrlPatterns("/shopify/webhook/*");

        return registrationBean;
    }
}
