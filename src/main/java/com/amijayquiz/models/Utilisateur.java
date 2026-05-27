package com.amijayquiz.models;

import java.sql.Timestamp;

public class Utilisateur {
    
    private int id;
    private String pseudo;
    private String email;
    private String motDePasse;
    private String role;
    private Timestamp dateInscription;
    private boolean actif;
    
    // Constructeur par défaut
    public Utilisateur() {
        this.role = "joueur";
        this.actif = true;
    }
    
    // Constructeur pour l'inscription (pseudo + mot de passe)
    public Utilisateur(String pseudo, String motDePasse) {
        this.pseudo = pseudo;
        this.motDePasse = motDePasse;
        this.role = "joueur";
        this.actif = true;
    }
    
    // Constructeur pour l'inscription (pseudo + email + mot de passe)
    public Utilisateur(String pseudo, String email, String motDePasse) {
        this.pseudo = pseudo;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = "joueur";
        this.actif = true;
    }
    
    // Constructeur complet
    public Utilisateur(int id, String pseudo, String email, String motDePasse, String role, Timestamp dateInscription, boolean actif) {
        this.id = id;
        this.pseudo = pseudo;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.dateInscription = dateInscription;
        this.actif = actif;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getPseudo() {
        return pseudo;
    }
    
    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getMotDePasse() {
        return motDePasse;
    }
    
    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public Timestamp getDateInscription() {
        return dateInscription;
    }
    
    public void setDateInscription(Timestamp dateInscription) {
        this.dateInscription = dateInscription;
    }
    
    public boolean isActif() {
        return actif;
    }
    
    public void setActif(boolean actif) {
        this.actif = actif;
    }
    
    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", pseudo='" + pseudo + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", dateInscription=" + dateInscription +
                ", actif=" + actif +
                '}';
    }
}