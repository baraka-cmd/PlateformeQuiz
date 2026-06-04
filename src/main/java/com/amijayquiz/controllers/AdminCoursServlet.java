// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminCoursServlet.java
// RÔLE    : Contrôleur pour la liste des cours (admin)
//           - Liste paginée avec filtres et recherche
//           - Statistiques globales
//           - Actions : Voir, Modifier, Modules, Chapitres, Supprimer
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.CoursDAO;
import com.amijayquiz.dao.ThemeDAO;
import com.amijayquiz.models.Cours;
import com.amijayquiz.models.Utilisateur;

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

/**
 * AdminCoursServlet - Liste des cours pour l'administrateur
 * 
 * Affiche tous les cours avec :
 * - Pagination (15 par page)
 * - Filtres : thème, niveau, statut
 * - Recherche textuelle
 * - Tri : ID, titre, thème, niveau, date, apprenants
 * - Statistiques globales
 * - Actions par cours
 * 
 * URL : /admin/cours
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminCoursServlet", urlPatterns = {"/admin/cours"})
public class AdminCoursServlet extends HttpServlet {

    // Nombre de cours par page
    private static final int COURS_PAR_PAGE = 15;

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

        // 2. Récupération des paramètres de filtre
        String recherche = request.getParameter("recherche");
        String filtreTheme = request.getParameter("theme");
        String filtreNiveau = request.getParameter("niveau");
        String filtreStatut = request.getParameter("statut");
        String tri = request.getParameter("tri");
        String pageStr = request.getParameter("page");

        // Valeurs par défaut
        if (recherche == null) recherche = "";
        if (filtreTheme == null) filtreTheme = "tous";
        if (filtreNiveau == null) filtreNiveau = "tous";
        if (filtreStatut == null) filtreStatut = "tous";
        if (tri == null) tri = "id";
        int page = 1;
        try {
            if (pageStr != null && !pageStr.isEmpty()) {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        // 3. Initialisation des DAO
        CoursDAO coursDAO = new CoursDAO();
        ThemeDAO themeDAO = new ThemeDAO();

        // 4. Récupération de tous les cours
        List<Cours> tousLesCours = coursDAO.listerTous();

        // 5. Récupération des thèmes pour le filtre
        List<com.amijayquiz.models.Theme> themes = themeDAO.listerTous();

        // 6. Application des filtres
        List<Cours> coursFiltres = new ArrayList<>(tousLesCours);

        // Filtre par thème
        if (!"tous".equals(filtreTheme)) {
            List<Cours> filtresTheme = new ArrayList<>();
            for (Cours c : coursFiltres) {
                if (c.getTheme() != null && c.getTheme().equals(filtreTheme)) {
                    filtresTheme.add(c);
                }
            }
            coursFiltres = filtresTheme;
        }

        // Filtre par niveau
        if (!"tous".equals(filtreNiveau)) {
            List<Cours> filtresNiveau = new ArrayList<>();
            for (Cours c : coursFiltres) {
                if (c.getNiveau() != null && c.getNiveau().equals(filtreNiveau)) {
                    filtresNiveau.add(c);
                }
            }
            coursFiltres = filtresNiveau;
        }

        // Filtre par statut
        if (!"tous".equals(filtreStatut)) {
            List<Cours> filtresStatut = new ArrayList<>();
            for (Cours c : coursFiltres) {
                if (c.getStatut() != null && c.getStatut().equalsIgnoreCase(filtreStatut)) {
                    filtresStatut.add(c);
                }
            }
            coursFiltres = filtresStatut;
        }

        // Recherche textuelle
        if (recherche != null && !recherche.trim().isEmpty()) {
            String termeRecherche = recherche.toLowerCase().trim();
            List<Cours> filtresRecherche = new ArrayList<>();
            for (Cours c : coursFiltres) {
                boolean correspond = false;
                if (c.getTitre() != null && c.getTitre().toLowerCase().contains(termeRecherche)) {
                    correspond = true;
                }
                if (c.getDescription() != null && c.getDescription().toLowerCase().contains(termeRecherche)) {
                    correspond = true;
                }
                if (c.getTheme() != null && c.getTheme().toLowerCase().contains(termeRecherche)) {
                    correspond = true;
                }
                if (correspond) {
                    filtresRecherche.add(c);
                }
            }
            coursFiltres = filtresRecherche;
        }

        // 7. Tri
        trierCours(coursFiltres, tri);

        // 8. Pagination
        int totalCoursFiltres = coursFiltres.size();
        int totalPages = (int) Math.ceil((double) totalCoursFiltres / COURS_PAR_PAGE);
        if (totalPages < 1) totalPages = 1;
        if (page > totalPages) page = totalPages;

        int debut = (page - 1) * COURS_PAR_PAGE;
        int fin = Math.min(debut + COURS_PAR_PAGE, totalCoursFiltres);
        List<Cours> coursPage;
        if (totalCoursFiltres > 0) {
            coursPage = coursFiltres.subList(debut, fin);
        } else {
            coursPage = new ArrayList<>();
        }

        // 9. Pour chaque cours, ajouter les statistiques (apprenants, certificats)
        List<Map<String, Object>> coursAvecStats = new ArrayList<>();
        for (Cours cours : coursPage) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("cours", cours);
            data.put("nbApprenants", coursDAO.compterApprenants(cours.getId()));
            data.put("nbCertificats", coursDAO.compterCertificats(cours.getId()));
            coursAvecStats.add(data);
        }

        // 10. Statistiques globales
        Map<String, Object> statsGlobales = calculerStatistiques(tousLesCours, coursDAO);

        // 11. Construction de l'URL de pagination
        StringBuilder urlPagination = new StringBuilder("/admin/cours?");
        if (recherche != null && !recherche.isEmpty()) urlPagination.append("recherche=").append(recherche).append("&");
        if (!"tous".equals(filtreTheme)) urlPagination.append("theme=").append(filtreTheme).append("&");
        if (!"tous".equals(filtreNiveau)) urlPagination.append("niveau=").append(filtreNiveau).append("&");
        if (!"tous".equals(filtreStatut)) urlPagination.append("statut=").append(filtreStatut).append("&");
        if (!"id".equals(tri)) urlPagination.append("tri=").append(tri).append("&");

        // 12. Message de succès/erreur
        String messageSucces = (String) session.getAttribute("messageSucces");
        String messageErreur = (String) session.getAttribute("messageErreur");
        if (messageSucces != null) session.removeAttribute("messageSucces");
        if (messageErreur != null) session.removeAttribute("messageErreur");

        // 13. Passage des attributs
        request.setAttribute("pageTitle", "Gestion des cours - Administration - AmijayQuiz");
        request.setAttribute("coursAvecStats", coursAvecStats);
        request.setAttribute("totalCours", tousLesCours.size());
        request.setAttribute("totalCoursFiltres", totalCoursFiltres);
        request.setAttribute("page", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("recherche", recherche);
        request.setAttribute("filtreTheme", filtreTheme);
        request.setAttribute("filtreNiveau", filtreNiveau);
        request.setAttribute("filtreStatut", filtreStatut);
        request.setAttribute("tri", tri);
        request.setAttribute("themes", themes);
        request.setAttribute("statsGlobales", statsGlobales);
        request.setAttribute("urlPagination", urlPagination.toString());
        request.setAttribute("messageSucces", messageSucces);
        request.setAttribute("messageErreur", messageErreur);

        // 14. Affichage
        request.getRequestDispatcher("/views/admin/admin-cours.jsp").forward(request, response);
    }

    /**
     * Trie la liste des cours selon le critère spécifié
     */
    private void trierCours(List<Cours> cours, String tri) {
        switch (tri) {
            case "titre":
                cours.sort((a, b) -> {
                    String titreA = a.getTitre() != null ? a.getTitre() : "";
                    String titreB = b.getTitre() != null ? b.getTitre() : "";
                    return titreA.compareToIgnoreCase(titreB);
                });
                break;
            case "titre-desc":
                cours.sort((a, b) -> {
                    String titreA = a.getTitre() != null ? a.getTitre() : "";
                    String titreB = b.getTitre() != null ? b.getTitre() : "";
                    return titreB.compareToIgnoreCase(titreA);
                });
                break;
            case "theme":
                cours.sort((a, b) -> {
                    String themeA = a.getTheme() != null ? a.getTheme() : "";
                    String themeB = b.getTheme() != null ? b.getTheme() : "";
                    return themeA.compareToIgnoreCase(themeB);
                });
                break;
            case "niveau":
                cours.sort((a, b) -> {
                    String nivA = a.getNiveau() != null ? a.getNiveau() : "";
                    String nivB = b.getNiveau() != null ? b.getNiveau() : "";
                    return nivA.compareTo(nivB);
                });
                break;
            case "recent":
                cours.sort((a, b) -> {
                    if (a.getDateCreation() == null) return 1;
                    if (b.getDateCreation() == null) return -1;
                    return b.getDateCreation().compareTo(a.getDateCreation());
                });
                break;
            case "ancien":
                cours.sort((a, b) -> {
                    if (a.getDateCreation() == null) return 1;
                    if (b.getDateCreation() == null) return -1;
                    return a.getDateCreation().compareTo(b.getDateCreation());
                });
                break;
            case "id":
            default:
                cours.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
                break;
        }
    }

    /**
     * Calcule les statistiques globales des cours
     */
    private Map<String, Object> calculerStatistiques(List<Cours> tousLesCours, CoursDAO coursDAO) {
        Map<String, Object> stats = new LinkedHashMap<>();

        int total = tousLesCours.size();
        int brouillon = 0;
        int publie = 0;
        int archive = 0;
        int totalApprenants = 0;
        int totalCertificats = 0;

        // Compter les thèmes distincts
        List<String> themesDistincts = new ArrayList<>();
        for (Cours c : tousLesCours) {
            // Statuts
            String statut = c.getStatut();
            if ("brouillon".equals(statut)) brouillon++;
            else if ("publié".equals(statut) || "publié".equals(statut)) publie++;
            else if ("archivé".equals(statut) || "archive".equals(statut)) archive++;

            // Thèmes distincts
            String theme = c.getTheme();
            if (theme != null && !themesDistincts.contains(theme)) {
                themesDistincts.add(theme);
            }

            // Apprenants et certificats
            totalApprenants += coursDAO.compterApprenants(c.getId());
            totalCertificats += coursDAO.compterCertificats(c.getId());
        }

        stats.put("total", total);
        stats.put("brouillon", brouillon);
        stats.put("publie", publie);
        stats.put("archive", archive);
        stats.put("themes", themesDistincts.size());
        stats.put("totalApprenants", totalApprenants);
        stats.put("totalCertificats", totalCertificats);

        return stats;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}