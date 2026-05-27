package com.amijayquiz.models;

import java.sql.Timestamp;

public class Quiz {
    
    private int id;
    private String titre;
    private String description;
    private int dureeMaxMinutes;
    private String niveau;
    private int ordrePartie;
    private Timestamp dateCreation;
    private int nombreQuestions;
    
    // Constructeur par défaut
    public Quiz() {
        this.dureeMaxMinutes = 10;
        this.niveau = "elementaire";
        this.ordrePartie = 1;
        this.nombreQuestions = 0;
    }
    
    // Constructeur pour création
    public Quiz(String titre, String description, int dureeMaxMinutes, String niveau, int ordrePartie) {
        this.titre = titre;
        this.description = description;
        this.dureeMaxMinutes = dureeMaxMinutes;
        this.niveau = niveau;
        this.ordrePartie = ordrePartie;
        this.nombreQuestions = 0;
    }
    
    // Constructeur complet
    public Quiz(int id, String titre, String description, int dureeMaxMinutes, String niveau, int ordrePartie, Timestamp dateCreation, int nombreQuestions) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.dureeMaxMinutes = dureeMaxMinutes;
        this.niveau = niveau;
        this.ordrePartie = ordrePartie;
        this.dateCreation = dateCreation;
        this.nombreQuestions = nombreQuestions;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
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
    
    public int getDureeMaxMinutes() {
        return dureeMaxMinutes;
    }
    
    public void setDureeMaxMinutes(int dureeMaxMinutes) {
        this.dureeMaxMinutes = dureeMaxMinutes;
    }
    
    public String getNiveau() {
        return niveau;
    }
    
    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }
    
    public int getOrdrePartie() {
        return ordrePartie;
    }
    
    public void setOrdrePartie(int ordrePartie) {
        this.ordrePartie = ordrePartie;
    }
    
    public Timestamp getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public int getNombreQuestions() {
        return nombreQuestions;
    }
    
    public void setNombreQuestions(int nombreQuestions) {
        this.nombreQuestions = nombreQuestions;
    }
    
    /**
     * Retourne le nom du niveau formaté avec la première lettre en majuscule
     */
    public String getNiveauFormate() {
        if (niveau == null || niveau.isEmpty()) {
            return "Élémentaire";
        }
        switch (niveau.toLowerCase()) {
            case "elementaire":
                return "Élémentaire";
            case "moyen":
                return "Moyen";
            case "avance":
                return "Avancé";
            default:
                return niveau.substring(0, 1).toUpperCase() + niveau.substring(1);
        }
    }
    
    /**
     * Retourne la couleur associée au niveau
     */
    public String getCouleurNiveau() {
        if (niveau == null) {
            return "#667eea";
        }
        switch (niveau.toLowerCase()) {
            case "elementaire":
                return "#27ae60";
            case "moyen":
                return "#f39c12";
            case "avance":
                return "#e74c3c";
            default:
                return "#667eea";
        }
    }
    
    /**
     * Retourne l'icône associée au niveau
     */
    public String getIconeNiveau() {
        if (niveau == null) {
            return "fas fa-circle";
        }
        switch (niveau.toLowerCase()) {
            case "elementaire":
                return "fas fa-seedling";
            case "moyen":
                return "fas fa-fire";
            case "avance":
                return "fas fa-star";
            default:
                return "fas fa-circle";
        }
    }
    
    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", niveau='" + niveau + '\'' +
                ", ordrePartie=" + ordrePartie +
                ", dureeMaxMinutes=" + dureeMaxMinutes +
                ", nombreQuestions=" + nombreQuestions +
                '}';
    }
}