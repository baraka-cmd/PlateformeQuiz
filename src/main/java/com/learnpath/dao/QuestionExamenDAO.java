// ============================================================
// FICHIER : src/main/java/com/LearnPath/dao/QuestionExamenDAO.java
// RÔLE    : Accès aux données de la table "questions_examens"
//           Récupération des questions d'un examen de module
// ============================================================

package com.learnpath.dao;

import com.learnpath.models.QuestionExamen;
import com.learnpath.utils.ConnexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * QuestionExamenDAO - Data Access Object pour les questions d'examens
 * 
 * Gère la récupération des questions à choix multiples (A, B, C, D)
 * pour chaque examen de module.
 * 
 * @author LearnPath
 * @version 1.0
 */
public class QuestionExamenDAO {

    // ===== CONSTANTES DE REQUÊTES SQL =====
    private static final String SELECT_PAR_EXAMEN = 
        "SELECT * FROM questions_examens WHERE examen_id = ? ORDER BY numero";
    
    private static final String SELECT_PAR_ID = 
        "SELECT * FROM questions_examens WHERE id = ?";
    
    private static final String COUNT_PAR_EXAMEN = 
        "SELECT COUNT(*) FROM questions_examens WHERE examen_id = ?";
    
    private static final String INSERT = 
        "INSERT INTO questions_examens (examen_id, numero, question, proposition1, " +
        "proposition2, proposition3, proposition4, bonne_reponse) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE = 
        "UPDATE questions_examens SET question = ?, proposition1 = ?, proposition2 = ?, " +
        "proposition3 = ?, proposition4 = ?, bonne_reponse = ? WHERE id = ?";
    
    private static final String DELETE = 
        "DELETE FROM questions_examens WHERE id = ?";
    
    private static final String DELETE_PAR_EXAMEN = 
        "DELETE FROM questions_examens WHERE examen_id = ?";

    // ===== MÉTHODES DE LECTURE =====

    /**
     * Liste toutes les questions d'un examen, triées par numéro
     * 
     * @param examenId ID de l'examen
     * @return Liste des questions (vide si aucune)
     */
    public List<QuestionExamen> listerParExamen(int examenId) {
        List<QuestionExamen> liste = new ArrayList<>();
        if (examenId <= 0) return liste;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return liste;

            preparedStatement = connexion.prepareStatement(SELECT_PAR_EXAMEN);
            preparedStatement.setInt(1, examenId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                liste.add(extraireQuestion(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : liste questions examen " + examenId);
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Trouve une question par son ID
     * 
     * @param id ID de la question
     * @return La question trouvée ou null
     */
    public QuestionExamen trouverParId(int id) {
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
                return extraireQuestion(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : recherche question ID " + id);
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Compte le nombre de questions d'un examen
     * 
     * @param examenId ID de l'examen
     * @return Nombre de questions
     */
    public int compterParExamen(int examenId) {
        if (examenId <= 0) return 0;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;

            preparedStatement = connexion.prepareStatement(COUNT_PAR_EXAMEN);
            preparedStatement.setInt(1, examenId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : comptage questions examen " + examenId);
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    // ===== MÉTHODES D'ÉCRITURE =====

    /**
     * Ajoute une nouvelle question à un examen
     * 
     * @param question La question à ajouter
     * @return true si l'ajout a réussi
     */
    public boolean ajouter(QuestionExamen question) {
        if (question == null) return false;
        if (question.getExamenId() <= 0) return false;
        if (question.getQuestion() == null || question.getQuestion().trim().isEmpty()) return false;
        if (question.getBonneReponse() < 1 || question.getBonneReponse() > 4) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(INSERT);
            preparedStatement.setInt(1, question.getExamenId());
            preparedStatement.setInt(2, question.getNumero());
            preparedStatement.setString(3, question.getQuestion().trim());
            preparedStatement.setString(4, question.getProposition1() != null ? question.getProposition1().trim() : "");
            preparedStatement.setString(5, question.getProposition2() != null ? question.getProposition2().trim() : "");
            preparedStatement.setString(6, question.getProposition3() != null ? question.getProposition3().trim() : "");
            preparedStatement.setString(7, question.getProposition4() != null ? question.getProposition4().trim() : "");
            preparedStatement.setInt(8, question.getBonneReponse());

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : ajout question examen");
            System.err.println("   Question : " + question.getQuestion());
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Ajoute plusieurs questions en une seule transaction
     * 
     * @param questions Liste des questions à ajouter
     * @return true si toutes les questions ont été ajoutées
     */
    public boolean ajouterPlusieurs(List<QuestionExamen> questions) {
        if (questions == null || questions.isEmpty()) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            // Désactiver l'auto-commit pour la transaction
            connexion.setAutoCommit(false);

            preparedStatement = connexion.prepareStatement(INSERT);

            for (QuestionExamen question : questions) {
                if (question.getQuestion() == null || question.getQuestion().trim().isEmpty()) continue;
                if (question.getBonneReponse() < 1 || question.getBonneReponse() > 4) continue;

                preparedStatement.setInt(1, question.getExamenId());
                preparedStatement.setInt(2, question.getNumero());
                preparedStatement.setString(3, question.getQuestion().trim());
                preparedStatement.setString(4, question.getProposition1() != null ? question.getProposition1().trim() : "");
                preparedStatement.setString(5, question.getProposition2() != null ? question.getProposition2().trim() : "");
                preparedStatement.setString(6, question.getProposition3() != null ? question.getProposition3().trim() : "");
                preparedStatement.setString(7, question.getProposition4() != null ? question.getProposition4().trim() : "");
                preparedStatement.setInt(8, question.getBonneReponse());
                preparedStatement.addBatch();
            }

            int[] resultats = preparedStatement.executeBatch();
            connexion.commit();

            // Vérifier que toutes les insertions ont réussi
            for (int resultat : resultats) {
                if (resultat <= 0 && resultat != java.sql.Statement.SUCCESS_NO_INFO) {
                    return false;
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : ajout multiple questions examen");
            System.err.println("   Message : " + e.getMessage());
            if (connexion != null) {
                try { connexion.rollback(); } catch (SQLException ex) { /* Ignorer */ }
            }
        } finally {
            if (connexion != null) {
                try { connexion.setAutoCommit(true); } catch (SQLException e) { /* Ignorer */ }
            }
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Modifie une question existante
     * 
     * @param question La question à modifier
     * @return true si la modification a réussi
     */
    public boolean modifier(QuestionExamen question) {
        if (question == null || question.getId() <= 0) return false;
        if (question.getQuestion() == null || question.getQuestion().trim().isEmpty()) return false;
        if (question.getBonneReponse() < 1 || question.getBonneReponse() > 4) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(UPDATE);
            preparedStatement.setString(1, question.getQuestion().trim());
            preparedStatement.setString(2, question.getProposition1() != null ? question.getProposition1().trim() : "");
            preparedStatement.setString(3, question.getProposition2() != null ? question.getProposition2().trim() : "");
            preparedStatement.setString(4, question.getProposition3() != null ? question.getProposition3().trim() : "");
            preparedStatement.setString(5, question.getProposition4() != null ? question.getProposition4().trim() : "");
            preparedStatement.setInt(6, question.getBonneReponse());
            preparedStatement.setInt(7, question.getId());

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : modification question ID " + question.getId());
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Supprime une question par son ID
     * 
     * @param id ID de la question
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
            System.err.println("❌ Erreur : suppression question ID " + id);
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Supprime toutes les questions d'un examen
     * 
     * @param examenId ID de l'examen
     * @return true si la suppression a réussi
     */
    public boolean supprimerParExamen(int examenId) {
        if (examenId <= 0) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(DELETE_PAR_EXAMEN);
            preparedStatement.setInt(1, examenId);
            preparedStatement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : suppression questions examen " + examenId);
            System.err.println("   Message : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    // ===== MÉTHODES PRIVÉES =====

    /**
     * Extrait une question d'un ResultSet
     * 
     * @param resultSet Le ResultSet positionné sur la ligne courante
     * @return L'objet QuestionExamen extrait
     * @throws SQLException En cas d'erreur SQL
     */
    private QuestionExamen extraireQuestion(ResultSet resultSet) throws SQLException {
        QuestionExamen question = new QuestionExamen();
        question.setId(resultSet.getInt("id"));
        question.setExamenId(resultSet.getInt("examen_id"));
        question.setNumero(resultSet.getInt("numero"));
        question.setQuestion(resultSet.getString("question"));
        question.setProposition1(resultSet.getString("proposition1"));
        question.setProposition2(resultSet.getString("proposition2"));
        question.setProposition3(resultSet.getString("proposition3"));
        question.setProposition4(resultSet.getString("proposition4"));
        question.setBonneReponse(resultSet.getInt("bonne_reponse"));
        return question;
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