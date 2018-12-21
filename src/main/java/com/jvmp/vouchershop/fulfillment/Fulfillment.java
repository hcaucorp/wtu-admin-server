package com.jvmp.vouchershop.fulfillment;

import com.jvmp.vouchershop.domain.Voucher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Fulfillment {

    @Id
    @GeneratedValue(generator = "fulfillment_generator")
    @SequenceGenerator(
            name = "fulfillment_generator",
            sequenceName = "fulfillment_sequence"
    )
    Long id;

    Set<Voucher> vouchers;

    long orderId;

    FulfillmentStatus status;
}
