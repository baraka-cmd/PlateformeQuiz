package com.amijayquiz.services;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Service pour les opérations cryptographiques
 * Signature HMAC, hachage, validation webhook
 * @author BARAKA
 */
public class CryptoService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String SECRET_KEY = "votre_cle_secrete_pawapay"; // TODO: À remplacer par la vraie clé
    
    /**
     * Génère une signature HMAC-SHA256 d'un message
     */
    public static String generateHmacSignature(String message, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), 
                HMAC_ALGORITHM
            );
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Génère une signature HMAC avec la clé par défaut
     */
    public static String generateHmacSignature(String message) {
        return generateHmacSignature(message, SECRET_KEY);
    }
    
    /**
     * Vérifie la validité d'une signature HMAC
     */
    public static boolean verifyHmacSignature(String message, String signature, String secret) {
        String expectedSignature = generateHmacSignature(message, secret);
        if (expectedSignature == null || signature == null) {
            return false;
        }
        return expectedSignature.equals(signature);
    }
    
    /**
     * Vérifie la signature avec la clé par défaut
     */
    public static boolean verifyHmacSignature(String message, String signature) {
        return verifyHmacSignature(message, signature, SECRET_KEY);
    }
    
    /**
     * Génère un identifiant unique sécurisé
     */
    public static String generateSecureId() {
        return java.util.UUID.randomUUID().toString();
    }
    
    /**
     * Hache un mot de passe avec SHA-256
     */
    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Valide une signature de webhook PawaPay
     * @param payload Corps brut de la requête
     * @param signature Signature reçue dans le header
     * @return true si signature valide
     */
    public static boolean validateWebhookSignature(String payload, String signature) {
        if (signature == null || signature.isEmpty()) {
            return false;
        }
        // Nettoyer la signature si elle contient "sha256="
        String cleanSignature = signature;
        if (signature.startsWith("sha256=")) {
            cleanSignature = signature.substring(7);
        }
        return verifyHmacSignature(payload, cleanSignature);
    }
    
    /**
     * Génère un token aléatoire sécurisé
     */
    public static String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}