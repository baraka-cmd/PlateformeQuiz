// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/AdminThemesServlet.java
// RÔLE    : Contrôleur pour la liste des thèmes avec filtrage,
//           tri et pagination fonctionnels (Rétention d'état).
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.ThemeDAO;
import com.learnpath.models.Theme;
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
import java.util.stream.Collectors;

@WebServlet(name = "AdminThemesServlet", urlPatterns = {"/admin/themes"})
public class AdminThemesServlet extends HttpServlet {

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

        // ============================================================
        // RECOUVREMENT DES PARAMÈTRES DE FILTRAGE & TRI
        // ============================================================
        String recherche = request.getParameter("recherche");
        String filtreOrdre = request.getParameter("ordre"); 
        String tri = request.getParameter("tri");
        String pageParam = request.getParameter("page");

        // Application des valeurs par défaut si absents
        if (recherche == null) recherche = "";
        if (filtreOrdre == null) filtreOrdre = "tous";
        if (tri == null) tri = "ordre";
        
        int pageActuelle = 1;
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try { 
                pageActuelle = Integer.parseInt(pageParam); 
            } catch(NumberFormatException e) { 
                pageActuelle = 1; 
            }
        }

        // 2. Récupération des données initiales depuis la BDD
        ThemeDAO themeDAO = new ThemeDAO();
        List<Theme> tousLesThemes = themeDAO.listerTous();
        int totalThemesGlobal = tousLesThemes != null ? tousLesThemes.size() : 0;

        // 3. Construction de la structure de données complète (Thème + nbCours)
        List<Map<String, Object>> themesAvecStats = new ArrayList<>();
        if (tousLesThemes != null) {
            for (Theme theme : tousLesThemes) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("theme", theme);
                data.put("nbCours", themeDAO.compterCoursParTheme(theme.getNom()));
                themesAvecStats.add(data);
            }
        }

        // ============================================================
        // CORRECTION : Déclaration des variables en "final" pour la Lambda
        // ============================================================
        final String finalRecherche = recherche.toLowerCase().trim();
        final String finalFiltreOrdre = filtreOrdre;
        final String finalTri = tri;

        List<Map<String, Object>> resultatsFiltrés = themesAvecStats.stream()
            .filter(data -> {
                // Filtre par texte recherché (Nom ou Description)
                Theme t = (Theme) data.get("theme");
                boolean matchRecherche = finalRecherche.isEmpty() || 
                    (t.getNom() != null && t.getNom().toLowerCase().contains(finalRecherche)) ||
                    (t.getDescription() != null && t.getDescription().toLowerCase().contains(finalRecherche));
                
                // Filtre par liaison de cours
                int nbCours = (Integer) data.get("nbCours");
                boolean matchCours = "tous".equals(finalFiltreOrdre) ||
                    ("avec-cours".equals(finalFiltreOrdre) && nbCours > 0) ||
                    ("sans-cours".equals(finalFiltreOrdre) && nbCours == 0);

                return matchRecherche && matchCours;
            })
            .sorted((d1, d2) -> {
                Theme t1 = (Theme) d1.get("theme");
                Theme t2 = (Theme) d2.get("theme");
                
                // Utilisation de la variable finalTri (sécurisée pour la lambda)
                switch (finalTri) {
                    case "nom":
                        return t1.getNom().compareToIgnoreCase(t2.getNom());
                    case "nom-desc":
                        return t2.getNom().compareToIgnoreCase(t1.getNom());
                    case "cours":
                        return Integer.compare((Integer) d2.get("nbCours"), (Integer) d1.get("nbCours"));
                    case "recent":
                        if (t1.getDateCreation() == null || t2.getDateCreation() == null) return 0;
                        return t2.getDateCreation().compareTo(t1.getDateCreation());
                    case "ancien":
                        if (t1.getDateCreation() == null || t2.getDateCreation() == null) return 0;
                        return t1.getDateCreation().compareTo(t2.getDateCreation());
                    case "ordre":
                    default:
                        return Integer.compare(t1.getOrdre(), t2.getOrdre());
                }
            })
            .collect(Collectors.toList());

        // ============================================================
        // GESTION DE LA PAGINATION SUR LA LISTE FILTRÉE
        // ============================================================
        int totalThemesFiltres = resultatsFiltrés.size();
        int limiteParPage = 10; 
        int totalPages = (int) Math.ceil((double) totalThemesFiltres / limiteParPage);
        if (totalPages == 0) totalPages = 1;
        if (pageActuelle > totalPages) pageActuelle = totalPages;

        int indexDebut = (pageActuelle - 1) * limiteParPage;
        int indexFin = Math.min(indexDebut + limiteParPage, totalThemesFiltres);
        
        List<Map<String, Object>> resultatsPagines = new ArrayList<>();
        if (indexDebut < totalThemesFiltres) {
            resultatsPagines = resultatsFiltrés.subList(indexDebut, indexFin);
        }

        // Reconstruire la QueryString propre pour les liens de pagination du JSP
        String urlPagination = "/admin/themes?recherche=" + java.net.URLEncoder.encode(recherche, "UTF-8") 
                             + "&ordre=" + filtreOrdre 
                             + "&tri=" + tri + "&";

        // 4. Message de succès/erreur
        String messageSucces = (String) session.getAttribute("messageSucces");
        String messageErreur = (String) session.getAttribute("messageErreur");
        if (messageSucces != null) session.removeAttribute("messageSucces");
        if (messageErreur != null) session.removeAttribute("messageErreur");

        // 5. Passage des attributs obligatoires attendus par le JSP
        request.setAttribute("pageTitle", "Gestion des thèmes - Administration - LearnPath");
        request.setAttribute("themes", resultatsPagines); 
        request.setAttribute("totalThemes", totalThemesGlobal); 
        request.setAttribute("totalThemesFiltres", totalThemesFiltres); 
        
        // Renvoyer l'état actuel des filtres au JSP pour la rétention d'état
        request.setAttribute("recherche", recherche);
        request.setAttribute("filtreOrdre", filtreOrdre);
        request.setAttribute("tri", tri);
        request.setAttribute("page", pageActuelle);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("urlPagination", urlPagination);

        request.setAttribute("messageSucces", messageSucces);
        request.setAttribute("messageErreur", messageErreur);

        // 6. Affichage de la vue
        request.getRequestDispatcher("/views/admin/admin-themes.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
