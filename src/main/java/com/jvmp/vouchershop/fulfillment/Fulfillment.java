package com.jvmp.vouchershop.fulfillment;

import com.jvmp.vouchershop.voucher.Voucher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;

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
    private List<Voucher> vouchers;

    @Column(nullable = false, updatable = false)
    private Long orderId;

    @Column(nullable = false, updatable = false)
    private FulfillmentStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "completed_at", nullable = false, updatable = false)
    @CreatedDate
    private Date completedAt;
}
