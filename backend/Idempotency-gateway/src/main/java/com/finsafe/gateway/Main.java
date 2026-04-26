package com.finsafe.gateway;

import com.finsafe.gateway.controller.PaymentController;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        PaymentController controller = new PaymentController();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/process-payment", controller::handle);
        server.setExecutor(null); 
        System.out.println("FinSafe Idempotency Gateway running on port 8080...");
        server.start();
    }
}