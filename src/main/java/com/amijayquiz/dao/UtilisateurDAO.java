package com.amijayquiz.dao;

import com.amijayquiz.models.Utilisateur;
import com.amijayquiz.utils.ConnexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UtilisateurDAO {
    
    /**
     * Vérifie si un pseudo existe déjà dans la base de données
     * 
     * @param pseudo Le pseudo à vérifier
     * @return true si le pseudo existe déjà, false sinon
     */
    public boolean existePseudo(String pseudo) {
        String requete = "SELECT COUNT(*) FROM utilisateurs WHERE pseudo = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, pseudo);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                int compteur = resultSet.getInt(1);
                return compteur > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du pseudo !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return false;
    }
    
    /**
     * Vérifie si un email existe déjà dans la base de données
     * 
     * @param email L'email à vérifier
     * @return true si l'email existe déjà, false sinon
     */
    public boolean existeEmail(String email) {
        String requete = "SELECT COUNT(*) FROM utilisateurs WHERE email = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                int compteur = resultSet.getInt(1);
                return compteur > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'email !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return false;
    }
    
    /**
     * Inscrit un nouvel utilisateur dans la base de données
     * 
     * @param utilisateur L'utilisateur à inscrire (pseudo, email, motDePasse déjà hashé, role)
     * @return true si l'inscription a réussi, false sinon
     */
    public boolean inscrire(Utilisateur utilisateur) {
        String requete = "INSERT INTO utilisateurs (pseudo, email, mot_de_passe, role, date_inscription, actif) VALUES (?, ?, ?, ?, ?, ?)";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, utilisateur.getPseudo());
            preparedStatement.setString(2, utilisateur.getEmail());
            preparedStatement.setString(3, utilisateur.getMotDePasse());
            preparedStatement.setString(4, utilisateur.getRole());
            preparedStatement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setBoolean(6, utilisateur.isActif());
            
            int lignesInserees = preparedStatement.executeUpdate();
            
            return lignesInserees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'inscription de l'utilisateur !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }
        
        return false;
    }
    
    
    /**
 * Trouve un utilisateur par son ID
 * 
 * @param id L'ID de l'utilisateur
 * @return L'utilisateur trouvé ou null
 */
public Utilisateur trouverParId(int id) {
    String requete = "SELECT * FROM utilisateurs WHERE id = ?";
    Connection connexion = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    Utilisateur utilisateur = null;

    try {
        connexion = ConnexionDB.getConnexion();
        preparedStatement = connexion.prepareStatement(requete);
        preparedStatement.setInt(1, id);
        resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            utilisateur = extraireUtilisateur(resultSet);
        }

    } catch (SQLException e) {
        System.err.println("❌ Erreur : recherche utilisateur ID " + id);
        System.err.println("   Message : " + e.getMessage());
        e.printStackTrace();
    } finally {
        fermerRessources(null, preparedStatement, resultSet);
    }

    return utilisateur;
}
    
    /**
     * Récupère un utilisateur par son pseudo
     * 
     * @param pseudo Le pseudo de l'utilisateur recherché
     * @return L'utilisateur trouvé ou null si non trouvé
     */
    public Utilisateur trouverParPseudo(String pseudo) {
        String requete = "SELECT * FROM utilisateurs WHERE pseudo = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Utilisateur utilisateur = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, pseudo);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                utilisateur = extraireUtilisateur(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de l'utilisateur par pseudo !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return utilisateur;
    }
    
    /**
     * Récupère un utilisateur par son email
     * 
     * @param email L'email de l'utilisateur recherché
     * @return L'utilisateur trouvé ou null si non trouvé
     */
    public Utilisateur trouverParEmail(String email) {
        String requete = "SELECT * FROM utilisateurs WHERE email = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Utilisateur utilisateur = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                utilisateur = extraireUtilisateur(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de l'utilisateur par email !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return utilisateur;
    }
    
    /**
     * Connecte un utilisateur en vérifiant son pseudo et mot de passe
     * (Gardé pour compatibilité avec l'admin)
     * 
     * @param pseudo Le pseudo de l'utilisateur
     * @param motDePasseHashe Le mot de passe déjà hashé en SHA-256
     * @return L'utilisateur connecté ou null si échec
     */
    public Utilisateur connecter(String pseudo, String motDePasseHashe) {
        String requete = "SELECT * FROM utilisateurs WHERE pseudo = ? AND mot_de_passe = ? AND actif = 1";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Utilisateur utilisateur = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, pseudo);
            preparedStatement.setString(2, motDePasseHashe);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                utilisateur = extraireUtilisateur(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion de l'utilisateur (pseudo) !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return utilisateur;
    }
    
    /**
     * Connecte un utilisateur en vérifiant son email et mot de passe
     * 
     * @param email L'email de l'utilisateur
     * @param motDePasseHashe Le mot de passe déjà hashé en SHA-256
     * @return L'utilisateur connecté ou null si échec
     */
    public Utilisateur connecterParEmail(String email, String motDePasseHashe) {
        String requete = "SELECT * FROM utilisateurs WHERE email = ? AND mot_de_passe = ? AND actif = 1";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Utilisateur utilisateur = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, motDePasseHashe);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                utilisateur = extraireUtilisateur(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion de l'utilisateur (email) !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return utilisateur;
    }
    
    /**
     * Extrait un objet Utilisateur d'un ResultSet
     */
    private Utilisateur extraireUtilisateur(ResultSet resultSet) throws SQLException {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(resultSet.getInt("id"));
        utilisateur.setPseudo(resultSet.getString("pseudo"));
        utilisateur.setMotDePasse(resultSet.getString("mot_de_passe"));
        utilisateur.setRole(resultSet.getString("role"));
        utilisateur.setDateInscription(resultSet.getTimestamp("date_inscription"));
        utilisateur.setActif(resultSet.getBoolean("actif"));
        
        // Récupérer l'email (peut être null)
        String email = resultSet.getString("email");
        if (email != null) {
            utilisateur.setEmail(email);
        }
        
        return utilisateur;
    }
    
    
    /**
 * Modifie le mot de passe d'un utilisateur
 * 
 * @param idUtilisateur L'identifiant de l'utilisateur
 * @param nouveauMotDePasseHashe Le nouveau mot de passe déjà hashé
 * @return true si la modification a réussi, false sinon
 */
public boolean modifierMotDePasse(int idUtilisateur, String nouveauMotDePasseHashe) {
    String requete = "UPDATE utilisateurs SET mot_de_passe = ? WHERE id = ?";
    Connection connexion = null;
    PreparedStatement preparedStatement = null;
    
    try {
        connexion = ConnexionDB.getConnexion();
        preparedStatement = connexion.prepareStatement(requete);
        preparedStatement.setString(1, nouveauMotDePasseHashe);
        preparedStatement.setInt(2, idUtilisateur);
        
        int lignesModifiees = preparedStatement.executeUpdate();
        
        return lignesModifiees > 0;
        
    } catch (SQLException e) {
        System.err.println("Erreur lors de la modification du mot de passe !");
        System.err.println("Message : " + e.getMessage());
        e.printStackTrace();
    } finally {
        fermerRessources(null, preparedStatement, null);
    }
    
    return false;
}



// ============================================================
// FICHIER : src/main/java/com/amijayquiz/dao/UtilisateurDAO.java
// MÉTHODES À AJOUTER dans la classe existante
// ============================================================

    /**
     * Met à jour le token "Se souvenir de moi" pour un utilisateur
     * 
     * @param idUtilisateur L'ID de l'utilisateur
     * @param token         Le token généré (UUID hashé)
     * @return true si la mise à jour a réussi
     */
    public boolean mettreAJourRememberToken(int idUtilisateur, String token) {
        String requete = "UPDATE utilisateurs SET remember_token = ? WHERE id = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, token);
            preparedStatement.setInt(2, idUtilisateur);
            
            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du remember token !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }
        
        return false;
    }
    
    /**
     * Supprime le token "Se souvenir de moi" (déconnexion)
     * 
     * @param idUtilisateur L'ID de l'utilisateur
     * @return true si la mise à jour a réussi
     */
    public boolean supprimerRememberToken(int idUtilisateur) {
        String requete = "UPDATE utilisateurs SET remember_token = NULL WHERE id = ?";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setInt(1, idUtilisateur);
            
            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du remember token !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }
        
        return false;
    }
    
    /**
     * Trouve un utilisateur par son token "Se souvenir de moi"
     * 
     * @param token Le token stocké dans le cookie
     * @return L'utilisateur trouvé ou null
     */
    public Utilisateur trouverParRememberToken(String token) {
        String requete = "SELECT * FROM utilisateurs WHERE remember_token = ? AND actif = 1";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Utilisateur utilisateur = null;
        
        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(requete);
            preparedStatement.setString(1, token);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                utilisateur = extraireUtilisateur(resultSet);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par remember token !");
            System.err.println("Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }
        
        return utilisateur;
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