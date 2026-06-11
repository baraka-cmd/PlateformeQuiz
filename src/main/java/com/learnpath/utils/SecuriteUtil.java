// ============================================================
// FICHIER : src/main/java/com/learnpath/utils/SecuriteUtil.java
// RÔLE    : Utilitaire de sécurité pour le module paiement
//           - Génération de références uniques
//           - Validation de numéros de téléphone
//           - Journalisation des événements de paiement
//           - Protection contre les attaques CSRF
// ============================================================

package com.learnpath.utils;

import com.learnpath.config.PawaPayConfig;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * SecuriteUtil - Utilitaire de sécurité pour les paiements
 * 
 * Centralise toutes les fonctions de sécurité liées aux transactions :
 * génération de références infalsifiables, validation des données entrantes,
 * et journalisation des événements pour l'audit.
 * 
 * @author LearnPath
 * @version 1.0
 */
public final class SecuriteUtil {

    // ===== CONSTRUCTEUR PRIVÉ =====
    private SecuriteUtil() {
        throw new UnsupportedOperationException("Classe utilitaire - ne pas instancier");
    }

    // ===== CONSTANTES =====
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String CARACTERES_REFERENCE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LONGUEUR_RANDOM_REF = 4;

    // Regex téléphone : accepte +243XXXXXXXXX ou 0XXXXXXXXX
    private static final Pattern PATTERN_TELEPHONE = Pattern.compile(
        "^(\\+243|0)[0-9]{9}$"
    );

    // ============================================================
    // GÉNÉRATION DE RÉFÉRENCE UNIQUE
    // ============================================================

    /**
     * Génère une référence de paiement unique et infalsifiable.
     * 
     * Format : PAW-{userId}-{timestamp}-{random4caracteres}
     * Exemple : PAW-15-1620000000-K7X2
     * 
     * Garanties :
     * - Intègre l'ID utilisateur (traçabilité)
     * - Intègre un timestamp (ordre chronologique)
     * - Termine par 4 caractères aléatoires (unicité même si même seconde)
     * 
     * @param userId L'ID de l'utilisateur qui paie
     * @return Référence unique (max 100 caractères)
     */
    public static String genererReference(int userId) {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * Vérifie qu'une référence correspond bien au format attendu
     * 
     * @param reference La référence à valider
     * @return true si le format est valide
     */
    public static boolean estReferenceValide(String reference) {
        if (reference == null || reference.isEmpty()) return false;
        return reference.matches("^" + PawaPayConfig.PREFIXE_REFERENCE + "-[0-9]+-[0-9]+-[A-Z0-9]{4}$");
    }

    /**
     * Extrait l'ID utilisateur d'une référence de paiement
     * 
     * @param reference Référence format PAW-{userId}-{timestamp}-{random}
     * @return L'ID utilisateur, ou -1 si format invalide
     */
    public static int extraireUserIdDeReference(String reference) {
        if (!estReferenceValide(reference)) return -1;

        try {
            String[] parts = reference.split("-");
            if (parts.length >= 2) {
                return Integer.parseInt(parts[1]);
            }
        } catch (NumberFormatException e) {
            // Format invalide
        }
        return -1;
    }

    // ============================================================
    // VALIDATION TÉLÉPHONE
    // ============================================================

    /**
     * Valide un numéro de téléphone au format congolais.
     * Accepte : +243XXXXXXXXX ou 0XXXXXXXXX
     * 
     * @param telephone Le numéro à valider
     * @return true si le format est valide
     */
    public static boolean estTelephoneValide(String telephone) {
        if (telephone == null || telephone.trim().isEmpty()) return false;
        return PATTERN_TELEPHONE.matcher(telephone.trim()).matches();
    }

    /**
     * Normalise un numéro de téléphone au format international +243
     * 
     * @param telephone Numéro brut (0XXXXXXXXX ou +243XXXXXXXXX)
     * @return Numéro au format +243XXXXXXXXX, ou null si invalide
     */
    public static String normaliserTelephone(String telephone) {
        if (telephone == null) return null;
        telephone = telephone.trim();

        // Enlever le +
        if (telephone.startsWith("+")) {
            telephone = telephone.substring(1);
        }

        return telephone;
    }

    /**
     * Masque partiellement un numéro de téléphone pour l'affichage.
     * Exemple : +243984270438 → +243984***438
     * 
     * @param telephone Le numéro complet
     * @return Numéro masqué
     */
    public static String masquerTelephone(String telephone) {
        if (telephone == null || telephone.length() < 9) return telephone;
        return telephone.substring(0, telephone.length() - 6) 
            + "***" 
            + telephone.substring(telephone.length() - 3);
    }

    // ============================================================
    // VALIDATION MONTANT
    // ============================================================

    /**
     * Vérifie que le montant correspond au tarif attendu
     * 
     * @param montantRecu Montant reçu du webhook
     * @return true si le montant est correct (tolérance 0.01)
     */
    public static boolean estMontantCorrect(double montantRecu) {
        return Math.abs(montantRecu - PawaPayConfig.MONTANT_USD) < 0.01;
    }

    // ============================================================
    // JOURNALISATION (LOGS)
    // ============================================================

    /**
     * Journalise un événement de paiement avec horodatage.
     * En production, remplacer par un framework de logging (Log4j, SLF4J).
     * 
     * @param niveau   Niveau du log (INFO, WARN, ERROR, SUCCESS)
     * @param message  Message descriptif
     */
    public static void journaliser(String niveau, String message) {
        String horodatage = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        String thread = Thread.currentThread().getName();

        String couleur;
        switch (niveau.toUpperCase()) {
            case "SUCCESS": couleur = "\u001B[32m"; break; // Vert
            case "ERROR":   couleur = "\u001B[31m"; break; // Rouge
            case "WARN":    couleur = "\u001B[33m"; break; // Jaune
            default:        couleur = "\u001B[0m";  break; // Blanc
        }

        System.out.println(couleur 
            + "[" + horodatage + "] [" + niveau.toUpperCase() + "] [" + thread + "] "
            + "[Paiement] " + message
            + "\u001B[0m");
    }

    /**
     * Raccourci pour journaliser un succès
     */
    public static void logSucces(String message) {
        journaliser("SUCCESS", message);
    }

    /**
     * Raccourci pour journaliser une erreur
     */
    public static void logErreur(String message) {
        journaliser("ERROR", message);
    }

    /**
     * Raccourci pour journaliser un avertissement
     */
    public static void logWarn(String message) {
        journaliser("WARN", message);
    }

    /**
     * Raccourci pour journaliser une info
     */
    public static void logInfo(String message) {
        journaliser("INFO", message);
    }

    // ============================================================
    // ANTI-CSRF (PROTECTION FORMULAIRES)
    // ============================================================

    /**
     * Génère un token anti-CSRF pour les formulaires de paiement
     * 
     * @return Token aléatoire de 32 caractères hexadécimaux
     */
    public static String genererTokenCSRF() {
        byte[] bytes = new byte[16]; // 128 bits
        SECURE_RANDOM.nextBytes(bytes);

        StringBuilder sb = new StringBuilder(32);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Vérifie qu'un token CSRF est valide (comparaison sécurisée)
     * 
     * @param tokenSession Token stocké en session
     * @param tokenRequete Token reçu du formulaire
     * @return true si les tokens correspondent
     */
    public static boolean verifierTokenCSRF(String tokenSession, String tokenRequete) {
        if (tokenSession == null || tokenRequete == null) return false;

        // Comparaison en temps constant pour éviter les attaques temporelles
        byte[] bytesSession = tokenSession.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] bytesRequete = tokenRequete.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        if (bytesSession.length != bytesRequete.length) return false;

        int resultat = 0;
        for (int i = 0; i < bytesSession.length; i++) {
            resultat |= bytesSession[i] ^ bytesRequete[i];
        }

        return resultat == 0;
    }

    // ============================================================
    // MÉTHODES PRIVÉES
    // ============================================================

    /**
     * Génère une chaîne aléatoire de longueur donnée
     * 
     * @param longueur Nombre de caractères
     * @return Chaîne aléatoire en majuscules + chiffres
     */
    private static String genererChaineAleatoire(int longueur) {
        StringBuilder sb = new StringBuilder(longueur);
        for (int i = 0; i < longueur; i++) {
            int index = SECURE_RANDOM.nextInt(CARACTERES_REFERENCE.length());
            sb.append(CARACTERES_REFERENCE.charAt(index));
        }
        return sb.toString();
    }
}