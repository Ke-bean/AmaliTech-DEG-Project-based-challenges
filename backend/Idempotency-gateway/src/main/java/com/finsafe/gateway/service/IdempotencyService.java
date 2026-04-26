package com.finsafe.gateway.service;

import com.finsafe.gateway.model.PaymentRecord;
import com.finsafe.gateway.model.PaymentRequest;
import java.util.concurrent.ConcurrentHashMap;

public class IdempotencyService {
    private final ConcurrentHashMap<String, PaymentRecord> store = new ConcurrentHashMap<>();

    public PaymentRecord getOrLock(String key, PaymentRequest request) throws Exception {
        PaymentRecord existing = store.get(key);
        
        if (existing != null) {
            if (!existing.getRequest().equals(request)) {
                throw new Exception("409 Conflict: Idempotency key already used for a different request body.");
            }
        }

        return store.compute(key, (k, record) -> {
            if (record == null) {
                return new PaymentRecord(PaymentRecord.Status.IN_PROGRESS, request);
            }
            return record;
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