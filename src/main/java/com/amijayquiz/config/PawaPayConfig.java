package com.amijayquiz.config;

/**
 * Configuration PawaPay - Stockage sécurisé des identifiants API
 * ⚠️ En production, utiliser des variables d'environnement ou JNDI
 */
public class PawaPayConfig {
    
    // Token API PawaPay (Sandbox)
    // TODO: Déplacer dans les variables d'environnement en production
    private static final String API_TOKEN = "eyJraWQiOiIxIiwiYWxnIjoiRVMyNTYifQ.eyJ0dCI6IkFBVCIsInN1YiI6IjIxOTUwIiwibWF2IjoiMSIsImV4cCI6MjA5NTM0Nzg2MywiaWF0IjoxNzc5NzI4NjYzLCJwbSI6IkRBRixQQUYiLCJqdGkiOiI5OGNhM2NlZi1hMjFlLTQ0MTQtYTZjZi04ZTlhYzk3YzY5MjYifQ.z_yAgeH6jADRDor7q2JIuFD2409xrPzuPBez1vnEYLimSWPGuSKfTH3QtjA08cBhPbrnbi61_F00kW_qThxDSg";
    
    // Environnement
    private static final boolean IS_SANDBOX = true;
    
    // URLs
    private static final String SANDBOX_BASE_URL = "https://api.sandbox.pawapay.io";
    private static final String LIVE_BASE_URL = "https://api.pawapay.io";
    
    // Endpoints
    private static final String CREATE_PAYMENT_ENDPOINT = "/api/v1/payments";
    private static final String GET_PAYMENT_ENDPOINT = "/api/v1/payments/%s";
    
    // Webhook (à adapter avec ton domaine)
    private static final String WEBHOOK_URL = "https://ton-domaine.com/api/paiements/webhook";
    
    // Timeouts (millisecondes)
    private static final int CONNECTION_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 30000;
    
    /**
     * Récupère le token API
     */
    public static String getApiToken() {
        return API_TOKEN;
    }
    
    /**
     * Récupère l'URL de base selon l'environnement
     */
    public static String getBaseUrl() {
        return IS_SANDBOX ? SANDBOX_BASE_URL : LIVE_BASE_URL;
    }
    
    /**
     * Récupère l'URL complète pour créer un paiement
     */
    public static String getCreatePaymentUrl() {
        return getBaseUrl() + CREATE_PAYMENT_ENDPOINT;
    }
    
    /**
     * Récupère l'URL complète pour vérifier un paiement
     */
    public static String getPaymentStatusUrl(String reference) {
        return getBaseUrl() + String.format(GET_PAYMENT_ENDPOINT, reference);
    }
    
    /**
     * Récupère l'URL du webhook
     */
    public static String getWebhookUrl() {
        return WEBHOOK_URL;
    }
    
    /**
     * Vérifie si on est en mode sandbox
     */
    public static boolean isSandbox() {
        return IS_SANDBOX;
    }
    
    /**
     * Récupère le timeout de connexion
     */
    public static int getConnectionTimeout() {
        return CONNECTION_TIMEOUT;
    }
    
    /**
     * Récupère le timeout de lecture
     */
    public static int getReadTimeout() {
        return READ_TIMEOUT;
    }
    
    /**
     * Devise par défaut
     */
    public static String getDefaultCurrency() {
        return "USD";
    }
}