package com.finsafe.gateway.controller;

import com.finsafe.gateway.model.*;
import com.finsafe.gateway.service.IdempotencyService;

public class PaymentController {
    private final IdempotencyService idService = new IdempotencyService();

    public String processPayment(String key, PaymentRequest request) throws Exception {
        PaymentRecord record = idService.getOrLock(key, request);

        synchronized (record) {
            // 1. Handle In-Flight: Wait until processing is finished
            while (record.getStatus() == PaymentRecord.Status.IN_PROGRESS) {
                record.wait(); 
            }

            // 2. Handle Duplicate Completed Request
            if (record.getStatus() == PaymentRecord.Status.COMPLETED) {
                return "X-Cache-Hit: true | " + record.getResponse();
            }

            // 3. New Request: Process payment
            Thread.sleep(2000); // Simulate processing
            String result = "Charged " + request.getAmount() + " " + request.getCurrency();
            
            idService.complete(key, result);
            record.notifyAll(); // Wake up any waiting threads
            return result;
        }
    }
}