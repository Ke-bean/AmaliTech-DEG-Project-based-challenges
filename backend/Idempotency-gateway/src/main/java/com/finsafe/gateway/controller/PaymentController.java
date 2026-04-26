package com.finsafe.gateway.controller;

import com.finsafe.gateway.model.*;
import com.finsafe.gateway.service.IdempotencyService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class PaymentController implements HttpHandler {
    private final IdempotencyService idService = new IdempotencyService();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String key = exchange.getRequestHeaders().getFirst("Idempotency-Key");
            if (key == null) {
                sendResponse(exchange, 400, "Missing Idempotency-Key");
                return;
            }
    
           
            PaymentRequest request = new PaymentRequest(100, "GHS"); 
            PaymentRecord record = idService.getOrLock(key, request);
            
            synchronized (record) {
              
                if (record.getStatus() == PaymentRecord.Status.COMPLETED) {
                    exchange.getResponseHeaders().add("X-Cache-Hit", "true");
                    sendResponse(exchange, 200, record.getResponse());
                    return;
                }
    
             
                if (record.getStatus() == PaymentRecord.Status.IN_PROGRESS) {
                   
                }
    
                
                Thread.sleep(2000);
                String result = "Charged 100 GHS";
                
                idService.complete(key, result);
                sendResponse(exchange, 200, result);
            }
        } catch (Exception e) {
            e.printStackTrace(); 
            try { sendResponse(exchange, 500, "Internal Error"); } catch (Exception ex) {}
        }
    }

    private void sendResponse(HttpExchange exchange, int status, String response) throws Exception {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}