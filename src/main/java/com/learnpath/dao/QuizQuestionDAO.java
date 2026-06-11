package com.learnpath.dao;

import com.learnpath.utils.ConnexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuizQuestionDAO {
    
    /**
     * Associe une question à un quiz
     * 
     * @param idQuiz L'identifiant du quiz
     * @param idQuestion L'identifiant de la question
     * @return true si l'association a réussi, false sinon
     */
    public boolean associerQuestionAuQuiz(int idQuiz, int idQuestion) {
        String requete = "INSERT INTO quiz_questions (id_quiz, id_question) VALUES (?, ?)";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, idQuiz);
            preparedStatement.setInt(2, idQuestion);
            
            int lignesInserees = preparedStatement.executeUpdate();
            
            return lignesInserees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'association question-quiz !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }
        
        return false;
    }
    
    /**
     * Associe plusieurs questions à un quiz en une seule transaction
     * 
     * @param idQuiz L'identifiant du quiz
     * @param idsQuestions Liste des identifiants des questions
     * @return true si toutes les associations ont réussi, false sinon
     */
    public boolean associerQuestionsAuQuiz(int idQuiz, int[] idsQuestions) {
        String requete = "INSERT INTO quiz_questions (id_quiz, id_question) VALUES (?, ?)";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            connexion.setAutoCommit(false);
            
            preparedStatement = connexion.prepareStatement(requete);
            
            for (int idQuestion : idsQuestions) {
                preparedStatement.setInt(1, idQuiz);
                preparedStatement.setInt(2, idQuestion);
                preparedStatement.addBatch();
            }
            
            int[] resultats = preparedStatement.executeBatch();
            connexion.commit();
            
            // Vérifier que toutes les insertions ont réussi
            for (int resultat : resultats) {
                if (resultat <= 0 && resultat != java.sql.Statement.SUCCESS_NO_INFO) {
                    connexion.rollback();
                    return false;
                }
            }
            
            return true;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'association multiple question-quiz !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
            
            if (connexion != null) {
                try {
                    connexion.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (connexion != null) {
                try {
                    connexion.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            fermerRessources(null, preparedStatement, null);
        }
        
        return false;
    }
    
    /**
     * Retire une question d'un quiz
     * 
     * @param idQuiz L'identifiant du quiz
     * @param idQuestion L'identifiant de la question
     * @return true si le retrait a réussi, false sinon
     */
    public boolean retirerQuestionDuQuiz(int idQuiz, int idQuestion) {
        String requete = "DELETE FROM quiz_questions WHERE id_quiz = ? AND id_question = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, idQuiz);
            preparedStatement.setInt(2, idQuestion);
            
            int lignesSupprimees = preparedStatement.executeUpdate();
            
            return lignesSupprimees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du retrait de la question du quiz !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }
        
        return false;
    }
    
    /**
     * Supprime toutes les associations de questions pour un quiz
     * 
     * @param idQuiz L'identifiant du quiz
     * @return true si la suppression a réussi, false sinon
     */
    public boolean supprimerToutesAssociations(int idQuiz) {
        String requete = "DELETE FROM quiz_questions WHERE id_quiz = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, idQuiz);
            
            preparedStatement.executeUpdate();
            
            return true;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression des associations du quiz !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }
        
        return false;
    }
    
    /**
     * Récupère la liste des IDs des questions associées à un quiz
     * 
     * @param idQuiz L'identifiant du quiz
     * @return Liste des IDs des questions
     */
    public List<Integer> listerIdsQuestionsParQuiz(int idQuiz) {
        String requete = "SELECT id_question FROM quiz_questions WHERE id_quiz = ? ORDER BY id_question";
        List<Integer> idsQuestions = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, idQuiz);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                idsQuestions.add(resultSet.getInt("id_question"));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des IDs des questions du quiz !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return idsQuestions;
    }
    
    /**
     * Vérifie si une question est déjà associée à un quiz
     * 
     * @param idQuiz L'identifiant du quiz
     * @param idQuestion L'identifiant de la question
     * @return true si l'association existe déjà
     */
    public boolean associationExiste(int idQuiz, int idQuestion) {
        String requete = "SELECT COUNT(*) FROM quiz_questions WHERE id_quiz = ? AND id_question = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, idQuiz);
            preparedStatement.setInt(2, idQuestion);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'association !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return false;
    }
    
    /**
     * Méthode utilitaire pour fermer les ressources JDBC
     */
    private void fermerRessources(Connection connexion, PreparedStatement preparedStatement, ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture du ResultSet !");
                e.printStackTrace();
            }
        }
        
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture du PreparedStatement !");
                e.printStackTrace();
            }
        }
        
        if (connexion != null) {
            try {
                connexion.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion !");
                e.printStackTrace();
            }
        }
    }
}