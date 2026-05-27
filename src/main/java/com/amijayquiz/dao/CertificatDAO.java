// ============================================================
// FICHIER : src/main/java/com/amijayquiz/dao/CertificatDAO.java
// RÔLE    : Accès aux données de la table "certificats"
//           Gestion des certificats de réussite
// ============================================================

package com.amijayquiz.dao;

import com.amijayquiz.models.Certificat;
import com.amijayquiz.utils.ConnexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * CertificatDAO - Data Access Object pour les certificats de réussite
 * 
 * Gère toutes les opérations liées aux certificats :
 * création, consultation, vérification par code unique.
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
public class CertificatDAO {

    // ===== CONSTANTES DE REQUÊTES SQL =====
    private static final String SELECT_PAR_ID = 
        "SELECT * FROM certificats WHERE id = ?";
    
    private static final String SELECT_PAR_CODE = 
        "SELECT * FROM certificats WHERE code_verification = ?";
    
    private static final String SELECT_PAR_UTILISATEUR_COURS = 
        "SELECT * FROM certificats WHERE utilisateur_id = ? AND cours_id = ?";
    
    private static final String SELECT_PAR_UTILISATEUR = 
        "SELECT * FROM certificats WHERE utilisateur_id = ? ORDER BY date_obtention DESC";
    
    private static final String SELECT_PAR_COURS = 
        "SELECT * FROM certificats WHERE cours_id = ? ORDER BY score_final DESC";
    
    private static final String INSERT = 
        "INSERT INTO certificats (utilisateur_id, cours_id, score_final, score_max, mention, " +
        "code_verification, date_obtention) VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String DELETE = 
        "DELETE FROM certificats WHERE id = ?";
    
    private static final String COUNT_PAR_UTILISATEUR = 
        "SELECT COUNT(*) FROM certificats WHERE utilisateur_id = ?";
    
    private static final String COUNT_PAR_COURS = 
        "SELECT COUNT(*) FROM certificats WHERE cours_id = ?";
    
    private static final String MOYENNE_SCORES_COURS = 
        "SELECT AVG(score_final) FROM certificats WHERE cours_id = ?";
    
    private static final String MEILLEUR_SCORE_COURS = 
        "SELECT MAX(score_final) FROM certificats WHERE cours_id = ?";
    
    private static final String SELECT_DERNIERS_CERTIFICATS = 
        "SELECT * FROM certificats ORDER BY date_obtention DESC LIMIT ?";

    // ===== MÉTHODES DE LECTURE =====

    /**
     * Trouve un certificat par son ID
     * 
     * @param id ID du certificat
     * @return Le certificat trouvé ou null
     */
    public Certificat trouverParId(int id) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_ID);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return extraireCertificat(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : recherche certificat ID " + id);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Trouve un certificat par son code de vérification unique
     * Utilisé pour la vérification publique d'un certificat
     * 
     * @param code Code de vérification
     * @return Le certificat trouvé ou null
     */
    public Certificat trouverParCode(String code) {
        if (code == null || code.trim().isEmpty()) return null;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_CODE);
            preparedStatement.setString(1, code.trim());
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return extraireCertificat(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : recherche certificat code " + code);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Vérifie si un utilisateur a déjà un certificat pour un cours
     * 
     * @param utilisateurId ID de l'utilisateur
     * @param coursId       ID du cours
     * @return Le certificat trouvé ou null
     */
    public Certificat trouverParUtilisateurEtCours(int utilisateurId, int coursId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_UTILISATEUR_COURS);
            preparedStatement.setInt(1, utilisateurId);
            preparedStatement.setInt(2, coursId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return extraireCertificat(resultSet);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : certificat utilisateur " + utilisateurId + " cours " + coursId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Liste tous les certificats d'un utilisateur
     * 
     * @param utilisateurId ID de l'utilisateur
     * @return Liste des certificats
     */
    public List<Certificat> listerParUtilisateur(int utilisateurId) {
        List<Certificat> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_UTILISATEUR);
            preparedStatement.setInt(1, utilisateurId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                liste.add(extraireCertificat(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : certificats utilisateur " + utilisateurId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Liste tous les certificats délivrés pour un cours
     * 
     * @param coursId ID du cours
     * @return Liste des certificats triés par score décroissant
     */
    public List<Certificat> listerParCours(int coursId) {
        List<Certificat> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_PAR_COURS);
            preparedStatement.setInt(1, coursId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                liste.add(extraireCertificat(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : certificats cours " + coursId);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Récupère les derniers certificats délivrés (pour page d'accueil)
     * 
     * @param limite Nombre maximum de certificats
     * @return Liste des derniers certificats
     */
    public List<Certificat> listerDerniers(int limite) {
        List<Certificat> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(SELECT_DERNIERS_CERTIFICATS);
            preparedStatement.setInt(1, Math.min(limite, 50));
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                liste.add(extraireCertificat(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : derniers certificats");
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    // ===== MÉTHODES D'ÉCRITURE =====

    /**
     * Ajoute un nouveau certificat (délivrance après réussite d'un examen)
     * Vérifie qu'il n'existe pas déjà un certificat pour ce cours
     * 
     * @param certificat Le certificat à créer
     * @return true si la création a réussi
     */
    public boolean ajouter(Certificat certificat) {
        if (certificat == null) return false;
        if (!certificat.isValide()) return false;

        // Vérifier qu'il n'existe pas déjà un certificat pour ce cours
        Certificat existant = trouverParUtilisateurEtCours(
            certificat.getUtilisateurId(), 
            certificat.getCoursId()
        );

        if (existant != null) {
            System.err.println("⚠️ Certificat déjà existant pour utilisateur " + 
                certificat.getUtilisateurId() + " cours " + certificat.getCoursId());
            return false;
        }

        // Vérifier que le code de vérification n'est pas déjà utilisé
        Certificat memeCode = trouverParCode(certificat.getCodeVerification());
        if (memeCode != null) {
            System.err.println("⚠️ Code de vérification déjà utilisé : " + certificat.getCodeVerification());
            return false;
        }

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(INSERT);
            preparedStatement.setInt(1, certificat.getUtilisateurId());
            preparedStatement.setInt(2, certificat.getCoursId());
            preparedStatement.setDouble(3, certificat.getScoreFinal());
            preparedStatement.setInt(4, certificat.getScoreMax());
            preparedStatement.setString(5, certificat.getMention());
            preparedStatement.setString(6, certificat.getCodeVerification());
            preparedStatement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : création certificat");
            System.err.println("   Utilisateur : " + certificat.getUtilisateurId());
            System.err.println("   Cours : " + certificat.getCoursId());
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Supprime un certificat par son ID
     * 
     * @param id ID du certificat à supprimer
     * @return true si la suppression a réussi
     */
    public boolean supprimer(int id) {
        if (id <= 0) return false;

        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(DELETE);
            preparedStatement.setInt(1, id);

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur : suppression certificat ID " + id);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    // ===== MÉTHODES STATISTIQUES =====

    /**
     * Compte le nombre de certificats d'un utilisateur
     * 
     * @param utilisateurId ID de l'utilisateur
     * @return Nombre de certificats
     */
    public int compterParUtilisateur(int utilisateurId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(COUNT_PAR_UTILISATEUR);
            preparedStatement.setInt(1, utilisateurId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : comptage certificats utilisateur " + utilisateurId);
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    /**
     * Compte le nombre de certificats délivrés pour un cours
     * 
     * @param coursId ID du cours
     * @return Nombre de certificats
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
            System.err.println("❌ Erreur : comptage certificats cours " + coursId);
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    /**
     * Calcule la moyenne des scores pour un cours
     * 
     * @param coursId ID du cours
     * @return Score moyen ou 0.0 si aucun certificat
     */
    public double getMoyenneScores(int coursId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(MOYENNE_SCORES_COURS);
            preparedStatement.setInt(1, coursId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                double moyenne = resultSet.getDouble(1);
                return resultSet.wasNull() ? 0.0 : moyenne;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : moyenne scores cours " + coursId);
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0.0;
    }

    /**
     * Récupère le meilleur score pour un cours
     * 
     * @param coursId ID du cours
     * @return Meilleur score ou 0.0
     */
    public double getMeilleurScore(int coursId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            preparedStatement = connexion.prepareStatement(MEILLEUR_SCORE_COURS);
            preparedStatement.setInt(1, coursId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                double score = resultSet.getDouble(1);
                return resultSet.wasNull() ? 0.0 : score;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : meilleur score cours " + coursId);
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0.0;
    }

    // ===== MÉTHODES PRIVÉES =====

    /**
     * Extrait un certificat d'un ResultSet
     * 
     * @param resultSet Le ResultSet positionné sur la ligne courante
     * @return L'objet Certificat extrait
     * @throws SQLException En cas d'erreur SQL
     */
    private Certificat extraireCertificat(ResultSet resultSet) throws SQLException {
        Certificat certificat = new Certificat();
        certificat.setId(resultSet.getInt("id"));
        certificat.setUtilisateurId(resultSet.getInt("utilisateur_id"));
        certificat.setCoursId(resultSet.getInt("cours_id"));
        certificat.setScoreFinal(resultSet.getDouble("score_final"));
        certificat.setScoreMax(resultSet.getInt("score_max"));
        certificat.setMention(resultSet.getString("mention"));
        certificat.setCodeVerification(resultSet.getString("code_verification"));
        certificat.setDateObtention(resultSet.getTimestamp("date_obtention"));
        return certificat;
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