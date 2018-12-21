package com.jvmp.vouchershop.shopify.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jvmp.vouchershop.shopify.jackson.FlexDateDeserializer;
import com.jvmp.vouchershop.shopify.jackson.FlexDateSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@JsonNaming(SnakeCaseStrategy.class)

//@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Order
{
    private long id;

    private String name;

    private BigDecimal totalPrice;

    private BigDecimal discount;

    private FinancialStatus financialStatus;

    private Customer customer;

    private Address billingAddress;

    private Address shippingAddress;

    private List<LineItem> lineItems;

    @JsonDeserialize(using = FlexDateDeserializer.class)
    @JsonSerialize(using = FlexDateSerializer.class)
    private Date createdAt;

    @JsonDeserialize(using = FlexDateDeserializer.class)
    @JsonSerialize(using = FlexDateSerializer.class)
    private Date processedAt;
}
