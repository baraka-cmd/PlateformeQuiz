// ============================================================
// FICHIER : src/main/java/com/LearnPath/dao/ThemeDAO.java
// RÔLE    : Accès aux données de la table "themes"
//           CRUD complet
// ============================================================

package com.learnpath.dao;

import com.learnpath.models.Theme;
import com.learnpath.utils.ConnexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * ThemeDAO - Data Access Object pour les thèmes de cours
 * 
 * Gère toutes les opérations CRUD sur la table "themes".
 * 
 * @author LearnPath
 * @version 1.0
 */
public class ThemeDAO {

    // ===== CONSTANTES DE REQUÊTES SQL =====
    private static final String SELECT_TOUS = 
        "SELECT * FROM themes ORDER BY ordre ASC, nom ASC";
    
    private static final String SELECT_PAR_ID = 
        "SELECT * FROM themes WHERE id = ?";
    
    private static final String SELECT_PAR_NOM = 
        "SELECT * FROM themes WHERE nom = ?";
    
    private static final String INSERT = 
        "INSERT INTO themes (nom, description, icone, couleur, ordre, date_creation) VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE = 
        "UPDATE themes SET nom = ?, description = ?, icone = ?, couleur = ?, ordre = ? WHERE id = ?";
    
    private static final String DELETE = 
        "DELETE FROM themes WHERE id = ?";
    
    private static final String COUNT_PAR_THEME = 
        "SELECT COUNT(*) FROM cours WHERE theme = ? AND est_actif = TRUE";
    
    private static final String MAX_ORDRE = 
        "SELECT MAX(ordre) FROM themes";

    // ===== MÉTHODES DE LECTURE =====

    /**
     * Liste tous les thèmes triés par ordre puis par nom
     * 
     * @return Liste de tous les thèmes
     */
    public List<Theme> listerTous() {
        List<Theme> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return liste;

            preparedStatement = connexion.prepareStatement(SELECT_TOUS);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                liste.add(extraireTheme(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("❌ [ThemeDAO] Erreur liste thèmes : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Trouve un thème par son ID
     * 
     * @param id ID du thème
     * @return Le thème trouvé ou null
     */
    public Theme trouverParId(int id) {
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
                return extraireTheme(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ [ThemeDAO] Erreur recherche thème ID " + id + " : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Trouve un thème par son nom (insensible à la casse)
     * 
     * @param nom Nom du thème
     * @return Le thème trouvé ou null
     */
    public Theme trouverParNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) return null;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return null;

            preparedStatement = connexion.prepareStatement(SELECT_PAR_NOM);
            preparedStatement.setString(1, nom.trim());
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return extraireTheme(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ [ThemeDAO] Erreur recherche thème '" + nom + "' : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    // ===== MÉTHODES D'ÉCRITURE =====

    /**
     * Ajoute un nouveau thème
     * 
     * @param theme Le thème à ajouter
     * @return true si l'ajout a réussi
     */
    public boolean ajouter(Theme theme) {
    if (theme == null) {
        System.out.println("⚠️ [ThemeDAO] ajouter() : theme est null");
        return false;
    }
    if (theme.getNom() == null || theme.getNom().trim().isEmpty()) {
        System.out.println("⚠️ [ThemeDAO] ajouter() : nom vide");
        return false;
    }

    // Vérifier que le nom n'existe pas déjà
    Theme existant = trouverParNom(theme.getNom().trim());
    if (existant != null) {
        System.err.println("⚠️ [ThemeDAO] Un thème avec ce nom existe déjà : " + theme.getNom());
        return false;
    }

    Connection connexion = null;
    PreparedStatement preparedStatement = null;

    try {
        connexion = ConnexionDB.getConnexion();
        if (connexion == null) {
            System.out.println("⚠️ [ThemeDAO] ajouter() : connexion BDD null");
            return false;
        }

        preparedStatement = connexion.prepareStatement(INSERT);
        preparedStatement.setString(1, theme.getNom().trim());
        preparedStatement.setString(2, theme.getDescription());
        preparedStatement.setString(3, theme.getIcone());
        preparedStatement.setString(4, theme.getCouleur());
        preparedStatement.setInt(5, theme.getOrdre());
        preparedStatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

        int lignes = preparedStatement.executeUpdate();

        System.out.println("📋 [ThemeDAO] ajouter() : " + lignes + " ligne(s) insérée(s)");

        if (lignes > 0) {
            System.out.println("✅ [ThemeDAO] Thème ajouté : " + theme.getNom());
            return true;
        } else {
            System.out.println("⚠️ [ThemeDAO] ajouter() : executeUpdate a retourné 0");
        }

    } catch (SQLException e) {
        System.err.println("❌ [ThemeDAO] Erreur ajout thème : " + e.getMessage());
        e.printStackTrace();
    } finally {
        fermerRessources(null, preparedStatement, null);
    }

    return false;
}
    /**
     * Modifie un thème existant
     * 
     * @param theme Le thème à modifier (avec ID)
     * @return true si la modification a réussi
     */
    public boolean modifier(Theme theme) {
        if (theme == null || theme.getId() <= 0) return false;
        if (theme.getNom() == null || theme.getNom().trim().isEmpty()) return false;

        // Vérifier que le nouveau nom n'existe pas déjà (sauf pour ce thème)
        Theme existant = trouverParNom(theme.getNom().trim());
        if (existant != null && existant.getId() != theme.getId()) {
            System.err.println("⚠️ [ThemeDAO] Un autre thème utilise déjà ce nom : " + theme.getNom());
            return false;
        }

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(UPDATE);
            preparedStatement.setString(1, theme.getNom().trim());
            preparedStatement.setString(2, theme.getDescription());
            preparedStatement.setString(3, theme.getIcone());
            preparedStatement.setString(4, theme.getCouleur());
            preparedStatement.setInt(5, theme.getOrdre());
            preparedStatement.setInt(6, theme.getId());

            int lignes = preparedStatement.executeUpdate();

            if (lignes > 0) {
                System.out.println("✅ [ThemeDAO] Thème modifié : " + theme.getNom());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ [ThemeDAO] Erreur modification thème ID " + theme.getId() + " : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Supprime un thème par son ID
     * Vérifie qu'aucun cours n'est associé à ce thème avant suppression
     * 
     * @param id ID du thème à supprimer
     * @return true si la suppression a réussi
     */
    public boolean supprimer(int id) {
        if (id <= 0) return false;

        // Vérifier si des cours utilisent ce thème
        Theme theme = trouverParId(id);
        if (theme == null) return false;

        if (compterCoursParTheme(theme.getNom()) > 0) {
            System.err.println("⚠️ [ThemeDAO] Impossible de supprimer : des cours utilisent ce thème.");
            return false;
        }

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(DELETE);
            preparedStatement.setInt(1, id);

            int lignes = preparedStatement.executeUpdate();

            if (lignes > 0) {
                System.out.println("✅ [ThemeDAO] Thème supprimé ID : " + id);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ [ThemeDAO] Erreur suppression thème ID " + id + " : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    // ===== MÉTHODES DE COMPTAGE =====

    /**
     * Compte le nombre de cours associés à un thème
     * 
     * @param nomTheme Nom du thème
     * @return Nombre de cours utilisant ce thème
     */
    public int compterCoursParTheme(String nomTheme) {
        if (nomTheme == null || nomTheme.isEmpty()) return 0;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;

            preparedStatement = connexion.prepareStatement(COUNT_PAR_THEME);
            preparedStatement.setString(1, nomTheme);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ [ThemeDAO] Erreur comptage cours thème '" + nomTheme + "' : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    /**
     * Retourne l'ordre maximum actuel (pour auto-incrémenter)
     * 
     * @return Valeur max de l'ordre, ou 0 si aucun thème
     */
    public int getOrdreMaximum() {
        Connection connexion = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;

            statement = connexion.createStatement();
            resultSet = statement.executeQuery(MAX_ORDRE);

            if (resultSet.next()) {
                int max = resultSet.getInt(1);
                return resultSet.wasNull() ? 0 : max;
            }

        } catch (SQLException e) {
            System.err.println("❌ [ThemeDAO] Erreur ordre maximum : " + e.getMessage());
        } finally {
            fermerRessources(null, 
                statement instanceof PreparedStatement ? (PreparedStatement) statement : null, 
                resultSet);
        }

        return 0;
    }

    // ===== MÉTHODES PRIVÉES =====

    /**
     * Extrait un thème d'un ResultSet
     */
    private Theme extraireTheme(ResultSet resultSet) throws SQLException {
        Theme theme = new Theme();
        theme.setId(resultSet.getInt("id"));
        theme.setNom(resultSet.getString("nom"));
        theme.setDescription(resultSet.getString("description"));
        theme.setIcone(resultSet.getString("icone"));
        theme.setCouleur(resultSet.getString("couleur"));
        theme.setOrdre(resultSet.getInt("ordre"));
        theme.setDateCreation(resultSet.getTimestamp("date_creation"));
        return theme;
    }

    /**
     * Ferme les ressources JDBC
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