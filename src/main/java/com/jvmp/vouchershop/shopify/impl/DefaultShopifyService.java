package com.jvmp.vouchershop.shopify.impl;

import com.jvmp.vouchershop.shopify.ShopifyService;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

@Service
public class DefaultShopifyService implements ShopifyService {

    @Override
    public void markOrderFulfilled(long orderId) {
        throw new NotImplementedException(" kevin! ");
    }
}
