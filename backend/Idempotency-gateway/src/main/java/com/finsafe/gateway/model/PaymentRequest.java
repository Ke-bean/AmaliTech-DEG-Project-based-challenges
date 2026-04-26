package com.finsafe.gateway.model;

public class PaymentRequest {
    private int amount;
    private String currency;

    // Constructor
    public PaymentRequest(int amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    // Getters - These are what you were missing!
    public int getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}