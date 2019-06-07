package com.jvmp.vouchershop.voucher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.annotation.MatchesPattern;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.io.Serializable;

@Data
@Wither
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vouchers")
@EntityListeners(AuditingEntityListener.class)
public class Voucher implements Serializable {

    public static final String CODE_PATTERN = "^wtu[a-z]{3}-[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";

    @Column(nullable = false, updatable = false)
    @Positive
    private long amount;

    @Column(nullable = false, updatable = false, unique = true)
    @NotBlank
    @MatchesPattern(CODE_PATTERN)
    private String code;

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
     * By convention expires after 2 years. Date, no time info.
     */
    @Column(name = "expires_at")
    private long expiresAt;
}
