package com.finsafe.gateway.model;

public class PaymentRequest {
    private int amount;
    private String currency;

   
    public PaymentRequest(int amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public int getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}