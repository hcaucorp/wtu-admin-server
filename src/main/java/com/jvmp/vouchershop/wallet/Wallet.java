package com.jvmp.vouchershop.wallet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
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

    /**
     * In millis. Minimum value 1322697600000L (12/01/2011 @ 12:00am (UTC))
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Min(1322697600000L) //
    private long createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Wallet wallet = (Wallet) o;

        if (!mnemonic.equals(wallet.mnemonic)) return false;
        return currency.equals(wallet.currency);
    }

    @Override
    public int hashCode() {
        int result = mnemonic.hashCode();
        result = 31 * result + currency.hashCode();
        return result;
    }
}


