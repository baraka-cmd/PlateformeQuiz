// ============================================================
// FICHIER : src/main/java/com/LearnPath/models/Ressource.java
// RÔLE    : Modèle représentant une ressource complémentaire
//           associée à une leçon (PDF, image, code source, lien)
// ============================================================

package com.learnpath.models;

import java.sql.Timestamp;

/**
 * Ressource - Ressource complémentaire d'une leçon
 * 
 * Une ressource est un fichier ou lien attaché à une leçon.
 * Types supportés : pdf, image, zip, code, lien externe.
 * 
 * @author LearnPath
 * @version 1.0
 */
public class Ressource {

    // ===== ATTRIBUTS =====
    private int id;
    private int leconId;
    private String titre;
    private String description;
    private String type;
    private String urlFichier;
    private String taille;
    private Timestamp dateAjout;

    // ===== CONSTANTES DE TYPE =====
    public static final String TYPE_PDF   = "pdf";
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_ZIP   = "zip";
    public static final String TYPE_CODE  = "code";
    public static final String TYPE_LIEN  = "lien";

    // ===== CONSTRUCTEURS =====

    /**
     * Constructeur par défaut
     */
    public Ressource() {
        this.type = TYPE_PDF;
    }

    /**
     * Constructeur pour création d'une ressource
     * 
     * @param leconId    ID de la leçon associée
     * @param titre      Titre de la ressource
     * @param type       Type (pdf, image, zip, code, lien)
     * @param urlFichier URL du fichier ou lien
     */
    public Ressource(int leconId, String titre, String type, String urlFichier) {
        this.leconId = leconId;
        this.titre = titre;
        this.type = (type != null && !type.isEmpty()) ? type : TYPE_PDF;
        this.urlFichier = urlFichier;
    }

    /**
     * Constructeur complet
     * 
     * @param id          ID de la ressource
     * @param leconId     ID de la leçon associée
     * @param titre       Titre de la ressource
     * @param description Description détaillée
     * @param type        Type de ressource
     * @param urlFichier  URL du fichier
     * @param taille      Taille lisible (ex: "2.5 MB")
     * @param dateAjout   Date d'ajout
     */
    public Ressource(int id, int leconId, String titre, String description, 
                     String type, String urlFichier, String taille, Timestamp dateAjout) {
        this.id = id;
        this.leconId = leconId;
        this.titre = titre;
        this.description = description;
        this.type = (type != null && !type.isEmpty()) ? type : TYPE_PDF;
        this.urlFichier = urlFichier;
        this.taille = taille;
        this.dateAjout = dateAjout;
    }

    // ===== GETTERS ET SETTERS =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLeconId() {
        return leconId;
    }

    public void setLeconId(int leconId) {
        this.leconId = leconId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = (type != null && !type.isEmpty()) ? type : TYPE_PDF;
    }

    public String getUrlFichier() {
        return urlFichier;
    }

    public void setUrlFichier(String urlFichier) {
        this.urlFichier = urlFichier;
    }

    public String getTaille() {
        return taille;
    }

    public void setTaille(String taille) {
        this.taille = taille;
    }

    public Timestamp getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(Timestamp dateAjout) {
        this.dateAjout = dateAjout;
    }

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Retourne l'icône Font Awesome correspondant au type de ressource
     * 
     * @return Classe CSS de l'icône
     */
    public String getIcone() {
        switch (type != null ? type : "") {
            case TYPE_PDF:   return "fas fa-file-pdf";
            case TYPE_IMAGE: return "fas fa-image";
            case TYPE_ZIP:   return "fas fa-file-archive";
            case TYPE_CODE:  return "fas fa-code";
            case TYPE_LIEN:  return "fas fa-external-link-alt";
            default:         return "fas fa-file";
        }
    }

    /**
     * Retourne la couleur associée au type de ressource
     * 
     * @return Code couleur hexadécimal
     */
    public String getCouleurType() {
        switch (type != null ? type : "") {
            case TYPE_PDF:   return "#e74c3c";
            case TYPE_IMAGE: return "#27ae60";
            case TYPE_ZIP:   return "#f39c12";
            case TYPE_CODE:  return "#667eea";
            case TYPE_LIEN:  return "#2980b9";
            default:         return "#888888";
        }
    }

    /**
     * Vérifie si le type de ressource est valide
     * 
     * @return true si le type est valide
     */
    public boolean isTypeValide() {
        return TYPE_PDF.equals(type) || TYPE_IMAGE.equals(type) || 
               TYPE_ZIP.equals(type) || TYPE_CODE.equals(type) || 
               TYPE_LIEN.equals(type);
    }

    // ===== MÉTHODES STANDARD =====

    @Override
    public String toString() {
        return "Ressource{" +
                "id=" + id +
                ", leconId=" + leconId +
                ", titre='" + titre + '\'' +
                ", type='" + type + '\'' +
                ", urlFichier='" + urlFichier + '\'' +
                '}';
    }
}