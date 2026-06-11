package com.learnpath.models;

import java.sql.Timestamp;

public class Partie {
    
    private int id;
    private int idJoueur;
    private int idQuiz;
    private int score;
    private int totalQuestions;
    private int tempsRealiseSecondes;
    private Timestamp datePartie;
    private String titreQuiz;
    private String pseudoJoueur;
    
    // Constructeur par défaut
    public Partie() {
        this.score = 0;
        this.totalQuestions = 0;
        this.tempsRealiseSecondes = 0;
    }
    
    // Constructeur pour enregistrement d'une partie
    public Partie(int idJoueur, int idQuiz, int score, int totalQuestions, int tempsRealiseSecondes) {
        this.idJoueur = idJoueur;
        this.idQuiz = idQuiz;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.tempsRealiseSecondes = tempsRealiseSecondes;
    }
    
    // Constructeur complet
    public Partie(int id, int idJoueur, int idQuiz, int score, int totalQuestions, int tempsRealiseSecondes, Timestamp datePartie) {
        this.id = id;
        this.idJoueur = idJoueur;
        this.idQuiz = idQuiz;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.tempsRealiseSecondes = tempsRealiseSecondes;
        this.datePartie = datePartie;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getIdJoueur() {
        return idJoueur;
    }
    
    public void setIdJoueur(int idJoueur) {
        this.idJoueur = idJoueur;
    }
    
    public int getIdQuiz() {
        return idQuiz;
    }
    
    public void setIdQuiz(int idQuiz) {
        this.idQuiz = idQuiz;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public int getTotalQuestions() {
        return totalQuestions;
    }
    
    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
    
    public int getTempsRealiseSecondes() {
        return tempsRealiseSecondes;
    }
    
    public void setTempsRealiseSecondes(int tempsRealiseSecondes) {
        this.tempsRealiseSecondes = tempsRealiseSecondes;
    }
    
    public Timestamp getDatePartie() {
        return datePartie;
    }
    
    public void setDatePartie(Timestamp datePartie) {
        this.datePartie = datePartie;
    }
    
    public String getTitreQuiz() {
        return titreQuiz;
    }
    
    public void setTitreQuiz(String titreQuiz) {
        this.titreQuiz = titreQuiz;
    }
    
    public String getPseudoJoueur() {
        return pseudoJoueur;
    }
    
    public void setPseudoJoueur(String pseudoJoueur) {
        this.pseudoJoueur = pseudoJoueur;
    }
    
    /**
     * Calcule le pourcentage de réussite
     */
    public double getPourcentage() {
        if (totalQuestions == 0) {
            return 0.0;
        }
        return (double) score / totalQuestions * 100;
    }
    
    /**
     * Formate le temps réalisé en chaîne lisible (ex: 2 min 30 s)
     */
    public String getTempsFormate() {
        int minutes = tempsRealiseSecondes / 60;
        int secondes = tempsRealiseSecondes % 60;
        
        if (minutes > 0) {
            return minutes + " min " + secondes + " s";
        } else {
            return secondes + " s";
        }
    }
    
    @Override
    public String toString() {
        return "Partie{" +
                "id=" + id +
                ", idJoueur=" + idJoueur +
                ", idQuiz=" + idQuiz +
                ", score=" + score +
                ", totalQuestions=" + totalQuestions +
                ", pourcentage=" + String.format("%.1f%%", getPourcentage()) +
                ", temps=" + getTempsFormate() +
                '}';
    }
}