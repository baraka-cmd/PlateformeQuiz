// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/AdminResultatsServlet.java
// RÔLE    : Contrôleur administrateur pour la consultation
//           des résultats des joueurs
//           - Liste paginée de tous les résultats
//           - Filtres par thème, niveau, résultat
//           - Statistiques globales
// ============================================================

package com.learnpath.controllers;

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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.learnpath.dao.ProgressionCoursUtilisateurDAO;

/**
 * AdminResultatsServlet - Consultation des résultats par l'administrateur
 * 
 * GET : Affiche la liste paginée de tous les résultats
 *       avec filtres, recherche et statistiques globales
 * 
 * URL : /admin/resultats
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebServlet(name = "AdminResultatsServlet", urlPatterns = {"/admin/resultats"})
public class AdminResultatsServlet extends HttpServlet {

    // Nombre de résultats par page
    private static final int RESULTATS_PAR_PAGE = 20;

    // ============================================================
    // DOGET : AFFICHAGE DE LA LISTE DES RÉSULTATS
    // ============================================================
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

        // 2. Récupération des paramètres de filtre
        String recherche = request.getParameter("recherche");
        String filtreTheme = request.getParameter("theme");
        String filtreNiveau = request.getParameter("niveau");
        String filtreResultat = request.getParameter("resultat");
        String tri = request.getParameter("tri");
        String pageStr = request.getParameter("page");
        // Récupération du paramètre tab (quiz ou cours)
        String tab = request.getParameter("tab");
        if (tab == null) tab = "quiz";

        // Valeurs par défaut
        if (filtreTheme == null) filtreTheme = "tous";
        if (filtreNiveau == null) filtreNiveau = "tous";
        if (filtreResultat == null) filtreResultat = "tous";
        if (tri == null) tri = "date";
        int page = 1;
        try {
            if (pageStr != null && !pageStr.isEmpty()) {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        // 3. Construction de la requête SQL avec filtres
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT p.id, p.id_joueur, p.id_quiz, p.score, p.total_questions, ");
        sqlBuilder.append("p.temps_realise_secondes, p.date_partie, ");
        sqlBuilder.append("u.pseudo AS pseudo_joueur, u.email AS email_joueur, ");
        sqlBuilder.append("q.titre AS titre_quiz, q.niveau AS niveau_quiz ");
        sqlBuilder.append("FROM parties p ");
        sqlBuilder.append("JOIN utilisateurs u ON p.id_joueur = u.id ");
        sqlBuilder.append("JOIN quiz q ON p.id_quiz = q.id ");
        sqlBuilder.append("WHERE 1=1 ");

        // Paramètres pour les filtres
        List<Object> params = new ArrayList<>();

        // Filtre par thème (recherche dans le titre du quiz)
        if (!"tous".equals(filtreTheme)) {
            sqlBuilder.append("AND q.titre LIKE ? ");
            params.add(filtreTheme + "%");
        }

        // Filtre par niveau
        if (!"tous".equals(filtreNiveau)) {
            sqlBuilder.append("AND q.niveau = ? ");
            params.add(filtreNiveau);
        }

        // Filtre par résultat (réussi/échoué)
        if ("reussies".equals(filtreResultat)) {
            sqlBuilder.append("AND p.score >= 8 ");
        } else if ("echouees".equals(filtreResultat)) {
            sqlBuilder.append("AND p.score < 8 ");
        }

        // Recherche textuelle
        if (recherche != null && !recherche.trim().isEmpty()) {
            sqlBuilder.append("AND (u.pseudo LIKE ? OR q.titre LIKE ?) ");
            String filtre = "%" + recherche.trim() + "%";
            params.add(filtre);
            params.add(filtre);
        }

        // Tri
        switch (tri) {
            case "score":
                sqlBuilder.append("ORDER BY p.score DESC, p.temps_realise_secondes ASC ");
                break;
            case "joueur":
                sqlBuilder.append("ORDER BY u.pseudo ASC ");
                break;
            case "quiz":
                sqlBuilder.append("ORDER BY q.titre ASC ");
                break;
            case "date":
            default:
                sqlBuilder.append("ORDER BY p.date_partie DESC ");
                break;
        }

        // 4. Comptage total pour la pagination
        String countSql = "SELECT COUNT(*) FROM (" + sqlBuilder.toString() + ") AS tmp";
        int totalResultats = compterResultats(countSql, params);

        // 5. Pagination
        int totalPages = (int) Math.ceil((double) totalResultats / RESULTATS_PAR_PAGE);
        if (page > totalPages && totalPages > 0) page = totalPages;

        int offset = (page - 1) * RESULTATS_PAR_PAGE;
        sqlBuilder.append("LIMIT ? OFFSET ? ");
        params.add(RESULTATS_PAR_PAGE);
        params.add(offset);

        // 6. Exécution de la requête
        List<Map<String, Object>> resultats = executerRequete(sqlBuilder.toString(), params);

        // 7. Récupération des thèmes distincts pour le filtre
        List<String> themes = listerThemesDistincts();

        // 8. Statistiques globales
        Map<String, Object> stats = calculerStatistiques();

        // 9. Construction de l'URL de pagination
        StringBuilder urlPagination = new StringBuilder("/admin/resultats?");
        if (recherche != null && !recherche.isEmpty()) urlPagination.append("recherche=").append(recherche).append("&");
        if (!"tous".equals(filtreTheme)) urlPagination.append("theme=").append(filtreTheme).append("&");
        if (!"tous".equals(filtreNiveau)) urlPagination.append("niveau=").append(filtreNiveau).append("&");
        if (!"tous".equals(filtreResultat)) urlPagination.append("resultat=").append(filtreResultat).append("&");
        if (!"date".equals(tri)) urlPagination.append("tri=").append(tri).append("&");

        // 10. Passage des attributs
        request.setAttribute("pageTitle", "Résultats des joueurs - Administration - LearnPath");
        request.setAttribute("resultats", resultats);
        request.setAttribute("totalResultats", totalResultats);
        request.setAttribute("page", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("recherche", recherche);
        request.setAttribute("filtreTheme", filtreTheme);
        request.setAttribute("filtreNiveau", filtreNiveau);
        request.setAttribute("filtreResultat", filtreResultat);
        request.setAttribute("tri", tri);
        request.setAttribute("themes", themes);
        request.setAttribute("stats", stats);
        request.setAttribute("urlPagination", urlPagination.toString());

        
        
        // ============================================================
        // DONNÉES ONGLET COURS
        // ============================================================
        if ("cours".equals(tab)) {
            ProgressionCoursUtilisateurDAO progressionCoursUtilDAO = new ProgressionCoursUtilisateurDAO();

            // Paramètres spécifiques à l'onglet cours
            String filtreStatutCours = request.getParameter("statutCours");
            if (filtreStatutCours == null) filtreStatutCours = "tous";

            // Récupérer les thèmes depuis la table themes
            com.learnpath.dao.ThemeDAO themeDAO = new com.learnpath.dao.ThemeDAO();
            List<com.learnpath.models.Theme> themesList = themeDAO.listerTous();
            List<String> themesCours = new ArrayList<>();
            if (themesList != null) {
                for (com.learnpath.models.Theme t : themesList) {
                    themesCours.add(t.getNom());
                }
            }

            // Filtres
            String filtreThemeCours = request.getParameter("themeCours");
            String filtreNiveauCours = request.getParameter("niveauCours");
            String triCours = request.getParameter("triCours");
            String ordreCours = request.getParameter("ordreCours");

            if (filtreThemeCours == null) filtreThemeCours = "tous";
            if (filtreNiveauCours == null) filtreNiveauCours = "tous";
            if (triCours == null) triCours = "derniere_activite";
            if (ordreCours == null) ordreCours = "DESC";

            int pageCours = 1;
            String pageCoursStr = request.getParameter("pageCours");
            if (pageCoursStr != null && !pageCoursStr.isEmpty()) {
                try {
                    pageCours = Integer.parseInt(pageCoursStr);
                    if (pageCours < 1) pageCours = 1;
                } catch (NumberFormatException e) {}
            }

            int limiteCours = 20;
            int offsetCours = (pageCours - 1) * limiteCours;

            // Récupérer les données
            List<Map<String, Object>> progressionCours = progressionCoursUtilDAO.listerProgressionCours(
                offsetCours, limiteCours, recherche, filtreThemeCours, filtreNiveauCours, filtreStatutCours, triCours, ordreCours
            );

            int totalProgression = progressionCoursUtilDAO.compterProgressionCours(
                recherche, filtreThemeCours, filtreNiveauCours, filtreStatutCours
            );

            int totalPagesCours = (int) Math.ceil((double) totalProgression / limiteCours);
            if (totalPagesCours < 1) totalPagesCours = 1;
            if (pageCours > totalPagesCours) pageCours = totalPagesCours;

            // Statistiques cours
            Map<String, Object> statsCours = progressionCoursUtilDAO.getStatistiquesCours();

            // URL pagination cours
            StringBuilder urlPaginationCours = new StringBuilder("/admin/resultats?tab=cours&");
            if (recherche != null && !recherche.isEmpty()) urlPaginationCours.append("recherche=").append(recherche).append("&");
            if (!"tous".equals(filtreThemeCours)) urlPaginationCours.append("themeCours=").append(filtreThemeCours).append("&");
            if (!"tous".equals(filtreNiveauCours)) urlPaginationCours.append("niveauCours=").append(filtreNiveauCours).append("&");
            if (!"tous".equals(filtreStatutCours)) urlPaginationCours.append("statutCours=").append(filtreStatutCours).append("&");

            // Attributs pour l'onglet cours
            request.setAttribute("progressionCours", progressionCours);
            request.setAttribute("totalProgression", totalProgression);
            request.setAttribute("pageCours", pageCours);
            request.setAttribute("totalPagesCours", totalPagesCours);
            request.setAttribute("filtreThemeCours", filtreThemeCours);
            request.setAttribute("filtreNiveauCours", filtreNiveauCours);
            request.setAttribute("filtreStatutCours", filtreStatutCours);
            request.setAttribute("triCours", triCours);
            request.setAttribute("ordreCours", ordreCours);
            request.setAttribute("themesCours", themesCours);
            request.setAttribute("statsCours", statsCours);
            request.setAttribute("urlPaginationCours", urlPaginationCours.toString());
        }

        // Attribut commun
        request.setAttribute("tab", tab);
        // 11. Affichage
        request.getRequestDispatcher("/views/admin/admin-resultats.jsp").forward(request, response);
    }

    // ============================================================
    // MÉTHODES PRIVÉES
    // ============================================================

    /**
     * Compte le nombre total de résultats correspondant aux filtres
     */
    private int compterResultats(String sql, List<Object> params) {
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
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ [AdminResultats] Erreur comptage : " + e.getMessage());
        } finally {
            fermerRessources(null, ps, rs);
        }

        return 0;
    }

    /**
     * Exécute la requête et retourne la liste des résultats
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
                row.put("id_joueur", rs.getInt("id_joueur"));
                row.put("id_quiz", rs.getInt("id_quiz"));
                row.put("score", rs.getInt("score"));
                row.put("total_questions", rs.getInt("total_questions"));
                row.put("temps_realise_secondes", rs.getInt("temps_realise_secondes"));
                row.put("date_partie", rs.getTimestamp("date_partie"));
                row.put("pseudo_joueur", rs.getString("pseudo_joueur"));
                row.put("email_joueur", rs.getString("email_joueur"));
                row.put("titre_quiz", rs.getString("titre_quiz"));
                row.put("niveau_quiz", rs.getString("niveau_quiz"));

                // Calculer le pourcentage
                int score = rs.getInt("score");
                int total = rs.getInt("total_questions");
                double pourcentage = (total > 0) ? (score * 100.0 / total) : 0;
                row.put("pourcentage", Math.round(pourcentage * 10.0) / 10.0);
                row.put("reussi", score >= 8);

                // Formater le temps
                int temps = rs.getInt("temps_realise_secondes");
                int minutes = temps / 60;
                int secondes = temps % 60;
                row.put("temps_formate", minutes + " min " + secondes + " s");

                liste.add(row);
            }

        } catch (SQLException e) {
            System.err.println("❌ [AdminResultats] Erreur requête : " + e.getMessage());
        } finally {
            fermerRessources(null, ps, rs);
        }

        return liste;
    }

    /**
     * Récupère la liste des thèmes distincts pour le filtre
     */
    private List<String> listerThemesDistincts() {
        List<String> themes = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return themes;

            String sql = "SELECT DISTINCT q.titre FROM quiz q JOIN parties p ON q.id = p.id_quiz ORDER BY q.titre";
            ps = connexion.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                String titre = rs.getString("titre");
                if (titre != null && titre.contains(" - Partie")) {
                    String theme = titre.substring(0, titre.lastIndexOf(" - Partie"));
                    if (!themes.contains(theme)) {
                        themes.add(theme);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ [AdminResultats] Erreur thèmes : " + e.getMessage());
        } finally {
            fermerRessources(null, ps, rs);
        }

        return themes;
    }

    /**
     * Calcule les statistiques globales
     */
    private Map<String, Object> calculerStatistiques() {
        Map<String, Object> stats = new LinkedHashMap<>();
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return stats;

            // Total parties
            ps = connexion.prepareStatement("SELECT COUNT(*) FROM parties");
            rs = ps.executeQuery();
            stats.put("totalParties", rs.next() ? rs.getInt(1) : 0);
            rs.close();
            ps.close();

            // Total joueurs distincts
            ps = connexion.prepareStatement("SELECT COUNT(DISTINCT id_joueur) FROM parties");
            rs = ps.executeQuery();
            stats.put("totalJoueurs", rs.next() ? rs.getInt(1) : 0);
            rs.close();
            ps.close();

            // Taux de réussite global
            ps = connexion.prepareStatement(
                "SELECT ROUND(AVG(CASE WHEN score >= 8 THEN 100.0 ELSE 0 END), 1) FROM parties");
            rs = ps.executeQuery();
            stats.put("tauxReussite", rs.next() ? rs.getDouble(1) : 0);
            rs.close();
            ps.close();

            // Parties aujourd'hui
            ps = connexion.prepareStatement("SELECT COUNT(*) FROM parties WHERE DATE(date_partie) = CURDATE()");
            rs = ps.executeQuery();
            stats.put("partiesAujourdhui", rs.next() ? rs.getInt(1) : 0);
            rs.close();
            ps.close();

            // Score moyen
            ps = connexion.prepareStatement(
                "SELECT ROUND(AVG((score * 100.0) / total_questions), 1) FROM parties");
            rs = ps.executeQuery();
            stats.put("scoreMoyen", rs.next() ? rs.getDouble(1) : 0);

        } catch (SQLException e) {
            System.err.println("❌ [AdminResultats] Erreur stats : " + e.getMessage());
        } finally {
            fermerRessources(null, ps, rs);
        }

        return stats;
    }

    /**
     * Ferme les ressources JDBC
     */
    private void fermerRessources(Connection connexion, PreparedStatement ps, ResultSet rs) {
        if (rs != null) { try { rs.close(); } catch (SQLException e) { /* Ignorer */ } }
        if (ps != null) { try { ps.close(); } catch (SQLException e) { /* Ignorer */ } }
        if (connexion != null) { try { connexion.close(); } catch (SQLException e) { /* Ignorer */ } }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}