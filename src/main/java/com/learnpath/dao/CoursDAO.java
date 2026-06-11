// ============================================================
// FICHIER : src/main/java/com/LearnPath/dao/CoursDAO.java
// RÔLE    : Accès aux données de la table "cours"
//           CRUD complet + filtrage + statistiques + recherche
// ============================================================

package com.learnpath.dao;

import com.learnpath.models.Chapitre;
import com.learnpath.models.Cours;
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

/**
 * CoursDAO - Data Access Object pour les cours
 * 
 * Gère toutes les opérations CRUD sur la table "cours".
 * Charge également les chapitres et leçons associés.
 * Fournit des méthodes de filtrage, recherche et statistiques.
 * 
 * @author LearnPath
 * @version 1.0
 */
public class CoursDAO {

    // ===== CONSTANTES DE REQUÊTES SQL =====
    private static final String SELECT_TOUS = 
        "SELECT * FROM cours ORDER BY theme, niveau, ordre";
    
    private static final String SELECT_PUBLIES = 
        "SELECT * FROM cours WHERE statut = 'publié' ORDER BY theme, niveau, ordre";
    
    private static final String SELECT_PAR_THEME = 
        "SELECT * FROM cours WHERE theme = ? ORDER BY niveau, ordre";
    
    private static final String SELECT_PAR_THEME_NIVEAU = 
        "SELECT * FROM cours WHERE theme = ? AND niveau = ? ORDER BY ordre";
    
    private static final String SELECT_PAR_NIVEAU = 
        "SELECT * FROM cours WHERE niveau = ? ORDER BY theme, ordre";
    
    private static final String SELECT_PAR_ID = 
        "SELECT * FROM cours WHERE id = ?";
    
    private static final String SELECT_RECHERCHE = 
        "SELECT * FROM cours WHERE titre LIKE ? OR description LIKE ? OR theme LIKE ? ORDER BY theme, niveau";
    
    private static final String INSERT = 
        "INSERT INTO cours (theme, niveau, titre, description, objectifs, prerequis, image, " +
        "duree_estimee, nombre_chapitres, nombre_lecons, ordre, statut, date_creation, date_maj) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE = 
        "UPDATE cours SET theme = ?, niveau = ?, titre = ?, description = ?, objectifs = ?, " +
        "prerequis = ?, image = ?, duree_estimee = ?, nombre_chapitres = ?, nombre_lecons = ?, " +
        "ordre = ?, statut = ?, date_maj = ? WHERE id = ?";
    
    private static final String DELETE = 
        "DELETE FROM cours WHERE id = ?";
    
    private static final String COUNT_TOUS = 
        "SELECT COUNT(*) FROM cours";
    
    private static final String COUNT_PAR_THEME = 
        "SELECT COUNT(*) FROM cours WHERE theme = ?";
    
    private static final String SELECT_THEMES_DISTINCTS = 
        "SELECT DISTINCT theme FROM cours ORDER BY theme";
    
    private static final String SELECT_STATS_GLOBALES = 
        "SELECT theme, niveau, COUNT(*) AS nb_cours FROM cours GROUP BY theme, niveau ORDER BY theme, niveau";

    // ===== MÉTHODES DE LECTURE =====

    /**
     * Liste tous les cours (tous statuts confondus)
     * 
     * @return Liste de tous les cours
     */
    public List<Cours> listerTous() {
        List<Cours> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_TOUS);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                liste.add(extraireCours(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : liste de tous les cours");
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Liste uniquement les cours publiés
     * 
     * @return Liste des cours publiés
     */
    public List<Cours> listerPubliés() {
        List<Cours> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PUBLIES);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                liste.add(extraireCours(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : liste des cours publiés");
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Liste les cours d'un thème spécifique
     * 
     * @param theme Le thème (ex: "Développement Web")
     * @return Liste des cours du thème
     */
    public List<Cours> listerParTheme(String theme) {
        List<Cours> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_THEME);
            preparedStatement.setString(1, theme);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                liste.add(extraireCours(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : liste cours thème " + theme);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Liste les cours d'un thème et niveau spécifiques
     * 
     * @param theme  Le thème
     * @param niveau Le niveau (elementaire, moyen, avance)
     * @return Liste des cours correspondants
     */
    public List<Cours> listerParThemeEtNiveau(String theme, String niveau) {
        List<Cours> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_THEME_NIVEAU);
            preparedStatement.setString(1, theme);
            preparedStatement.setString(2, niveau);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                liste.add(extraireCours(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : liste cours " + theme + " / " + niveau);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Liste les cours d'un niveau spécifique (tous thèmes)
     * 
     * @param niveau Le niveau
     * @return Liste des cours du niveau
     */
    public List<Cours> listerParNiveau(String niveau) {
        List<Cours> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_NIVEAU);
            preparedStatement.setString(1, niveau);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                liste.add(extraireCours(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : liste cours niveau " + niveau);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Trouve un cours par son ID et charge ses chapitres avec leçons
     * 
     * @param id ID du cours
     * @return Le cours trouvé avec ses chapitres, ou null
     */
    public Cours trouverParId(int id) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_ID);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Cours cours = extraireCours(resultSet);
                // Charger les chapitres (et leurs leçons)
                ChapitreDAO chapitreDAO = new ChapitreDAO();
                List<Chapitre> chapitres = chapitreDAO.listerParCours(cours.getId());
                cours.setChapitres(chapitres);
                // Mettre à jour le nombre réel de chapitres et leçons
                cours.setNombreChapitres(chapitres.size());
                cours.setNombreLecons(cours.getNombreTotalLecons());
                return cours;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : recherche cours ID " + id);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Recherche des cours par mot-clé (titre, description, thème)
     * 
     * @param motCle Le mot-clé de recherche
     * @return Liste des cours correspondants
     */
    public List<Cours> rechercher(String motCle) {
        List<Cours> liste = new ArrayList<>();
        if (motCle == null || motCle.trim().isEmpty()) return liste;

        String pattern = "%" + motCle.trim() + "%";
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_RECHERCHE);
            preparedStatement.setString(1, pattern);
            preparedStatement.setString(2, pattern);
            preparedStatement.setString(3, pattern);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                liste.add(extraireCours(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : recherche cours '" + motCle + "'");
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    // ===== MÉTHODES D'ÉCRITURE =====

    /**
     * Ajoute un nouveau cours
     * 
     * @param cours Le cours à ajouter (sans ID)
     * @return true si l'ajout a réussi
     */
    public boolean ajouter(Cours cours) {
        if (cours == null) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(INSERT);
            preparedStatement.setString(1, cours.getTheme());
            preparedStatement.setString(2, cours.getNiveau());
            preparedStatement.setString(3, cours.getTitre());
            preparedStatement.setString(4, cours.getDescription());
            preparedStatement.setString(5, cours.getObjectifs());
            preparedStatement.setString(6, cours.getPrerequis());
            preparedStatement.setString(7, cours.getImage());
            preparedStatement.setString(8, cours.getDureeEstimee());
            preparedStatement.setInt(9, cours.getNombreChapitres());
            preparedStatement.setInt(10, cours.getNombreLecons());
            preparedStatement.setInt(11, cours.getOrdre());
            preparedStatement.setString(12, cours.getStatut());
            Timestamp now = new Timestamp(System.currentTimeMillis());
            preparedStatement.setTimestamp(13, now);
            preparedStatement.setTimestamp(14, now);

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : ajout cours");
            System.err.println("   Titre : " + cours.getTitre());
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Modifie un cours existant
     * 
     * @param cours Le cours à modifier (avec ID)
     * @return true si la modification a réussi
     */
    public boolean modifier(Cours cours) {
        if (cours == null || cours.getId() <= 0) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(UPDATE);
            preparedStatement.setString(1, cours.getTheme());
            preparedStatement.setString(2, cours.getNiveau());
            preparedStatement.setString(3, cours.getTitre());
            preparedStatement.setString(4, cours.getDescription());
            preparedStatement.setString(5, cours.getObjectifs());
            preparedStatement.setString(6, cours.getPrerequis());
            preparedStatement.setString(7, cours.getImage());
            preparedStatement.setString(8, cours.getDureeEstimee());
            preparedStatement.setInt(9, cours.getNombreChapitres());
            preparedStatement.setInt(10, cours.getNombreLecons());
            preparedStatement.setInt(11, cours.getOrdre());
            preparedStatement.setString(12, cours.getStatut());
            preparedStatement.setTimestamp(13, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setInt(14, cours.getId());

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : modification cours ID " + cours.getId());
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Supprime un cours par son ID (supprime aussi chapitres, leçons, ressources)
     * 
     * @param id ID du cours à supprimer
     * @return true si la suppression a réussi
     */
    public boolean supprimer(int id) {
        if (id <= 0) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            // Supprimer d'abord les chapitres (qui suppriment les leçons, qui suppriment les ressources)
            ChapitreDAO chapitreDAO = new ChapitreDAO();
            chapitreDAO.supprimerParCours(id);

            // Puis supprimer le cours
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(DELETE);
            preparedStatement.setInt(1, id);

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : suppression cours ID " + id);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    // ===== MÉTHODES STATISTIQUES =====

    /**
     * Compte le nombre total de cours
     * 
     * @return Nombre total de cours
     */
    public int compterTous() {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(COUNT_TOUS);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : comptage total cours");
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    /**
     * Compte le nombre de cours pour un thème
     * 
     * @param theme Le thème
     * @return Nombre de cours
     */
    public int compterParTheme(String theme) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(COUNT_PAR_THEME);
            preparedStatement.setString(1, theme);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : comptage cours thème " + theme);
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    /**
     * Retourne la liste de tous les thèmes distincts
     * 
     * @return Liste des noms de thèmes
     */
    public List<String> listerThemes() {
        List<String> themes = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_THEMES_DISTINCTS);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                themes.add(resultSet.getString("theme"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : liste des thèmes distincts");
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return themes;
    }

    /**
     * Retourne les statistiques globales des cours (par thème et niveau)
     * 
     * @return Map avec les compteurs
     */
    public List<Map<String, Object>> getStatistiques() {
        List<Map<String, Object>> stats = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_STATS_GLOBALES);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("theme", resultSet.getString("theme"));
                row.put("niveau", resultSet.getString("niveau"));
                row.put("nb_cours", resultSet.getInt("nb_cours"));
                stats.add(row);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : statistiques cours");
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return stats;
    }

    // ===== MÉTHODES PRIVÉES =====

    /**
     * Extrait un cours d'un ResultSet (sans les chapitres)
     * 
     * @param resultSet Le ResultSet positionné sur la ligne courante
     * @return L'objet Cours extrait
     * @throws SQLException En cas d'erreur SQL
     */
    private Cours extraireCours(ResultSet resultSet) throws SQLException {
        Cours cours = new Cours();
        cours.setId(resultSet.getInt("id"));
        cours.setTheme(resultSet.getString("theme"));
        cours.setNiveau(resultSet.getString("niveau"));
        cours.setTitre(resultSet.getString("titre"));
        cours.setDescription(resultSet.getString("description"));
        cours.setObjectifs(resultSet.getString("objectifs"));
        cours.setPrerequis(resultSet.getString("prerequis"));
        cours.setImage(resultSet.getString("image"));
        cours.setDureeEstimee(resultSet.getString("duree_estimee"));
        cours.setNombreChapitres(resultSet.getInt("nombre_chapitres"));
        cours.setNombreLecons(resultSet.getInt("nombre_lecons"));
        cours.setOrdre(resultSet.getInt("ordre"));
        cours.setStatut(resultSet.getString("statut"));
        cours.setDateCreation(resultSet.getTimestamp("date_creation"));
        cours.setDateMaj(resultSet.getTimestamp("date_maj"));
        return cours;
    }
    
    
    /**
 * Compte le nombre d'apprenants distincts pour un cours
 */
public int compterApprenants(int coursId) {
    Connection connexion = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
        connexion = ConnexionDB.getConnexion();
        if (connexion == null) return 0;
        ps = connexion.prepareStatement(
            "SELECT COUNT(DISTINCT utilisateur_id) FROM progression_cours WHERE cours_id = ?");
        ps.setInt(1, coursId);
        rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
        System.err.println("❌ Erreur compterApprenants : " + e.getMessage());
    } finally {
        // fermer les ressources
    }
    return 0;
}

/**
 * Compte le nombre de certificats délivrés pour un cours
 */
public int compterCertificats(int coursId) {
    Connection connexion = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
        connexion = ConnexionDB.getConnexion();
        if (connexion == null) return 0;
        ps = connexion.prepareStatement(
            "SELECT COUNT(*) FROM certificats WHERE cours_id = ? AND est_valide = TRUE");
        ps.setInt(1, coursId);
        rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
        System.err.println("❌ Erreur compterCertificats : " + e.getMessage());
    } finally {
        // fermer les ressources
    }
    return 0;
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