// ============================================================
// FICHIER : src/main/java/com/amijayquiz/dao/PaiementDAO.java
// RÔLE    : Accès aux données de la table "paiements"
//           CRUD complet + recherche par référence/transaction
//           + vérification doublons + statistiques
// ============================================================

package com.amijayquiz.dao;

import com.amijayquiz.models.Paiement;
import com.amijayquiz.utils.ConnexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * PaiementDAO - Data Access Object pour les transactions de paiement
 * 
 * Gère toutes les opérations CRUD sur la table "paiements".
 * Méthodes de recherche par référence, transaction ID, utilisateur.
 * Protection contre les doublons via la contrainte UNIQUE sur reference.
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
public class PaiementDAO {

    // ===== CONSTANTES DE REQUÊTES SQL =====
    private static final String INSERT = 
        "INSERT INTO paiements (utilisateur_id, reference, montant, devise, telephone, statut, date_creation) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_STATUT = 
        "UPDATE paiements SET statut = ?, transaction_id = ?, date_maj = NOW() WHERE reference = ?";

    private static final String SELECT_PAR_REFERENCE = 
        "SELECT * FROM paiements WHERE reference = ?";

    private static final String SELECT_PAR_TRANSACTION_ID = 
        "SELECT * FROM paiements WHERE transaction_id = ?";

    private static final String SELECT_PAR_UTILISATEUR = 
        "SELECT * FROM paiements WHERE utilisateur_id = ? ORDER BY date_creation DESC";

    private static final String SELECT_DERNIER_PAIEMENT = 
        "SELECT * FROM paiements WHERE utilisateur_id = ? ORDER BY date_creation DESC LIMIT 1";

    private static final String COUNT_PAR_UTILISATEUR = 
        "SELECT COUNT(*) FROM paiements WHERE utilisateur_id = ?";

    private static final String COUNT_PAR_UTILISATEUR_STATUT = 
        "SELECT COUNT(*) FROM paiements WHERE utilisateur_id = ? AND statut = ?";

    private static final String SELECT_TOUS = 
        "SELECT * FROM paiements ORDER BY date_creation DESC";

    // ============================================================
    // INSERTION
    // ============================================================

    /**
     * Enregistre un nouveau paiement dans la base de données.
     * Vérifie d'abord qu'aucun paiement avec la même référence n'existe.
     * 
     * @param paiement Le paiement à enregistrer
     * @return true si l'insertion a réussi
     */
    public boolean inserer(Paiement paiement) {
        if (paiement == null) return false;
        if (paiement.getReference() == null || paiement.getReference().trim().isEmpty()) return false;
        if (paiement.getUtilisateurId() <= 0) return false;

        // Vérifier que la référence n'existe pas déjà (doublon)
        if (existeReference(paiement.getReference())) {
            System.err.println("⚠️ [PaiementDAO] Doublon détecté - Référence existe déjà : " + paiement.getReference());
            return false;
        }

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(INSERT);
            preparedStatement.setInt(1, paiement.getUtilisateurId());
            preparedStatement.setString(2, paiement.getReference());
            preparedStatement.setDouble(3, paiement.getMontant());
            preparedStatement.setString(4, paiement.getDevise());
            preparedStatement.setString(5, paiement.getTelephone());
            preparedStatement.setString(6, paiement.getStatut());
            preparedStatement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));

            int lignes = preparedStatement.executeUpdate();

            if (lignes > 0) {
                System.out.println("✅ [PaiementDAO] Paiement enregistré - Réf: " + paiement.getReference());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ [PaiementDAO] Erreur insertion paiement : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    // ============================================================
    // MISE À JOUR
    // ============================================================

    /**
     * Met à jour le statut et le transaction_id d'un paiement.
     * Appelé lors de la réception du webhook PawaPay.
     * 
     * @param reference      Référence du paiement
     * @param statut         Nouveau statut (SUCCESS, FAILED...)
     * @param transactionId  ID de transaction retourné par PawaPay
     * @return true si la mise à jour a réussi
     */
    public boolean mettreAJourStatut(String reference, String statut, String transactionId) {
        if (reference == null || reference.trim().isEmpty()) return false;
        if (statut == null || statut.trim().isEmpty()) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(UPDATE_STATUT);
            preparedStatement.setString(1, statut);
            preparedStatement.setString(2, transactionId);
            preparedStatement.setString(3, reference);

            int lignes = preparedStatement.executeUpdate();

            if (lignes > 0) {
                System.out.println("✅ [PaiementDAO] Statut mis à jour - Réf: " + reference + " → " + statut);
                return true;
            } else {
                System.err.println("⚠️ [PaiementDAO] Aucune ligne mise à jour - Réf: " + reference);
            }

        } catch (SQLException e) {
            System.err.println("❌ [PaiementDAO] Erreur mise à jour statut : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    // ============================================================
    // RECHERCHE
    // ============================================================

    /**
     * Trouve un paiement par sa référence unique
     * 
     * @param reference Référence du paiement
     * @return Le paiement trouvé, ou null
     */
    public Paiement trouverParReference(String reference) {
        if (reference == null || reference.trim().isEmpty()) return null;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return null;

            preparedStatement = connexion.prepareStatement(SELECT_PAR_REFERENCE);
            preparedStatement.setString(1, reference);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return extrairePaiement(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ [PaiementDAO] Erreur recherche par référence : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Trouve un paiement par son ID de transaction PawaPay
     * 
     * @param transactionId ID transaction PawaPay
     * @return Le paiement trouvé, ou null
     */
    public Paiement trouverParTransactionId(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) return null;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return null;

            preparedStatement = connexion.prepareStatement(SELECT_PAR_TRANSACTION_ID);
            preparedStatement.setString(1, transactionId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return extrairePaiement(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ [PaiementDAO] Erreur recherche par transaction ID : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Liste tous les paiements d'un utilisateur
     * 
     * @param utilisateurId ID de l'utilisateur
     * @return Liste des paiements (du plus récent au plus ancien)
     */
    public List<Paiement> listerParUtilisateur(int utilisateurId) {
        List<Paiement> liste = new ArrayList<>();
        if (utilisateurId <= 0) return liste;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return liste;

            preparedStatement = connexion.prepareStatement(SELECT_PAR_UTILISATEUR);
            preparedStatement.setInt(1, utilisateurId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                liste.add(extrairePaiement(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("❌ [PaiementDAO] Erreur liste paiements utilisateur : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Récupère le dernier paiement d'un utilisateur
     * 
     * @param utilisateurId ID de l'utilisateur
     * @return Le dernier paiement, ou null
     */
    public Paiement trouverDernierPaiement(int utilisateurId) {
        if (utilisateurId <= 0) return null;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return null;

            preparedStatement = connexion.prepareStatement(SELECT_DERNIER_PAIEMENT);
            preparedStatement.setInt(1, utilisateurId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return extrairePaiement(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ [PaiementDAO] Erreur dernier paiement : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    // ============================================================
    // VÉRIFICATIONS
    // ============================================================

    /**
     * Vérifie si une référence existe déjà (anti-doublon)
     * 
     * @param reference Référence à vérifier
     * @return true si la référence existe déjà
     */
    public boolean existeReference(String reference) {
        if (reference == null || reference.trim().isEmpty()) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            String sql = "SELECT COUNT(*) FROM paiements WHERE reference = ?";
            preparedStatement = connexion.prepareStatement(sql);
            preparedStatement.setString(1, reference);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ [PaiementDAO] Erreur vérification référence : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return false;
    }

    /**
     * Vérifie si un utilisateur a déjà un paiement réussi
     * 
     * @param utilisateurId ID de l'utilisateur
     * @return true si au moins un paiement SUCCESS existe
     */
    public boolean aPaiementReussi(int utilisateurId) {
        return compterParUtilisateurEtStatut(utilisateurId, Paiement.STATUT_SUCCESS) > 0;
    }

    /**
     * Vérifie si un utilisateur a un paiement en attente
     * 
     * @param utilisateurId ID de l'utilisateur
     * @return true si au moins un paiement PENDING existe
     */
    public boolean aPaiementEnAttente(int utilisateurId) {
        return compterParUtilisateurEtStatut(utilisateurId, Paiement.STATUT_PENDING) > 0;
    }

    // ============================================================
    // COMPTAGE
    // ============================================================

    /**
     * Compte le nombre de paiements d'un utilisateur
     * 
     * @param utilisateurId ID de l'utilisateur
     * @return Nombre de paiements
     */
    public int compterParUtilisateur(int utilisateurId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;

            preparedStatement = connexion.prepareStatement(COUNT_PAR_UTILISATEUR);
            preparedStatement.setInt(1, utilisateurId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ [PaiementDAO] Erreur comptage : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    /**
     * Compte les paiements d'un utilisateur par statut
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param statut        Statut recherché
     * @return Nombre de paiements avec ce statut
     */
    private int compterParUtilisateurEtStatut(int utilisateurId, String statut) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;

            preparedStatement = connexion.prepareStatement(COUNT_PAR_UTILISATEUR_STATUT);
            preparedStatement.setInt(1, utilisateurId);
            preparedStatement.setString(2, statut);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ [PaiementDAO] Erreur comptage par statut : " + e.getMessage());
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
     * Extrait un objet Paiement d'un ResultSet
     * 
     * @param resultSet Le ResultSet positionné sur la ligne courante
     * @return L'objet Paiement extrait
     * @throws SQLException En cas d'erreur SQL
     */
    private Paiement extrairePaiement(ResultSet resultSet) throws SQLException {
        Paiement paiement = new Paiement();
        paiement.setId(resultSet.getInt("id"));
        paiement.setUtilisateurId(resultSet.getInt("utilisateur_id"));
        paiement.setReference(resultSet.getString("reference"));
        paiement.setTransactionId(resultSet.getString("transaction_id"));
        paiement.setMontant(resultSet.getDouble("montant"));
        paiement.setDevise(resultSet.getString("devise"));
        paiement.setTelephone(resultSet.getString("telephone"));
        paiement.setStatut(resultSet.getString("statut"));
        paiement.setDateCreation(resultSet.getTimestamp("date_creation"));
        paiement.setDateMaj(resultSet.getTimestamp("date_maj"));
        return paiement;
    }

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