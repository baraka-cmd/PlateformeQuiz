package com.amijayquiz.dao;

import com.amijayquiz.models.Utilisateur;
import com.amijayquiz.utils.ConnexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {
    
    /**
     * Vérifie si un pseudo existe déjà dans la base de données
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
            fermerRessources(preparedStatement, resultSet);
        }
        
        return false;
    }
    
    /**
     * Vérifie si un email existe déjà dans la base de données
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
            fermerRessources(preparedStatement, resultSet);
        }
        
        return false;
    }
    
    /**
     * Inscrit un nouvel utilisateur dans la base de données
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
            fermerRessources(preparedStatement, null);
        }
        
        return false;
    }
    
    /**
     * Trouve un utilisateur par son ID
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
            fermerRessources(preparedStatement, resultSet);
        }

        return utilisateur;
    }
    
    /**
     * Récupère un utilisateur par son pseudo
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
            fermerRessources(preparedStatement, resultSet);
        }
        
        return utilisateur;
    }
    
    /**
     * Récupère un utilisateur par son email
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
            fermerRessources(preparedStatement, resultSet);
        }
        
        return utilisateur;
    }
    
    /**
     * Connecte un utilisateur en vérifiant son pseudo et mot de passe
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
            fermerRessources(preparedStatement, resultSet);
        }
        
        return utilisateur;
    }
    
    /**
     * Connecte un utilisateur en vérifiant son email et mot de passe
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
            fermerRessources(preparedStatement, resultSet);
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
        
        String email = resultSet.getString("email");
        if (email != null) {
            utilisateur.setEmail(email);
        }
        
        return utilisateur;
    }
    
    /**
     * Modifie le mot de passe d'un utilisateur
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
            fermerRessources(preparedStatement, null);
        }
        
        return false;
    }

    /**
     * Met à jour le token "Se souvenir de moi" pour un utilisateur
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
            fermerRessources(preparedStatement, null);
        }
        
        return false;
    }
    
    /**
     * Supprime le token "Se souvenir de moi" (déconnexion)
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
            fermerRessources(preparedStatement, null);
        }
        
        return false;
    }
    
    /**
     * Trouve un utilisateur par son token "Se souvenir de moi"
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
            fermerRessources(preparedStatement, resultSet);
        }
        
        return utilisateur;
    }

    // ============================================================
    // MÉTHODES ADMIN UTILISATEURS
    // ============================================================

    /**
     * Liste tous les utilisateurs avec pagination, filtres et recherche
     * VERSION CORRIGÉE - Sans sous-requêtes complexes
     */
    public List<Utilisateur> listerTous(int offset, int limite, String recherche, 
                                         String filtreRole, String filtreStatut, 
                                         String filtrePremium, String tri, String ordre) {
        List<Utilisateur> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return liste;

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM utilisateurs WHERE 1=1 ");

            List<Object> params = new ArrayList<>();

            // Recherche textuelle
            if (recherche != null && !recherche.trim().isEmpty()) {
                sql.append("AND (pseudo LIKE ? OR email LIKE ?) ");
                String filtre = "%" + recherche.trim() + "%";
                params.add(filtre);
                params.add(filtre);
            }

            // Filtre par rôle
            if (filtreRole != null && !"tous".equals(filtreRole)) {
                sql.append("AND role = ? ");
                params.add(filtreRole);
            }

            // Filtre par statut
            if (filtreStatut != null && !"tous".equals(filtreStatut)) {
                if ("actif".equals(filtreStatut)) {
                    sql.append("AND actif = TRUE ");
                } else if ("inactif".equals(filtreStatut)) {
                    sql.append("AND actif = FALSE ");
                }
            }

            // Tri sécurisé (whitelist)
            String colonneTri = "id";
            if (tri != null) {
                switch (tri) {
                    case "pseudo":  colonneTri = "pseudo"; break;
                    case "email":   colonneTri = "email"; break;
                    case "role":    colonneTri = "role"; break;
                    case "date_inscription": colonneTri = "date_inscription"; break;
                    default:        colonneTri = "id";
                }
            }
            String direction = "DESC".equalsIgnoreCase(ordre) ? "DESC" : "ASC";
            sql.append("ORDER BY ").append(colonneTri).append(" ").append(direction).append(" ");

            // Pagination
            sql.append("LIMIT ? OFFSET ?");
            params.add(limite);
            params.add(offset);

            // Exécution
            ps = connexion.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            rs = ps.executeQuery();

            while (rs.next()) {
                liste.add(extraireUtilisateur(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ [UtilisateurDAO] Erreur listerTous : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(ps, rs);
        }

        return liste;
    }

    /**
     * Compte le nombre total d'utilisateurs selon les filtres
     * VERSION CORRIGÉE - Sans sous-requêtes complexes
     */
    public int compterTous(String recherche, String filtreRole, 
                            String filtreStatut, String filtrePremium) {
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(*) FROM utilisateurs WHERE 1=1 ");

            List<Object> params = new ArrayList<>();

            if (recherche != null && !recherche.trim().isEmpty()) {
                sql.append("AND (pseudo LIKE ? OR email LIKE ?) ");
                String filtre = "%" + recherche.trim() + "%";
                params.add(filtre);
                params.add(filtre);
            }

            if (filtreRole != null && !"tous".equals(filtreRole)) {
                sql.append("AND role = ? ");
                params.add(filtreRole);
            }

            if (filtreStatut != null && !"tous".equals(filtreStatut)) {
                if ("actif".equals(filtreStatut)) {
                    sql.append("AND actif = TRUE ");
                } else if ("inactif".equals(filtreStatut)) {
                    sql.append("AND actif = FALSE ");
                }
            }

            ps = connexion.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ [UtilisateurDAO] Erreur compterTous : " + e.getMessage());
        } finally {
            fermerRessources(ps, rs);
        }

        return 0;
    }

    /**
     * Active le compte d'un utilisateur
     */
    public boolean activer(int id) {
        Connection connexion = null;
        PreparedStatement ps = null;
        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;
            ps = connexion.prepareStatement("UPDATE utilisateurs SET actif = TRUE WHERE id = ?");
            ps.setInt(1, id);
            int lignes = ps.executeUpdate();
            if (lignes > 0) {
                System.out.println("✅ [UtilisateurDAO] Compte activé : ID=" + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ [UtilisateurDAO] Erreur activation : " + e.getMessage());
        } finally {
            fermerRessources(ps, null);
        }
        return false;
    }

    /**
     * Désactive le compte d'un utilisateur
     */
    public boolean desactiver(int id) {
        Connection connexion = null;
        PreparedStatement ps = null;
        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;
            ps = connexion.prepareStatement("UPDATE utilisateurs SET actif = FALSE WHERE id = ?");
            ps.setInt(1, id);
            int lignes = ps.executeUpdate();
            if (lignes > 0) {
                System.out.println("✅ [UtilisateurDAO] Compte désactivé : ID=" + id);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ [UtilisateurDAO] Erreur désactivation : " + e.getMessage());
        } finally {
            fermerRessources(ps, null);
        }
        return false;
    }

    /**
     * Change le rôle d'un utilisateur (admin ↔ joueur)
     */
    public boolean changerRole(int id, String nouveauRole) {
    // Accepter "admin", "joueur" ET "gestionnaire"
    if (!"admin".equals(nouveauRole) && !"joueur".equals(nouveauRole) && !"gestionnaire".equals(nouveauRole)) {
        System.err.println("❌ [UtilisateurDAO] Rôle invalide : " + nouveauRole);
        return false;
    }
    Connection connexion = null;
    PreparedStatement ps = null;
    try {
        connexion = ConnexionDB.getConnexion();
        if (connexion == null) return false;
        ps = connexion.prepareStatement("UPDATE utilisateurs SET role = ? WHERE id = ?");
        ps.setString(1, nouveauRole);
        ps.setInt(2, id);
        int lignes = ps.executeUpdate();
        if (lignes > 0) {
            System.out.println("✅ [UtilisateurDAO] Rôle changé : ID=" + id + " → " + nouveauRole);
            return true;
        }
    } catch (SQLException e) {
        System.err.println("❌ [UtilisateurDAO] Erreur changement rôle : " + e.getMessage());
    } finally {
        fermerRessources(ps, null);
    }
    return false;
}

    /**
     * Compte les utilisateurs par rôle
     */
    public int compterParRole(String role) {
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;
            ps = connexion.prepareStatement("SELECT COUNT(*) FROM utilisateurs WHERE role = ?");
            ps.setString(1, role);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("❌ [UtilisateurDAO] Erreur compterParRole : " + e.getMessage());
        } finally {
            fermerRessources(ps, rs);
        }
        return 0;
    }

    /**
     * Compte les utilisateurs par statut
     */
    public int compterParStatut(boolean actif) {
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;
            ps = connexion.prepareStatement("SELECT COUNT(*) FROM utilisateurs WHERE actif = ?");
            ps.setBoolean(1, actif);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("❌ [UtilisateurDAO] Erreur compterParStatut : " + e.getMessage());
        } finally {
            fermerRessources(ps, rs);
        }
        return 0;
    }

    /**
     * Ferme les ressources JDBC (NE FERME PAS LA CONNEXION)
     */
    private void fermerRessources(PreparedStatement preparedStatement, ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture du ResultSet !");
            }
        }
        
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture du PreparedStatement !");
            }
        }
        // NOTE : La connexion n'est JAMAIS fermée ici car elle est partagée (Singleton)
    }
}