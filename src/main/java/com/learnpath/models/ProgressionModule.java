// ============================================================
// FICHIER : src/main/java/com/LearnPath/models/ProgressionModule.java
// RÔLE    : Modèle représentant la progression d'un utilisateur
//           dans un module de cours
//           Stocke le résultat de l'examen et le statut de réussite
// ============================================================

package com.learnpath.models;

import java.sql.Timestamp;

/**
 * ProgressionModule - Progression d'un utilisateur dans un module
 * 
 * Enregistre le résultat de l'examen d'un module pour un utilisateur.
 * Si le score est supérieur ou égal à 75% (15/20), le module est validé
 * et le module suivant est débloqué.
 * 
 * @author LearnPath
 * @version 1.0
 */
public class ProgressionModule {

    // ===== ATTRIBUTS =====
    private int id;
    private int utilisateurId;
    private int coursId;
    private int moduleNumero;
    private boolean reussi;
    private int score;
    private int totalQuestions;
    private double pourcentage;
    private Timestamp dateRealisation;
    private int tentatives;

    // ===== CONSTANTES =====
    public static final int SCORE_MINIMUM = 15;    // 15/20 minimum
    public static final double SEUIL_REUSSITE = 75.0; // 75% minimum
    public static final int TOTAL_QUESTIONS_DEFAUT = 20;

    // ===== CONSTRUCTEURS =====

    /**
     * Constructeur par défaut
     */
    public ProgressionModule() {
        this.reussi = false;
        this.score = 0;
        this.totalQuestions = TOTAL_QUESTIONS_DEFAUT;
        this.pourcentage = 0.0;
        this.tentatives = 0;
    }

    /**
     * Constructeur pour enregistrement d'une progression
     * 
     * @param utilisateurId  ID de l'utilisateur
     * @param coursId        ID du cours
     * @param moduleNumero   Numéro du module
     * @param score          Score obtenu (nombre de bonnes réponses)
     * @param totalQuestions Nombre total de questions
     */
    public ProgressionModule(int utilisateurId, int coursId, int moduleNumero, 
                             int score, int totalQuestions) {
        this.utilisateurId = utilisateurId;
        this.coursId = coursId;
        this.moduleNumero = moduleNumero;
        this.setScore(score);
        this.setTotalQuestions(totalQuestions);
        this.tentatives = 1;
        this.dateRealisation = new Timestamp(System.currentTimeMillis());
    }

    /**
     * Constructeur complet
     * 
     * @param id              ID de la progression
     * @param utilisateurId   ID de l'utilisateur
     * @param coursId         ID du cours
     * @param moduleNumero    Numéro du module
     * @param reussi          Module réussi ?
     * @param score           Score obtenu
     * @param totalQuestions  Nombre total de questions
     * @param pourcentage     Pourcentage obtenu
     * @param dateRealisation Date de réalisation
     * @param tentatives      Nombre de tentatives
     */
    public ProgressionModule(int id, int utilisateurId, int coursId, int moduleNumero,
                             boolean reussi, int score, int totalQuestions, 
                             double pourcentage, Timestamp dateRealisation, int tentatives) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.coursId = coursId;
        this.moduleNumero = moduleNumero;
        this.reussi = reussi;
        this.score = score;
        this.totalQuestions = totalQuestions > 0 ? totalQuestions : TOTAL_QUESTIONS_DEFAUT;
        this.pourcentage = pourcentage;
        this.dateRealisation = dateRealisation;
        this.tentatives = tentatives;
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

    public int getModuleNumero() {
        return moduleNumero;
    }

    public void setModuleNumero(int moduleNumero) {
        this.moduleNumero = moduleNumero;
    }

    public boolean isReussi() {
        return reussi;
    }

    public void setReussi(boolean reussi) {
        this.reussi = reussi;
    }

    public int getScore() {
        return score;
    }

    /**
     * Définit le score et calcule automatiquement le pourcentage et la réussite
     * 
     * @param score Nombre de bonnes réponses
     */
    public void setScore(int score) {
        if (score < 0) {
            this.score = 0;
        } else if (totalQuestions > 0 && score > totalQuestions) {
            this.score = totalQuestions;
        } else {
            this.score = score;
        }
        // Recalculer le pourcentage et la réussite
        this.calculerPourcentage();
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    /**
     * Définit le nombre total de questions et recalcule le pourcentage
     * 
     * @param totalQuestions Nombre total de questions
     */
    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions > 0 ? totalQuestions : TOTAL_QUESTIONS_DEFAUT;
        // Recalculer le pourcentage et la réussite
        this.calculerPourcentage();
    }

    public double getPourcentage() {
        return pourcentage;
    }

    public void setPourcentage(double pourcentage) {
        if (pourcentage < 0) {
            this.pourcentage = 0.0;
        } else if (pourcentage > 100) {
            this.pourcentage = 100.0;
        } else {
            this.pourcentage = pourcentage;
        }
        // Mettre à jour le statut de réussite basé sur le pourcentage
        this.reussi = this.pourcentage >= SEUIL_REUSSITE;
    }

    public Timestamp getDateRealisation() {
        return dateRealisation;
    }

    public void setDateRealisation(Timestamp dateRealisation) {
        this.dateRealisation = dateRealisation;
    }

    public int getTentatives() {
        return tentatives;
    }

    public void setTentatives(int tentatives) {
        this.tentatives = Math.max(tentatives, 0);
    }

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Calcule le pourcentage basé sur le score et le total
     */
    private void calculerPourcentage() {
        if (this.totalQuestions > 0) {
            this.pourcentage = (double) this.score / this.totalQuestions * 100;
            // Arrondir à 2 décimales
            this.pourcentage = Math.round(this.pourcentage * 100.0) / 100.0;
        } else {
            this.pourcentage = 0.0;
        }
        // Déterminer si le module est réussi
        this.reussi = this.pourcentage >= SEUIL_REUSSITE;
    }

    /**
     * Incrémente le nombre de tentatives
     */
    public void incrementerTentatives() {
        this.tentatives++;
    }

    /**
     * Vérifie si le module est réussi selon le seuil
     * 
     * @return true si le pourcentage >= 75%
     */
    public boolean estReussi() {
        return this.pourcentage >= SEUIL_REUSSITE;
    }

    /**
     * Retourne le pourcentage formaté
     * 
     * @return "85%"
     */
    public String getPourcentageFormate() {
        return String.format("%.0f%%", pourcentage);
    }

    /**
     * Retourne le score formaté
     * 
     * @return "17/20"
     */
    public String getScoreFormate() {
        return score + "/" + totalQuestions;
    }

    /**
     * Retourne le nom du module formaté
     * 
     * @return "Module 1"
     */
    public String getModuleFormate() {
        return "Module " + moduleNumero;
    }

    /**
     * Vérifie si c'est la première tentative
     * 
     * @return true si tentatives == 1
     */
    public boolean isPremiereTentative() {
        return tentatives <= 1;
    }

    /**
     * Retourne le statut formaté
     * 
     * @return "✅ Réussi" ou "❌ Échoué"
     */
    public String getStatutFormate() {
        if (reussi) {
            return "✅ Réussi";
        } else if (tentatives > 0) {
            return "❌ Échoué (" + tentatives + " tentative" + (tentatives > 1 ? "s" : "") + ")";
        }
        return "⏳ Non tenté";
    }

    // ===== MÉTHODES STANDARD =====

    @Override
    public String toString() {
        return "ProgressionModule{" +
                "id=" + id +
                ", utilisateurId=" + utilisateurId +
                ", coursId=" + coursId +
                ", moduleNumero=" + moduleNumero +
                ", reussi=" + reussi +
                ", score=" + getScoreFormate() +
                ", pourcentage=" + getPourcentageFormate() +
                ", tentatives=" + tentatives +
                ", statut=" + getStatutFormate() +
                '}';
    }
}