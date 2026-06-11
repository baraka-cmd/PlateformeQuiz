package com.learnpath.dao;

import com.learnpath.models.Partie;
import com.learnpath.utils.ConnexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PartieDAO {
    
    /**
     * Enregistre une nouvelle partie dans la base de données
     * 
     * @param partie La partie à enregistrer
     * @return true si l'enregistrement a réussi, false sinon
     */
    public boolean enregistrerPartie(Partie partie) {
        String requete = "INSERT INTO parties (id_joueur, id_quiz, score, total_questions, temps_realise_secondes, date_partie) VALUES (?, ?, ?, ?, ?, ?)";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, partie.getIdJoueur());
            preparedStatement.setInt(2, partie.getIdQuiz());
            preparedStatement.setInt(3, partie.getScore());
            preparedStatement.setInt(4, partie.getTotalQuestions());
            preparedStatement.setInt(5, partie.getTempsRealiseSecondes());
            preparedStatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            
            int lignesInserees = preparedStatement.executeUpdate();
            
            return lignesInserees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'enregistrement de la partie !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }
        
        return false;
    }
    
    /**
     * Récupère l'historique des parties d'un joueur
     * 
     * @param idJoueur L'identifiant du joueur
     * @return Liste des parties du joueur
     */
    public List<Partie> historiqueJoueur(int idJoueur) {
        String requete = "SELECT p.*, q.titre AS titre_quiz " +
                         "FROM parties p " +
                         "JOIN quiz q ON p.id_quiz = q.id " +
                         "WHERE p.id_joueur = ? " +
                         "ORDER BY p.date_partie DESC";
        
        List<Partie> listeParties = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, idJoueur);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                Partie partie = new Partie();
                partie.setId(resultSet.getInt("id"));
                partie.setIdJoueur(resultSet.getInt("id_joueur"));
                partie.setIdQuiz(resultSet.getInt("id_quiz"));
                partie.setScore(resultSet.getInt("score"));
                partie.setTotalQuestions(resultSet.getInt("total_questions"));
                partie.setTempsRealiseSecondes(resultSet.getInt("temps_realise_secondes"));
                partie.setDatePartie(resultSet.getTimestamp("date_partie"));
                partie.setTitreQuiz(resultSet.getString("titre_quiz"));
                
                listeParties.add(partie);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'historique du joueur !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return listeParties;
    }
    
    /**
     * Compte le nombre total de parties jouées par un joueur
     * 
     * @param idJoueur L'identifiant du joueur
     * @return Le nombre de parties
     */
    public int compterPartiesJoueur(int idJoueur) {
        String requete = "SELECT COUNT(*) FROM parties WHERE id_joueur = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int nbParties = 0;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, idJoueur);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                nbParties = resultSet.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des parties du joueur !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return nbParties;
    }
    
    /**
     * Récupère le meilleur score d'un joueur (en pourcentage)
     * 
     * @param idJoueur L'identifiant du joueur
     * @return Le meilleur pourcentage, ou 0 si aucune partie
     */
    public double meilleurScoreJoueur(int idJoueur) {
        String requete = "SELECT MAX((score * 100.0) / total_questions) AS meilleur_pourcentage " +
                         "FROM parties WHERE id_joueur = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        double meilleurScore = 0.0;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, idJoueur);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                meilleurScore = resultSet.getDouble("meilleur_pourcentage");
                if (resultSet.wasNull()) {
                    meilleurScore = 0.0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du meilleur score !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return meilleurScore;
    }
    
    /**
     * Calcule la moyenne des scores d'un joueur (en pourcentage)
     * 
     * @param idJoueur L'identifiant du joueur
     * @return La moyenne des pourcentages, ou 0 si aucune partie
     */
    public double moyenneScoreJoueur(int idJoueur) {
        String requete = "SELECT AVG((score * 100.0) / total_questions) AS moyenne_pourcentage " +
                         "FROM parties WHERE id_joueur = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        double moyenne = 0.0;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, idJoueur);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                moyenne = resultSet.getDouble("moyenne_pourcentage");
                if (resultSet.wasNull()) {
                    moyenne = 0.0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul de la moyenne des scores !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return moyenne;
    }
    
    /**
     * Récupère les résultats de tous les joueurs pour un quiz donné (pour l'admin)
     * 
     * @param idQuiz L'identifiant du quiz
     * @return Liste des parties pour ce quiz
     */
    public List<Partie> resultatsParQuiz(int idQuiz) {
        String requete = "SELECT p.*, u.pseudo AS pseudo_joueur, q.titre AS titre_quiz " +
                         "FROM parties p " +
                         "JOIN utilisateurs u ON p.id_joueur = u.id " +
                         "JOIN quiz q ON p.id_quiz = q.id " +
                         "WHERE p.id_quiz = ? " +
                         "ORDER BY p.score DESC, p.temps_realise_secondes ASC";
        
        List<Partie> listeParties = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, idQuiz);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                Partie partie = new Partie();
                partie.setId(resultSet.getInt("id"));
                partie.setIdJoueur(resultSet.getInt("id_joueur"));
                partie.setIdQuiz(resultSet.getInt("id_quiz"));
                partie.setScore(resultSet.getInt("score"));
                partie.setTotalQuestions(resultSet.getInt("total_questions"));
                partie.setTempsRealiseSecondes(resultSet.getInt("temps_realise_secondes"));
                partie.setDatePartie(resultSet.getTimestamp("date_partie"));
                partie.setTitreQuiz(resultSet.getString("titre_quiz"));
                partie.setPseudoJoueur(resultSet.getString("pseudo_joueur"));
                
                listeParties.add(partie);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des résultats du quiz !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return listeParties;
    }
    
    /**
     * Calcule la moyenne des scores pour un quiz donné (pour l'admin)
     * 
     * @param idQuiz L'identifiant du quiz
     * @return La moyenne des pourcentages
     */
    public double moyenneScoreParQuiz(int idQuiz) {
        String requete = "SELECT AVG((score * 100.0) / total_questions) AS moyenne " +
                         "FROM parties WHERE id_quiz = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        double moyenne = 0.0;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, idQuiz);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                moyenne = resultSet.getDouble("moyenne");
                if (resultSet.wasNull()) {
                    moyenne = 0.0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul de la moyenne du quiz !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return moyenne;
    }
    
    /**
     * Compte le nombre de participants pour un quiz donné (pour l'admin)
     * 
     * @param idQuiz L'identifiant du quiz
     * @return Le nombre de participants
     */
    public int compterParticipantsParQuiz(int idQuiz) {
        String requete = "SELECT COUNT(DISTINCT id_joueur) FROM parties WHERE id_quiz = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int nbParticipants = 0;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, idQuiz);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                nbParticipants = resultSet.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des participants !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return nbParticipants;
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