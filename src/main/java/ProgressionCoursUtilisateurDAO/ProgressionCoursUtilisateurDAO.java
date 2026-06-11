// ============================================================
// FICHIER : src/main/java/com/learnpath/dao/ProgressionCoursUtilisateurDAO.java
// RÔLE    : DAO pour les statistiques détaillées de progression
//           des utilisateurs dans les cours (pour admin)
// ============================================================

package com.learnpath.dao;

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

public class ProgressionCoursUtilisateurDAO {

    /**
     * Récupère la progression détaillée de tous les utilisateurs dans les cours
     * avec pagination, filtres et recherche
     */
    public List<Map<String, Object>> listerProgressionCours(
            int offset, int limite, String recherche, 
            String filtreTheme, String filtreNiveau, String filtreStatut,
            String tri, String ordre) {

        List<Map<String, Object>> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return liste;

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            sql.append("u.id AS utilisateur_id, u.pseudo, ");
            sql.append("c.id AS cours_id, c.titre AS titre_cours, c.theme, c.niveau, ");
            sql.append("COUNT(DISTINCT pc.lecon_id) AS lecons_terminees, ");
            sql.append("(SELECT COUNT(*) FROM lecons l JOIN chapitres ch ON l.chapitre_id = ch.id WHERE ch.cours_id = c.id) AS total_lecons, ");
            sql.append("(SELECT COUNT(*) FROM progression_modules pm WHERE pm.utilisateur_id = u.id AND pm.cours_id = c.id AND pm.reussi = 1) AS modules_reussis, ");
            sql.append("MAX(pc.date_completion) AS derniere_activite, ");
            sql.append("CASE WHEN cert.id IS NOT NULL THEN TRUE ELSE FALSE END AS a_certificat, ");
            sql.append("cert.mention, cert.score_final, cert.code_verification ");
            sql.append("FROM progression_cours pc ");
            sql.append("JOIN utilisateurs u ON pc.utilisateur_id = u.id ");
            sql.append("JOIN cours c ON pc.cours_id = c.id ");
            sql.append("LEFT JOIN certificats cert ON cert.utilisateur_id = u.id AND cert.cours_id = c.id AND cert.est_valide = TRUE ");
            sql.append("WHERE 1=1 ");

            List<Object> params = new ArrayList<>();

            // Recherche textuelle
            if (recherche != null && !recherche.trim().isEmpty()) {
                sql.append("AND (u.pseudo LIKE ? OR c.titre LIKE ?) ");
                String filtre = "%" + recherche.trim() + "%";
                params.add(filtre);
                params.add(filtre);
            }

            // Filtre par thème
            if (filtreTheme != null && !"tous".equals(filtreTheme)) {
                sql.append("AND c.theme = ? ");
                params.add(filtreTheme);
            }

            // Filtre par niveau
            if (filtreNiveau != null && !"tous".equals(filtreNiveau)) {
                sql.append("AND c.niveau = ? ");
                params.add(filtreNiveau);
            }

            // GROUP BY
            sql.append("GROUP BY u.id, u.pseudo, c.id, c.titre, c.theme, c.niveau, cert.id, cert.mention, cert.score_final, cert.code_verification ");

            // Filtre par statut (après GROUP BY)
            if (filtreStatut != null && !"tous".equals(filtreStatut)) {
                int totalLeconsSubquery = 0; // Sera calculé par HAVING
                if ("termine".equals(filtreStatut)) {
                    sql.append("HAVING COUNT(DISTINCT pc.lecon_id) >= (SELECT COUNT(*) FROM lecons l JOIN chapitres ch ON l.chapitre_id = ch.id WHERE ch.cours_id = c.id) ");
                } else if ("certifie".equals(filtreStatut)) {
                    sql.append("HAVING cert.id IS NOT NULL ");
                } else if ("en_cours".equals(filtreStatut)) {
                    sql.append("HAVING COUNT(DISTINCT pc.lecon_id) > 0 AND COUNT(DISTINCT pc.lecon_id) < (SELECT COUNT(*) FROM lecons l JOIN chapitres ch ON l.chapitre_id = ch.id WHERE ch.cours_id = c.id) AND cert.id IS NULL ");
                }
            }

            // Tri
            String colonneTri = "derniere_activite";
            if (tri != null) {
                switch (tri) {
                    case "pseudo":     colonneTri = "u.pseudo"; break;
                    case "cours":      colonneTri = "c.titre"; break;
                    case "theme":      colonneTri = "c.theme"; break;
                    case "niveau":     colonneTri = "c.niveau"; break;
                    case "progression":colonneTri = "lecons_terminees"; break;
                    case "modules":    colonneTri = "modules_reussis"; break;
                    default:           colonneTri = "derniere_activite";
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
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("utilisateur_id", rs.getInt("utilisateur_id"));
                row.put("pseudo", rs.getString("pseudo"));
                row.put("cours_id", rs.getInt("cours_id"));
                row.put("titre_cours", rs.getString("titre_cours"));
                row.put("theme", rs.getString("theme"));
                row.put("niveau", rs.getString("niveau"));
                
                int leconsTerminees = rs.getInt("lecons_terminees");
                int totalLecons = rs.getInt("total_lecons");
                row.put("lecons_terminees", leconsTerminees);
                row.put("total_lecons", totalLecons);
                
                int pourcentage = totalLecons > 0 ? (leconsTerminees * 100 / totalLecons) : 0;
                row.put("pourcentage", pourcentage);
                row.put("modules_reussis", rs.getInt("modules_reussis"));
                row.put("derniere_activite", rs.getTimestamp("derniere_activite"));
                row.put("a_certificat", rs.getBoolean("a_certificat"));
                row.put("mention", rs.getString("mention"));
                row.put("score_final", rs.getDouble("score_final"));
                
                // Déterminer le statut
                String statut;
                if (rs.getBoolean("a_certificat")) {
                    statut = "certifie";
                } else if (leconsTerminees >= totalLecons && totalLecons > 0) {
                    statut = "termine";
                } else if (leconsTerminees > 0) {
                    statut = "en_cours";
                } else {
                    statut = "non_commence";
                }
                row.put("statut", statut);

                liste.add(row);
            }

        } catch (SQLException e) {
            System.err.println("❌ [ProgressionCoursUtilisateurDAO] Erreur listerProgressionCours : " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) { try { rs.close(); } catch (SQLException e) {} }
            if (ps != null) { try { ps.close(); } catch (SQLException e) {} }
        }

        return liste;
    }

    /**
     * Compte le nombre total de progressions selon les filtres
     */
    public int compterProgressionCours(String recherche, String filtreTheme, 
                                         String filtreNiveau, String filtreStatut) {
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(*) FROM (");
            sql.append("SELECT u.id, c.id ");
            sql.append("FROM progression_cours pc ");
            sql.append("JOIN utilisateurs u ON pc.utilisateur_id = u.id ");
            sql.append("JOIN cours c ON pc.cours_id = c.id ");
            sql.append("LEFT JOIN certificats cert ON cert.utilisateur_id = u.id AND cert.cours_id = c.id AND cert.est_valide = TRUE ");
            sql.append("WHERE 1=1 ");

            List<Object> params = new ArrayList<>();

            if (recherche != null && !recherche.trim().isEmpty()) {
                sql.append("AND (u.pseudo LIKE ? OR c.titre LIKE ?) ");
                String filtre = "%" + recherche.trim() + "%";
                params.add(filtre);
                params.add(filtre);
            }

            if (filtreTheme != null && !"tous".equals(filtreTheme)) {
                sql.append("AND c.theme = ? ");
                params.add(filtreTheme);
            }

            if (filtreNiveau != null && !"tous".equals(filtreNiveau)) {
                sql.append("AND c.niveau = ? ");
                params.add(filtreNiveau);
            }

            sql.append("GROUP BY u.id, c.id ");

            if (filtreStatut != null && !"tous".equals(filtreStatut)) {
                if ("termine".equals(filtreStatut)) {
                    sql.append("HAVING COUNT(DISTINCT pc.lecon_id) >= (SELECT COUNT(*) FROM lecons l JOIN chapitres ch ON l.chapitre_id = ch.id WHERE ch.cours_id = c.id) ");
                } else if ("certifie".equals(filtreStatut)) {
                    sql.append("HAVING MAX(cert.id) IS NOT NULL ");
                } else if ("en_cours".equals(filtreStatut)) {
                    sql.append("HAVING COUNT(DISTINCT pc.lecon_id) > 0 AND COUNT(DISTINCT pc.lecon_id) < (SELECT COUNT(*) FROM lecons l JOIN chapitres ch ON l.chapitre_id = ch.id WHERE ch.cours_id = c.id) AND MAX(cert.id) IS NULL ");
                }
            }

            sql.append(") AS tmp");

            ps = connexion.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ [ProgressionCoursUtilisateurDAO] Erreur compterProgressionCours : " + e.getMessage());
        } finally {
            if (rs != null) { try { rs.close(); } catch (SQLException e) {} }
            if (ps != null) { try { ps.close(); } catch (SQLException e) {} }
        }

        return 0;
    }

    /**
     * Récupère les statistiques globales pour les cours
     */
    public Map<String, Object> getStatistiquesCours() {
        Map<String, Object> stats = new LinkedHashMap<>();
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return stats;

            // Apprenants actifs (ayant au moins 1 progression)
            ps = connexion.prepareStatement("SELECT COUNT(DISTINCT utilisateur_id) FROM progression_cours");
            rs = ps.executeQuery();
            stats.put("apprenantsActifs", rs.next() ? rs.getInt(1) : 0);
            rs.close(); ps.close();

            // Total inscriptions (paires utilisateur-cours)
            ps = connexion.prepareStatement("SELECT COUNT(DISTINCT CONCAT(utilisateur_id, '-', cours_id)) FROM progression_cours");
            rs = ps.executeQuery();
            stats.put("totalInscriptions", rs.next() ? rs.getInt(1) : 0);
            rs.close(); ps.close();

            // Certificats délivrés
            ps = connexion.prepareStatement("SELECT COUNT(*) FROM certificats WHERE est_valide = TRUE");
            rs = ps.executeQuery();
            stats.put("totalCertificats", rs.next() ? rs.getInt(1) : 0);
            rs.close(); ps.close();

            // Taux de complétion moyen
            ps = connexion.prepareStatement("SELECT AVG(pourcentage) FROM progression_modules WHERE reussi = 1");
            rs = ps.executeQuery();
            stats.put("tauxCompletionMoyen", rs.next() ? Math.round(rs.getDouble(1) * 10.0) / 10.0 : 0);
            rs.close(); ps.close();

            // Cours terminés (utilisateurs ayant 100% des leçons)
            ps = connexion.prepareStatement(
                "SELECT COUNT(*) FROM (" +
                "SELECT pc.utilisateur_id, pc.cours_id, COUNT(DISTINCT pc.lecon_id) AS lecons_faites, " +
                "(SELECT COUNT(*) FROM lecons l JOIN chapitres ch ON l.chapitre_id = ch.id WHERE ch.cours_id = pc.cours_id) AS total " +
                "FROM progression_cours pc GROUP BY pc.utilisateur_id, pc.cours_id " +
                "HAVING lecons_faites >= total) AS tmp");
            rs = ps.executeQuery();
            stats.put("coursTermines", rs.next() ? rs.getInt(1) : 0);

        } catch (SQLException e) {
            System.err.println("❌ [ProgressionCoursUtilisateurDAO] Erreur stats : " + e.getMessage());
        } finally {
            if (rs != null) { try { rs.close(); } catch (SQLException e) {} }
            if (ps != null) { try { ps.close(); } catch (SQLException e) {} }
        }

        return stats;
    }
}