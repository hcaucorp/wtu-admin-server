package com.jvmp.vouchershop.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Wither;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Data
@Wither
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "wallets")
@JsonIgnoreProperties(
        value = {"createdAt"},
        allowGetters = true)
public class Wallet implements Serializable {

    @Id
    @GeneratedValue(generator = "wallet_generator")
    @SequenceGenerator(
            name = "wallet_generator",
            sequenceName = "wallet_sequence"
    )
    Long id;

    @Column(nullable = false)
    String description;

    @Column(unique = true)
    String address;

    @Column(nullable = false, updatable = false)
    String mnemonic;

    @Column(nullable = false, updatable = false, length = 3)
    @Size(min = 3, max = 3)
    String currency;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    Date createdAt;
}


