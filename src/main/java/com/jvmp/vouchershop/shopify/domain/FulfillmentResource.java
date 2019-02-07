package com.jvmp.vouchershop.shopify.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(SnakeCaseStrategy.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FulfillmentResource {
    private FulfillmentItem fulfillment;
}
