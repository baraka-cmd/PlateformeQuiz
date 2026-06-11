// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/AdminCertificatsServlet.java
// RÔLE    : Contrôleur pour la gestion des certificats (admin)
//           GET  : Liste paginée avec filtres
//           POST : Révoquer ou restaurer un certificat
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.CertificatDAO;
import com.learnpath.dao.CoursDAO;
import com.learnpath.dao.UtilisateurDAO;
import com.learnpath.models.Certificat;
import com.learnpath.models.Cours;
import com.learnpath.models.Utilisateur;
import com.learnpath.utils.ConnexionDB;

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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminCertificatsServlet - Gestion des certificats par l'admin
 * 
 * GET  : Liste paginée de tous les certificats avec filtres
 *        - Filtre par cours, par statut (valide/révoqué)
 *        - Recherche par code de vérification
 * 
 * POST : action=revoquer → Révoque un certificat
 *        action=restaurer → Restaure un certificat révoqué
 * 
 * URL : /admin/certificats
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebServlet(name = "AdminCertificatsServlet", urlPatterns = {"/admin/certificats"})
public class AdminCertificatsServlet extends HttpServlet {

    private static final int CERTIFICATS_PAR_PAGE = 20;

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
        if (!"admin".equals(utilisateur.getRole()) && !"gestionnaire".equals(utilisateur.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        // 2. Récupération des paramètres
        String recherche = request.getParameter("recherche");
        String filtreCours = request.getParameter("cours");
        String filtreStatut = request.getParameter("statut");
        String pageStr = request.getParameter("page");

        if (recherche == null) recherche = "";
        if (filtreCours == null) filtreCours = "tous";
        if (filtreStatut == null) filtreStatut = "tous";
        int page = 1;
        try {
            if (pageStr != null && !pageStr.isEmpty()) {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        // 3. Construction de la requête avec filtres
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT c.*, u.pseudo AS pseudo_utilisateur, co.titre AS titre_cours ");
        sql.append("FROM certificats c ");
        sql.append("JOIN utilisateurs u ON c.utilisateur_id = u.id ");
        sql.append("JOIN cours co ON c.cours_id = co.id ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        // Filtre par cours
        if (!"tous".equals(filtreCours)) {
            sql.append("AND c.cours_id = ? ");
            try {
                params.add(Integer.parseInt(filtreCours));
            } catch (NumberFormatException e) {
                params.add(0);
            }
        }

        // Filtre par statut
        if ("valides".equals(filtreStatut)) {
            sql.append("AND c.est_valide = TRUE ");
        } else if ("revoques".equals(filtreStatut)) {
            sql.append("AND c.est_valide = FALSE ");
        }

        // Recherche par code
        if (recherche != null && !recherche.trim().isEmpty()) {
            sql.append("AND (c.code_verification LIKE ? OR u.pseudo LIKE ?) ");
            String filtre = "%" + recherche.trim() + "%";
            params.add(filtre);
            params.add(filtre);
        }

        // Tri
        sql.append("ORDER BY c.date_obtention DESC ");

        // Comptage total
        String countSql = "SELECT COUNT(*) FROM (" + sql.toString() + ") AS tmp";
        int total = compter(countSql, params);

        // Pagination
        int totalPages = (int) Math.ceil((double) total / CERTIFICATS_PAR_PAGE);
        if (totalPages < 1) totalPages = 1;
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * CERTIFICATS_PAR_PAGE;
        sql.append("LIMIT ? OFFSET ?");
        params.add(CERTIFICATS_PAR_PAGE);
        params.add(offset);

        // 4. Exécution
        List<Map<String, Object>> certificats = executerRequete(sql.toString(), params);

        // 5. Récupération des cours pour le filtre
        CoursDAO coursDAO = new CoursDAO();
        List<Cours> coursList = coursDAO.listerTous();

        // 6. Message succès/erreur
        String messageSucces = (String) session.getAttribute("messageSucces");
        String messageErreur = (String) session.getAttribute("messageErreur");
        if (messageSucces != null) session.removeAttribute("messageSucces");
        if (messageErreur != null) session.removeAttribute("messageErreur");

        // 7. URL pagination
        StringBuilder urlPagination = new StringBuilder("/admin/certificats?");
        if (recherche != null && !recherche.isEmpty()) urlPagination.append("recherche=").append(recherche).append("&");
        if (!"tous".equals(filtreCours)) urlPagination.append("cours=").append(filtreCours).append("&");
        if (!"tous".equals(filtreStatut)) urlPagination.append("statut=").append(filtreStatut).append("&");

        // 8. Passage des attributs
        request.setAttribute("pageTitle", "Certificats - Administration - LearnPath");
        request.setAttribute("certificats", certificats);
        request.setAttribute("total", total);
        request.setAttribute("page", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("recherche", recherche);
        request.setAttribute("filtreCours", filtreCours);
        request.setAttribute("filtreStatut", filtreStatut);
        request.setAttribute("coursList", coursList);
        request.setAttribute("urlPagination", urlPagination.toString());
        request.setAttribute("messageSucces", messageSucces);
        request.setAttribute("messageErreur", messageErreur);

        // 9. Affichage
        request.getRequestDispatcher("/views/admin/admin-certificats.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // 1. Vérification authentification + rôle admin
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (!"admin".equals(utilisateur.getRole()) && !"gestionnaire".equals(utilisateur.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        // 2. Récupération des paramètres
        String action = request.getParameter("action");
        String idStr = request.getParameter("id");

        if (idStr == null || idStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID du certificat manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/certificats");
            return;
        }

        int certificatId;
        try {
            certificatId = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de certificat invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/certificats");
            return;
        }

        // 3. Traitement selon l'action
        if ("revoquer".equals(action)) {
            revoquerCertificat(certificatId, session);
        } else if ("restaurer".equals(action)) {
            restaurerCertificat(certificatId, session);
        } else {
            session.setAttribute("messageErreur", "Action invalide.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/certificats");
    }

    /**
     * Révoque un certificat
     */
    private void revoquerCertificat(int certificatId, HttpSession session) {
        Connection connexion = null;
        PreparedStatement ps = null;
        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) {
                session.setAttribute("messageErreur", "Erreur de connexion à la base de données.");
                return;
            }
            ps = connexion.prepareStatement(
                "UPDATE certificats SET est_valide = FALSE, date_revocation = NOW() WHERE id = ? AND est_valide = TRUE");
            ps.setInt(1, certificatId);
            int lignes = ps.executeUpdate();

            if (lignes > 0) {
                session.setAttribute("messageSucces", "Certificat #" + certificatId + " révoqué avec succès.");
                System.out.println("✅ [AdminCertificats] Certificat révoqué : ID=" + certificatId);
            } else {
                session.setAttribute("messageErreur", "Certificat introuvable ou déjà révoqué.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur révocation certificat : " + e.getMessage());
            session.setAttribute("messageErreur", "Erreur lors de la révocation.");
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
            if (connexion != null) try { connexion.close(); } catch (SQLException e) {}
        }
    }

    /**
     * Restaure un certificat révoqué
     */
    private void restaurerCertificat(int certificatId, HttpSession session) {
        Connection connexion = null;
        PreparedStatement ps = null;
        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) {
                session.setAttribute("messageErreur", "Erreur de connexion à la base de données.");
                return;
            }
            ps = connexion.prepareStatement(
                "UPDATE certificats SET est_valide = TRUE, date_revocation = NULL WHERE id = ? AND est_valide = FALSE");
            ps.setInt(1, certificatId);
            int lignes = ps.executeUpdate();

            if (lignes > 0) {
                session.setAttribute("messageSucces", "Certificat #" + certificatId + " restauré avec succès.");
                System.out.println("✅ [AdminCertificats] Certificat restauré : ID=" + certificatId);
            } else {
                session.setAttribute("messageErreur", "Certificat introuvable ou déjà valide.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur restauration certificat : " + e.getMessage());
            session.setAttribute("messageErreur", "Erreur lors de la restauration.");
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
            if (connexion != null) try { connexion.close(); } catch (SQLException e) {}
        }
    }

    /**
     * Compte le nombre de résultats
     */
    private int compter(String sql, List<Object> params) {
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;
            ps = connexion.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("❌ Erreur compter certificats : " + e.getMessage());
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
            if (connexion != null) try { connexion.close(); } catch (SQLException e) {}
        }
        return 0;
    }

    /**
     * Exécute la requête et retourne les résultats
     */
    private List<Map<String, Object>> executerRequete(String sql, List<Object> params) {
        List<Map<String, Object>> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return liste;
            ps = connexion.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("utilisateurId", rs.getInt("utilisateur_id"));
                row.put("coursId", rs.getInt("cours_id"));
                row.put("scoreFinal", rs.getDouble("score_final"));
                row.put("mention", rs.getString("mention"));
                row.put("codeVerification", rs.getString("code_verification"));
                row.put("dateObtention", rs.getTimestamp("date_obtention"));
                row.put("estValide", rs.getBoolean("est_valide"));
                row.put("dateRevocation", rs.getTimestamp("date_revocation"));
                row.put("pseudo", rs.getString("pseudo_utilisateur"));
                row.put("titreCours", rs.getString("titre_cours"));
                liste.add(row);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur executerRequete certificats : " + e.getMessage());
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
            if (connexion != null) try { connexion.close(); } catch (SQLException e) {}
        }
        return liste;
    }
}