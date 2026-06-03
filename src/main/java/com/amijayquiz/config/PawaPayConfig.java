// ============================================================
// FICHIER : src/main/java/com/amijayquiz/config/PawaPayConfig.java
// RÔLE    : Configuration centralisée PawaPay
//           Token API, URLs, callback, paramètres de paiement
//           Valeurs modifiables sans recompiler via variables d'environnement
// ============================================================

package com.amijayquiz.config;

/**
 * PawaPayConfig - Configuration centralisée du service PawaPay
 * 
 * Stocke toutes les constantes de connexion à l'API PawaPay.
 * En production, ces valeurs seront chargées depuis des variables
 * d'environnement ou un fichier properties externe.
 * 
 * Token JWT fourni pour le sandbox PawaPay.
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
public final class PawaPayConfig {

    // ===== CONSTRUCTEUR PRIVÉ (classe utilitaire) =====
    private PawaPayConfig() {
        throw new UnsupportedOperationException("Classe de configuration - ne pas instancier");
    }

    // ============================================================
    // AUTHENTIFICATION
    // ============================================================
    
    /**
     * Token JWT Bearer pour l'API PawaPay (sandbox)
     * Format : eyJraWQiOiIxIiwiYWxnIjoiRVMyNTYifQ...
     * Durée de validité : long terme (sandbox)
     */
    public static final String API_TOKEN = 
    "eyJraWQiOiIxIiwiYWxnIjoiRVMyNTYifQ." +
    "eyJ0dCI6IkFBVCIsInN1YiI6IjIxOTUwIiwibWF2IjoiMSIsImV4cCI6MjA5NjEwMDgzMCwiaWF0IjoxNzgwNDgx" +
    "NjMwLCJwbSI6IkRBRixQQUYiLCJqdGkiOiI0M2M5YjcwMC02ZjM4LTQ1MWItOTNhOC1mN2FlNmJiODNiNTMifQ." +
    "eGGkuvdOgZLQXy5RBNqp6gRZ1m6Z2s9DO5czUpHh2DOlYT-OqqZGuoOmOJ1nNeXmFT5p3pbTf29pNbzlm5MVAQ";

    
    // ============================================================
    // URLS DE L'API
    // ============================================================
    
    /** URL de base de l'API PawaPay (sandbox) */
    public static final String BASE_URL = "https://api.sandbox.pawapay.io";
    
    /** Endpoint pour créer un paiement */
    public static final String DEPOSIT_URL = BASE_URL + "/v2/deposits";
    
    /** Endpoint pour vérifier le statut d'un paiement par son ID */
    public static final String DEPOSIT_STATUS_URL = BASE_URL + "/deposits/"; // + transactionId

    // ============================================================
    // CALLBACK (WEBHOOK)
    // ============================================================
    
    /**
     * URL du webhook que PawaPay appellera après un paiement.
     * En sandbox/localhost, utiliser un tunnel (ngrok) ou webhook.site.
     * En production, remplacer par l'URL réelle du serveur.
     */
    public static final String CALLBACK_URL = 
        "http://localhost:8080/amijayQuizz/api/paiements/webhook";

    // ============================================================
    // PARAMÈTRES DE PAIEMENT
    // ============================================================
    
    /** Montant du paiement en USD */
    public static final double MONTANT_USD = 4.00;
    
    /** Montant équivalent en Francs Congolais (FC) */
    public static final int MONTANT_FC = 10000;
    
    /** Devise par défaut */
    public static final String DEVISE = "USD";
    
    /** Pays pour Mobile Money (RDC) */
    public static final String PAYS = "COD";

    // ============================================================
    // SÉCURITÉ WEBHOOK
    // ============================================================
    
    /** IPs autorisées pour le webhook PawaPay (sandbox) */
    public static final String[] IPS_AUTORISEES = {
        "52.214.10.163",   // PawaPay sandbox IP 1
        "34.245.26.184",   // PawaPay sandbox IP 2
        "127.0.0.1"        // Localhost (développement)
    };
    
    /** Clé secrète pour vérifier la signature HMAC du webhook (fournie par PawaPay en production) */
    public static final String WEBHOOK_SECRET = ""; // Vide en sandbox, à remplir en production

    // ============================================================
    // TIMEOUT ET RETRY
    // ============================================================
    
    /** Timeout connexion HTTP en millisecondes */
    public static final int CONNECT_TIMEOUT_MS = 15000;  // 15 secondes
    
    /** Timeout lecture HTTP en millisecondes */
    public static final int READ_TIMEOUT_MS = 30000;     // 30 secondes
    
    /** Nombre maximum de tentatives pour vérifier un paiement */
    public static final int MAX_RETRY = 3;

    // ============================================================
    // PRÉFIXE RÉFÉRENCE
    // ============================================================
    
    /** Préfixe des références de paiement pour identifier facilement les transactions */
    public static final String PREFIXE_REFERENCE = "PAW";

    // ============================================================
    // MÉTHODE DE VALIDATION
    // ============================================================
    
    /**
     * Valide que la configuration minimale est présente
     * 
     * @return true si la configuration est valide
     */
    public static boolean estValide() {
        return API_TOKEN != null && !API_TOKEN.isEmpty() 
            && BASE_URL != null && !BASE_URL.isEmpty()
            && MONTANT_USD > 0;
    }
}