// ============================================================
// FICHIER : src/main/java/com/LearnPath/models/ProgressionCours.java
// RÔLE    : Modèle représentant la progression d'un utilisateur
//           dans une leçon d'un cours (suivi individuel)
// ============================================================

package com.learnpath.models;

import java.sql.Timestamp;

/**
 * ProgressionCours - Suivi de progression d'un utilisateur dans un cours
 * 
 * Enregistre leçon par leçon si l'utilisateur a terminé,
 * son score au quiz éventuel et le temps passé.
 * Permet de calculer l'avancement global dans un cours.
 * 
 * @author LearnPath
 * @version 1.0
 */
public class ProgressionCours {

    // ===== ATTRIBUTS =====
    private int id;
    private int utilisateurId;
    private int coursId;
    private int chapitreId;
    private int leconId;
    private boolean termine;
    private int scoreQuiz;
    private int tempsPasse;
    private Timestamp dateDebut;
    private Timestamp dateCompletion;

    // ===== CONSTRUCTEURS =====

    /**
     * Constructeur par défaut
     */
    public ProgressionCours() {
        this.termine = false;
        this.scoreQuiz = 0;
        this.tempsPasse = 0;
    }

    /**
     * Constructeur pour création (quand l'utilisateur commence une leçon)
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param coursId       ID du cours
     * @param chapitreId    ID du chapitre
     * @param leconId       ID de la leçon
     */
    public ProgressionCours(int utilisateurId, int coursId, int chapitreId, int leconId) {
        this.utilisateurId = utilisateurId;
        this.coursId = coursId;
        this.chapitreId = chapitreId;
        this.leconId = leconId;
        this.termine = false;
        this.scoreQuiz = 0;
        this.tempsPasse = 0;
        this.dateDebut = new Timestamp(System.currentTimeMillis());
    }

    /**
     * Constructeur complet
     * 
     * @param id              ID de la progression
     * @param utilisateurId   ID de l'utilisateur
     * @param coursId         ID du cours
     * @param chapitreId      ID du chapitre
     * @param leconId         ID de la leçon
     * @param termine         Leçon terminée ?
     * @param scoreQuiz       Score au quiz
     * @param tempsPasse      Temps passé en secondes
     * @param dateDebut       Date de début
     * @param dateCompletion  Date de fin
     */
    public ProgressionCours(int id, int utilisateurId, int coursId, int chapitreId,
                            int leconId, boolean termine, int scoreQuiz, int tempsPasse,
                            Timestamp dateDebut, Timestamp dateCompletion) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.coursId = coursId;
        this.chapitreId = chapitreId;
        this.leconId = leconId;
        this.termine = termine;
        this.scoreQuiz = scoreQuiz;
        this.tempsPasse = tempsPasse;
        this.dateDebut = dateDebut;
        this.dateCompletion = dateCompletion;
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

    public int getChapitreId() {
        return chapitreId;
    }

    public void setChapitreId(int chapitreId) {
        this.chapitreId = chapitreId;
    }

    public int getLeconId() {
        return leconId;
    }

    public void setLeconId(int leconId) {
        this.leconId = leconId;
    }

    public boolean isTermine() {
        return termine;
    }

    public void setTermine(boolean termine) {
        this.termine = termine;
    }

    public int getScoreQuiz() {
        return scoreQuiz;
    }

    public void setScoreQuiz(int scoreQuiz) {
        this.scoreQuiz = scoreQuiz;
    }

    public int getTempsPasse() {
        return tempsPasse;
    }

    public void setTempsPasse(int tempsPasse) {
        this.tempsPasse = tempsPasse;
    }

    public Timestamp getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Timestamp dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Timestamp getDateCompletion() {
        return dateCompletion;
    }

    public void setDateCompletion(Timestamp dateCompletion) {
        this.dateCompletion = dateCompletion;
    }

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Marque la leçon comme terminée avec la date du jour
     */
    public void marquerTermine() {
        this.termine = true;
        this.dateCompletion = new Timestamp(System.currentTimeMillis());
    }

    /**
     * Marque la leçon comme terminée avec score
     * 
     * @param score Score obtenu au quiz
     */
    public void marquerTermineAvecScore(int score) {
        this.termine = true;
        this.scoreQuiz = score;
        this.dateCompletion = new Timestamp(System.currentTimeMillis());
    }

    /**
     * Ajoute du temps passé sur la leçon
     * 
     * @param secondes Nombre de secondes à ajouter
     */
    public void ajouterTemps(int secondes) {
        if (secondes > 0) {
            this.tempsPasse += secondes;
        }
    }

    /**
     * Calcule la durée passée entre le début et maintenant
     * Utile si la leçon n'est pas encore terminée
     * 
     * @return Durée en secondes
     */
    public long getDureeActuelle() {
        if (dateDebut == null) return 0;
        long maintenant = System.currentTimeMillis();
        long debut = dateDebut.getTime();
        return (maintenant - debut) / 1000;
    }

    /**
     * Formate le temps passé en chaîne lisible
     * 
     * @return Temps formaté (ex: "12 min 30 s")
     */
    public String getTempsPasseFormate() {
        int minutes = tempsPasse / 60;
        int secondes = tempsPasse % 60;

        if (minutes > 0) {
            return minutes + " min " + secondes + " s";
        }
        return secondes + " s";
    }

    /**
     * Vérifie si la progression a un score de quiz
     * 
     * @return true si un score a été enregistré
     */
    public boolean hasScoreQuiz() {
        return scoreQuiz > 0;
    }

    // ===== MÉTHODES STANDARD =====

    @Override
    public String toString() {
        return "ProgressionCours{" +
                "id=" + id +
                ", utilisateurId=" + utilisateurId +
                ", coursId=" + coursId +
                ", chapitreId=" + chapitreId +
                ", leconId=" + leconId +
                ", termine=" + termine +
                ", scoreQuiz=" + scoreQuiz +
                ", tempsPasse=" + getTempsPasseFormate() +
                '}';
    }
}