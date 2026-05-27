package com.amijayquiz.controllers;

import com.amijayquiz.models.PaymentStatus;
import com.amijayquiz.services.PaymentService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/paiements/webhook")
public class WebhookServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Lire le corps de la requête
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        
        String body = sb.toString();
        
        // Journaliser le webhook reçu (pour debugging)
        System.out.println("[Webhook] Reçu: " + body);
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        try {
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            
            String status = json.get("status").getAsString();
            String reference = json.get("reference").getAsString();
            String transactionId = json.has("transactionId") ? json.get("transactionId").getAsString() : "";
            
            PaymentStatus paymentStatus = PaymentStatus.fromCode(status);
            
            // Traiter la confirmation
            boolean processed = PaymentService.processPaymentConfirmation(reference, transactionId, paymentStatus);
            
            if (processed) {
                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("status", "OK");
                out.print(responseJson.toString());
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("status", "ERROR");
                responseJson.addProperty("message", "Paiement non trouvé");
                out.print(responseJson.toString());
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("status", "ERROR");
            responseJson.addProperty("message", e.getMessage());
            out.print(responseJson.toString());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        out.flush();
    }
}