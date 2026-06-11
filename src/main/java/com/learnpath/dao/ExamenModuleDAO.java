// ============================================================
// FICHIER : src/main/java/com/LearnPath/dao/ExamenModuleDAO.java
// RÔLE    : Accès aux données de la table "examens_modules"
//           CRUD pour les examens de modules
// ============================================================

package com.learnpath.dao;

import com.learnpath.models.ExamenModule;
import com.learnpath.utils.ConnexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * ExamenModuleDAO - Data Access Object pour les examens de modules
 * 
 * Gère toutes les opérations liées aux examens de modules :
 * recherche par cours/module, liste des examens d'un cours.
 * 
 * @author LearnPath
 * @version 1.0
 */
public class ExamenModuleDAO {

    // ===== CONSTANTES DE REQUÊTES SQL =====
    private static final String SELECT_PAR_ID = 
        "SELECT * FROM examens_modules WHERE id = ?";
    
    private static final String SELECT_PAR_COURS_MODULE = 
        "SELECT * FROM examens_modules WHERE cours_id = ? AND module_numero = ?";
    
    private static final String SELECT_PAR_COURS = 
        "SELECT * FROM examens_modules WHERE cours_id = ? ORDER BY module_numero";
    
    private static final String COUNT_PAR_COURS = 
        "SELECT COUNT(*) FROM examens_modules WHERE cours_id = ?";
    
    private static final String INSERT = 
        "INSERT INTO examens_modules (cours_id, module_numero, titre, description, " +
        "nombre_questions, seuil_reussite, date_creation) VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE = 
        "UPDATE examens_modules SET titre = ?, description = ?, nombre_questions = ?, " +
        "seuil_reussite = ? WHERE id = ?";
    
    private static final String DELETE = 
        "DELETE FROM examens_modules WHERE id = ?";

    // ===== MÉTHODES DE LECTURE =====

    /**
     * Trouve un examen par son ID
     * 
     * @param id ID de l'examen
     * @return L'examen trouvé ou null
     */
    public ExamenModule trouverParId(int id) {
        if (id <= 0) return null;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return null;

            preparedStatement = connexion.prepareStatement(SELECT_PAR_ID);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return extraireExamen(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : recherche examen ID " + id);
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Trouve un examen par son cours et numéro de module
     * 
     * @param coursId      ID du cours
     * @param moduleNumero Numéro du module
     * @return L'examen trouvé ou null
     */
    public ExamenModule trouverParCoursEtModule(int coursId, int moduleNumero) {
        if (coursId <= 0 || moduleNumero <= 0) return null;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return null;

            preparedStatement = connexion.prepareStatement(SELECT_PAR_COURS_MODULE);
            preparedStatement.setInt(1, coursId);
            preparedStatement.setInt(2, moduleNumero);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return extraireExamen(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : recherche examen cours " + coursId + " module " + moduleNumero);
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Liste tous les examens d'un cours
     * 
     * @param coursId ID du cours
     * @return Liste des examens triés par numéro de module
     */
    public List<ExamenModule> listerParCours(int coursId) {
        List<ExamenModule> liste = new ArrayList<>();
        if (coursId <= 0) return liste;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return liste;

            preparedStatement = connexion.prepareStatement(SELECT_PAR_COURS);
            preparedStatement.setInt(1, coursId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                liste.add(extraireExamen(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : liste examens cours " + coursId);
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Compte le nombre d'examens pour un cours
     * 
     * @param coursId ID du cours
     * @return Nombre d'examens
     */
    public int compterParCours(int coursId) {
        if (coursId <= 0) return 0;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;

            preparedStatement = connexion.prepareStatement(COUNT_PAR_COURS);
            preparedStatement.setInt(1, coursId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : comptage examens cours " + coursId);
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    // ===== MÉTHODES D'ÉCRITURE =====

    /**
     * Ajoute un nouvel examen
     * 
     * @param examen L'examen à ajouter
     * @return true si l'ajout a réussi
     */
    public boolean ajouter(ExamenModule examen) {
        if (examen == null) return false;
        if (examen.getCoursId() <= 0 || examen.getModuleNumero() <= 0) return false;
        if (examen.getTitre() == null || examen.getTitre().trim().isEmpty()) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(INSERT);
            preparedStatement.setInt(1, examen.getCoursId());
            preparedStatement.setInt(2, examen.getModuleNumero());
            preparedStatement.setString(3, examen.getTitre().trim());
            preparedStatement.setString(4, examen.getDescription());
            preparedStatement.setInt(5, examen.getNombreQuestions());
            preparedStatement.setInt(6, examen.getSeuilReussite());
            preparedStatement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : ajout examen module " + examen.getModuleNumero());
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Modifie un examen existant
     * 
     * @param examen L'examen à modifier
     * @return true si la modification a réussi
     */
    public boolean modifier(ExamenModule examen) {
        if (examen == null || examen.getId() <= 0) return false;
        if (examen.getTitre() == null || examen.getTitre().trim().isEmpty()) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(UPDATE);
            preparedStatement.setString(1, examen.getTitre().trim());
            preparedStatement.setString(2, examen.getDescription());
            preparedStatement.setInt(3, examen.getNombreQuestions());
            preparedStatement.setInt(4, examen.getSeuilReussite());
            preparedStatement.setInt(5, examen.getId());

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : modification examen ID " + examen.getId());
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Supprime un examen par son ID
     * 
     * @param id ID de l'examen
     * @return true si la suppression a réussi
     */
    public boolean supprimer(int id) {
        if (id <= 0) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(DELETE);
            preparedStatement.setInt(1, id);

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : suppression examen ID " + id);
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    // ===== MÉTHODES PRIVÉES =====

    /**
     * Extrait un examen d'un ResultSet
     * 
     * @param resultSet Le ResultSet positionné sur la ligne courante
     * @return L'objet ExamenModule extrait
     * @throws SQLException En cas d'erreur SQL
     */
    private ExamenModule extraireExamen(ResultSet resultSet) throws SQLException {
        ExamenModule examen = new ExamenModule();
        examen.setId(resultSet.getInt("id"));
        examen.setCoursId(resultSet.getInt("cours_id"));
        examen.setModuleNumero(resultSet.getInt("module_numero"));
        examen.setTitre(resultSet.getString("titre"));
        examen.setDescription(resultSet.getString("description"));
        examen.setNombreQuestions(resultSet.getInt("nombre_questions"));
        examen.setSeuilReussite(resultSet.getInt("seuil_reussite"));
        examen.setDateCreation(resultSet.getTimestamp("date_creation"));
        return examen;
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