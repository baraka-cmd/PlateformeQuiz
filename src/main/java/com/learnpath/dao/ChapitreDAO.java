// ============================================================
// FICHIER : src/main/java/com/LearnPath/dao/ChapitreDAO.java
// RÔLE    : Accès aux données de la table "chapitres"
//           CRUD complet + leçons associées + navigation
// ============================================================

package com.learnpath.dao;

import com.learnpath.models.Chapitre;
import com.learnpath.models.Lecon;
import com.learnpath.utils.ConnexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * ChapitreDAO - Data Access Object pour les chapitres des cours
 * 
 * Gère toutes les opérations CRUD sur la table "chapitres".
 * Charge également les leçons associées via LeconDAO.
 * 
 * @author LearnPath
 * @version 1.1
 */
public class ChapitreDAO {

    // ===== CONSTANTES DE REQUÊTES SQL =====
    private static final String SELECT_PAR_COURS = 
        "SELECT * FROM chapitres WHERE cours_id = ? ORDER BY module_numero, ordre";
    
    private static final String SELECT_PAR_ID = 
        "SELECT * FROM chapitres WHERE id = ?";
    
    private static final String INSERT = 
        "INSERT INTO chapitres (cours_id, titre, description, objectifs, ordre, module_numero, quiz_id, date_creation) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE = 
        "UPDATE chapitres SET titre = ?, description = ?, objectifs = ?, ordre = ?, module_numero = ?, quiz_id = ? WHERE id = ?";
    
    private static final String DELETE = 
        "DELETE FROM chapitres WHERE id = ?";
    
    private static final String DELETE_PAR_COURS = 
        "DELETE FROM chapitres WHERE cours_id = ?";
    
    private static final String COUNT_PAR_COURS = 
        "SELECT COUNT(*) FROM chapitres WHERE cours_id = ?";
    
    private static final String SELECT_PROCHAIN_CHAPITRE = 
        "SELECT * FROM chapitres WHERE cours_id = ? AND ordre > ? ORDER BY module_numero, ordre LIMIT 1";
    
    private static final String SELECT_PREMIER_CHAPITRE = 
        "SELECT * FROM chapitres WHERE cours_id = ? ORDER BY module_numero, ordre LIMIT 1";

    // ===== MÉTHODES CRUD =====

    /**
     * Liste tous les chapitres d'un cours, avec leurs leçons
     * 
     * @param coursId ID du cours
     * @return Liste des chapitres (vide si aucun)
     */
    public List<Chapitre> listerParCours(int coursId) {
        List<Chapitre> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_COURS);
            preparedStatement.setInt(1, coursId);
            resultSet = preparedStatement.executeQuery();

            LeconDAO leconDAO = new LeconDAO();

            while (resultSet.next()) {
                Chapitre chapitre = extraireChapitre(resultSet);
                // Charger les leçons associées
                List<Lecon> lecons = leconDAO.listerParChapitre(chapitre.getId());
                chapitre.setLecons(lecons);
                liste.add(chapitre);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : liste des chapitres du cours " + coursId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Trouve un chapitre par son ID, avec ses leçons
     * 
     * @param id ID du chapitre
     * @return Le chapitre trouvé ou null
     */
    public Chapitre trouverParId(int id) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_ID);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Chapitre chapitre = extraireChapitre(resultSet);
                // Charger les leçons associées
                LeconDAO leconDAO = new LeconDAO();
                List<Lecon> lecons = leconDAO.listerParChapitre(chapitre.getId());
                chapitre.setLecons(lecons);
                return chapitre;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : recherche chapitre ID " + id);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Ajoute un nouveau chapitre
     * 
     * @param chapitre Le chapitre à ajouter (sans ID)
     * @return true si l'ajout a réussi
     */
    public boolean ajouter(Chapitre chapitre) {
        if (chapitre == null) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(INSERT);
            preparedStatement.setInt(1, chapitre.getCoursId());
            preparedStatement.setString(2, chapitre.getTitre());
            preparedStatement.setString(3, chapitre.getDescription());
            preparedStatement.setString(4, chapitre.getObjectifs());
            preparedStatement.setInt(5, chapitre.getOrdre());
            preparedStatement.setInt(6, chapitre.getModuleNumero());
            if (chapitre.getQuizId() != null) {
                preparedStatement.setInt(7, chapitre.getQuizId());
            } else {
                preparedStatement.setNull(7, java.sql.Types.INTEGER);
            }
            preparedStatement.setTimestamp(8, new Timestamp(System.currentTimeMillis()));

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : ajout chapitre");
            System.err.println("   Titre : " + chapitre.getTitre());
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Modifie un chapitre existant
     * 
     * @param chapitre Le chapitre à modifier (avec ID)
     * @return true si la modification a réussi
     */
    public boolean modifier(Chapitre chapitre) {
        if (chapitre == null || chapitre.getId() <= 0) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(UPDATE);
            preparedStatement.setString(1, chapitre.getTitre());
            preparedStatement.setString(2, chapitre.getDescription());
            preparedStatement.setString(3, chapitre.getObjectifs());
            preparedStatement.setInt(4, chapitre.getOrdre());
            preparedStatement.setInt(5, chapitre.getModuleNumero());
            if (chapitre.getQuizId() != null) {
                preparedStatement.setInt(6, chapitre.getQuizId());
            } else {
                preparedStatement.setNull(6, java.sql.Types.INTEGER);
            }
            preparedStatement.setInt(7, chapitre.getId());

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : modification chapitre ID " + chapitre.getId());
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Supprime un chapitre par son ID (supprime aussi ses leçons)
     * 
     * @param id ID du chapitre à supprimer
     * @return true si la suppression a réussi
     */
    public boolean supprimer(int id) {
        if (id <= 0) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            // Supprimer d'abord les leçons du chapitre
            LeconDAO leconDAO = new LeconDAO();
            leconDAO.supprimerParChapitre(id);

            // Puis supprimer le chapitre
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(DELETE);
            preparedStatement.setInt(1, id);

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : suppression chapitre ID " + id);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Supprime tous les chapitres d'un cours (et leurs leçons)
     * 
     * @param coursId ID du cours
     * @return true si la suppression a réussi
     */
    public boolean supprimerParCours(int coursId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            // Supprimer d'abord les leçons de tous les chapitres
            List<Chapitre> chapitres = listerParCours(coursId);
            LeconDAO leconDAO = new LeconDAO();
            for (Chapitre chapitre : chapitres) {
                leconDAO.supprimerParChapitre(chapitre.getId());
            }

            // Puis supprimer les chapitres
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(DELETE_PAR_COURS);
            preparedStatement.setInt(1, coursId);
            preparedStatement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : suppression chapitres cours " + coursId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    // ===== MÉTHODES DE COMPTAGE =====

    /**
     * Compte le nombre de chapitres dans un cours
     * 
     * @param coursId ID du cours
     * @return Nombre de chapitres
     */
    public int compterParCours(int coursId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(COUNT_PAR_COURS);
            preparedStatement.setInt(1, coursId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : comptage chapitres cours " + coursId);
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    // ===== MÉTHODES DE NAVIGATION =====

    /**
     * Trouve le prochain chapitre dans le même cours
     * 
     * @param coursId     ID du cours
     * @param ordreActuel Ordre du chapitre actuel
     * @return Le chapitre suivant ou null si c'est le dernier
     */
    public Chapitre trouverProchainChapitre(int coursId, int ordreActuel) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PROCHAIN_CHAPITRE);
            preparedStatement.setInt(1, coursId);
            preparedStatement.setInt(2, ordreActuel);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return extraireChapitre(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : prochain chapitre cours " + coursId);
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Trouve le premier chapitre d'un cours
     * 
     * @param coursId ID du cours
     * @return Le premier chapitre ou null
     */
    public Chapitre trouverPremierChapitre(int coursId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PREMIER_CHAPITRE);
            preparedStatement.setInt(1, coursId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return extraireChapitre(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : premier chapitre cours " + coursId);
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    // ===== MÉTHODES PRIVÉES =====

    /**
     * Extrait un chapitre d'un ResultSet
     * 
     * @param resultSet Le ResultSet positionné sur la ligne courante
     * @return L'objet Chapitre extrait
     * @throws SQLException En cas d'erreur SQL
     */
    private Chapitre extraireChapitre(ResultSet resultSet) throws SQLException {
        Chapitre chapitre = new Chapitre();
        chapitre.setId(resultSet.getInt("id"));
        chapitre.setCoursId(resultSet.getInt("cours_id"));
        chapitre.setTitre(resultSet.getString("titre"));
        chapitre.setDescription(resultSet.getString("description"));
        chapitre.setObjectifs(resultSet.getString("objectifs"));
        chapitre.setOrdre(resultSet.getInt("ordre"));
        chapitre.setModuleNumero(resultSet.getInt("module_numero"));

        int quizId = resultSet.getInt("quiz_id");
        if (!resultSet.wasNull()) {
            chapitre.setQuizId(quizId);
        }

        chapitre.setDateCreation(resultSet.getTimestamp("date_creation"));
        return chapitre;
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