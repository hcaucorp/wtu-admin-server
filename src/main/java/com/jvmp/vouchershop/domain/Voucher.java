package com.jvmp.vouchershop.domain;

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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Data
@Wither
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vouchers")
@EntityListeners(AuditingEntityListener.class)
public class Voucher implements Serializable {

    @Column(nullable = false, updatable = false)
    @Positive
    long amount;

    @Column(nullable = false, updatable = false, unique = true)
    String code;

    @Column(nullable = false, updatable = false, length = 3)
    @Size(min = 3, max = 3)
    String currency;

    @Id
    @GeneratedValue(generator = "voucher_generator")
    @SequenceGenerator(
            name = "voucher_generator",
            sequenceName = "voucher_sequence"
    )
    Long id;

    @Column(nullable = false)
    boolean published;

    @Column(nullable = false)
    boolean redeemed;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    Date createdAt;

    @Column(name = "expires_at")
    Date expiresAt;

    public Voucher(String code, @Positive long amount, @Size(min = 3, max = 3) String currency) {
        this.amount = amount;
        this.currency = currency;
        this.code = code;
        this.redeemed = false;
        this.published = false;
    }
}
