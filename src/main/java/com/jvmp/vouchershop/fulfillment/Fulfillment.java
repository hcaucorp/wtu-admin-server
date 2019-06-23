package com.jvmp.vouchershop.fulfillment;

import com.jvmp.vouchershop.voucher.Voucher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.util.Set;

@Data
@Wither
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "fulfillment")
@EntityListeners(AuditingEntityListener.class)
public class Fulfillment {

    @Id
    @GeneratedValue(generator = "fulfillment_generator")
    @SequenceGenerator(
            name = "fulfillment_generator",
            sequenceName = "fulfillment_sequence"
    )
    private Long id;

    @OneToMany
    private Set<Voucher> vouchers;

    @Column(name = "order_id", nullable = false, updatable = false)
    private Long orderId;

    @Column(name = "orderNumber")
    private Long orderNumber;

    @Column(name = "completed_at", nullable = false, updatable = false)
    @CreatedDate
    @Min(1322697600) // 12/01/2011 @ 12:00am (UTC)
    private long completedAt;
}
