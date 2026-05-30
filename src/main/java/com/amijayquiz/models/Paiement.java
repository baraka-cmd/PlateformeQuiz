// ============================================================
// FICHIER : src/main/java/com/amijayquiz/models/Paiement.java
// RÔLE    : Modèle représentant une transaction de paiement
//           Stocke toutes les infos d'un paiement PawaPay
//           Traçabilité complète : référence, statut, dates
// ============================================================

package com.amijayquiz.models;

import java.sql.Timestamp;

/**
 * Paiement - Modèle représentant une transaction de paiement
 * 
 * Chaque tentative de paiement est enregistrée avec :
 * - Une référence unique (PAW-{userId}-{timestamp}-{random})
 * - Le statut de la transaction (PENDING, SUCCESS, FAILED...)
 * - L'ID de transaction retourné par PawaPay
 * - Les dates de création et de mise à jour
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
public class Paiement {

    // ===== ATTRIBUTS =====
    private int id;
    private int utilisateurId;
    private String reference;
    private String transactionId;
    private double montant;
    private String devise;
    private String telephone;
    private String statut;
    private Timestamp dateCreation;
    private Timestamp dateMaj;

    // ===== CONSTANTES DE STATUT =====
    public static final String STATUT_PENDING   = "PENDING";
    public static final String STATUT_SUCCESS   = "SUCCESS";
    public static final String STATUT_FAILED    = "FAILED";
    public static final String STATUT_CANCELLED = "CANCELLED";
    public static final String STATUT_EXPIRED   = "EXPIRED";

    // ===== CONSTRUCTEURS =====

    /**
     * Constructeur par défaut
     */
    public Paiement() {
        this.statut = STATUT_PENDING;
        this.devise = "USD";
        this.montant = 0.0;
    }

    /**
     * Constructeur pour création d'un paiement
     * 
     * @param utilisateurId ID de l'utilisateur qui paie
     * @param reference     Référence unique de la transaction
     * @param montant       Montant du paiement
     * @param telephone     Numéro de téléphone Mobile Money
     */
    public Paiement(int utilisateurId, String reference, double montant, String telephone) {
        this.utilisateurId = utilisateurId;
        this.reference = reference;
        this.montant = montant;
        this.telephone = telephone;
        this.devise = "USD";
        this.statut = STATUT_PENDING;
    }

    /**
     * Constructeur complet (depuis la base de données)
     * 
     * @param id             ID en base
     * @param utilisateurId  ID de l'utilisateur
     * @param reference      Référence unique
     * @param transactionId  ID transaction PawaPay
     * @param montant        Montant payé
     * @param devise         Devise (USD)
     * @param telephone      Numéro de téléphone
     * @param statut         Statut (PENDING, SUCCESS, FAILED...)
     * @param dateCreation   Date de création
     * @param dateMaj        Date de dernière mise à jour
     */
    public Paiement(int id, int utilisateurId, String reference, String transactionId,
                    double montant, String devise, String telephone, String statut,
                    Timestamp dateCreation, Timestamp dateMaj) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.reference = reference;
        this.transactionId = transactionId;
        this.montant = montant;
        this.devise = (devise != null && !devise.isEmpty()) ? devise : "USD";
        this.telephone = telephone;
        this.statut = (statut != null && !statut.isEmpty()) ? statut : STATUT_PENDING;
        this.dateCreation = dateCreation;
        this.dateMaj = dateMaj;
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

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = (devise != null && !devise.isEmpty()) ? devise : "USD";
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = (statut != null && !statut.isEmpty()) ? statut : STATUT_PENDING;
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

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Vérifie si le paiement a réussi
     * 
     * @return true si le statut est SUCCESS
     */
    public boolean isReussi() {
        return STATUT_SUCCESS.equals(statut);
    }

    /**
     * Vérifie si le paiement est en attente
     * 
     * @return true si le statut est PENDING
     */
    public boolean isEnAttente() {
        return STATUT_PENDING.equals(statut);
    }

    /**
     * Vérifie si le paiement a échoué
     * 
     * @return true si le statut est FAILED
     */
    public boolean isEchoue() {
        return STATUT_FAILED.equals(statut);
    }

    /**
     * Vérifie si le paiement est annulé
     * 
     * @return true si le statut est CANCELLED
     */
    public boolean isAnnule() {
        return STATUT_CANCELLED.equals(statut);
    }

    /**
     * Vérifie si le paiement a expiré
     * 
     * @return true si le statut est EXPIRED
     */
    public boolean isExpire() {
        return STATUT_EXPIRED.equals(statut);
    }

    /**
     * Vérifie si le paiement est terminé (succès, échec, annulé ou expiré)
     * 
     * @return true si le paiement n'est plus en attente
     */
    public boolean estTermine() {
        return STATUT_SUCCESS.equals(statut) 
            || STATUT_FAILED.equals(statut) 
            || STATUT_CANCELLED.equals(statut) 
            || STATUT_EXPIRED.equals(statut);
    }

    /**
     * Retourne le montant formaté avec la devise
     * 
     * @return "4.00 USD" ou "10000 FC"
     */
    public String getMontantFormate() {
        if ("USD".equals(devise)) {
            return String.format("%.2f USD", montant);
        } else if ("CDF".equals(devise) || "FC".equals(devise)) {
            return String.format("%.0f FC", montant);
        }
        return String.format("%.2f %s", montant, devise);
    }

    /**
     * Retourne une version masquée du numéro de téléphone
     * Exemple : +243984***438
     * 
     * @return Numéro masqué ou "Non renseigné"
     */
    public String getTelephoneMasque() {
        if (telephone == null || telephone.length() < 9) return "Non renseigné";
        return telephone.substring(0, telephone.length() - 6) 
            + "***" 
            + telephone.substring(telephone.length() - 3);
    }

    /**
     * Retourne l'icône correspondant au statut
     * 
     * @return Classe CSS Font Awesome
     */
    public String getIconeStatut() {
        if (statut == null) return "fas fa-question-circle";

        switch (statut) {
            case STATUT_SUCCESS:   return "fas fa-check-circle";
            case STATUT_PENDING:   return "fas fa-clock";
            case STATUT_FAILED:    return "fas fa-times-circle";
            case STATUT_CANCELLED: return "fas fa-ban";
            case STATUT_EXPIRED:   return "fas fa-hourglass-end";
            default:               return "fas fa-question-circle";
        }
    }

    /**
     * Retourne la couleur correspondant au statut
     * 
     * @return Code couleur hexadécimal
     */
    public String getCouleurStatut() {
        if (statut == null) return "#888888";

        switch (statut) {
            case STATUT_SUCCESS:   return "#27ae60";
            case STATUT_PENDING:   return "#f39c12";
            case STATUT_FAILED:    return "#e74c3c";
            case STATUT_CANCELLED: return "#95a5a6";
            case STATUT_EXPIRED:   return "#e67e22";
            default:               return "#888888";
        }
    }

    /**
     * Retourne le libellé formaté du statut en français
     * 
     * @return Libellé du statut
     */
    public String getStatutFormate() {
        if (statut == null) return "Inconnu";

        switch (statut) {
            case STATUT_SUCCESS:   return "Réussi";
            case STATUT_PENDING:   return "En attente";
            case STATUT_FAILED:    return "Échoué";
            case STATUT_CANCELLED: return "Annulé";
            case STATUT_EXPIRED:   return "Expiré";
            default:               return statut;
        }
    }

    // ===== MÉTHODES STANDARD =====

    @Override
    public String toString() {
        return "Paiement{" +
                "id=" + id +
                ", utilisateurId=" + utilisateurId +
                ", reference='" + reference + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", montant=" + getMontantFormate() +
                ", telephone='" + getTelephoneMasque() + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}