package com.finsafe.gateway.service;

import com.finsafe.gateway.model.PaymentRecord;
import com.finsafe.gateway.model.PaymentRequest;
import java.util.concurrent.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class IdempotencyService {
    private final ConcurrentHashMap<String, PaymentRecord> store = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public IdempotencyService() {
        scheduler.scheduleAtFixedRate(this::cleanupExpiredRecords, 1, 1, TimeUnit.HOURS);
    }

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

    private void cleanupExpiredRecords() {
        Instant expiryTime = Instant.now().minus(24, ChronoUnit.HOURS);
        store.entrySet().removeIf(entry -> entry.getValue().getCreatedAt().isBefore(expiryTime));
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}