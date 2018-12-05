package com.jvmp.vouchershop.domain;

import lombok.*;

import javax.persistence.*;

@Value
@Builder
@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(generator = "wallet_generator")
    @SequenceGenerator(
            name = "wallet_generator",
            sequenceName = "wallet_sequence"
    )
    public final Long id;

    public final String description;
    public final String address;
    public final String extendedPrivateKey;

    @Transient
    public final org.bitcoinj.wallet.Wallet btcWallet;
}


