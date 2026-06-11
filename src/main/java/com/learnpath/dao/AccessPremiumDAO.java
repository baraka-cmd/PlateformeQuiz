// ============================================================
// FICHIER : src/main/java/com/LearnPath/dao/AccessPremiumDAO.java
// RÔLE    : Correction du nom de colonne transaction_reference
// ============================================================

package com.learnpath.dao;

import com.learnpath.utils.ConnexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * AccessPremiumDAO - Data Access Object pour la table acces_premium
 * 
 * Gère le statut premium des utilisateurs :
 * - Activation après paiement réussi
 * - Désactivation (expiration ou admin)
 * - Vérification du statut premium
 * - Synchronisation avec la colonne premium de la table utilisateurs
 * 
 * Un seul paiement = premium à vie (pas de date d'expiration).
 * 
 * @author LearnPath
 * @version 1.0
 */
public class AccessPremiumDAO {

    // ===== CONSTANTES DE REQUÊTES SQL =====
    private static final String INSERT_OU_UPDATE = 
        "INSERT INTO acces_premium (utilisateur_id, premium_actif, date_activation, transaction_reference, montant_paye, devise) " +
        "VALUES (?, TRUE, NOW(), ?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE premium_actif = TRUE, date_activation = NOW(), " +
        "transaction_reference = VALUES(transaction_reference), montant_paye = VALUES(montant_paye), " +
        "devise = VALUES(devise), updated_at = NOW()";

    private static final String SELECT_PAR_UTILISATEUR = 
        "SELECT * FROM acces_premium WHERE utilisateur_id = ?";

    private static final String DESACTIVER = 
        "UPDATE acces_premium SET premium_actif = FALSE, updated_at = NOW() WHERE utilisateur_id = ?";

    private static final String EST_PREMIUM = 
        "SELECT premium_actif FROM acces_premium WHERE utilisateur_id = ?";

    private static final String COUNT_PREMIUM_ACTIFS = 
        "SELECT COUNT(*) FROM acces_premium WHERE premium_actif = TRUE";

    // ============================================================
    // ACTIVATION PREMIUM
    // ============================================================

    /**
     * Active le statut premium pour un utilisateur après paiement réussi.
     * Utilise INSERT ... ON DUPLICATE KEY UPDATE pour éviter les doublons.
     * Met également à jour la colonne premium dans la table utilisateurs.
     * 
     * @param utilisateurId    ID de l'utilisateur
     * @param referencePaiement Référence du paiement réussi
     * @param montant          Montant payé
     * @param devise           Devise du paiement
     * @return true si l'activation a réussi
     */
    public boolean activerPremium(int utilisateurId, String referencePaiement, 
                                   double montant, String devise) {
        if (utilisateurId <= 0) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            // 1. Insérer ou mettre à jour dans acces_premium
            preparedStatement = connexion.prepareStatement(INSERT_OU_UPDATE);
            preparedStatement.setInt(1, utilisateurId);
            preparedStatement.setString(2, referencePaiement != null ? referencePaiement : "");
            preparedStatement.setDouble(3, montant);
            preparedStatement.setString(4, devise != null ? devise : "USD");

            int lignes = preparedStatement.executeUpdate();

            if (lignes > 0) {
                // 2. Synchroniser la table utilisateurs
                synchroniserTableUtilisateurs(utilisateurId, true, connexion);

                System.out.println("✅ [AccessPremiumDAO] Premium activé - UserID: " + utilisateurId 
                    + " - Réf: " + referencePaiement);
                return true;
            } else {
                System.err.println("⚠️ [AccessPremiumDAO] Aucune ligne affectée - UserID: " + utilisateurId);
            }

        } catch (SQLException e) {
            System.err.println("❌ [AccessPremiumDAO] Erreur activation premium : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    // ============================================================
    // DÉSACTIVATION PREMIUM
    // ============================================================

    /**
     * Désactive le statut premium d'un utilisateur (admin ou expiration).
     * Met également à jour la colonne premium dans la table utilisateurs.
     * 
     * @param utilisateurId ID de l'utilisateur
     * @return true si la désactivation a réussi
     */
    public boolean desactiverPremium(int utilisateurId) {
        if (utilisateurId <= 0) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            // 1. Désactiver dans acces_premium
            preparedStatement = connexion.prepareStatement(DESACTIVER);
            preparedStatement.setInt(1, utilisateurId);

            int lignes = preparedStatement.executeUpdate();

            // 2. Synchroniser la table utilisateurs
            synchroniserTableUtilisateurs(utilisateurId, false, connexion);

            if (lignes > 0) {
                System.out.println("✅ [AccessPremiumDAO] Premium désactivé - UserID: " + utilisateurId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ [AccessPremiumDAO] Erreur désactivation premium : " + e.getMessage());
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
     * Vérifie si un utilisateur a le statut premium actif.
     * Vérifie d'abord acces_premium, puis la colonne premium de utilisateurs.
     * 
     * @param utilisateurId ID de l'utilisateur
     * @return true si l'utilisateur est premium
     */
    public boolean estPremium(int utilisateurId) {
        if (utilisateurId <= 0) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            // Vérifier dans acces_premium
            preparedStatement = connexion.prepareStatement(EST_PREMIUM);
            preparedStatement.setInt(1, utilisateurId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getBoolean("premium_actif");
            }

            // Pas d'entrée dans acces_premium, vérifier la table utilisateurs
            resultSet.close();
            preparedStatement.close();

            String sqlUtilisateur = "SELECT premium FROM utilisateurs WHERE id = ?";
            preparedStatement = connexion.prepareStatement(sqlUtilisateur);
            preparedStatement.setInt(1, utilisateurId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getBoolean("premium");
            }

        } catch (SQLException e) {
            System.err.println("❌ [AccessPremiumDAO] Erreur vérification premium : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return false;
    }

    /**
     * Récupère les informations premium d'un utilisateur
     * 
     * @param utilisateurId ID de l'utilisateur
     * @return Map contenant les infos premium, ou null si pas premium
     */
    public java.util.Map<String, Object> getInfosPremium(int utilisateurId) {
        if (utilisateurId <= 0) return null;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return null;

            preparedStatement = connexion.prepareStatement(SELECT_PAR_UTILISATEUR);
            preparedStatement.setInt(1, utilisateurId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                java.util.Map<String, Object> infos = new java.util.LinkedHashMap<>();
                infos.put("premiumActif", resultSet.getBoolean("premium_actif"));
                infos.put("dateActivation", resultSet.getTimestamp("date_activation"));
                infos.put("montantPaye", resultSet.getDouble("montant_paye"));
                infos.put("devise", resultSet.getString("devise"));
                infos.put("referencePaiement", resultSet.getString("transaction_reference"));
                return infos;
            }

        } catch (SQLException e) {
            System.err.println("❌ [AccessPremiumDAO] Erreur infos premium : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    // ============================================================
    // SYNCHRONISATION
    // ============================================================

    /**
     * Synchronise la colonne premium de la table utilisateurs avec acces_premium.
     * Appelée après chaque activation/désactivation.
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param premium       true pour activer, false pour désactiver
     * @param connexion     Connexion existante (pour transaction)
     */
    private void synchroniserTableUtilisateurs(int utilisateurId, boolean premium, Connection connexion) {
        PreparedStatement preparedStatement = null;

        try {
            String sql = "UPDATE utilisateurs SET premium = ? WHERE id = ?";
            preparedStatement = connexion.prepareStatement(sql);
            preparedStatement.setBoolean(1, premium);
            preparedStatement.setInt(2, utilisateurId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("⚠️ [AccessPremiumDAO] Erreur synchro utilisateurs : " + e.getMessage());
        } finally {
            if (preparedStatement != null) {
                try { preparedStatement.close(); } catch (SQLException e) { /* Ignorer */ }
            }
        }
    }

    /**
     * Synchronise tous les utilisateurs : met à jour la colonne premium
     * depuis la table acces_premium. Utile après une migration de données.
     * 
     * @return Nombre d'utilisateurs synchronisés
     */
    public int synchroniserTousUtilisateurs() {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;

            String sql = "UPDATE utilisateurs u " +
                         "JOIN acces_premium ap ON u.id = ap.utilisateur_id " +
                         "SET u.premium = ap.premium_actif " +
                         "WHERE ap.premium_actif = TRUE";

            preparedStatement = connexion.prepareStatement(sql);
            int lignes = preparedStatement.executeUpdate();

            System.out.println("✅ [AccessPremiumDAO] Synchronisation : " + lignes + " utilisateurs mis à jour");
            return lignes;

        } catch (SQLException e) {
            System.err.println("❌ [AccessPremiumDAO] Erreur synchro globale : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return 0;
    }

    // ============================================================
    // STATISTIQUES
    // ============================================================

    /**
     * Compte le nombre d'utilisateurs premium actifs
     * 
     * @return Nombre d'utilisateurs premium
     */
    public int compterPremiumActifs() {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;

            preparedStatement = connexion.prepareStatement(COUNT_PREMIUM_ACTIFS);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ [AccessPremiumDAO] Erreur comptage premium : " + e.getMessage());
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