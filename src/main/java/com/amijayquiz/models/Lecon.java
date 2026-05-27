// ============================================================
// FICHIER : src/main/java/com/amijayquiz/models/Lecon.java
// RÔLE    : Modèle représentant une leçon dans un chapitre
//           Contient le contenu texte, code source,
//           ressources et temps de lecture estimé
// ============================================================

package com.amijayquiz.models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Lecon - Une leçon dans un chapitre de cours
 * 
 * Une leçon est l'unité d'apprentissage de base.
 * Elle contient du texte enrichi (HTML) avec explications complètes,
 * du code source de démonstration prêt à copier-coller,
 * et des ressources téléchargeables.
 * 
 * Pas de vidéo : l'apprentissage se fait par la lecture
 * et la pratique avec des exemples de code.
 * 
 * @author AmijayQuiz
 * @version 1.1
 */
public class Lecon {

    // ===== ATTRIBUTS =====
    private int id;
    private int chapitreId;
    private String titre;
    private String contenuTexte;
    private String urlVideo;
    private String dureeVideo;
    private String codeSource;
    private String fichierJoint;
    private int ordre;
    private boolean gratuit;
    private Timestamp dateCreation;
    private List<Ressource> ressources;

    // ===== CONSTRUCTEURS =====

    /**
     * Constructeur par défaut
     */
    public Lecon() {
        this.ordre = 1;
        this.gratuit = true;
        this.ressources = new ArrayList<>();
    }

    /**
     * Constructeur pour création d'une leçon
     * 
     * @param chapitreId ID du chapitre parent
     * @param titre      Titre de la leçon
     * @param ordre      Ordre dans le chapitre
     */
    public Lecon(int chapitreId, String titre, int ordre) {
        this.chapitreId = chapitreId;
        this.titre = titre;
        this.ordre = ordre;
        this.gratuit = true;
        this.ressources = new ArrayList<>();
    }

    /**
     * Constructeur complet
     * 
     * @param id            ID de la leçon
     * @param chapitreId    ID du chapitre parent
     * @param titre         Titre de la leçon
     * @param contenuTexte  Contenu texte enrichi (HTML)
     * @param urlVideo      URL de la vidéo (optionnelle, NULL par défaut)
     * @param dureeVideo    Durée de la vidéo (optionnelle)
     * @param codeSource    Code source de démonstration
     * @param fichierJoint  Fichier téléchargeable
     * @param ordre         Ordre dans le chapitre
     * @param gratuit       Accessible gratuitement ?
     * @param dateCreation  Date de création
     */
    public Lecon(int id, int chapitreId, String titre, String contenuTexte,
                 String urlVideo, String dureeVideo, String codeSource,
                 String fichierJoint, int ordre, boolean gratuit, Timestamp dateCreation) {
        this.id = id;
        this.chapitreId = chapitreId;
        this.titre = titre;
        this.contenuTexte = contenuTexte;
        this.urlVideo = urlVideo;
        this.dureeVideo = dureeVideo;
        this.codeSource = codeSource;
        this.fichierJoint = fichierJoint;
        this.ordre = ordre;
        this.gratuit = gratuit;
        this.dateCreation = dateCreation;
        this.ressources = new ArrayList<>();
    }

    // ===== GETTERS ET SETTERS =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChapitreId() {
        return chapitreId;
    }

    public void setChapitreId(int chapitreId) {
        this.chapitreId = chapitreId;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenuTexte() {
        return contenuTexte;
    }

    public void setContenuTexte(String contenuTexte) {
        this.contenuTexte = contenuTexte;
    }

    public String getUrlVideo() {
        return urlVideo;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }

    public String getDureeVideo() {
        return dureeVideo;
    }

    public void setDureeVideo(String dureeVideo) {
        this.dureeVideo = dureeVideo;
    }

    public String getCodeSource() {
        return codeSource;
    }

    public void setCodeSource(String codeSource) {
        this.codeSource = codeSource;
    }

    public String getFichierJoint() {
        return fichierJoint;
    }

    public void setFichierJoint(String fichierJoint) {
        this.fichierJoint = fichierJoint;
    }

    public int getOrdre() {
        return ordre;
    }

    public void setOrdre(int ordre) {
        this.ordre = ordre;
    }

    public boolean isGratuit() {
        return gratuit;
    }

    public void setGratuit(boolean gratuit) {
        this.gratuit = gratuit;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    public List<Ressource> getRessources() {
        return ressources;
    }

    public void setRessources(List<Ressource> ressources) {
        this.ressources = (ressources != null) ? ressources : new ArrayList<>();
    }

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Ajoute une ressource à la leçon
     * 
     * @param ressource La ressource à ajouter
     */
    public void ajouterRessource(Ressource ressource) {
        if (ressource != null) {
            this.ressources.add(ressource);
        }
    }

    /**
     * Retourne le nombre de ressources associées
     * 
     * @return Nombre de ressources
     */
    public int getNombreRessources() {
        return (ressources != null) ? ressources.size() : 0;
    }

    /**
     * Vérifie si la leçon a une vidéo
     * 
     * @return true si une vidéo est associée
     */
    public boolean hasVideo() {
        return urlVideo != null && !urlVideo.trim().isEmpty();
    }

    /**
     * Vérifie si la leçon a du code source
     * 
     * @return true si du code source est présent
     */
    public boolean hasCodeSource() {
        return codeSource != null && !codeSource.trim().isEmpty();
    }

    /**
     * Vérifie si la leçon a un fichier joint
     * 
     * @return true si un fichier est disponible
     */
    public boolean hasFichierJoint() {
        return fichierJoint != null && !fichierJoint.trim().isEmpty();
    }

    /**
     * Vérifie si la leçon a des ressources
     * 
     * @return true si des ressources sont disponibles
     */
    public boolean hasRessources() {
        return ressources != null && !ressources.isEmpty();
    }

    /**
     * Vérifie si la leçon a du contenu texte (explications)
     * 
     * @return true si du contenu texte est présent
     */
    public boolean hasContenuTexte() {
        return contenuTexte != null && !contenuTexte.trim().isEmpty();
    }

    /**
     * Calcule le nombre de mots dans le contenu texte
     * Utile pour estimer le temps de lecture
     * 
     * @return Nombre de mots (0 si pas de contenu)
     */
    public int getNombreMots() {
        if (!hasContenuTexte()) {
            return 0;
        }

        // Supprimer les balises HTML pour ne compter que le texte
        String texteSansHTML = contenuTexte.replaceAll("<[^>]+>", " ");
        // Supprimer les espaces multiples
        texteSansHTML = texteSansHTML.replaceAll("\\s+", " ").trim();
        
        if (texteSansHTML.isEmpty()) {
            return 0;
        }

        // Compter les mots (séparés par des espaces)
        String[] mots = texteSansHTML.split(" ");
        return mots.length;
    }

    /**
     * Calcule le temps de lecture estimé en minutes
     * Basé sur une vitesse moyenne de lecture de 200 mots par minute
     * Minimum 1 minute si du contenu existe
     * 
     * @return Temps de lecture estimé en minutes
     */
    public int getTempsLectureEstime() {
        int nombreMots = getNombreMots();
        
        if (nombreMots == 0) {
            return 0;
        }

        // 200 mots par minute est la vitesse moyenne de lecture
        int tempsEstime = (int) Math.ceil((double) nombreMots / 200);
        
        // Minimum 1 minute si du contenu existe
        return Math.max(tempsEstime, 1);
    }

    /**
     * Retourne le temps de lecture formaté
     * Exemples : "5 min", "12 min", "1 min"
     * 
     * @return Temps de lecture formaté
     */
    public String getTempsLectureFormate() {
        int minutes = getTempsLectureEstime();
        
        if (minutes == 0) {
            return "Moins d'1 min";
        }
        
        if (minutes == 1) {
            return "1 min";
        }
        
        return minutes + " min";
    }

    /**
     * Vérifie si la leçon est accessible gratuitement ou si l'utilisateur est Premium
     * 
     * @param estPremium True si l'utilisateur a un abonnement Premium
     * @return true si l'utilisateur peut accéder à cette leçon
     */
    public boolean estAccessible(boolean estPremium) {
        return gratuit || estPremium;
    }

    /**
     * Extrait l'ID de la vidéo YouTube de l'URL (conservé pour compatibilité)
     * 
     * @return ID YouTube ou null si non trouvé
     */
    public String getYoutubeVideoId() {
        if (!hasVideo()) return null;

        String url = urlVideo.trim();

        // Format : https://youtu.be/VIDEO_ID
        if (url.contains("youtu.be/")) {
            String[] parts = url.split("youtu.be/");
            if (parts.length > 1) {
                String id = parts[1];
                int paramIndex = id.indexOf('?');
                if (paramIndex > 0) {
                    id = id.substring(0, paramIndex);
                }
                return id;
            }
        }

        // Format : https://www.youtube.com/watch?v=VIDEO_ID
        if (url.contains("v=")) {
            String[] parts = url.split("v=");
            if (parts.length > 1) {
                String id = parts[1];
                int ampIndex = id.indexOf('&');
                if (ampIndex > 0) {
                    id = id.substring(0, ampIndex);
                }
                return id;
            }
        }

        // Format : https://www.youtube.com/embed/VIDEO_ID
        if (url.contains("/embed/")) {
            String[] parts = url.split("/embed/");
            if (parts.length > 1) {
                String id = parts[1];
                int paramIndex = id.indexOf('?');
                if (paramIndex > 0) {
                    id = id.substring(0, paramIndex);
                }
                return id;
            }
        }

        return null;
    }

    /**
     * Retourne l'URL d'embed YouTube pour l'intégration iframe (conservé pour compatibilité)
     * 
     * @return URL embed ou chaîne vide
     */
    public String getYoutubeEmbedUrl() {
        String videoId = getYoutubeVideoId();
        if (videoId != null) {
            return "https://www.youtube.com/embed/" + videoId;
        }
        return "";
    }

    // ===== MÉTHODES STANDARD =====

    @Override
    public String toString() {
        return "Lecon{" +
                "id=" + id +
                ", chapitreId=" + chapitreId +
                ", titre='" + titre + '\'' +
                ", ordre=" + ordre +
                ", gratuit=" + gratuit +
                ", hasVideo=" + hasVideo() +
                ", hasCodeSource=" + hasCodeSource() +
                ", nbRessources=" + getNombreRessources() +
                ", tempsLecture=" + getTempsLectureFormate() +
                '}';
    }
}