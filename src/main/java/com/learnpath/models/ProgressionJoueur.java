package com.learnpath.models;

import java.sql.Timestamp;

public class ProgressionJoueur {
    
    private int id;
    private int joueurId;
    private int quizId;
    private String niveau;
    private int partie;
    private int score;
    private int totalQuestions;
    private boolean reussi;
    private Timestamp dateRealisation;
    
    // Constructeur par défaut
    public ProgressionJoueur() {
        this.reussi = false;
        this.score = 0;
        this.totalQuestions = 0;
    }
    
    // Constructeur pour enregistrement
    public ProgressionJoueur(int joueurId, int quizId, String niveau, int partie, int score, int totalQuestions, boolean reussi) {
        this.joueurId = joueurId;
        this.quizId = quizId;
        this.niveau = niveau;
        this.partie = partie;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.reussi = reussi;
    }
    
    // Constructeur complet
    public ProgressionJoueur(int id, int joueurId, int quizId, String niveau, int partie, int score, int totalQuestions, boolean reussi, Timestamp dateRealisation) {
        this.id = id;
        this.joueurId = joueurId;
        this.quizId = quizId;
        this.niveau = niveau;
        this.partie = partie;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.reussi = reussi;
        this.dateRealisation = dateRealisation;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getJoueurId() {
        return joueurId;
    }
    
    public void setJoueurId(int joueurId) {
        this.joueurId = joueurId;
    }
    
    public int getQuizId() {
        return quizId;
    }
    
    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }
    
    public String getNiveau() {
        return niveau;
    }
    
    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }
    
    public int getPartie() {
        return partie;
    }
    
    public void setPartie(int partie) {
        this.partie = partie;
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
    
    public boolean isReussi() {
        return reussi;
    }
    
    public void setReussi(boolean reussi) {
        this.reussi = reussi;
    }
    
    public Timestamp getDateRealisation() {
        return dateRealisation;
    }
    
    public void setDateRealisation(Timestamp dateRealisation) {
        this.dateRealisation = dateRealisation;
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
    
    @Override
    public String toString() {
        return "ProgressionJoueur{" +
                "id=" + id +
                ", joueurId=" + joueurId +
                ", quizId=" + quizId +
                ", niveau='" + niveau + '\'' +
                ", partie=" + partie +
                ", score=" + score +
                ", reussi=" + reussi +
                '}';
    }
}