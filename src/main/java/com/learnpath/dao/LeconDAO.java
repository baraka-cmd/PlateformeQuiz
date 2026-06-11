// ============================================================
// FICHIER : src/main/java/com/LearnPath/dao/LeconDAO.java
// RÔLE    : Accès aux données de la table "lecons"
//           CRUD complet + méthodes spécifiques + ressources associées
// ============================================================

package com.learnpath.dao;

import com.learnpath.models.Lecon;
import com.learnpath.models.Ressource;
import com.learnpath.utils.ConnexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * LeconDAO - Data Access Object pour les leçons
 * 
 * Gère toutes les opérations CRUD sur la table "lecons".
 * Charge également les ressources associées via RessourceDAO.
 * 
 * @author LearnPath
 * @version 1.0
 */
public class LeconDAO {

    // ===== CONSTANTES DE REQUÊTES SQL =====
    private static final String SELECT_PAR_CHAPITRE = 
        "SELECT * FROM lecons WHERE chapitre_id = ? ORDER BY ordre";
    
    private static final String SELECT_PAR_ID = 
        "SELECT * FROM lecons WHERE id = ?";
    
    private static final String INSERT = 
        "INSERT INTO lecons (chapitre_id, titre, contenu_texte, url_video, duree_video, " +
        "code_source, fichier_joint, ordre, gratuit, date_creation) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE = 
        "UPDATE lecons SET titre = ?, contenu_texte = ?, url_video = ?, duree_video = ?, " +
        "code_source = ?, fichier_joint = ?, ordre = ?, gratuit = ? WHERE id = ?";
    
    private static final String DELETE = 
        "DELETE FROM lecons WHERE id = ?";
    
    private static final String DELETE_PAR_CHAPITRE = 
        "DELETE FROM lecons WHERE chapitre_id = ?";
    
    private static final String COUNT_PAR_CHAPITRE = 
        "SELECT COUNT(*) FROM lecons WHERE chapitre_id = ?";
    
    private static final String COUNT_PAR_COURS = 
        "SELECT COUNT(*) FROM lecons l JOIN chapitres c ON l.chapitre_id = c.id WHERE c.cours_id = ?";
    
    private static final String COUNT_GRATUITES_PAR_CHAPITRE = 
        "SELECT COUNT(*) FROM lecons WHERE chapitre_id = ? AND gratuit = 1";
    
    private static final String SELECT_PROCHAINE_LECON = 
        "SELECT * FROM lecons WHERE chapitre_id = ? AND ordre > ? ORDER BY ordre LIMIT 1";
    
    private static final String SELECT_PREMIERE_LECON_CHAPITRE = 
        "SELECT * FROM lecons WHERE chapitre_id = ? ORDER BY ordre LIMIT 1";

    // ===== MÉTHODES CRUD =====

    /**
     * Liste toutes les leçons d'un chapitre, avec leurs ressources
     * 
     * @param chapitreId ID du chapitre
     * @return Liste des leçons (vide si aucune)
     */
    public List<Lecon> listerParChapitre(int chapitreId) {
        List<Lecon> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_CHAPITRE);
            preparedStatement.setInt(1, chapitreId);
            resultSet = preparedStatement.executeQuery();

            RessourceDAO ressourceDAO = new RessourceDAO();

            while (resultSet.next()) {
                Lecon lecon = extraireLecon(resultSet);
                // Charger les ressources associées
                List<Ressource> ressources = ressourceDAO.listerParLecon(lecon.getId());
                lecon.setRessources(ressources);
                liste.add(lecon);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : liste des leçons du chapitre " + chapitreId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Trouve une leçon par son ID, avec ses ressources
     * 
     * @param id ID de la leçon
     * @return La leçon trouvée ou null
     */
    public Lecon trouverParId(int id) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_ID);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Lecon lecon = extraireLecon(resultSet);
                // Charger les ressources associées
                RessourceDAO ressourceDAO = new RessourceDAO();
                List<Ressource> ressources = ressourceDAO.listerParLecon(lecon.getId());
                lecon.setRessources(ressources);
                return lecon;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : recherche leçon ID " + id);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Ajoute une nouvelle leçon
     * 
     * @param lecon La leçon à ajouter (sans ID)
     * @return true si l'ajout a réussi
     */
    public boolean ajouter(Lecon lecon) {
        if (lecon == null) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(INSERT);
            preparedStatement.setInt(1, lecon.getChapitreId());
            preparedStatement.setString(2, lecon.getTitre());
            preparedStatement.setString(3, lecon.getContenuTexte());
            preparedStatement.setString(4, lecon.getUrlVideo());
            preparedStatement.setString(5, lecon.getDureeVideo());
            preparedStatement.setString(6, lecon.getCodeSource());
            preparedStatement.setString(7, lecon.getFichierJoint());
            preparedStatement.setInt(8, lecon.getOrdre());
            preparedStatement.setBoolean(9, lecon.isGratuit());
            preparedStatement.setTimestamp(10, new Timestamp(System.currentTimeMillis()));

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : ajout leçon");
            System.err.println("   Titre : " + lecon.getTitre());
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Modifie une leçon existante
     * 
     * @param lecon La leçon à modifier (avec ID)
     * @return true si la modification a réussi
     */
    public boolean modifier(Lecon lecon) {
        if (lecon == null || lecon.getId() <= 0) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(UPDATE);
            preparedStatement.setString(1, lecon.getTitre());
            preparedStatement.setString(2, lecon.getContenuTexte());
            preparedStatement.setString(3, lecon.getUrlVideo());
            preparedStatement.setString(4, lecon.getDureeVideo());
            preparedStatement.setString(5, lecon.getCodeSource());
            preparedStatement.setString(6, lecon.getFichierJoint());
            preparedStatement.setInt(7, lecon.getOrdre());
            preparedStatement.setBoolean(8, lecon.isGratuit());
            preparedStatement.setInt(9, lecon.getId());

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : modification leçon ID " + lecon.getId());
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Supprime une leçon par son ID (supprime aussi ses ressources)
     * 
     * @param id ID de la leçon à supprimer
     * @return true si la suppression a réussi
     */
    public boolean supprimer(int id) {
        if (id <= 0) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            // Supprimer d'abord les ressources associées
            RessourceDAO ressourceDAO = new RessourceDAO();
            ressourceDAO.supprimerParLecon(id);

            // Puis supprimer la leçon
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(DELETE);
            preparedStatement.setInt(1, id);

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : suppression leçon ID " + id);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Supprime toutes les leçons d'un chapitre (et leurs ressources)
     * 
     * @param chapitreId ID du chapitre
     * @return true si la suppression a réussi
     */
    public boolean supprimerParChapitre(int chapitreId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            // Supprimer d'abord les ressources de toutes les leçons du chapitre
            List<Lecon> lecons = listerParChapitre(chapitreId);
            RessourceDAO ressourceDAO = new RessourceDAO();
            for (Lecon lecon : lecons) {
                ressourceDAO.supprimerParLecon(lecon.getId());
            }

            // Puis supprimer les leçons
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(DELETE_PAR_CHAPITRE);
            preparedStatement.setInt(1, chapitreId);
            preparedStatement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : suppression leçons chapitre " + chapitreId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    // ===== MÉTHODES DE COMPTAGE =====

    /**
     * Compte le nombre de leçons dans un chapitre
     * 
     * @param chapitreId ID du chapitre
     * @return Nombre de leçons
     */
    public int compterParChapitre(int chapitreId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(COUNT_PAR_CHAPITRE);
            preparedStatement.setInt(1, chapitreId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : comptage leçons chapitre " + chapitreId);
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    /**
     * Compte le nombre total de leçons dans un cours
     * 
     * @param coursId ID du cours
     * @return Nombre total de leçons
     */
    public int compterParCours(int coursId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(COUNT_PAR_COURS);
            preparedStatement.setInt(1, coursId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : comptage leçons cours " + coursId);
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    /**
     * Compte le nombre de leçons gratuites dans un chapitre
     * 
     * @param chapitreId ID du chapitre
     * @return Nombre de leçons gratuites
     */
    public int compterGratuitesParChapitre(int chapitreId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(COUNT_GRATUITES_PAR_CHAPITRE);
            preparedStatement.setInt(1, chapitreId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : comptage leçons gratuites chapitre " + chapitreId);
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    // ===== MÉTHODES DE NAVIGATION =====

    /**
     * Trouve la prochaine leçon dans le même chapitre
     * 
     * @param chapitreId   ID du chapitre
     * @param ordreActuel  Ordre de la leçon actuelle
     * @return La leçon suivante ou null si c'est la dernière
     */
    public Lecon trouverProchaineLecon(int chapitreId, int ordreActuel) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PROCHAINE_LECON);
            preparedStatement.setInt(1, chapitreId);
            preparedStatement.setInt(2, ordreActuel);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return extraireLecon(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : prochaine leçon chapitre " + chapitreId);
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Trouve la première leçon d'un chapitre
     * 
     * @param chapitreId ID du chapitre
     * @return La première leçon ou null
     */
    public Lecon trouverPremiereLecon(int chapitreId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PREMIERE_LECON_CHAPITRE);
            preparedStatement.setInt(1, chapitreId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return extraireLecon(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : première leçon chapitre " + chapitreId);
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    // ===== MÉTHODES PRIVÉES =====

    /**
     * Extrait une leçon d'un ResultSet
     * 
     * @param resultSet Le ResultSet positionné sur la ligne courante
     * @return L'objet Lecon extrait
     * @throws SQLException En cas d'erreur SQL
     */
    private Lecon extraireLecon(ResultSet resultSet) throws SQLException {
        Lecon lecon = new Lecon();
        lecon.setId(resultSet.getInt("id"));
        lecon.setChapitreId(resultSet.getInt("chapitre_id"));
        lecon.setTitre(resultSet.getString("titre"));
        lecon.setContenuTexte(resultSet.getString("contenu_texte"));
        lecon.setUrlVideo(resultSet.getString("url_video"));
        lecon.setDureeVideo(resultSet.getString("duree_video"));
        lecon.setCodeSource(resultSet.getString("code_source"));
        lecon.setFichierJoint(resultSet.getString("fichier_joint"));
        lecon.setOrdre(resultSet.getInt("ordre"));
        lecon.setGratuit(resultSet.getBoolean("gratuit"));
        lecon.setDateCreation(resultSet.getTimestamp("date_creation"));
        return lecon;
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