package com.amijayquiz.services;

import com.amijayquiz.models.PaymentStatus;
import com.amijayquiz.models.WebhookPayload;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Service pour le traitement sécurisé des webhooks PawaPay
 * @author BARAKA
 */
public class WebhookService {

    private static final Gson gson = new Gson();
    
    /**
     * Parse et valide un payload de webhook
     */
    public static WebhookPayload parseWebhookPayload(String body) {
        try {
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            WebhookPayload payload = new WebhookPayload();
            
            if (json.has("status")) {
                payload.setStatus(json.get("status").getAsString());
            }
            if (json.has("transactionId")) {
                payload.setTransactionId(json.get("transactionId").getAsString());
            }
            if (json.has("reference")) {
                payload.setReference(json.get("reference").getAsString());
            }
            if (json.has("amount")) {
                payload.setAmount(json.get("amount").getAsDouble());
            }
            if (json.has("currency")) {
                payload.setCurrency(json.get("currency").getAsString());
            }
            if (json.has("customer")) {
                JsonObject customer = json.getAsJsonObject("customer");
                if (customer.has("phone")) {
                    payload.setCustomerPhone(customer.get("phone").getAsString());
                }
            }
            if (json.has("timestamp")) {
                payload.setTimestamp(json.get("timestamp").getAsString());
            }
            if (json.has("signature")) {
                payload.setSignature(json.get("signature").getAsString());
            }
            
            return payload;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Traite un webhook de paiement
     * @param body Corps brut de la requête
     * @param signature Signature du header
     * @return Résultat du traitement
     */
    public static WebhookResult processWebhook(String body, String signature) {
        WebhookResult result = new WebhookResult();
        
        // 1. Valider la signature
        boolean signatureValid = CryptoService.validateWebhookSignature(body, signature);
        if (!signatureValid) {
            result.setSuccess(false);
            result.setMessage("Signature invalide");
            result.setHttpCode(401);
            return result;
        }
        
        // 2. Parser le payload
        WebhookPayload payload = parseWebhookPayload(body);
        if (payload == null || payload.getReference() == null) {
            result.setSuccess(false);
            result.setMessage("Payload invalide");
            result.setHttpCode(400);
            return result;
        }
        
        // 3. Vérifier que la référence existe
        com.amijayquiz.models.Payment payment = com.amijayquiz.dao.PaymentDAO.findByReference(payload.getReference());
        if (payment == null) {
            result.setSuccess(false);
            result.setMessage("Référence non trouvée: " + payload.getReference());
            result.setHttpCode(404);
            return result;
        }
        
        // 4. Éviter les doublons
        if (payment.isSuccess()) {
            result.setSuccess(true);
            result.setMessage("Déjà traité");
            result.setHttpCode(200);
            return result;
        }
        
        // 5. Traiter selon le statut
        PaymentStatus status = PaymentStatus.fromCode(payload.getStatus());
        
        boolean processed = PaymentService.processPaymentConfirmation(
            payload.getReference(),
            payload.getTransactionId(),
            status
        );
        
        if (processed) {
            result.setSuccess(true);
            result.setMessage("Webhook traité avec succès");
            result.setHttpCode(200);
            result.setPayment(payment);
            result.setStatus(status);
        } else {
            result.setSuccess(false);
            result.setMessage("Erreur lors du traitement");
            result.setHttpCode(500);
        }
        
        return result;
    }
    
    /**
     * Classe interne pour le résultat du webhook
     */
    public static class WebhookResult {
        private boolean success;
        private String message;
        private int httpCode;
        private com.amijayquiz.models.Payment payment;
        private PaymentStatus status;
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public int getHttpCode() {
            return httpCode;
        }
        
        public void setHttpCode(int httpCode) {
            this.httpCode = httpCode;
        }
        
        public com.amijayquiz.models.Payment getPayment() {
            return payment;
        }
        
        public void setPayment(com.amijayquiz.models.Payment payment) {
            this.payment = payment;
        }
        
        public PaymentStatus getStatus() {
            return status;
        }
        
        public void setStatus(PaymentStatus status) {
            this.status = status;
        }
        
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("success", success);
            json.addProperty("message", message);
            if (payment != null) {
                json.addProperty("reference", payment.getReference());
                json.addProperty("status", status != null ? status.getCode() : null);
            }
            return json;
        }
    }
}