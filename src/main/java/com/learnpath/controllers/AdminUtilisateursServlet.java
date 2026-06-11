// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/AdminUtilisateursServlet.java
// RÔLE    : Contrôleur pour la gestion des utilisateurs (admin)
//           GET  : Liste paginée avec filtres, recherche, statistiques
//           SÉCURISÉ : Seul le Super Admin peut accéder
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.AccessPremiumDAO;
import com.learnpath.dao.UtilisateurDAO;
import com.learnpath.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AdminUtilisateursServlet", urlPatterns = {"/admin/utilisateurs"})
public class AdminUtilisateursServlet extends HttpServlet {

    private static final int UTILISATEURS_PAR_PAGE = 20;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Vérification authentification
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        Utilisateur adminConnecte = (Utilisateur) session.getAttribute("utilisateur");

        // 2. 🔒 VÉRIFICATION SUPER ADMIN UNIQUEMENT
        if (!"admin".equals(adminConnecte.getRole())) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        int adminId = adminConnecte.getId();

        // 3. Récupération des paramètres
        String recherche = request.getParameter("recherche");
        String filtreRole = request.getParameter("role");
        String filtreStatut = request.getParameter("statut");
        String filtrePremium = request.getParameter("premium");
        String tri = request.getParameter("tri");
        String ordre = request.getParameter("ordre");
        String pageStr = request.getParameter("page");

        // Valeurs par défaut
        if (recherche == null) recherche = "";
        if (filtreRole == null) filtreRole = "tous";
        if (filtreStatut == null) filtreStatut = "tous";
        if (filtrePremium == null) filtrePremium = "tous";
        if (tri == null) tri = "id";
        if (ordre == null) ordre = "DESC";

        int page = 1;
        try {
            if (pageStr != null && !pageStr.isEmpty()) {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        System.out.println("📋 [AdminUtilisateurs] Paramètres - Recherche: '" + recherche + 
            "', Rôle: " + filtreRole + ", Statut: " + filtreStatut + 
            ", Premium: " + filtrePremium + ", Tri: " + tri + ", Page: " + page);

        // 4. Initialisation du DAO
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        AccessPremiumDAO premiumDAO = new AccessPremiumDAO();

        try {
            // 5. Comptage total
            int totalUtilisateurs = utilisateurDAO.compterTous(recherche, filtreRole, filtreStatut, filtrePremium);
            int totalPages = (int) Math.ceil((double) totalUtilisateurs / UTILISATEURS_PAR_PAGE);
            if (totalPages < 1) totalPages = 1;
            if (page > totalPages) page = totalPages;

            int offset = (page - 1) * UTILISATEURS_PAR_PAGE;

            // 6. Récupération des utilisateurs
            List<Utilisateur> utilisateurs = utilisateurDAO.listerTous(
                offset, UTILISATEURS_PAR_PAGE, recherche, filtreRole, filtreStatut, filtrePremium, tri, ordre
            );

            System.out.println("📋 [AdminUtilisateurs] Résultats trouvés : " + 
                (utilisateurs != null ? utilisateurs.size() : 0) + " / " + totalUtilisateurs);

            // 7. Enrichir avec les stats premium
            List<Map<String, Object>> utilisateursAvecStats = new ArrayList<>();
            if (utilisateurs != null) {
                for (Utilisateur u : utilisateurs) {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("utilisateur", u);
                    data.put("estPremium", premiumDAO.estPremium(u.getId()));
                    data.put("infosPremium", premiumDAO.getInfosPremium(u.getId()));
                    utilisateursAvecStats.add(data);
                }
            }

            // 8. Statistiques globales
            Map<String, Object> statsGlobales = new LinkedHashMap<>();
            statsGlobales.put("total", totalUtilisateurs);
            statsGlobales.put("admins", utilisateurDAO.compterParRole("admin"));
            statsGlobales.put("gestionnaires", utilisateurDAO.compterParRole("gestionnaire"));
            statsGlobales.put("joueurs", utilisateurDAO.compterParRole("joueur"));
            statsGlobales.put("actifs", utilisateurDAO.compterParStatut(true));
            statsGlobales.put("inactifs", utilisateurDAO.compterParStatut(false));
            statsGlobales.put("premium", premiumDAO.compterPremiumActifs());

            // 9. URL pagination
            StringBuilder urlPagination = new StringBuilder("/admin/utilisateurs?");
            if (recherche != null && !recherche.isEmpty()) urlPagination.append("recherche=").append(recherche).append("&");
            if (!"tous".equals(filtreRole)) urlPagination.append("role=").append(filtreRole).append("&");
            if (!"tous".equals(filtreStatut)) urlPagination.append("statut=").append(filtreStatut).append("&");
            if (!"tous".equals(filtrePremium)) urlPagination.append("premium=").append(filtrePremium).append("&");
            if (!"id".equals(tri)) urlPagination.append("tri=").append(tri).append("&");
            if (!"DESC".equals(ordre)) urlPagination.append("ordre=").append(ordre).append("&");

            // 10. Passage des attributs
            request.setAttribute("pageTitle", "Gestion des utilisateurs - Administration - LearnPath");
            request.setAttribute("utilisateurs", utilisateursAvecStats);
            request.setAttribute("totalUtilisateurs", totalUtilisateurs);
            request.setAttribute("page", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("stats", statsGlobales);
            request.setAttribute("urlPagination", urlPagination.toString());
            request.setAttribute("adminId", adminId);

        } catch (Exception e) {
            System.err.println("❌ [AdminUtilisateurs] Erreur : " + e.getMessage());
            e.printStackTrace();
            
            request.setAttribute("utilisateurs", new ArrayList<>());
            request.setAttribute("totalUtilisateurs", 0);
            request.setAttribute("page", 1);
            request.setAttribute("totalPages", 1);
            request.setAttribute("stats", new LinkedHashMap<>());
            request.setAttribute("urlPagination", "/admin/utilisateurs?");
            request.setAttribute("adminId", adminId);
            
            session.setAttribute("messageErreur", "Une erreur est survenue lors du chargement des utilisateurs.");
        }

        // 11. Messages flash
        String messageSucces = (String) session.getAttribute("messageSucces");
        String messageErreur = (String) session.getAttribute("messageErreur");
        if (messageSucces != null) session.removeAttribute("messageSucces");
        if (messageErreur != null) session.removeAttribute("messageErreur");
        
        request.setAttribute("messageSucces", messageSucces);
        request.setAttribute("messageErreur", messageErreur);
        request.setAttribute("recherche", recherche);
        request.setAttribute("filtreRole", filtreRole);
        request.setAttribute("filtreStatut", filtreStatut);
        request.setAttribute("filtrePremium", filtrePremium);
        request.setAttribute("tri", tri);
        request.setAttribute("ordre", ordre);

        // 12. Affichage
        request.getRequestDispatcher("/views/admin/admin-utilisateurs.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}