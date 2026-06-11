// ============================================================
// FICHIER : src/main/java/com/LearnPath/models/Chapitre.java
// RÔLE    : Modèle représentant un chapitre dans un module de cours
//           Contient la liste ordonnée des leçons
// ============================================================

package com.learnpath.models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Chapitre - Un chapitre dans un module de cours
 * 
 * Un chapitre regroupe plusieurs leçons sur un même sujet.
 * Chaque chapitre appartient à un module (1 à 15).
 * L'ordre des chapitres définit la progression pédagogique.
 * Un quiz optionnel peut valider l'acquisition des connaissances.
 * 
 * @author LearnPath
 * @version 1.1
 */
public class Chapitre {

    // ===== ATTRIBUTS =====
    private int id;
    private int coursId;
    private String titre;
    private String description;
    private String objectifs;
    private int ordre;
    private int moduleNumero;
    private Integer quizId;
    private Timestamp dateCreation;
    private List<Lecon> lecons;

    // ===== CONSTRUCTEURS =====

    /**
     * Constructeur par défaut
     */
    public Chapitre() {
        this.ordre = 1;
        this.moduleNumero = 1;
        this.lecons = new ArrayList<>();
    }

    /**
     * Constructeur pour création d'un chapitre
     * 
     * @param coursId      ID du cours parent
     * @param titre        Titre du chapitre
     * @param ordre        Ordre dans le module
     * @param moduleNumero Numéro du module (1 à 15)
     */
    public Chapitre(int coursId, String titre, int ordre, int moduleNumero) {
        this.coursId = coursId;
        this.titre = titre;
        this.ordre = ordre;
        this.moduleNumero = moduleNumero > 0 ? moduleNumero : 1;
        this.lecons = new ArrayList<>();
    }

    /**
     * Constructeur complet
     * 
     * @param id            ID du chapitre
     * @param coursId       ID du cours parent
     * @param titre         Titre du chapitre
     * @param description   Description du chapitre
     * @param objectifs     Objectifs pédagogiques
     * @param ordre         Ordre dans le module
     * @param moduleNumero  Numéro du module (1 à 15)
     * @param quizId        ID du quiz de validation (peut être null)
     * @param dateCreation  Date de création
     */
    public Chapitre(int id, int coursId, String titre, String description,
                    String objectifs, int ordre, int moduleNumero, 
                    Integer quizId, Timestamp dateCreation) {
        this.id = id;
        this.coursId = coursId;
        this.titre = titre;
        this.description = description;
        this.objectifs = objectifs;
        this.ordre = ordre;
        this.moduleNumero = moduleNumero > 0 ? moduleNumero : 1;
        this.quizId = quizId;
        this.dateCreation = dateCreation;
        this.lecons = new ArrayList<>();
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

    public String getObjectifs() {
        return objectifs;
    }

    public void setObjectifs(String objectifs) {
        this.objectifs = objectifs;
    }

    public int getOrdre() {
        return ordre;
    }

    public void setOrdre(int ordre) {
        this.ordre = ordre;
    }

    public int getModuleNumero() {
        return moduleNumero;
    }

    public void setModuleNumero(int moduleNumero) {
        this.moduleNumero = moduleNumero > 0 ? moduleNumero : 1;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public void setQuizId(Integer quizId) {
        this.quizId = quizId;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    public List<Lecon> getLecons() {
        return lecons;
    }

    public void setLecons(List<Lecon> lecons) {
        this.lecons = (lecons != null) ? lecons : new ArrayList<>();
    }

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Ajoute une leçon au chapitre
     * 
     * @param lecon La leçon à ajouter
     */
    public void ajouterLecon(Lecon lecon) {
        if (lecon != null) {
            this.lecons.add(lecon);
        }
    }

    /**
     * Retourne le nombre total de leçons dans ce chapitre
     * 
     * @return Nombre de leçons
     */
    public int getNombreLecons() {
        return (lecons != null) ? lecons.size() : 0;
    }

    /**
     * Retourne le nombre de leçons gratuites dans ce chapitre
     * 
     * @return Nombre de leçons gratuites
     */
    public int getNombreLeconsGratuites() {
        if (lecons == null || lecons.isEmpty()) return 0;
        int count = 0;
        for (Lecon lecon : lecons) {
            if (lecon.isGratuit()) count++;
        }
        return count;
    }

    /**
     * Vérifie si le chapitre a un quiz de validation
     * 
     * @return true si un quiz est associé
     */
    public boolean hasQuiz() {
        return quizId != null && quizId > 0;
    }

    /**
     * Vérifie si toutes les leçons du chapitre sont gratuites
     * 
     * @return true si tout le chapitre est gratuit
     */
    public boolean isEntierementGratuit() {
        if (lecons == null || lecons.isEmpty()) return true;
        for (Lecon lecon : lecons) {
            if (!lecon.isGratuit()) return false;
        }
        return true;
    }

    /**
     * Retourne la durée totale estimée du chapitre
     * 
     * @return Durée estimée formatée (ex: "2h30")
     */
    public String getDureeEstimee() {
        int totalMinutes = 0;
        if (lecons != null) {
            for (Lecon lecon : lecons) {
                String duree = lecon.getDureeVideo();
                if (duree != null && duree.contains(":")) {
                    try {
                        String[] parts = duree.split(":");
                        totalMinutes += Integer.parseInt(parts[0]);
                        if (Integer.parseInt(parts[1]) > 30) totalMinutes++;
                    } catch (NumberFormatException e) {
                        // Ignorer
                    }
                }
            }
        }
        if (totalMinutes >= 60) {
            int heures = totalMinutes / 60;
            int minutes = totalMinutes % 60;
            return minutes > 0 ? heures + "h" + String.format("%02d", minutes) : heures + "h";
        }
        return totalMinutes + "min";
    }

    // ===== MÉTHODES STANDARD =====

    @Override
    public String toString() {
        return "Chapitre{" +
                "id=" + id +
                ", coursId=" + coursId +
                ", titre='" + titre + '\'' +
                ", moduleNumero=" + moduleNumero +
                ", ordre=" + ordre +
                ", nbLecons=" + getNombreLecons() +
                ", hasQuiz=" + hasQuiz() +
                '}';
    }
}