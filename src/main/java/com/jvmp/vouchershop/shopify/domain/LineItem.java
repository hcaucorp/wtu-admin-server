package com.jvmp.vouchershop.shopify.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.math.BigDecimal;

@Data
@Wither
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LineItem
{
    private long id;

    private long productId;

    private boolean productExists;

    private long variantId;

    private String title;

    private String name;

    private String variantTitle;

    private int quantity;

    private String sku;

    private BigDecimal price;
}
