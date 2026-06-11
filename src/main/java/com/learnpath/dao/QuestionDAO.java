package com.learnpath.dao;

import com.learnpath.models.Question;
import com.learnpath.utils.ConnexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {
    
    /**
     * Récupère toutes les questions associées à un quiz
     * 
     * @param idQuiz L'identifiant du quiz
     * @return Liste des questions du quiz
     */
    public List<Question> listerQuestionsParQuiz(int idQuiz) {
        String requete = "SELECT q.* FROM questions q " +
                         "JOIN quiz_questions qq ON q.id = qq.id_question " +
                         "WHERE qq.id_quiz = ? " +
                         "ORDER BY q.id";
        
        List<Question> listeQuestions = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, idQuiz);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                Question question = new Question();
                question.setId(resultSet.getInt("id"));
                question.setTexteQuestion(resultSet.getString("texte_question"));
                question.setProposition1(resultSet.getString("proposition1"));
                question.setProposition2(resultSet.getString("proposition2"));
                question.setProposition3(resultSet.getString("proposition3"));
                question.setProposition4(resultSet.getString("proposition4"));
                question.setBonneReponse(resultSet.getInt("bonne_reponse"));
                
                listeQuestions.add(question);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des questions du quiz !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return listeQuestions;
    }
    
    /**
     * Récupère une question par son ID
     * 
     * @param id L'identifiant de la question
     * @return La question trouvée ou null
     */
    public Question trouverParId(int id) {
        String requete = "SELECT * FROM questions WHERE id = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Question question = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                question = new Question();
                question.setId(resultSet.getInt("id"));
                question.setTexteQuestion(resultSet.getString("texte_question"));
                question.setProposition1(resultSet.getString("proposition1"));
                question.setProposition2(resultSet.getString("proposition2"));
                question.setProposition3(resultSet.getString("proposition3"));
                question.setProposition4(resultSet.getString("proposition4"));
                question.setBonneReponse(resultSet.getInt("bonne_reponse"));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de la question par ID !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return question;
    }
    
    /**
     * Récupère toutes les questions (pour l'admin)
     * 
     * @return Liste de toutes les questions
     */
    public List<Question> listerToutes() {
        String requete = "SELECT * FROM questions ORDER BY id DESC";
        List<Question> listeQuestions = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                Question question = new Question();
                question.setId(resultSet.getInt("id"));
                question.setTexteQuestion(resultSet.getString("texte_question"));
                question.setProposition1(resultSet.getString("proposition1"));
                question.setProposition2(resultSet.getString("proposition2"));
                question.setProposition3(resultSet.getString("proposition3"));
                question.setProposition4(resultSet.getString("proposition4"));
                question.setBonneReponse(resultSet.getInt("bonne_reponse"));
                
                listeQuestions.add(question);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de toutes les questions !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return listeQuestions;
    }
    
    /**
     * Ajoute une nouvelle question dans la base de données
     * 
     * @param question La question à ajouter
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean ajouter(Question question) {
        String requete = "INSERT INTO questions (texte_question, proposition1, proposition2, proposition3, proposition4, bonne_reponse) VALUES (?, ?, ?, ?, ?, ?)";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, question.getTexteQuestion());
            preparedStatement.setString(2, question.getProposition1());
            preparedStatement.setString(3, question.getProposition2());
            preparedStatement.setString(4, question.getProposition3());
            preparedStatement.setString(5, question.getProposition4());
            preparedStatement.setInt(6, question.getBonneReponse());
            
            int lignesInserees = preparedStatement.executeUpdate();
            
            return lignesInserees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la question !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }
        
        return false;
    }
    
    /**
     * Modifie une question existante
     * 
     * @param question La question à modifier
     * @return true si la modification a réussi, false sinon
     */
    public boolean modifier(Question question) {
        String requete = "UPDATE questions SET texte_question = ?, proposition1 = ?, proposition2 = ?, proposition3 = ?, proposition4 = ?, bonne_reponse = ? WHERE id = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, question.getTexteQuestion());
            preparedStatement.setString(2, question.getProposition1());
            preparedStatement.setString(3, question.getProposition2());
            preparedStatement.setString(4, question.getProposition3());
            preparedStatement.setString(5, question.getProposition4());
            preparedStatement.setInt(6, question.getBonneReponse());
            preparedStatement.setInt(7, question.getId());
            
            int lignesModifiees = preparedStatement.executeUpdate();
            
            return lignesModifiees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de la question !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }
        
        return false;
    }
    
    /**
     * Supprime une question par son ID
     * 
     * @param id L'identifiant de la question
     * @return true si la suppression a réussi, false sinon
     */
    public boolean supprimer(int id) {
        String requete = "DELETE FROM questions WHERE id = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, id);
            
            int lignesSupprimees = preparedStatement.executeUpdate();
            
            return lignesSupprimees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la question !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
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