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
import javax.persistence.Transient;

/**
 * VWallet for "Voucher Wallet" and disctinction from other "wallet" classes in libraries etc.
 */
@Data
@Wither
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "wallets")
public class VWallet {

    @Id
    @GeneratedValue(generator = "wallet_generator")
    @SequenceGenerator(
            name = "wallet_generator",
            sequenceName = "wallet_sequence"
    )
    Long id;

    String description;
    String address;
    String mnemonic;

    @Column(name = "creation_time", nullable = false, updatable = false)
    long creationTime;

    @Transient
    long balance;
}


