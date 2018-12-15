package com.jvmp.vouchershop.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Data
@Wither
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "wallets")
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


