// ============================================================
// FICHIER : src/main/java/com/amijayquiz/dao/ProgressionCoursDAO.java
// RÔLE    : Accès aux données de la table "progression_cours"
//           Suivi de progression utilisateur par leçon
// ============================================================

package com.amijayquiz.dao;

import com.amijayquiz.models.ProgressionCours;
import com.amijayquiz.utils.ConnexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * ProgressionCoursDAO - Data Access Object pour le suivi de progression
 * 
 * Gère l'enregistrement et la consultation de la progression
 * des utilisateurs dans les leçons des cours.
 * 
 * @author AmijayQuiz
 * @version 1.1
 */
public class ProgressionCoursDAO {

    // ===== CONSTANTES DE REQUÊTES SQL =====
    private static final String SELECT_PAR_UTILISATEUR_LECON = 
        "SELECT * FROM progression_cours WHERE utilisateur_id = ? AND lecon_id = ?";
    
    private static final String INSERT_DEBUT = 
        "INSERT INTO progression_cours (utilisateur_id, cours_id, chapitre_id, lecon_id, termine, " +
        "score_quiz, temps_passe, date_debut) VALUES (?, ?, ?, ?, 0, 0, 0, ?)";
    
    private static final String UPDATE_TERMINE = 
        "UPDATE progression_cours SET termine = 1, date_completion = ? WHERE utilisateur_id = ? AND lecon_id = ?";
    
    private static final String UPDATE_TERMINE_AVEC_SCORE = 
        "UPDATE progression_cours SET termine = 1, score_quiz = ?, date_completion = ? WHERE utilisateur_id = ? AND lecon_id = ?";
    
    private static final String UPDATE_AJOUTER_TEMPS = 
        "UPDATE progression_cours SET temps_passe = temps_passe + ? WHERE utilisateur_id = ? AND lecon_id = ?";
    
    private static final String SELECT_PAR_UTILISATEUR = 
        "SELECT * FROM progression_cours WHERE utilisateur_id = ? ORDER BY date_debut DESC";
    
    private static final String SELECT_PAR_UTILISATEUR_COURS = 
        "SELECT * FROM progression_cours WHERE utilisateur_id = ? AND cours_id = ? ORDER BY date_debut";
    
    private static final String COUNT_LECONS_TERMINEES_COURS = 
        "SELECT COUNT(*) FROM progression_cours WHERE utilisateur_id = ? AND cours_id = ? AND termine = 1";
    
    private static final String COUNT_LECONS_TERMINEES_CHAPITRE = 
        "SELECT COUNT(*) FROM progression_cours WHERE utilisateur_id = ? AND chapitre_id = ? AND termine = 1";
    
    private static final String COUNT_TOTAL_LECONS_COURS = 
        "SELECT COUNT(*) FROM lecons l JOIN chapitres c ON l.chapitre_id = c.id WHERE c.cours_id = ?";
    
    private static final String EST_COURS_TERMINE = 
        "SELECT COUNT(*) FROM progression_cours WHERE utilisateur_id = ? AND cours_id = ? AND termine = 1";
    
    private static final String EST_CHAPITRE_TERMINE = 
        "SELECT COUNT(*) FROM progression_cours WHERE utilisateur_id = ? AND chapitre_id = ? AND termine = 1";
    
    private static final String DELETE_PAR_UTILISATEUR_COURS = 
        "DELETE FROM progression_cours WHERE utilisateur_id = ? AND cours_id = ?";
    
    // ===== NOUVELLE REQUÊTE POUR compterLeconsTerminees =====
    private static final String COUNT_LE_CONS_TERMINEES_PAR_COURS = 
        "SELECT COUNT(*) FROM progression_cours pc " +
        "JOIN lecons l ON pc.lecon_id = l.id " +
        "JOIN chapitres c ON l.chapitre_id = c.id " +
        "WHERE pc.utilisateur_id = ? AND c.cours_id = ? AND pc.termine = 1";

    // ===== MÉTHODES DE LECTURE =====

    /**
     * Récupère la progression d'un utilisateur pour une leçon spécifique
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param leconId       ID de la leçon
     * @return La progression trouvée ou null
     */
    public ProgressionCours getProgression(int utilisateurId, int leconId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_UTILISATEUR_LECON);
            preparedStatement.setInt(1, utilisateurId);
            preparedStatement.setInt(2, leconId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return extraireProgression(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : progression utilisateur " + utilisateurId + " leçon " + leconId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Liste toute la progression d'un utilisateur (tous cours confondus)
     * 
     * @param utilisateurId ID de l'utilisateur
     * @return Liste des progressions
     */
    public List<ProgressionCours> listerProgressionUtilisateur(int utilisateurId) {
        List<ProgressionCours> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_UTILISATEUR);
            preparedStatement.setInt(1, utilisateurId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                liste.add(extraireProgression(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : progression utilisateur " + utilisateurId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Liste la progression d'un utilisateur dans un cours spécifique
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param coursId       ID du cours
     * @return Liste des progressions pour ce cours
     */
    public List<ProgressionCours> listerProgressionCours(int utilisateurId, int coursId) {
        List<ProgressionCours> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_UTILISATEUR_COURS);
            preparedStatement.setInt(1, utilisateurId);
            preparedStatement.setInt(2, coursId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                liste.add(extraireProgression(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : progression cours " + coursId + " utilisateur " + utilisateurId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    // ===== MÉTHODES D'ÉCRITURE =====

    /**
     * Enregistre le début d'une leçon pour un utilisateur
     * Si une progression existe déjà, ne fait rien (évite les doublons)
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param coursId       ID du cours
     * @param chapitreId    ID du chapitre
     * @param leconId       ID de la leçon
     * @return true si l'enregistrement a réussi
     */
    public boolean enregistrerDebut(int utilisateurId, int coursId, int chapitreId, int leconId) {
        // Vérifier si une progression existe déjà
        ProgressionCours existante = getProgression(utilisateurId, leconId);
        if (existante != null) {
            return true; // Déjà enregistré, ne pas créer de doublon
        }

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(INSERT_DEBUT);
            preparedStatement.setInt(1, utilisateurId);
            preparedStatement.setInt(2, coursId);
            preparedStatement.setInt(3, chapitreId);
            preparedStatement.setInt(4, leconId);
            preparedStatement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : début leçon " + leconId + " utilisateur " + utilisateurId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Marque une leçon comme terminée pour un utilisateur
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param leconId       ID de la leçon
     * @return true si la mise à jour a réussi
     */
    public boolean marquerTermine(int utilisateurId, int leconId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(UPDATE_TERMINE);
            preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setInt(2, utilisateurId);
            preparedStatement.setInt(3, leconId);

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : marquer terminé leçon " + leconId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Marque une leçon comme terminée avec un score de quiz
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param leconId       ID de la leçon
     * @param score         Score obtenu au quiz
     * @return true si la mise à jour a réussi
     */
    public boolean marquerTermineAvecScore(int utilisateurId, int leconId, int score) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(UPDATE_TERMINE_AVEC_SCORE);
            preparedStatement.setInt(1, score);
            preparedStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setInt(3, utilisateurId);
            preparedStatement.setInt(4, leconId);

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : marquer terminé avec score leçon " + leconId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Ajoute du temps passé sur une leçon
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param leconId       ID de la leçon
     * @param secondes      Nombre de secondes à ajouter
     * @return true si la mise à jour a réussi
     */
    public boolean ajouterTemps(int utilisateurId, int leconId, int secondes) {
        if (secondes <= 0) return true; // Rien à ajouter

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(UPDATE_AJOUTER_TEMPS);
            preparedStatement.setInt(1, secondes);
            preparedStatement.setInt(2, utilisateurId);
            preparedStatement.setInt(3, leconId);

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : ajout temps leçon " + leconId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    // ===== MÉTHODES DE CALCUL =====

    /**
     * Retourne le nombre de leçons terminées dans un cours
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param coursId       ID du cours
     * @return Nombre de leçons terminées
     */
    public int getNombreLeconsTerminees(int utilisateurId, int coursId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(COUNT_LECONS_TERMINEES_COURS);
            preparedStatement.setInt(1, utilisateurId);
            preparedStatement.setInt(2, coursId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : comptage leçons terminées cours " + coursId);
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    /**
     * Retourne le nombre total de leçons dans un cours
     * 
     * @param coursId ID du cours
     * @return Nombre total de leçons
     */
    public int getNombreTotalLecons(int coursId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(COUNT_TOTAL_LECONS_COURS);
            preparedStatement.setInt(1, coursId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : comptage total leçons cours " + coursId);
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    /**
     * Calcule le pourcentage de complétion d'un cours
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param coursId       ID du cours
     * @return Pourcentage (0.0 à 100.0)
     */
    public double getPourcentageCompletion(int utilisateurId, int coursId) {
        int total = getNombreTotalLecons(coursId);
        if (total == 0) return 0.0;
        int terminees = getNombreLeconsTerminees(utilisateurId, coursId);
        return (terminees * 100.0) / total;
    }

    /**
     * Compte le nombre de leçons terminées par un utilisateur pour un cours
     * (Méthode alias pour compatibilité avec NiveauCoursServlet)
     * 
     * @param userId   ID de l'utilisateur
     * @param courseId ID du cours
     * @return Nombre de leçons terminées
     */
    public int compterLeconsTerminees(int userId, int courseId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(COUNT_LE_CONS_TERMINEES_PAR_COURS);
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, courseId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : comptage leçons terminées utilisateur " + userId + " cours " + courseId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    // ===== MÉTHODES DE VÉRIFICATION =====

    /**
     * Vérifie si un cours est entièrement terminé par un utilisateur
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param coursId       ID du cours
     * @return true si toutes les leçons sont terminées
     */
    public boolean estCoursTermine(int utilisateurId, int coursId) {
        int total = getNombreTotalLecons(coursId);
        if (total == 0) return false;
        int terminees = getNombreLeconsTerminees(utilisateurId, coursId);
        return terminees >= total;
    }

    /**
     * Vérifie si un chapitre est entièrement terminé par un utilisateur
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param chapitreId    ID du chapitre
     * @return true si toutes les leçons du chapitre sont terminées
     */
    public boolean estChapitreTermine(int utilisateurId, int chapitreId) {
        LeconDAO leconDAO = new LeconDAO();
        int total = leconDAO.compterParChapitre(chapitreId);
        if (total == 0) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(EST_CHAPITRE_TERMINE);
            preparedStatement.setInt(1, utilisateurId);
            preparedStatement.setInt(2, chapitreId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int terminees = resultSet.getInt(1);
                return terminees >= total;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : vérification chapitre terminé " + chapitreId);
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return false;
    }

    /**
     * Vérifie si une leçon est terminée par un utilisateur
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param leconId       ID de la leçon
     * @return true si la leçon est terminée
     */
    public boolean estLeconTerminee(int utilisateurId, int leconId) {
        ProgressionCours progression = getProgression(utilisateurId, leconId);
        return progression != null && progression.isTermine();
    }

    // ===== MÉTHODES DE SUPPRESSION =====

    /**
     * Supprime toute la progression d'un utilisateur pour un cours
     * (Utile pour réinitialiser un cours)
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param coursId       ID du cours
     * @return true si la suppression a réussi
     */
    public boolean reinitialiserProgression(int utilisateurId, int coursId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(DELETE_PAR_UTILISATEUR_COURS);
            preparedStatement.setInt(1, utilisateurId);
            preparedStatement.setInt(2, coursId);
            preparedStatement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : réinitialisation progression cours " + coursId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
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
     * @return L'objet ProgressionCours extrait
     * @throws SQLException En cas d'erreur SQL
     */
    private ProgressionCours extraireProgression(ResultSet resultSet) throws SQLException {
        ProgressionCours progression = new ProgressionCours();
        progression.setId(resultSet.getInt("id"));
        progression.setUtilisateurId(resultSet.getInt("utilisateur_id"));
        progression.setCoursId(resultSet.getInt("cours_id"));
        progression.setChapitreId(resultSet.getInt("chapitre_id"));
        progression.setLeconId(resultSet.getInt("lecon_id"));
        progression.setTermine(resultSet.getBoolean("termine"));
        progression.setScoreQuiz(resultSet.getInt("score_quiz"));
        progression.setTempsPasse(resultSet.getInt("temps_passe"));
        progression.setDateDebut(resultSet.getTimestamp("date_debut"));
        progression.setDateCompletion(resultSet.getTimestamp("date_completion"));
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