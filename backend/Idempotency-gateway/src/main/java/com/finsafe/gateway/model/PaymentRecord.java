package com.finsafe.gateway.model;

import java.time.Instant;

public class PaymentRecord {
    public enum Status {IN_PROGRESS, COMPLETED, FAILED}

    private Status status;
    private PaymentRequest request;
    private String response;
    private final Instant createdAt;

    public PaymentRecord(Status status, PaymentRequest request){
        this.status = status;
        this.request = request;
        this.createdAt = Instant.now();
    }

    public Status getStatus() {return status;}
    public void setStatus(Status status) {this.status = status;}
    public PaymentRequest getRequest() {return request;}
    public String getResponse() {return response;}
    public void setResponse(String response) {this.response = response;}
    public Instant getCreatedAt() {return createdAt;}
}