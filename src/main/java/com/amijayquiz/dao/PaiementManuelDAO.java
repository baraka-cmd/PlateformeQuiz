// ============================================================
// FICHIER : src/main/java/com/amijayquiz/dao/PaiementManuelDAO.java
// RÔLE    : Accès aux données de la table "paiements_manuels"
//           Enregistrement des demandes de paiement manuel
//           Vérification des demandes en attente
// ============================================================

package com.amijayquiz.dao;

import com.amijayquiz.utils.ConnexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * PaiementManuelDAO - Data Access Object pour les paiements manuels
 * 
 * Gère les opérations sur la table "paiements_manuels" :
 * enregistrement des demandes, vérification du statut.
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
public class PaiementManuelDAO {

    // ===== CONSTANTES DE REQUÊTES SQL =====
    private static final String INSERT_DEMANDE = 
        "INSERT INTO paiements_manuels (utilisateur_id, id_transaction, operateur, telephone, statut, date_creation) " +
        "VALUES (?, ?, ?, ?, 'EN_ATTENTE', ?)";

    private static final String VERIFIER_DEMANDE_EN_ATTENTE = 
        "SELECT COUNT(*) FROM paiements_manuels WHERE utilisateur_id = ? AND statut = 'EN_ATTENTE'";

    private static final String COUNT_EN_ATTENTE = 
        "SELECT COUNT(*) FROM paiements_manuels WHERE statut = 'EN_ATTENTE'";

    private static final String COUNT_VALIDE_AUJOURDHUI = 
        "SELECT COUNT(*) FROM paiements_manuels WHERE statut = 'VALIDE' AND DATE(date_validation) = CURDATE()";

    // ============================================================
    // ENREGISTREMENT D'UNE DEMANDE
    // ============================================================

    /**
     * Enregistre une nouvelle demande de paiement manuel
     * 
     * @param utilisateurId  ID de l'utilisateur qui a payé
     * @param idTransaction  ID de transaction Mobile Money (copié du SMS)
     * @param operateur      Opérateur utilisé (AIRTEL, ORANGE, VODACOM)
     * @param telephone      Numéro de téléphone du client
     * @return true si l'enregistrement a réussi, false sinon
     */
    public boolean enregistrerDemande(int utilisateurId, String idTransaction, 
                                       String operateur, String telephone) {
        if (utilisateurId <= 0) return false;
        if (idTransaction == null || idTransaction.trim().isEmpty()) return false;
        if (operateur == null || operateur.trim().isEmpty()) return false;
        if (telephone == null || telephone.trim().isEmpty()) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(INSERT_DEMANDE);
            preparedStatement.setInt(1, utilisateurId);
            preparedStatement.setString(2, idTransaction.trim());
            preparedStatement.setString(3, operateur.trim());
            preparedStatement.setString(4, telephone.trim());
            preparedStatement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

            int lignes = preparedStatement.executeUpdate();

            if (lignes > 0) {
                System.out.println("✅ [PaiementManuelDAO] Demande enregistrée - UserID: " + utilisateurId 
                    + " - Transaction: " + idTransaction);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ [PaiementManuelDAO] Erreur enregistrement demande : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    // ============================================================
    // VÉRIFICATION
    // ============================================================

    /**
     * Vérifie si un utilisateur a déjà une demande en attente
     * 
     * @param utilisateurId ID de l'utilisateur
     * @return true si une demande est déjà en attente
     */
    public boolean aDemandeEnAttente(int utilisateurId) {
        if (utilisateurId <= 0) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(VERIFIER_DEMANDE_EN_ATTENTE);
            preparedStatement.setInt(1, utilisateurId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ [PaiementManuelDAO] Erreur vérification demande : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return false;
    }

    // ============================================================
    // STATISTIQUES (POUR L'ADMIN)
    // ============================================================

    /**
     * Compte le nombre de demandes en attente de validation
     * 
     * @return Nombre de demandes EN_ATTENTE
     */
    public int compterDemandesEnAttente() {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;

            preparedStatement = connexion.prepareStatement(COUNT_EN_ATTENTE);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ [PaiementManuelDAO] Erreur comptage : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    /**
     * Compte le nombre de demandes validées aujourd'hui
     * 
     * @return Nombre de demandes VALIDE aujourd'hui
     */
    public int compterValideAujourdhui() {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;

            preparedStatement = connexion.prepareStatement(COUNT_VALIDE_AUJOURDHUI);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ [PaiementManuelDAO] Erreur comptage aujourd'hui : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    // ============================================================
    // MÉTHODES PRIVÉES
    // ============================================================

    /**
     * Ferme les ressources JDBC
     * 
     * @param connexion         Connexion à fermer (ou null)
     * @param preparedStatement PreparedStatement à fermer (ou null)
     * @param resultSet         ResultSet à fermer (ou null)
     */
    private void fermerRessources(Connection connexion, PreparedStatement preparedStatement, 
                                   ResultSet resultSet) {
        if (resultSet != null) {
            try { resultSet.close(); } catch (SQLException e) { /* Ignorer */ }
        }
        if (preparedStatement != null) {
            try { preparedStatement.close(); } catch (SQLException e) { /* Ignorer */ }
        }
        if (connexion != null) {
            try { connexion.close(); } catch (SQLException e) { /* Ignorer */ }
        }
    }
}