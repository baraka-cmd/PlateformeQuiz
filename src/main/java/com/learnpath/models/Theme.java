// ============================================================
// FICHIER : src/main/java/com/LearnPath/models/Theme.java
// RÔLE    : Modèle représentant un thème de cours
// ============================================================

package com.learnpath.models;

import java.sql.Timestamp;

/**
 * Theme - Un thème de cours sur la plateforme
 * 
 * Chaque thème regroupe plusieurs cours.
 * 12 thèmes existent par défaut, l'admin peut en ajouter.
 * 
 * @author LearnPath
 * @version 1.0
 */
public class Theme {

    // ===== ATTRIBUTS =====
    private int id;
    private String nom;
    private String description;
    private String icone;
    private String couleur;
    private int ordre;
    private Timestamp dateCreation;

    // ===== CONSTRUCTEURS =====

    public Theme() {
        this.icone = "fas fa-book";
        this.couleur = "#667eea";
        this.ordre = 1;
    }

    public Theme(String nom, String description, String icone, String couleur, int ordre) {
        this.nom = nom;
        this.description = description;
        this.icone = (icone != null && !icone.isEmpty()) ? icone : "fas fa-book";
        this.couleur = (couleur != null && !couleur.isEmpty()) ? couleur : "#667eea";
        this.ordre = ordre > 0 ? ordre : 1;
    }

    public Theme(int id, String nom, String description, String icone, 
                 String couleur, int ordre, Timestamp dateCreation) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.icone = (icone != null && !icone.isEmpty()) ? icone : "fas fa-book";
        this.couleur = (couleur != null && !couleur.isEmpty()) ? couleur : "#667eea";
        this.ordre = ordre > 0 ? ordre : 1;
        this.dateCreation = dateCreation;
    }

    // ===== GETTERS ET SETTERS =====

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcone() { return icone; }
    public void setIcone(String icone) { 
        this.icone = (icone != null && !icone.isEmpty()) ? icone : "fas fa-book"; 
    }

    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { 
        this.couleur = (couleur != null && !couleur.isEmpty()) ? couleur : "#667eea"; 
    }

    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre > 0 ? ordre : 1; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Retourne la couleur en version claire (avec transparence)
     * Utile pour les fonds de carte
     */
    public String getCouleurClaire() {
        return couleur + "20"; // 20 = 12.5% d'opacité en hex
    }

    /**
     * Vérifie si l'icône est valide (commence par "fas fa-" ou "fab fa-")
     */
    public boolean isIconeValide() {
        return icone != null && (icone.startsWith("fas fa-") || icone.startsWith("fab fa-") || icone.startsWith("far fa-"));
    }

    @Override
    public String toString() {
        return "Theme{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", icone='" + icone + '\'' +
                ", couleur='" + couleur + '\'' +
                ", ordre=" + ordre +
                '}';
    }
}