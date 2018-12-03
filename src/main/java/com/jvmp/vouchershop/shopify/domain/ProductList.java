package com.jvmp.vouchershop.shopify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ProductList
{
    @JsonProperty(value = JsonConstants.PRODUCTS)
    private List<Product> products;
}
