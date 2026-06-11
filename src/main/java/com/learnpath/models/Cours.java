// ============================================================
// FICHIER : src/main/java/com/LearnPath/models/Cours.java
// RÔLE    : Modèle représentant un cours complet sur la plateforme
//           Appartient à un thème et un niveau
//           Contient la liste des chapitres
// ============================================================

package com.learnpath.models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Cours - Un cours complet sur la plateforme LearnPath
 * 
 * Un cours appartient à un thème (ex: Développement Web) et un niveau
 * (élémentaire, moyen, avancé). Il est composé de plusieurs chapitres
 * qui contiennent eux-mêmes des leçons.
 * 
 * Chaque cours a un statut : brouillon, publié ou archivé.
 * 
 * @author LearnPath
 * @version 1.0
 */
public class Cours {

    // ===== ATTRIBUTS =====
    private int id;
    private String theme;
    private String niveau;
    private String titre;
    private String description;
    private String objectifs;
    private String prerequis;
    private String image;
    private String dureeEstimee;
    private int nombreChapitres;
    private int nombreLecons;
    private int ordre;
    private String statut;
    private Timestamp dateCreation;
    private Timestamp dateMaj;
    private List<Chapitre> chapitres;

    // ===== CONSTANTES =====
    public static final String NIVEAU_ELEMENTAIRE = "elementaire";
    public static final String NIVEAU_MOYEN       = "moyen";
    public static final String NIVEAU_AVANCE      = "avance";

    public static final String STATUT_BROUILLON   = "brouillon";
    public static final String STATUT_PUBLIE      = "publié";
    public static final String STATUT_ARCHIVE     = "archivé";

    // ===== CONSTRUCTEURS =====

    /**
     * Constructeur par défaut
     */
    public Cours() {
        this.niveau = NIVEAU_ELEMENTAIRE;
        this.ordre = 1;
        this.statut = STATUT_BROUILLON;
        this.nombreChapitres = 0;
        this.nombreLecons = 0;
        this.chapitres = new ArrayList<>();
    }

    /**
     * Constructeur pour création d'un cours
     * 
     * @param theme        Thème du cours
     * @param niveau       Niveau (elementaire, moyen, avance)
     * @param titre        Titre du cours
     * @param description  Description du cours
     */
    public Cours(String theme, String niveau, String titre, String description) {
        this.theme = theme;
        this.niveau = (niveau != null && !niveau.isEmpty()) ? niveau : NIVEAU_ELEMENTAIRE;
        this.titre = titre;
        this.description = description;
        this.ordre = 1;
        this.statut = STATUT_BROUILLON;
        this.nombreChapitres = 0;
        this.nombreLecons = 0;
        this.chapitres = new ArrayList<>();
    }

    /**
     * Constructeur complet
     * 
     * @param id               ID du cours
     * @param theme            Thème du cours
     * @param niveau           Niveau
     * @param titre            Titre
     * @param description      Description
     * @param objectifs        Objectifs pédagogiques
     * @param prerequis        Prérequis
     * @param image            URL de l'image
     * @param dureeEstimee     Durée estimée
     * @param nombreChapitres  Nombre de chapitres
     * @param nombreLecons     Nombre de leçons
     * @param ordre            Ordre d'affichage
     * @param statut           Statut (brouillon, publié, archivé)
     * @param dateCreation     Date de création
     * @param dateMaj          Date de mise à jour
     */
    public Cours(int id, String theme, String niveau, String titre, String description,
                 String objectifs, String prerequis, String image, String dureeEstimee,
                 int nombreChapitres, int nombreLecons, int ordre, String statut,
                 Timestamp dateCreation, Timestamp dateMaj) {
        this.id = id;
        this.theme = theme;
        this.niveau = (niveau != null && !niveau.isEmpty()) ? niveau : NIVEAU_ELEMENTAIRE;
        this.titre = titre;
        this.description = description;
        this.objectifs = objectifs;
        this.prerequis = prerequis;
        this.image = image;
        this.dureeEstimee = dureeEstimee;
        this.nombreChapitres = nombreChapitres;
        this.nombreLecons = nombreLecons;
        this.ordre = ordre;
        this.statut = (statut != null && !statut.isEmpty()) ? statut : STATUT_BROUILLON;
        this.dateCreation = dateCreation;
        this.dateMaj = dateMaj;
        this.chapitres = new ArrayList<>();
    }

    // ===== GETTERS ET SETTERS =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = (niveau != null && !niveau.isEmpty()) ? niveau : NIVEAU_ELEMENTAIRE;
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

    public String getPrerequis() {
        return prerequis;
    }

    public void setPrerequis(String prerequis) {
        this.prerequis = prerequis;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDureeEstimee() {
        return dureeEstimee;
    }

    public void setDureeEstimee(String dureeEstimee) {
        this.dureeEstimee = dureeEstimee;
    }

    public int getNombreChapitres() {
        return nombreChapitres;
    }

    public void setNombreChapitres(int nombreChapitres) {
        this.nombreChapitres = nombreChapitres;
    }

    public int getNombreLecons() {
        return nombreLecons;
    }

    public void setNombreLecons(int nombreLecons) {
        this.nombreLecons = nombreLecons;
    }

    public int getOrdre() {
        return ordre;
    }

    public void setOrdre(int ordre) {
        this.ordre = ordre;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = (statut != null && !statut.isEmpty()) ? statut : STATUT_BROUILLON;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Timestamp getDateMaj() {
        return dateMaj;
    }

    public void setDateMaj(Timestamp dateMaj) {
        this.dateMaj = dateMaj;
    }

    public List<Chapitre> getChapitres() {
        return chapitres;
    }

    public void setChapitres(List<Chapitre> chapitres) {
        this.chapitres = (chapitres != null) ? chapitres : new ArrayList<>();
    }

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Ajoute un chapitre au cours
     * 
     * @param chapitre Le chapitre à ajouter
     */
    public void ajouterChapitre(Chapitre chapitre) {
        if (chapitre != null) {
            this.chapitres.add(chapitre);
            this.nombreChapitres = this.chapitres.size();
            // Mettre à jour le nombre total de leçons
            this.nombreLecons = getNombreTotalLecons();
        }
    }

    /**
     * Calcule le nombre total de leçons dans tous les chapitres
     * 
     * @return Nombre total de leçons
     */
    public int getNombreTotalLecons() {
        if (chapitres == null || chapitres.isEmpty()) return 0;

        int total = 0;
        for (Chapitre chapitre : chapitres) {
            total += chapitre.getNombreLecons();
        }
        return total;
    }

    /**
     * Retourne le nombre de leçons gratuites dans tout le cours
     * 
     * @return Nombre de leçons gratuites
     */
    public int getNombreLeconsGratuites() {
        if (chapitres == null || chapitres.isEmpty()) return 0;

        int total = 0;
        for (Chapitre chapitre : chapitres) {
            total += chapitre.getNombreLeconsGratuites();
        }
        return total;
    }

    /**
     * Vérifie si le cours est publié
     * 
     * @return true si le statut est "publié"
     */
    public boolean isPublie() {
        return STATUT_PUBLIE.equals(statut);
    }

    /**
     * Vérifie si le cours est en brouillon
     * 
     * @return true si le statut est "brouillon"
     */
    public boolean isBrouillon() {
        return STATUT_BROUILLON.equals(statut);
    }

    /**
     * Vérifie si le cours est archivé
     * 
     * @return true si le statut est "archivé"
     */
    public boolean isArchive() {
        return STATUT_ARCHIVE.equals(statut);
    }

    /**
     * Retourne le nom formaté du niveau (première lettre en majuscule)
     * 
     * @return Nom formaté du niveau
     */
    public String getNiveauFormate() {
        if (niveau == null || niveau.isEmpty()) return "Élémentaire";

        switch (niveau.toLowerCase()) {
            case NIVEAU_ELEMENTAIRE: return "Élémentaire";
            case NIVEAU_MOYEN:       return "Moyen";
            case NIVEAU_AVANCE:      return "Avancé";
            default:                 return niveau.substring(0, 1).toUpperCase() + niveau.substring(1);
        }
    }

    /**
     * Retourne l'icône Font Awesome associée au niveau
     * 
     * @return Classe CSS de l'icône
     */
    public String getIconeNiveau() {
        if (niveau == null) return "fas fa-circle";

        switch (niveau.toLowerCase()) {
            case NIVEAU_ELEMENTAIRE: return "fas fa-seedling";
            case NIVEAU_MOYEN:       return "fas fa-fire";
            case NIVEAU_AVANCE:      return "fas fa-star";
            default:                 return "fas fa-circle";
        }
    }

    /**
     * Retourne la couleur associée au niveau
     * 
     * @return Code couleur hexadécimal
     */
    public String getCouleurNiveau() {
        if (niveau == null) return "#888888";

        switch (niveau.toLowerCase()) {
            case NIVEAU_ELEMENTAIRE: return "#27ae60";
            case NIVEAU_MOYEN:       return "#f39c12";
            case NIVEAU_AVANCE:      return "#e74c3c";
            default:                 return "#888888";
        }
    }

    /**
     * Retourne l'icône associée au thème du cours
     * 
     * @return Classe CSS de l'icône
     */
    public String getIconeTheme() {
        if (theme == null) return "fas fa-book";

        String t = theme.toLowerCase();
        if (t.contains("web"))              return "fas fa-globe";
        if (t.contains("java"))             return "fas fa-coffee";
        if (t.contains("python"))           return "fab fa-python";
        if (t.contains("php"))              return "fab fa-php";
        if (t.contains("base") || t.contains("sql")) return "fas fa-database";
        if (t.contains("sécurit"))          return "fas fa-shield-alt";
        if (t.contains("mobile"))           return "fas fa-mobile-alt";
        if (t.contains("intelligence") || t.contains("ia")) return "fas fa-robot";
        if (t.contains("design") || t.contains("ui")) return "fas fa-palette";
        if (t.contains("réseau"))           return "fas fa-network-wired";
        if (t.contains("système") || t.contains("linux")) return "fab fa-linux";
        if (t.contains("data") || t.contains("science")) return "fas fa-chart-pie";

        return "fas fa-book";
    }

    /**
     * Retourne la couleur associée au thème
     * 
     * @return Code couleur hexadécimal
     */
    public String getCouleurTheme() {
        if (theme == null) return "#667eea";

        String t = theme.toLowerCase();
        if (t.contains("web"))              return "#667eea";
        if (t.contains("java"))             return "#f093fb";
        if (t.contains("python"))           return "#4facfe";
        if (t.contains("php"))              return "#8892bf";
        if (t.contains("base") || t.contains("sql")) return "#43e97b";
        if (t.contains("sécurit"))          return "#f5576c";
        if (t.contains("mobile"))           return "#f39c12";
        if (t.contains("intelligence") || t.contains("ia")) return "#9b59b6";
        if (t.contains("design") || t.contains("ui")) return "#e91e63";
        if (t.contains("réseau"))           return "#00bcd4";
        if (t.contains("système") || t.contains("linux")) return "#ff9800";
        if (t.contains("data") || t.contains("science")) return "#795548";

        return "#667eea";
    }

    /**
     * Calcule le pourcentage de complétion basé sur le nombre de leçons terminées
     * 
     * @param leconsTerminees Nombre de leçons terminées par l'utilisateur
     * @return Pourcentage (0-100)
     */
    public int getPourcentageCompletion(int leconsTerminees) {
        int total = getNombreTotalLecons();
        if (total == 0) return 0;
        return (leconsTerminees * 100) / total;
    }

    /**
     * Calcule la durée totale du cours en minutes
     * 
     * @return Durée en minutes
     */
    public int getDureeTotaleMinutes() {
        int total = 0;
        if (chapitres != null) {
            for (Chapitre chapitre : chapitres) {
                String duree = chapitre.getDureeEstimee();
                if (duree != null) {
                    try {
                        if (duree.contains("h")) {
                            String[] parts = duree.split("h");
                            total += Integer.parseInt(parts[0]) * 60;
                            if (parts.length > 1 && !parts[1].isEmpty()) {
                                total += Integer.parseInt(parts[1].replace("min", "").trim());
                            }
                        } else if (duree.contains("min")) {
                            total += Integer.parseInt(duree.replace("min", "").trim());
                        }
                    } catch (NumberFormatException e) {
                        // Ignorer
                    }
                }
            }
        }
        return total;
    }

    // ===== MÉTHODES STANDARD =====

    @Override
    public String toString() {
        return "Cours{" +
                "id=" + id +
                ", theme='" + theme + '\'' +
                ", niveau='" + niveau + '\'' +
                ", titre='" + titre + '\'' +
                ", nbChapitres=" + nombreChapitres +
                ", nbLecons=" + nombreLecons +
                ", statut='" + statut + '\'' +
                '}';
    }
}