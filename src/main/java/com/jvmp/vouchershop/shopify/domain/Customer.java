package com.jvmp.vouchershop.shopify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jvmp.vouchershop.shopify.jackson.FlexDateDeserializer;
import com.jvmp.vouchershop.shopify.jackson.FlexDateSerializer;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Customer
{
    @JsonProperty(value = JsonConstants.ID)
    private long id;

    @JsonProperty(value = JsonConstants.EMAIL)
    private String email;

    @JsonProperty(value = JsonConstants.FIRST_NAME)
    private String firstName;

    @JsonProperty(value = JsonConstants.LAST_NAME)
    private String lastName;

    @JsonProperty(value = JsonConstants.NOTE)
    private String note;

    @JsonProperty(value = JsonConstants.TAGS)
    private String tags;

    @JsonProperty(value = JsonConstants.ACCEPTS_MARKETING)
    private Boolean acceptsMarketing;

    @JsonProperty(value = JsonConstants.CREATED_AT)
    @JsonDeserialize(using = FlexDateDeserializer.class)
    @JsonSerialize(using = FlexDateSerializer.class)
    private Date createdAt;

    @JsonProperty(value = JsonConstants.ADDRESSES)
    private List<Address> addresses;
}
