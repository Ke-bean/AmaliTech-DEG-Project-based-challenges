package com.finsafe.gateway.model;

public class PaymentRecord {
    public enum Status {IN_PROGRESS, COMPLETED, FAILED}

    private Status status;
    private PaymentRequest request;
    private String response;

    public PaymentRecord(Status status, PaymentRequest request){
        this.status = status;
        this.request = request;
    }

    public Status getStatus() {return status;}
    public void setStatus(Status status) {this.status = status;}
    public PaymentRequest getReqiest() {return request;}
    public String getResponse() {return response;}
    public void setResponse(String response) {this.response = response;}
}