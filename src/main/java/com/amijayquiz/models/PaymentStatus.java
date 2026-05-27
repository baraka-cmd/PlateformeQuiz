package com.amijayquiz.models;

/**
 * Statuts possibles pour un paiement
 */
public enum PaymentStatus {
    
    PENDING("PENDING", "En attente de paiement"),
    SUCCESS("SUCCESS", "Paiement réussi"),
    FAILED("FAILED", "Paiement échoué"),
    CANCELLED("CANCELLED", "Annulé"),
    EXPIRED("EXPIRED", "Expiré");
    
    private final String code;
    private final String label;
    
    PaymentStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getLabel() {
        return label;
    }
    
    public static PaymentStatus fromCode(String code) {
        for (PaymentStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return PENDING;
    }
    
    public boolean isSuccess() {
        return this == SUCCESS;
    }
    
    public boolean isPending() {
        return this == PENDING;
    }
    
    public boolean isFailed() {
        return this == FAILED;
    }
}