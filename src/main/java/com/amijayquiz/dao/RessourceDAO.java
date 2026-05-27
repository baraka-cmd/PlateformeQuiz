// ============================================================
// FICHIER : src/main/java/com/amijayquiz/dao/RessourceDAO.java
// RÔLE    : Accès aux données de la table "ressources"
//           CRUD complet + méthodes spécifiques
// ============================================================

package com.amijayquiz.dao;

import com.amijayquiz.models.Ressource;
import com.amijayquiz.utils.ConnexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * RessourceDAO - Data Access Object pour les ressources des leçons
 * 
 * Gère toutes les opérations CRUD sur la table "ressources".
 * Utilise des PreparedStatement pour éviter les injections SQL.
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
public class RessourceDAO {

    // ===== CONSTANTES DE REQUÊTES SQL =====
    private static final String SELECT_PAR_LECON = 
        "SELECT * FROM ressources WHERE lecon_id = ? ORDER BY date_ajout";
    
    private static final String SELECT_PAR_ID = 
        "SELECT * FROM ressources WHERE id = ?";
    
    private static final String INSERT = 
        "INSERT INTO ressources (lecon_id, titre, description, type, url_fichier, taille, date_ajout) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE = 
        "UPDATE ressources SET titre = ?, description = ?, type = ?, url_fichier = ?, taille = ? WHERE id = ?";
    
    private static final String DELETE = 
        "DELETE FROM ressources WHERE id = ?";
    
    private static final String DELETE_PAR_LECON = 
        "DELETE FROM ressources WHERE lecon_id = ?";
    
    private static final String COUNT_PAR_LECON = 
        "SELECT COUNT(*) FROM ressources WHERE lecon_id = ?";

    // ===== MÉTHODES CRUD =====

    /**
     * Liste toutes les ressources d'une leçon
     * 
     * @param leconId ID de la leçon
     * @return Liste des ressources (vide si aucune)
     */
    public List<Ressource> listerParLecon(int leconId) {
        List<Ressource> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_LECON);
            preparedStatement.setInt(1, leconId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                liste.add(extraireRessource(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : liste des ressources de la leçon " + leconId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Trouve une ressource par son ID
     * 
     * @param id ID de la ressource
     * @return La ressource trouvée ou null
     */
    public Ressource trouverParId(int id) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_ID);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return extraireRessource(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : recherche ressource ID " + id);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Ajoute une nouvelle ressource dans la base de données
     * 
     * @param ressource La ressource à ajouter (sans ID)
     * @return true si l'ajout a réussi
     */
    public boolean ajouter(Ressource ressource) {
        if (ressource == null) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(INSERT);
            preparedStatement.setInt(1, ressource.getLeconId());
            preparedStatement.setString(2, ressource.getTitre());
            preparedStatement.setString(3, ressource.getDescription());
            preparedStatement.setString(4, ressource.getType());
            preparedStatement.setString(5, ressource.getUrlFichier());
            preparedStatement.setString(6, ressource.getTaille());
            preparedStatement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : ajout ressource");
            System.err.println("   Titre : " + ressource.getTitre());
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Modifie une ressource existante
     * 
     * @param ressource La ressource à modifier (avec ID)
     * @return true si la modification a réussi
     */
    public boolean modifier(Ressource ressource) {
        if (ressource == null || ressource.getId() <= 0) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(UPDATE);
            preparedStatement.setString(1, ressource.getTitre());
            preparedStatement.setString(2, ressource.getDescription());
            preparedStatement.setString(3, ressource.getType());
            preparedStatement.setString(4, ressource.getUrlFichier());
            preparedStatement.setString(5, ressource.getTaille());
            preparedStatement.setInt(6, ressource.getId());

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : modification ressource ID " + ressource.getId());
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Supprime une ressource par son ID
     * 
     * @param id ID de la ressource à supprimer
     * @return true si la suppression a réussi
     */
    public boolean supprimer(int id) {
        if (id <= 0) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(DELETE);
            preparedStatement.setInt(1, id);

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : suppression ressource ID " + id);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Supprime toutes les ressources d'une leçon
     * 
     * @param leconId ID de la leçon
     * @return true si la suppression a réussi
     */
    public boolean supprimerParLecon(int leconId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(DELETE_PAR_LECON);
            preparedStatement.setInt(1, leconId);

            preparedStatement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : suppression ressources leçon " + leconId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Compte le nombre de ressources d'une leçon
     * 
     * @param leconId ID de la leçon
     * @return Nombre de ressources
     */
    public int compterParLecon(int leconId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(COUNT_PAR_LECON);
            preparedStatement.setInt(1, leconId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : comptage ressources leçon " + leconId);
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    // ===== MÉTHODES PRIVÉES =====

    /**
     * Extrait une ressource d'un ResultSet
     * 
     * @param resultSet Le ResultSet positionné sur la ligne courante
     * @return L'objet Ressource extrait
     * @throws SQLException En cas d'erreur SQL
     */
    private Ressource extraireRessource(ResultSet resultSet) throws SQLException {
        Ressource ressource = new Ressource();
        ressource.setId(resultSet.getInt("id"));
        ressource.setLeconId(resultSet.getInt("lecon_id"));
        ressource.setTitre(resultSet.getString("titre"));
        ressource.setDescription(resultSet.getString("description"));
        ressource.setType(resultSet.getString("type"));
        ressource.setUrlFichier(resultSet.getString("url_fichier"));
        ressource.setTaille(resultSet.getString("taille"));
        ressource.setDateAjout(resultSet.getTimestamp("date_ajout"));
        return ressource;
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
            try { resultSet.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        if (preparedStatement != null) {
            try { preparedStatement.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        if (connexion != null) {
            try { connexion.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}