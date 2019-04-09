package com.jvmp.vouchershop.shopify.impl;

import com.jvmp.vouchershop.shopify.ShopifyApiClient;
import com.jvmp.vouchershop.shopify.domain.FulfillmentItem;
import com.jvmp.vouchershop.shopify.domain.FulfillmentResource;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DefaultShopifyServiceTest {

    private DefaultShopifyService defaultShopifyService;
    private long locationId;

    @Mock
    private ShopifyApiClient apiClient;

    @Before
    public void setUp() {
        locationId = RandomUtils.nextLong(1, Long.MAX_VALUE);

        defaultShopifyService = new DefaultShopifyService(apiClient, locationId);
    }

    @Test
    public void markOrderFulfilled() {
        long orderNumber = RandomUtils.nextLong(1, Long.MAX_VALUE);

        defaultShopifyService.markOrderFulfilled(orderNumber);

        ArgumentCaptor<FulfillmentResource> captor = ArgumentCaptor.forClass(FulfillmentResource.class);
        verify(apiClient, times(1)).fulfillOrder(eq(orderNumber), captor.capture());

        FulfillmentResource result = captor.getValue();
        assertNotNull(result);

        FulfillmentItem item = result.getFulfillment();

        assertNotNull(item);
        assertEquals(locationId, item.getLocationId());
        assertEquals("Order ID: " + orderNumber, item.getTrackingNumber());
        assertTrue(item.getTrackingUrls().isEmpty()); //TODO change to assertFalse after adding tracking url endpoint
        assertTrue(item.isNotifyCustomer());
    }
}