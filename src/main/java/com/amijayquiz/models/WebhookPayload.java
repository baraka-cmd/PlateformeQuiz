package com.amijayquiz.models;

/**
 * Modèle représentant les données reçues du webhook PawaPay
 * @author BARAKA
 */
public class WebhookPayload {
    
    private String status;
    private String transactionId;
    private String reference;
    private double amount;
    private String currency;
    private String customerPhone;
    private String timestamp;
    private String signature;
    
    public WebhookPayload() {}
    
    // Getters et Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getCustomerPhone() {
        return customerPhone;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    // Méthodes utilitaires
    public boolean isSuccess() {
        return "SUCCESS".equalsIgnoreCase(status);
    }
    
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }
    
    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(status);
    }
    
    @Override
    public String toString() {
        return "WebhookPayload{" +
                "status='" + status + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", reference='" + reference + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}