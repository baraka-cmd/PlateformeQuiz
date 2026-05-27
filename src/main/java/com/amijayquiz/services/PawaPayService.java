package com.amijayquiz.services;

import com.amijayquiz.config.PawaPayConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Service d'appel à l'API PawaPay
 */
public class PawaPayService {

    /**
     * Crée un paiement sur PawaPay
     */
    public static JsonObject createPayment(String reference, double amount, String phoneNumber, String callbackUrl) throws Exception {
        URL url = new URL(PawaPayConfig.getCreatePaymentUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + PawaPayConfig.getApiToken());
        conn.setDoOutput(true);
        conn.setConnectTimeout(PawaPayConfig.getConnectionTimeout());
        conn.setReadTimeout(PawaPayConfig.getReadTimeout());
        
        String jsonBody = String.format(
            "{\"amount\":%.2f,\"currency\":\"%s\",\"reference\":\"%s\",\"customer\":{\"phone\":\"%s\"},\"callbackUrl\":\"%s\"}",
            amount, PawaPayConfig.getDefaultCurrency(), reference, phoneNumber, callbackUrl
        );
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        StringBuilder response = new StringBuilder();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream(),
                StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        
        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
        jsonResponse.addProperty("httpCode", responseCode);
        
        return jsonResponse;
    }
    
    /**
     * Vérifie le statut d'un paiement
     */
    public static JsonObject getPaymentStatus(String reference) throws Exception {
        URL url = new URL(PawaPayConfig.getPaymentStatusUrl(reference));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + PawaPayConfig.getApiToken());
        conn.setConnectTimeout(PawaPayConfig.getConnectionTimeout());
        conn.setReadTimeout(PawaPayConfig.getReadTimeout());
        
        int responseCode = conn.getResponseCode();
        StringBuilder response = new StringBuilder();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream(),
                StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        
        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
        jsonResponse.addProperty("httpCode", responseCode);
        
        return jsonResponse;
    }
}