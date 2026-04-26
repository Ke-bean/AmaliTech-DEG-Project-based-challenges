package com.finsafe.gateway.controller;

import com.finsafe.gateway.model.*;
import com.finsafe.gateway.service.IdempotencyService;

public class PaymentController {
    private final IdempotencyService idService = new IdempotencyService();

    public String processPayment(String key, PaymentRequest request) throws Exception {
        PaymentRecord record = idService.getOrLock(key, request);

        synchronized (record) {
           
            while (record.getStatus() == PaymentRecord.Status.IN_PROGRESS) {
                record.wait(); 
            }

           
            if (record.getStatus() == PaymentRecord.Status.COMPLETED) {
                return "X-Cache-Hit: true | " + record.getResponse();
            }

           
            Thread.sleep(2000); 
            String result = "Charged " + request.getAmount() + " " + request.getCurrency();
            
            idService.complete(key, result);
            record.notifyAll(); 
            return result;
        }
    }
}