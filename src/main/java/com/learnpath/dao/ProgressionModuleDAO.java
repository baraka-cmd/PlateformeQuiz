// ============================================================
// FICHIER : src/main/java/com/LearnPath/dao/ProgressionModuleDAO.java
// RÔLE    : Accès aux données de la table "progression_modules"
//           Gestion de la progression des utilisateurs par module
// ============================================================

package com.learnpath.dao;

import com.learnpath.models.ProgressionModule;
import com.learnpath.utils.ConnexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * ProgressionModuleDAO - Data Access Object pour la progression par module
 * 
 * Gère l'enregistrement et la consultation de la progression
 * des utilisateurs dans les modules de cours.
 * Un module est réussi si le score est >= 75% (15/20).
 * 
 * @author LearnPath
 * @version 1.0
 */
public class ProgressionModuleDAO {

    // ===== CONSTANTES DE REQUÊTES SQL =====
    private static final String SELECT_PAR_UTILISATEUR_COURS_MODULE = 
        "SELECT * FROM progression_modules WHERE utilisateur_id = ? AND cours_id = ? AND module_numero = ?";
    
    private static final String SELECT_PAR_UTILISATEUR_COURS = 
        "SELECT * FROM progression_modules WHERE utilisateur_id = ? AND cours_id = ? ORDER BY module_numero";
    
    private static final String INSERT = 
        "INSERT INTO progression_modules (utilisateur_id, cours_id, module_numero, reussi, " +
        "score, total_questions, pourcentage, date_realisation, tentatives) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE = 
        "UPDATE progression_modules SET reussi = ?, score = ?, total_questions = ?, " +
        "pourcentage = ?, date_realisation = ?, tentatives = ? " +
        "WHERE utilisateur_id = ? AND cours_id = ? AND module_numero = ?";
    
    private static final String COUNT_MODULES_REUSSIS = 
        "SELECT COUNT(*) FROM progression_modules WHERE utilisateur_id = ? AND cours_id = ? AND reussi = 1";
    
    private static final String COUNT_MODULES_TENTES = 
        "SELECT COUNT(*) FROM progression_modules WHERE utilisateur_id = ? AND cours_id = ?";
    
    private static final String DELETE_PAR_UTILISATEUR_COURS = 
        "DELETE FROM progression_modules WHERE utilisateur_id = ? AND cours_id = ?";

    // ===== MÉTHODES DE LECTURE =====

    /**
     * Récupère la progression d'un utilisateur pour un module spécifique
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param coursId       ID du cours
     * @param moduleNumero  Numéro du module
     * @return La progression trouvée ou null
     */
    public ProgressionModule getProgression(int utilisateurId, int coursId, int moduleNumero) {
        if (utilisateurId <= 0 || coursId <= 0 || moduleNumero <= 0) return null;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return null;

            preparedStatement = connexion.prepareStatement(SELECT_PAR_UTILISATEUR_COURS_MODULE);
            preparedStatement.setInt(1, utilisateurId);
            preparedStatement.setInt(2, coursId);
            preparedStatement.setInt(3, moduleNumero);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return extraireProgression(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : progression utilisateur " + utilisateurId + 
                " cours " + coursId + " module " + moduleNumero);
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Liste toute la progression d'un utilisateur pour un cours
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param coursId       ID du cours
     * @return Liste des progressions triées par numéro de module
     */
    public List<ProgressionModule> listerProgression(int utilisateurId, int coursId) {
        List<ProgressionModule> liste = new ArrayList<>();
        if (utilisateurId <= 0 || coursId <= 0) return liste;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return liste;

            preparedStatement = connexion.prepareStatement(SELECT_PAR_UTILISATEUR_COURS);
            preparedStatement.setInt(1, utilisateurId);
            preparedStatement.setInt(2, coursId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                liste.add(extraireProgression(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : liste progression utilisateur " + utilisateurId + " cours " + coursId);
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Compte le nombre de modules réussis par un utilisateur pour un cours
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param coursId       ID du cours
     * @return Nombre de modules réussis
     */
    public int compterModulesReussis(int utilisateurId, int coursId) {
        if (utilisateurId <= 0 || coursId <= 0) return 0;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;

            preparedStatement = connexion.prepareStatement(COUNT_MODULES_REUSSIS);
            preparedStatement.setInt(1, utilisateurId);
            preparedStatement.setInt(2, coursId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : comptage modules réussis utilisateur " + utilisateurId);
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    // ===== MÉTHODES D'ÉCRITURE =====

    /**
     * Enregistre ou met à jour la progression d'un utilisateur pour un module
     * Si une progression existe déjà, on la met à jour (sauf si l'ancien score est meilleur)
     * 
     * @param progression La progression à enregistrer
     * @return true si l'opération a réussi
     */
    public boolean enregistrerOuMettreAJour(ProgressionModule progression) {
        if (progression == null) return false;
        if (progression.getUtilisateurId() <= 0 || progression.getCoursId() <= 0 || progression.getModuleNumero() <= 0) {
            return false;
        }

        // Vérifier si une progression existe déjà
        ProgressionModule existante = getProgression(
            progression.getUtilisateurId(), 
            progression.getCoursId(), 
            progression.getModuleNumero()
        );

        if (existante != null) {
            // Incrémenter les tentatives
            progression.setTentatives(existante.getTentatives() + 1);
            
            // Si l'ancien score est meilleur, on garde l'ancien
            if (existante.getPourcentage() >= progression.getPourcentage() && existante.isReussi()) {
                return true;
            }
            
            // Sinon, mettre à jour
            return mettreAJour(progression);
        } else {
            // Première tentative
            progression.setTentatives(1);
            return inserer(progression);
        }
    }

    /**
     * Insère une nouvelle progression
     * 
     * @param progression La progression à insérer
     * @return true si l'insertion a réussi
     */
    private boolean inserer(ProgressionModule progression) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(INSERT);
            preparedStatement.setInt(1, progression.getUtilisateurId());
            preparedStatement.setInt(2, progression.getCoursId());
            preparedStatement.setInt(3, progression.getModuleNumero());
            preparedStatement.setBoolean(4, progression.isReussi());
            preparedStatement.setInt(5, progression.getScore());
            preparedStatement.setInt(6, progression.getTotalQuestions());
            preparedStatement.setDouble(7, progression.getPourcentage());
            preparedStatement.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setInt(9, progression.getTentatives());

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : insertion progression module " + progression.getModuleNumero());
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Met à jour une progression existante
     * 
     * @param progression La progression à mettre à jour
     * @return true si la mise à jour a réussi
     */
    private boolean mettreAJour(ProgressionModule progression) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(UPDATE);
            preparedStatement.setBoolean(1, progression.isReussi());
            preparedStatement.setInt(2, progression.getScore());
            preparedStatement.setInt(3, progression.getTotalQuestions());
            preparedStatement.setDouble(4, progression.getPourcentage());
            preparedStatement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setInt(6, progression.getTentatives());
            preparedStatement.setInt(7, progression.getUtilisateurId());
            preparedStatement.setInt(8, progression.getCoursId());
            preparedStatement.setInt(9, progression.getModuleNumero());

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : mise à jour progression module " + progression.getModuleNumero());
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Vérifie si un module est réussi par un utilisateur
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param coursId       ID du cours
     * @param moduleNumero  Numéro du module
     * @return true si le module est réussi
     */
    public boolean estModuleReussi(int utilisateurId, int coursId, int moduleNumero) {
        ProgressionModule progression = getProgression(utilisateurId, coursId, moduleNumero);
        return progression != null && progression.isReussi();
    }

    /**
     * Vérifie si un module est débloqué
     * Le module 1 est toujours débloqué
     * Les modules suivants sont débloqués si le module précédent est réussi
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param coursId       ID du cours
     * @param moduleNumero  Numéro du module à vérifier
     * @return true si le module est débloqué
     */
    public boolean estModuleDebloque(int utilisateurId, int coursId, int moduleNumero) {
        // Module 1 toujours débloqué
        if (moduleNumero == 1) return true;
        
        // Pour les autres modules, vérifier si le module précédent est réussi
        return estModuleReussi(utilisateurId, coursId, moduleNumero - 1);
    }

    /**
     * Réinitialise la progression d'un utilisateur pour un cours
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param coursId       ID du cours
     * @return true si la réinitialisation a réussi
     */
    public boolean reinitialiserProgression(int utilisateurId, int coursId) {
        if (utilisateurId <= 0 || coursId <= 0) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(DELETE_PAR_UTILISATEUR_COURS);
            preparedStatement.setInt(1, utilisateurId);
            preparedStatement.setInt(2, coursId);
            preparedStatement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : réinitialisation progression cours " + coursId);
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    // ===== MÉTHODES PRIVÉES =====

    /**
     * Extrait une progression d'un ResultSet
     * 
     * @param resultSet Le ResultSet positionné sur la ligne courante
     * @return L'objet ProgressionModule extrait
     * @throws SQLException En cas d'erreur SQL
     */
    private ProgressionModule extraireProgression(ResultSet resultSet) throws SQLException {
        ProgressionModule progression = new ProgressionModule();
        progression.setId(resultSet.getInt("id"));
        progression.setUtilisateurId(resultSet.getInt("utilisateur_id"));
        progression.setCoursId(resultSet.getInt("cours_id"));
        progression.setModuleNumero(resultSet.getInt("module_numero"));
        progression.setReussi(resultSet.getBoolean("reussi"));
        progression.setScore(resultSet.getInt("score"));
        progression.setTotalQuestions(resultSet.getInt("total_questions"));
        progression.setPourcentage(resultSet.getDouble("pourcentage"));
        progression.setDateRealisation(resultSet.getTimestamp("date_realisation"));
        progression.setTentatives(resultSet.getInt("tentatives"));
        return progression;
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