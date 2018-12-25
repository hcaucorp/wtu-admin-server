package com.jvmp.vouchershop.shopify.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonNaming(SnakeCaseStrategy.class)
@Data
@AllArgsConstructor
public class FulfillmentResource {
    private FulfillmentItem fulfillment;
}
