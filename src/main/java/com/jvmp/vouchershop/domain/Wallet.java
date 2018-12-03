package com.jvmp.vouchershop.domain;

public class Wallet {

    private String description;
    private String address;
    private String extendedPrivateKey;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getExtendedPrivateKey() {
        return extendedPrivateKey;
    }

    public void setExtendedPrivateKey(String extendedPrivateKey) {
        this.extendedPrivateKey = extendedPrivateKey;
    }
}


