package com.finsafe.gateway.service;

import com.finsafe.gateway.model.PaymentRecord;
import com.finsafe.gateway.model.PaymentRequest;
import java.util.concurrent.ConcurrentHashMap;

public class IdempotencyService {
    private final ConcurrentHashMap<String, PaymentRecord> store = new ConcurrentHashMap<>();

    public PaymentRecord getOrLock(String key, PaymentRequest request) {
      
        return store.compute(key, (k, existing) -> {
            if (existing == null) {
                return new PaymentRecord(PaymentRecord.Status.IN_PROGRESS, request);
            }
            return existing;
        });
    }

    public void complete(String key, String response) {
        PaymentRecord record = store.get(key);
        if (record != null) {
            record.setResponse(response);
            record.setStatus(PaymentRecord.Status.COMPLETED);
        }
    }
}