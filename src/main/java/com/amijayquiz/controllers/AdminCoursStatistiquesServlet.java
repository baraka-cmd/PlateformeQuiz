// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminCoursStatistiquesServlet.java
// RÔLE    : Contrôleur pour les statistiques détaillées d'un cours
//           GET : Affiche les stats, progression, certificats
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.CertificatDAO;
import com.amijayquiz.dao.CoursDAO;
import com.amijayquiz.dao.ProgressionCoursDAO;
import com.amijayquiz.dao.ProgressionModuleDAO;
import com.amijayquiz.models.Certificat;
import com.amijayquiz.models.Cours;
import com.amijayquiz.models.Utilisateur;
import com.amijayquiz.utils.ConnexionDB;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminCoursStatistiquesServlet - Statistiques d'un cours
 * 
 * Affiche :
 * - Résumé (apprenants, certificats, taux de complétion)
 * - Progression par module (barres)
 * - Derniers certificats délivrés
 * - Derniers apprenants actifs
 * 
 * URL : /admin/cours/statistiques?id=X
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminCoursStatistiquesServlet", urlPatterns = {"/admin/cours/statistiques"})
public class AdminCoursStatistiquesServlet extends HttpServlet {

    private static final int NOMBRE_MODULES = 15;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Vérification authentification + rôle admin
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (!"admin".equals(utilisateur.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        // 2. Récupération et validation de l'ID
        String coursIdStr = request.getParameter("id");
        if (coursIdStr == null || coursIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID du cours manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        int coursId;
        try {
            coursId = Integer.parseInt(coursIdStr.trim());
            if (coursId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de cours invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 3. Récupération du cours
        CoursDAO coursDAO = new CoursDAO();
        Cours cours = coursDAO.trouverParId(coursId);

        if (cours == null) {
            session.setAttribute("messageErreur", "Ce cours n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        ProgressionCoursDAO progressionCoursDAO = new ProgressionCoursDAO();
        ProgressionModuleDAO progressionModuleDAO = new ProgressionModuleDAO();
        CertificatDAO certificatDAO = new CertificatDAO();

        // 4. Statistiques globales
        int nbApprenants = coursDAO.compterApprenants(coursId);
        int nbCertificats = coursDAO.compterCertificats(coursId);
        int totalLecons = progressionCoursDAO.getNombreTotalLecons(coursId);

        // 5. Progression par module
        List<Map<String, Object>> progressionModules = new ArrayList<>();
        for (int m = 1; m <= NOMBRE_MODULES; m++) {
            Map<String, Object> moduleStats = new LinkedHashMap<>();
            moduleStats.put("module", m);
            // Compter les utilisateurs ayant réussi ce module
            int reussis = compterApprenantsModuleReussi(coursId, m);
            moduleStats.put("reussis", reussis);
            int pourcentage = nbApprenants > 0 ? (reussis * 100 / nbApprenants) : 0;
            moduleStats.put("pourcentage", pourcentage);
            progressionModules.add(moduleStats);
        }

        // 6. Derniers certificats (5 max)
        List<Certificat> certificats = certificatDAO.listerParCours(coursId);
        List<Map<String, Object>> derniersCertificats = new ArrayList<>();
        if (certificats != null) {
            int limite = Math.min(certificats.size(), 5);
            for (int i = 0; i < limite; i++) {
                Certificat cert = certificats.get(i);
                Map<String, Object> certData = new LinkedHashMap<>();
                certData.put("certificat", cert);
                // Récupérer le pseudo
                String pseudo = getPseudoUtilisateur(cert.getUtilisateurId());
                certData.put("pseudo", pseudo != null ? pseudo : "Utilisateur #" + cert.getUtilisateurId());
                derniersCertificats.add(certData);
            }
        }

        // 7. Derniers apprenants actifs
        List<Map<String, Object>> derniersApprenants = getDerniersApprenants(coursId, 10);

        // 8. Passage des attributs
        request.setAttribute("pageTitle", "Statistiques - " + cours.getTitre() + " - Administration");
        request.setAttribute("cours", cours);
        request.setAttribute("nbApprenants", nbApprenants);
        request.setAttribute("nbCertificats", nbCertificats);
        request.setAttribute("totalLecons", totalLecons);
        request.setAttribute("progressionModules", progressionModules);
        request.setAttribute("certificats", derniersCertificats);
        request.setAttribute("apprenants", derniersApprenants);

        // 9. Affichage
        request.getRequestDispatcher("/views/admin/admin-cours-statistiques.jsp").forward(request, response);
    }

    /**
     * Compte le nombre d'apprenants ayant réussi un module donné
     */
    private int compterApprenantsModuleReussi(int coursId, int module) {
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;
            ps = connexion.prepareStatement(
                "SELECT COUNT(*) FROM progression_modules WHERE cours_id = ? AND module_numero = ? AND reussi = TRUE");
            ps.setInt(1, coursId);
            ps.setInt(2, module);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("❌ Erreur compterApprenantsModuleReussi : " + e.getMessage());
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
            if (connexion != null) try { connexion.close(); } catch (SQLException e) {}
        }
        return 0;
    }

    /**
     * Récupère le pseudo d'un utilisateur
     */
    private String getPseudoUtilisateur(int utilisateurId) {
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return null;
            ps = connexion.prepareStatement("SELECT pseudo FROM utilisateurs WHERE id = ?");
            ps.setInt(1, utilisateurId);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getString("pseudo");
        } catch (SQLException e) {
            System.err.println("❌ Erreur getPseudoUtilisateur : " + e.getMessage());
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
            if (connexion != null) try { connexion.close(); } catch (SQLException e) {}
        }
        return null;
    }

    /**
     * Récupère les derniers apprenants actifs sur un cours
     */
    private List<Map<String, Object>> getDerniersApprenants(int coursId, int limite) {
        List<Map<String, Object>> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return liste;
            ps = connexion.prepareStatement(
                "SELECT u.id, u.pseudo, MAX(pc.date_debut) AS derniere_activite, " +
                "COUNT(DISTINCT pc.lecon_id) AS lecons_vues " +
                "FROM progression_cours pc " +
                "JOIN utilisateurs u ON pc.utilisateur_id = u.id " +
                "WHERE pc.cours_id = ? " +
                "GROUP BY u.id, u.pseudo " +
                "ORDER BY derniere_activite DESC " +
                "LIMIT ?");
            ps.setInt(1, coursId);
            ps.setInt(2, limite);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("pseudo", rs.getString("pseudo"));
                row.put("derniereActivite", rs.getTimestamp("derniere_activite"));
                row.put("leconsVues", rs.getInt("lecons_vues"));
                liste.add(row);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getDerniersApprenants : " + e.getMessage());
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
            if (connexion != null) try { connexion.close(); } catch (SQLException e) {}
        }
        return liste;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}