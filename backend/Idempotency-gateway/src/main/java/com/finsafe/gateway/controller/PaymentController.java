package com.finsafe.gateway.controller;

import com.finsafe.gateway.model.*;
import com.finsafe.gateway.service.IdempotencyService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.InputStream;
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

            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            
            int amount = 100; 
            if (body.contains("\"amount\":")) {
                String val = body.split("\"amount\":")[1].split("}")[0].trim();
                amount = Integer.parseInt(val);
            }
            PaymentRequest request = new PaymentRequest(amount, "GHS");

            PaymentRecord record = idService.getOrLock(key, request);

            synchronized (record) {
                if (record.getStatus() == PaymentRecord.Status.COMPLETED) {
                    exchange.getResponseHeaders().add("X-Cache-Hit", "true");
                    sendResponse(exchange, 200, record.getResponse());
                    return;
                }

                String result = "Charged " + request.getAmount() + " " + request.getCurrency();
                
                idService.complete(key, result);
                sendResponse(exchange, 200, result);
            }
        } catch (Exception e) {
            String message = e.getMessage();
            int status = (message != null && message.contains("409")) ? 409 : 500;
            try { sendResponse(exchange, status, message != null ? message : "Internal Error"); } catch (Exception ignored) {}
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