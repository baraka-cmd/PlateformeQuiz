// ============================================================
// FICHIER : src/main/java/com/LearnPath/models/Certificat.java
// RÔLE    : Modèle représentant un certificat de réussite
//           Délivré après validation de l'examen final d'un cours
//           Contient un code de vérification unique
// ============================================================

package com.learnpath.models;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Certificat - Certificat de réussite d'un cours
 * 
 * Délivré à un utilisateur après avoir réussi l'examen final
 * d'un cours avec un score minimum de 80%.
 * Contient un code de vérification unique pour authentification.
 * 
 * @author LearnPath
 * @version 1.0
 */
public class Certificat {

    // ===== ATTRIBUTS =====
    private int id;
    private int utilisateurId;
    private int coursId;
    private double scoreFinal;
    private int scoreMax;
    private String mention;
    private String codeVerification;
    private Timestamp dateObtention;

    // ===== CONSTANTES =====
    public static final double SEUIL_REUSSITE = 80.0;
    public static final double SEUIL_MENTION_BIEN = 85.0;
    public static final double SEUIL_MENTION_TRES_BIEN = 90.0;
    public static final double SEUIL_MENTION_EXCELLENT = 95.0;

    public static final String MENTION_PASSABLE    = "Passable";
    public static final String MENTION_BIEN        = "Bien";
    public static final String MENTION_TRES_BIEN   = "Très Bien";
    public static final String MENTION_EXCELLENT   = "Excellent";

    // ===== CONSTRUCTEURS =====

    /**
     * Constructeur par défaut
     */
    public Certificat() {
        this.scoreMax = 100;
        this.mention = MENTION_PASSABLE;
    }

    /**
     * Constructeur pour création d'un certificat
     * 
     * @param utilisateurId    ID de l'utilisateur
     * @param coursId          ID du cours
     * @param scoreFinal       Score final obtenu
     * @param codeVerification Code unique de vérification
     */
    public Certificat(int utilisateurId, int coursId, double scoreFinal, String codeVerification) {
        this.utilisateurId = utilisateurId;
        this.coursId = coursId;
        this.scoreFinal = scoreFinal;
        this.scoreMax = 100;
        this.codeVerification = codeVerification;
        this.mention = calculerMention(scoreFinal);
        this.dateObtention = new Timestamp(System.currentTimeMillis());
    }

    /**
     * Constructeur complet
     * 
     * @param id               ID du certificat
     * @param utilisateurId    ID de l'utilisateur
     * @param coursId          ID du cours
     * @param scoreFinal       Score final
     * @param scoreMax         Score maximum
     * @param mention          Mention obtenue
     * @param codeVerification Code unique
     * @param dateObtention    Date d'obtention
     */
    public Certificat(int id, int utilisateurId, int coursId, double scoreFinal,
                      int scoreMax, String mention, String codeVerification, 
                      Timestamp dateObtention) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.coursId = coursId;
        this.scoreFinal = scoreFinal;
        this.scoreMax = scoreMax;
        this.mention = (mention != null && !mention.isEmpty()) ? mention : calculerMention(scoreFinal);
        this.codeVerification = codeVerification;
        this.dateObtention = dateObtention;
    }

    // ===== GETTERS ET SETTERS =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public int getCoursId() {
        return coursId;
    }

    public void setCoursId(int coursId) {
        this.coursId = coursId;
    }

    public double getScoreFinal() {
        return scoreFinal;
    }

    public void setScoreFinal(double scoreFinal) {
        this.scoreFinal = scoreFinal;
        this.mention = calculerMention(scoreFinal);
    }

    public int getScoreMax() {
        return scoreMax;
    }

    public void setScoreMax(int scoreMax) {
        this.scoreMax = scoreMax;
    }

    public String getMention() {
        return mention;
    }

    public void setMention(String mention) {
        this.mention = (mention != null && !mention.isEmpty()) ? mention : MENTION_PASSABLE;
    }

    public String getCodeVerification() {
        return codeVerification;
    }

    public void setCodeVerification(String codeVerification) {
        this.codeVerification = codeVerification;
    }

    public Timestamp getDateObtention() {
        return dateObtention;
    }

    public void setDateObtention(Timestamp dateObtention) {
        this.dateObtention = dateObtention;
    }

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Calcule automatiquement la mention selon le score
     * 
     * @param score Score obtenu
     * @return La mention correspondante
     */
    public static String calculerMention(double score) {
        if (score >= SEUIL_MENTION_EXCELLENT)  return MENTION_EXCELLENT;
        if (score >= SEUIL_MENTION_TRES_BIEN)  return MENTION_TRES_BIEN;
        if (score >= SEUIL_MENTION_BIEN)       return MENTION_BIEN;
        return MENTION_PASSABLE;
    }

    /**
     * Vérifie si le score est suffisant pour obtenir le certificat
     * 
     * @param score Score obtenu
     * @return true si le certificat est accordé
     */
    public static boolean estAccorde(double score) {
        return score >= SEUIL_REUSSITE;
    }

    /**
     * Retourne le pourcentage de réussite
     * 
     * @return Pourcentage formaté (ex: "85%")
     */
    public String getPourcentage() {
        if (scoreMax == 0) return "0%";
        double pourcentage = (scoreFinal / scoreMax) * 100;
        return String.format("%.0f%%", pourcentage);
    }

    /**
     * Retourne l'icône associée à la mention
     * 
     * @return Classe CSS Font Awesome
     */
    public String getIconeMention() {
        if (mention == null) return "fas fa-medal";

        switch (mention) {
            case MENTION_EXCELLENT:  return "fas fa-crown";
            case MENTION_TRES_BIEN:  return "fas fa-trophy";
            case MENTION_BIEN:       return "fas fa-star";
            case MENTION_PASSABLE:   return "fas fa-check-circle";
            default:                 return "fas fa-medal";
        }
    }

    /**
     * Retourne la couleur associée à la mention
     * 
     * @return Code couleur hexadécimal
     */
    public String getCouleurMention() {
        if (mention == null) return "#888888";

        switch (mention) {
            case MENTION_EXCELLENT:  return "#ffd700";
            case MENTION_TRES_BIEN:  return "#27ae60";
            case MENTION_BIEN:       return "#2980b9";
            case MENTION_PASSABLE:   return "#f39c12";
            default:                 return "#888888";
        }
    }

    /**
     * Formate la date d'obtention en français
     * 
     * @return Date formatée (ex: "15 juin 2026")
     */
    public String getDateFormatee() {
        if (dateObtention == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);
        return sdf.format(dateObtention);
    }

    /**
     * Vérifie si le certificat est valide (a un code de vérification)
     * 
     * @return true si le code est présent
     */
    public boolean isValide() {
        return codeVerification != null && !codeVerification.trim().isEmpty();
    }

    /**
     * Génère l'URL de vérification publique du certificat
     * 
     * @param baseUrl URL de base de la plateforme
     * @return URL complète de vérification
     */
    public String getUrlVerification(String baseUrl) {
        if (!isValide()) return "#";
        return baseUrl + "/verifier-certificat?code=" + codeVerification;
    }

    // ===== MÉTHODES STANDARD =====

    @Override
    public String toString() {
        return "Certificat{" +
                "id=" + id +
                ", utilisateurId=" + utilisateurId +
                ", coursId=" + coursId +
                ", scoreFinal=" + scoreFinal +
                ", mention='" + mention + '\'' +
                ", codeVerification='" + codeVerification + '\'' +
                ", dateObtention=" + getDateFormatee() +
                '}';
    }
}