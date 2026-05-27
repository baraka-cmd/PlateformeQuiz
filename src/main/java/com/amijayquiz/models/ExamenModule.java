// ============================================================
// FICHIER : src/main/java/com/amijayquiz/models/ExamenModule.java
// RÔLE    : Modèle représentant un examen de module
//           Chaque module du cours a un examen de 20 questions
//           avec un seuil de réussite à 75%
// ============================================================

package com.amijayquiz.models;

import java.sql.Timestamp;

/**
 * ExamenModule - Examen de validation d'un module
 * 
 * Chaque module du cours possède un examen composé de 20 questions
 * à choix multiples (A, B, C, D). Le seuil de réussite est de 75%
 * (15 bonnes réponses sur 20).
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
public class ExamenModule {

    // ===== ATTRIBUTS =====
    private int id;
    private int coursId;
    private int moduleNumero;
    private String titre;
    private String description;
    private int nombreQuestions;
    private int seuilReussite;
    private Timestamp dateCreation;

    // ===== CONSTANTES =====
    public static final int NOMBRE_QUESTIONS_DEFAUT = 20;
    public static final int SEUIL_REUSSITE_DEFAUT = 75;
    public static final int SCORE_MINIMUM = 15; // 15/20 = 75%

    // ===== CONSTRUCTEURS =====

    /**
     * Constructeur par défaut
     */
    public ExamenModule() {
        this.nombreQuestions = NOMBRE_QUESTIONS_DEFAUT;
        this.seuilReussite = SEUIL_REUSSITE_DEFAUT;
    }

    /**
     * Constructeur pour création d'un examen
     * 
     * @param coursId       ID du cours
     * @param moduleNumero  Numéro du module (1 à 15)
     * @param titre         Titre de l'examen
     * @param description   Description de l'examen
     */
    public ExamenModule(int coursId, int moduleNumero, String titre, String description) {
        this.coursId = coursId;
        this.moduleNumero = moduleNumero;
        this.titre = titre;
        this.description = description;
        this.nombreQuestions = NOMBRE_QUESTIONS_DEFAUT;
        this.seuilReussite = SEUIL_REUSSITE_DEFAUT;
    }

    /**
     * Constructeur complet
     * 
     * @param id              ID de l'examen
     * @param coursId         ID du cours
     * @param moduleNumero    Numéro du module
     * @param titre           Titre de l'examen
     * @param description     Description de l'examen
     * @param nombreQuestions Nombre de questions
     * @param seuilReussite   Seuil de réussite en pourcentage
     * @param dateCreation    Date de création
     */
    public ExamenModule(int id, int coursId, int moduleNumero, String titre,
                        String description, int nombreQuestions, int seuilReussite,
                        Timestamp dateCreation) {
        this.id = id;
        this.coursId = coursId;
        this.moduleNumero = moduleNumero;
        this.titre = titre;
        this.description = description;
        this.nombreQuestions = nombreQuestions > 0 ? nombreQuestions : NOMBRE_QUESTIONS_DEFAUT;
        this.seuilReussite = seuilReussite > 0 ? seuilReussite : SEUIL_REUSSITE_DEFAUT;
        this.dateCreation = dateCreation;
    }

    // ===== GETTERS ET SETTERS =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNombreQuestions() {
        return nombreQuestions;
    }

    public void setNombreQuestions(int nombreQuestions) {
        this.nombreQuestions = nombreQuestions > 0 ? nombreQuestions : NOMBRE_QUESTIONS_DEFAUT;
    }

    public int getSeuilReussite() {
        return seuilReussite;
    }

    public void setSeuilReussite(int seuilReussite) {
        this.seuilReussite = seuilReussite > 0 ? seuilReussite : SEUIL_REUSSITE_DEFAUT;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Calcule le score minimum requis pour réussir (nombre de bonnes réponses)
     * 
     * @return Nombre minimum de bonnes réponses pour réussir
     */
    public int getScoreMinimum() {
        return (int) Math.ceil((double) nombreQuestions * seuilReussite / 100);
    }

    /**
     * Vérifie si un score est suffisant pour réussir l'examen
     * 
     * @param score Nombre de bonnes réponses
     * @return true si le score est suffisant
     */
    public boolean estReussi(int score) {
        return score >= getScoreMinimum();
    }

    /**
     * Vérifie si un pourcentage est suffisant pour réussir l'examen
     * 
     * @param pourcentage Pourcentage obtenu
     * @return true si le pourcentage est suffisant
     */
    public boolean estReussi(double pourcentage) {
        return pourcentage >= seuilReussite;
    }

    /**
     * Calcule le pourcentage à partir d'un score
     * 
     * @param score Nombre de bonnes réponses
     * @return Pourcentage (0.0 à 100.0)
     */
    public double calculerPourcentage(int score) {
        if (nombreQuestions == 0) return 0.0;
        return (double) score / nombreQuestions * 100;
    }

    /**
     * Retourne le nom du module formaté
     * 
     * @return "Module X"
     */
    public String getModuleFormate() {
        return "Module " + moduleNumero;
    }

    /**
     * Retourne le seuil formaté
     * 
     * @return "75% (15/20)"
     */
    public String getSeuilFormate() {
        return seuilReussite + "% (" + getScoreMinimum() + "/" + nombreQuestions + ")";
    }

    // ===== MÉTHODES STANDARD =====

    @Override
    public String toString() {
        return "ExamenModule{" +
                "id=" + id +
                ", coursId=" + coursId +
                ", moduleNumero=" + moduleNumero +
                ", titre='" + titre + '\'' +
                ", nombreQuestions=" + nombreQuestions +
                ", seuilReussite=" + seuilReussite + "%" +
                ", scoreMinimum=" + getScoreMinimum() +
                '}';
    }
}