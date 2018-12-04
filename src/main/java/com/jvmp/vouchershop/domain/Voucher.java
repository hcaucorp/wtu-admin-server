package com.jvmp.vouchershop.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Date;

@Value
@Builder
@Entity
@Table(name = "vouchers")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
        value = {"createdAt"},
        allowGetters = true
)
public class Voucher implements Serializable {

    public final long amount;

    @NotBlank
    @Size(min = 3, max = 3)
    public final String currency;

    @Id
    @GeneratedValue(generator = "voucher_generator")
    @SequenceGenerator(
            name = "voucher_generator",
            sequenceName = "voucher_sequence"
    )
    public final Long id;
    public final boolean published;
    public final boolean redeemed;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private Date createdAt;
}
