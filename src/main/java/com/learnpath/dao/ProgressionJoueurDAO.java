package com.learnpath.dao;

import com.learnpath.models.ProgressionJoueur;
import com.learnpath.utils.ConnexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ProgressionJoueurDAO {
    
    /**
     * Enregistre ou met à jour la progression d'un joueur pour une partie
     * Si une progression existe déjà pour ce joueur et ce quiz, on la met à jour
     * Sinon, on en crée une nouvelle
     * 
     * @param progression La progression à enregistrer
     * @return true si l'opération a réussi
     */
    public boolean enregistrerOuMettreAJour(ProgressionJoueur progression) {
        // Vérifier si une progression existe déjà
        ProgressionJoueur existante = getProgression(progression.getJoueurId(), progression.getQuizId());
        
        if (existante != null) {
            // Mise à jour si le nouveau score est meilleur ou si la partie est maintenant réussie
            if (progression.getScore() > existante.getScore() || progression.isReussi()) {
                return mettreAJour(progression);
            }
            return true; // On garde l'ancienne (meilleure ou identique)
        } else {
            return inserer(progression);
        }
    }
    
    /**
     * Insère une nouvelle progression
     */
    private boolean inserer(ProgressionJoueur progression) {
        String requete = "INSERT INTO progression_joueur (joueur_id, quiz_id, niveau, partie, score, total_questions, reussi, date_realisation) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, progression.getJoueurId());
            preparedStatement.setInt(2, progression.getQuizId());
            preparedStatement.setString(3, progression.getNiveau());
            preparedStatement.setInt(4, progression.getPartie());
            preparedStatement.setInt(5, progression.getScore());
            preparedStatement.setInt(6, progression.getTotalQuestions());
            preparedStatement.setBoolean(7, progression.isReussi());
            preparedStatement.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            
            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion de la progression !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }
        
        return false;
    }
    
    /**
     * Met à jour une progression existante
     */
    private boolean mettreAJour(ProgressionJoueur progression) {
        String requete = "UPDATE progression_joueur SET score = ?, total_questions = ?, reussi = ?, date_realisation = ? WHERE joueur_id = ? AND quiz_id = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, progression.getScore());
            preparedStatement.setInt(2, progression.getTotalQuestions());
            preparedStatement.setBoolean(3, progression.isReussi());
            preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setInt(5, progression.getJoueurId());
            preparedStatement.setInt(6, progression.getQuizId());
            
            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la progression !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }
        
        return false;
    }
    
    /**
     * Récupère la progression d'un joueur pour un quiz spécifique
     */
    public ProgressionJoueur getProgression(int joueurId, int quizId) {
        String requete = "SELECT * FROM progression_joueur WHERE joueur_id = ? AND quiz_id = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ProgressionJoueur progression = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, joueurId);
            preparedStatement.setInt(2, quizId);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                progression = extraireProgression(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la progression !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return progression;
    }
    
    /**
     * Vérifie si une partie est réussie par un joueur
     */
    public boolean estPartieReussie(int joueurId, int quizId) {
        String requete = "SELECT reussi FROM progression_joueur WHERE joueur_id = ? AND quiz_id = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, joueurId);
            preparedStatement.setInt(2, quizId);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getBoolean("reussi");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de la partie réussie !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return false;
    }
    
    /**
     * Compte le nombre de parties réussies dans un niveau pour un joueur
     * 
     * @param joueurId L'ID du joueur
     * @param categorie La catégorie du quiz
     * @param niveau Le niveau (elementaire, moyen, avance)
     * @return Le nombre de parties réussies
     */
    public int compterPartiesReussiesNiveau(int joueurId, String categorie, String niveau) {
        String requete = "SELECT COUNT(*) FROM progression_joueur pj " +
                         "JOIN quiz q ON pj.quiz_id = q.id " +
                         "WHERE pj.joueur_id = ? AND q.titre LIKE ? AND pj.niveau = ? AND pj.reussi = 1";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int nbReussies = 0;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, joueurId);
            preparedStatement.setString(2, categorie + "%");
            preparedStatement.setString(3, niveau);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                nbReussies = resultSet.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des parties réussies du niveau !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return nbReussies;
    }
    
    /**
     * Vérifie si un niveau est débloqué pour un joueur
     * Un niveau est débloqué si :
     * - C'est le niveau élémentaire (toujours débloqué)
     * - Le niveau précédent est entièrement réussi
     * - Il a été explicitement débloqué
     */
    public boolean estNiveauDebloque(int joueurId, String categorie, String niveau) {
        // Le niveau élémentaire est toujours débloqué
        if ("elementaire".equals(niveau)) {
            return true;
        }
        
        // Vérifier si le niveau précédent est terminé
        String niveauPrecedent = "moyen".equals(niveau) ? "elementaire" : "moyen";
        
        // Compter le nombre total de parties du niveau précédent
        String requeteTotal = "SELECT COUNT(*) FROM quiz WHERE titre LIKE ? AND niveau = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int totalParties = 0;
        int partiesReussies = 0;
        
        try {
            connexion = ConnexionDB.getConnexion();
            
            // Nombre total de parties du niveau précédent
            preparedStatement = connexion.prepareStatement(requeteTotal);
            preparedStatement.setString(1, categorie + "%");
            preparedStatement.setString(2, niveauPrecedent);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                totalParties = resultSet.getInt(1);
            }
            
            fermerRessources(null, preparedStatement, resultSet);
            
            // Si aucune partie dans le niveau précédent, on considère débloqué
            if (totalParties == 0) {
                return true;
            }
            
            // Nombre de parties réussies du niveau précédent
            partiesReussies = compterPartiesReussiesNiveau(joueurId, categorie, niveauPrecedent);
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du déblocage du niveau !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        // Le niveau est débloqué si toutes les parties du niveau précédent sont réussies
        return partiesReussies >= totalParties;
    }
    
    /**
     * Vérifie si un niveau est complètement terminé par un joueur
     * 
     * @param joueurId L'ID du joueur
     * @param categorie La catégorie du quiz
     * @param niveau Le niveau (elementaire, moyen, avance)
     * @return true si toutes les parties du niveau sont réussies
     */
    public boolean estNiveauTermine(int joueurId, String categorie, String niveau) {
        // Compter le nombre total de parties dans le niveau
        QuizDAO quizDAO = new QuizDAO();
        int totalParties = quizDAO.compterPartiesParNiveau(categorie, niveau);
        
        // Compter le nombre de parties réussies dans le niveau
        int partiesReussies = compterPartiesReussiesNiveau(joueurId, categorie, niveau);
        
        // Le niveau est terminé si toutes les parties sont réussies
        return (totalParties > 0) && (partiesReussies >= totalParties);
    }
    
    /**
     * Récupère le nombre total de parties réussies par un joueur
     * 
     * @param joueurId L'ID du joueur
     * @return Le nombre total de parties réussies
     */
    public int compterTotalPartiesReussies(int joueurId) {
        String requete = "SELECT COUNT(*) FROM progression_joueur WHERE joueur_id = ? AND reussi = 1";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int totalReussies = 0;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, joueurId);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                totalReussies = resultSet.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage total des parties réussies !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return totalReussies;
    }
    
    /**
     * Récupère la progression pour tous les quiz d'un joueur
     * 
     * @param joueurId L'ID du joueur
     * @return Liste des progressions du joueur (tableau)
     */
    public List<ProgressionJoueur> getProgressionsJoueur(int joueurId) {
        String requete = "SELECT * FROM progression_joueur WHERE joueur_id = ? ORDER BY date_realisation DESC";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<ProgressionJoueur> listeProgression = new ArrayList<>();
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, joueurId);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                ProgressionJoueur progression = extraireProgression(resultSet);
                listeProgression.add(progression);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des progressions du joueur !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return listeProgression;
    }
    
    /**
     * Extrait un objet ProgressionJoueur d'un ResultSet
     */
    private ProgressionJoueur extraireProgression(ResultSet resultSet) throws SQLException {
        ProgressionJoueur progression = new ProgressionJoueur();
        progression.setId(resultSet.getInt("id"));
        progression.setJoueurId(resultSet.getInt("joueur_id"));
        progression.setQuizId(resultSet.getInt("quiz_id"));
        progression.setNiveau(resultSet.getString("niveau"));
        progression.setPartie(resultSet.getInt("partie"));
        progression.setScore(resultSet.getInt("score"));
        progression.setTotalQuestions(resultSet.getInt("total_questions"));
        progression.setReussi(resultSet.getBoolean("reussi"));
        progression.setDateRealisation(resultSet.getTimestamp("date_realisation"));
        return progression;
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
    
    /**
 * Débloque explicitement un niveau pour un joueur
 * Cette méthode est appelée quand un joueur termine toutes les parties d'un niveau
 * 
 * @param joueurId L'ID du joueur
 * @param categorie La catégorie (thème)
 * @param niveauSuperieur Le niveau à débloquer (moyen ou avance)
 */
public void debloquerNiveau(int joueurId, String categorie, String niveauSuperieur) {
    // Vérification dynamique via estNiveauDebloque()
    // Cette méthode est gardée pour compatibilité avec CorrectionServlet
    System.out.println("Niveau " + niveauSuperieur + " débloqué pour le joueur " + joueurId + 
                       " dans la catégorie " + categorie);
}
}