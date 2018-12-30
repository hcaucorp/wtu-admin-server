package com.jvmp.vouchershop.wallet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@Wither
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "wallets")
@EntityListeners(AuditingEntityListener.class)
public class Wallet implements Serializable {

    @Id
    @GeneratedValue(generator = "wallet_generator")
    @SequenceGenerator(
            name = "wallet_generator",
            sequenceName = "wallet_sequence"
    )
    private Long id;

    @Column(unique = true)
    private String address;

    @Transient
    private long balance;

    @Column(nullable = false, updatable = false)
    private String mnemonic;

    @Column(nullable = false, updatable = false, length = 3)
    @Size(min = 3, max = 3)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    @Min(1322697600) // 12/01/2011 @ 12:00am (UTC)
    private long createdAt;
}


