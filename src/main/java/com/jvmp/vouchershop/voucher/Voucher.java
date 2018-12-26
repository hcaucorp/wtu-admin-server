package com.jvmp.vouchershop.voucher;

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
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.io.Serializable;

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
    private long amount;

    @Column(nullable = false, updatable = false, unique = true)
    @NotBlank
    private String code;

    @Column(nullable = false, updatable = false, length = 3)
    @Size(min = 3, max = 3)
    private String currency;

    @Id
    @GeneratedValue(generator = "voucher_generator")
    @SequenceGenerator(
            name = "voucher_generator",
            sequenceName = "voucher_sequence"
    )
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private long walletId;

    @Column(nullable = false)
    private boolean published;

    @Column(nullable = false)
    private boolean redeemed;

    @Column(nullable = false)
    private boolean sold;

    @Column(nullable = false, updatable = false)
    @NotBlank
    private String sku;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    @Min(1322697600) // 12/01/2011 @ 12:00am (UTC)
    private long createdAt;

    /**
     * by convention expires after a year
     */
    @Column(name = "expiration_days")
    @Positive
    private long expirationDays = 365;
}
