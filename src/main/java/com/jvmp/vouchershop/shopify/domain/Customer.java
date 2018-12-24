package com.jvmp.vouchershop.shopify.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jvmp.vouchershop.shopify.jackson.FlexDateDeserializer;
import com.jvmp.vouchershop.shopify.jackson.FlexDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.util.Date;
import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Wither
@AllArgsConstructor
@NoArgsConstructor
public class Customer
{
    private long id;

    private String email;

    private String firstName;

    private String lastName;

    private String note;

    private String tags;

    private Boolean acceptsMarketing;

    @JsonDeserialize(using = FlexDateDeserializer.class)
    @JsonSerialize(using = FlexDateSerializer.class)
    private Date createdAt;

    private List<Address> addresses;
}
