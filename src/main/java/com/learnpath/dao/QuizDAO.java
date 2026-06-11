package com.learnpath.dao;

import com.learnpath.models.Quiz;
import com.learnpath.utils.ConnexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuizDAO {
    
    /**
     * Récupère la liste de tous les quiz disponibles avec leur nombre de questions
     * 
     * @return Liste des quiz
     */
    public List<Quiz> listerQuizDisponibles() {
        String requete = "SELECT q.*, " +
                         "(SELECT COUNT(*) FROM quiz_questions qq WHERE qq.id_quiz = q.id) AS nombre_questions " +
                         "FROM quiz q " +
                         "ORDER BY q.niveau, q.ordre_partie";
        
        List<Quiz> listeQuiz = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                Quiz quiz = extraireQuiz(resultSet);
                listeQuiz.add(quiz);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la liste des quiz !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return listeQuiz;
    }
    
    /**
     * Récupère les quiz groupés par catégorie et niveau
     * Retourne une Map structurée : Catégorie -> Niveau -> Liste de Quiz
     * 
     * @return Map organisée des quiz
     */
    public Map<String, Map<String, List<Quiz>>> listerQuizOrganises() {
        String requete = "SELECT q.*, " +
                         "(SELECT COUNT(*) FROM quiz_questions qq WHERE qq.id_quiz = q.id) AS nombre_questions, " +
                         "SUBSTRING_INDEX(q.titre, ' - ', 1) AS categorie " +
                         "FROM quiz q " +
                         "ORDER BY q.niveau, q.ordre_partie";
        
        Map<String, Map<String, List<Quiz>>> quizOrganises = new LinkedHashMap<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                Quiz quiz = extraireQuiz(resultSet);
                String categorie = resultSet.getString("categorie");
                String niveau = quiz.getNiveau();
                
                if (!quizOrganises.containsKey(categorie)) {
                    quizOrganises.put(categorie, new LinkedHashMap<>());
                }
                
                Map<String, List<Quiz>> niveauxMap = quizOrganises.get(categorie);
                if (!niveauxMap.containsKey(niveau)) {
                    niveauxMap.put(niveau, new ArrayList<>());
                }
                
                niveauxMap.get(niveau).add(quiz);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des quiz organisés !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return quizOrganises;
    }
    
    /**
     * NOUVELLE MÉTHODE : Récupère tous les quiz d'un thème spécifique
     * 
     * @param theme Le thème (ex: "Informatique Générale")
     * @return Liste des quiz du thème
     */
    public List<Quiz> listerQuizParTheme(String theme) {
        String requete = "SELECT q.*, " +
                         "(SELECT COUNT(*) FROM quiz_questions qq WHERE qq.id_quiz = q.id) AS nombre_questions " +
                         "FROM quiz q " +
                         "WHERE q.titre LIKE ? " +
                         "ORDER BY q.niveau, q.ordre_partie";
        
        List<Quiz> listeQuiz = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, theme + "%");
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                Quiz quiz = extraireQuiz(resultSet);
                listeQuiz.add(quiz);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des quiz par thème !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return listeQuiz;
    }
    
    /**
     * NOUVELLE MÉTHODE : Compte le nombre total de quiz dans un thème
     * 
     * @param theme Le thème
     * @return Nombre total de quiz dans le thème
     */
    public int compterQuizParTheme(String theme) {
        String requete = "SELECT COUNT(*) FROM quiz WHERE titre LIKE ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int nbQuiz = 0;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, theme + "%");
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                nbQuiz = resultSet.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des quiz par thème !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return nbQuiz;
    }
    
    /**
     * NOUVELLE MÉTHODE : Récupère les quiz d'un thème et d'un niveau donné
     * 
     * @param theme Le thème
     * @param niveau Le niveau (elementaire, moyen, avance)
     * @return Liste des quiz du thème et niveau
     */
    public List<Quiz> listerQuizParThemeEtNiveau(String theme, String niveau) {
        String requete = "SELECT q.*, " +
                         "(SELECT COUNT(*) FROM quiz_questions qq WHERE qq.id_quiz = q.id) AS nombre_questions " +
                         "FROM quiz q " +
                         "WHERE q.titre LIKE ? AND q.niveau = ? " +
                         "ORDER BY q.ordre_partie";
        
        List<Quiz> listeQuiz = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, theme + "%");
            preparedStatement.setString(2, niveau);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                Quiz quiz = extraireQuiz(resultSet);
                listeQuiz.add(quiz);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des quiz par thème et niveau !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return listeQuiz;
    }
    
    /**
     * NOUVELLE MÉTHODE : Retourne la prochaine partie à jouer pour un joueur
     * dans un thème et un niveau donnés
     * 
     * @param idJoueur L'identifiant du joueur
     * @param theme Le thème
     * @param niveau Le niveau
     * @return Le prochain quiz à jouer, ou null si tout est terminé
     */
    public Quiz getPartieSuivante(int idJoueur, String theme, String niveau) {
        // Récupérer tous les quiz du thème et niveau
        List<Quiz> quizDuNiveau = listerQuizParThemeEtNiveau(theme, niveau);
        
        if (quizDuNiveau.isEmpty()) {
            return null;
        }
        
        // Vérifier pour chaque quiz si la partie est déjà réussie
        ProgressionJoueurDAO progressionDAO = new ProgressionJoueurDAO();
        
        for (Quiz quiz : quizDuNiveau) {
            boolean reussie = progressionDAO.estPartieReussie(idJoueur, quiz.getId());
            if (!reussie) {
                return quiz; // Première partie non réussie trouvée
            }
        }
        
        return null; // Toutes les parties du niveau sont réussies
    }
    
    /**
     * Récupère les quiz d'un niveau spécifique
     * 
     * @param niveau Le niveau (elementaire, moyen, avance)
     * @return Liste des quiz du niveau
     */
    public List<Quiz> listerQuizParNiveau(String niveau) {
        String requete = "SELECT q.*, " +
                         "(SELECT COUNT(*) FROM quiz_questions qq WHERE qq.id_quiz = q.id) AS nombre_questions " +
                         "FROM quiz q WHERE q.niveau = ? " +
                         "ORDER BY q.ordre_partie";
        
        List<Quiz> listeQuiz = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, niveau);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                Quiz quiz = extraireQuiz(resultSet);
                listeQuiz.add(quiz);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des quiz par niveau !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return listeQuiz;
    }
    
    /**
     * Récupère les quiz d'une catégorie et d'un niveau
     * 
     * @param categorie La catégorie (ex: "Informatique Générale")
     * @param niveau Le niveau (elementaire, moyen, avance)
     * @return Liste des quiz
     */
    public List<Quiz> listerQuizParCategorieEtNiveau(String categorie, String niveau) {
        String requete = "SELECT q.*, " +
                         "(SELECT COUNT(*) FROM quiz_questions qq WHERE qq.id_quiz = q.id) AS nombre_questions " +
                         "FROM quiz q WHERE q.titre LIKE ? AND q.niveau = ? " +
                         "ORDER BY q.ordre_partie";
        
        List<Quiz> listeQuiz = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, categorie + "%");
            preparedStatement.setString(2, niveau);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                Quiz quiz = extraireQuiz(resultSet);
                listeQuiz.add(quiz);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des quiz par catégorie et niveau !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return listeQuiz;
    }
    
    /**
     * Récupère un quiz par son ID avec son nombre de questions
     * 
     * @param id L'identifiant du quiz
     * @return Le quiz trouvé ou null
     */
    public Quiz trouverParId(int id) {
        String requete = "SELECT q.*, " +
                         "(SELECT COUNT(*) FROM quiz_questions qq WHERE qq.id_quiz = q.id) AS nombre_questions " +
                         "FROM quiz q " +
                         "WHERE q.id = ?";
        
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Quiz quiz = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                quiz = extraireQuiz(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du quiz par ID !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return quiz;
    }
    
    /**
     * Récupère le quiz suivant dans la progression (même catégorie, même niveau, partie + 1)
     * 
     * @param idQuizActuel L'identifiant du quiz actuel
     * @return Le quiz suivant ou null si c'est le dernier
     */
    public Quiz getQuizSuivant(int idQuizActuel) {
        Quiz quizActuel = trouverParId(idQuizActuel);
        if (quizActuel == null) {
            return null;
        }
        
        // Extraire la catégorie du titre (partie avant " - ")
        String titreActuel = quizActuel.getTitre();
        String categorie = titreActuel.substring(0, titreActuel.indexOf(" - "));
        String niveau = quizActuel.getNiveau();
        int partieSuivante = quizActuel.getOrdrePartie() + 1;
        
        String requete = "SELECT q.*, " +
                         "(SELECT COUNT(*) FROM quiz_questions qq WHERE qq.id_quiz = q.id) AS nombre_questions " +
                         "FROM quiz q WHERE q.titre LIKE ? AND q.niveau = ? AND q.ordre_partie = ?";
        
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Quiz quiz = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, categorie + "%");
            preparedStatement.setString(2, niveau);
            preparedStatement.setInt(3, partieSuivante);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                quiz = extraireQuiz(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du quiz suivant !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return quiz;
    }
    
    /**
     * Récupère la première partie du niveau supérieur
     * 
     * @param categorie La catégorie
     * @param niveauSuperieur Le niveau supérieur (moyen ou avance)
     * @return Le premier quiz du niveau supérieur ou null
     */
    public Quiz getPremierQuizNiveau(String categorie, String niveauSuperieur) {
        String requete = "SELECT q.*, " +
                         "(SELECT COUNT(*) FROM quiz_questions qq WHERE qq.id_quiz = q.id) AS nombre_questions " +
                         "FROM quiz q WHERE q.titre LIKE ? AND q.niveau = ? AND q.ordre_partie = 1";
        
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Quiz quiz = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, categorie + "%");
            preparedStatement.setString(2, niveauSuperieur);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                quiz = extraireQuiz(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du premier quiz du niveau !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return quiz;
    }
    
    /**
     * Compte le nombre total de parties dans un niveau pour une catégorie
     * 
     * @param categorie La catégorie
     * @param niveau Le niveau
     * @return Le nombre de parties
     */
    public int compterPartiesParNiveau(String categorie, String niveau) {
        String requete = "SELECT COUNT(*) FROM quiz WHERE titre LIKE ? AND niveau = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int nbParties = 0;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, categorie + "%");
            preparedStatement.setString(2, niveau);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                nbParties = resultSet.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des parties par niveau !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return nbParties;
    }
    
    /**
     * Compte le nombre de questions dans un quiz
     * 
     * @param idQuiz L'identifiant du quiz
     * @return Le nombre de questions
     */
    public int compterQuestionsParQuiz(int idQuiz) {
        String requete = "SELECT COUNT(*) FROM quiz_questions WHERE id_quiz = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int nbQuestions = 0;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, idQuiz);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                nbQuestions = resultSet.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des questions du quiz !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return nbQuestions;
    }
    
    /**
     * Ajoute un nouveau quiz dans la base de données
     * 
     * @param quiz Le quiz à ajouter
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean ajouter(Quiz quiz) {
        String requete = "INSERT INTO quiz (titre, description, duree_max_minutes, niveau, ordre_partie, date_creation) VALUES (?, ?, ?, ?, ?, ?)";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, quiz.getTitre());
            preparedStatement.setString(2, quiz.getDescription());
            preparedStatement.setInt(3, quiz.getDureeMaxMinutes());
            preparedStatement.setString(4, quiz.getNiveau());
            preparedStatement.setInt(5, quiz.getOrdrePartie());
            preparedStatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            
            int lignesInserees = preparedStatement.executeUpdate();
            
            return lignesInserees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du quiz !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }
        
        return false;
    }
    
    /**
     * Modifie un quiz existant
     * 
     * @param quiz Le quiz à modifier (avec son id)
     * @return true si la modification a réussi, false sinon
     */
    public boolean modifier(Quiz quiz) {
        String requete = "UPDATE quiz SET titre = ?, description = ?, duree_max_minutes = ?, niveau = ?, ordre_partie = ? WHERE id = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, quiz.getTitre());
            preparedStatement.setString(2, quiz.getDescription());
            preparedStatement.setInt(3, quiz.getDureeMaxMinutes());
            preparedStatement.setString(4, quiz.getNiveau());
            preparedStatement.setInt(5, quiz.getOrdrePartie());
            preparedStatement.setInt(6, quiz.getId());
            
            int lignesModifiees = preparedStatement.executeUpdate();
            
            return lignesModifiees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification du quiz !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }
        
        return false;
    }
    
    /**
     * Supprime un quiz par son ID
     * 
     * @param id L'identifiant du quiz à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean supprimer(int id) {
        String requete = "DELETE FROM quiz WHERE id = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, id);
            
            int lignesSupprimees = preparedStatement.executeUpdate();
            
            return lignesSupprimees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du quiz !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }
        
        return false;
    }
    
    /**
     * Méthode utilitaire pour extraire un objet Quiz d'un ResultSet
     */
    private Quiz extraireQuiz(ResultSet resultSet) throws SQLException {
        Quiz quiz = new Quiz();
        quiz.setId(resultSet.getInt("id"));
        quiz.setTitre(resultSet.getString("titre"));
        quiz.setDescription(resultSet.getString("description"));
        quiz.setDureeMaxMinutes(resultSet.getInt("duree_max_minutes"));
        quiz.setNiveau(resultSet.getString("niveau"));
        quiz.setOrdrePartie(resultSet.getInt("ordre_partie"));
        quiz.setDateCreation(resultSet.getTimestamp("date_creation"));
        quiz.setNombreQuestions(resultSet.getInt("nombre_questions"));
        return quiz;
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