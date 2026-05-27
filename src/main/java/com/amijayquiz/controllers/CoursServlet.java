// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/CoursServlet.java
// RÔLE    : Contrôleur principal de la section Cours
//           Affiche la liste des thèmes disponibles avec progression
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.CoursDAO;
import com.amijayquiz.dao.ProgressionCoursDAO;
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
 * CoursServlet - Page principale de la section Cours
 * 
 * Affiche tous les thèmes de cours disponibles avec :
 * - Nombre de cours par thème
 * - Nombre de cours terminés
 * - Pourcentage de progression global
 * - Badge "Premium" sur les cours payants
 * 
 * URL : /joueur/cours
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "CoursServlet", urlPatterns = {"/joueur/cours"})
public class CoursServlet extends HttpServlet {

    // Configuration des thèmes (icône, couleur)
    private static final Map<String, String[]> THEMES_CONFIG = new LinkedHashMap<>();

    static {
        THEMES_CONFIG.put("Développement Web",       new String[]{"fas fa-globe",         "#667eea"});
        THEMES_CONFIG.put("Langage Java",            new String[]{"fas fa-coffee",        "#f093fb"});
        THEMES_CONFIG.put("Langage Python",          new String[]{"fab fa-python",        "#4facfe"});
        THEMES_CONFIG.put("Langage PHP",             new String[]{"fab fa-php",           "#8892bf"});
        THEMES_CONFIG.put("Bases de Données & SQL",  new String[]{"fas fa-database",      "#43e97b"});
        THEMES_CONFIG.put("Cybersécurité",           new String[]{"fas fa-shield-alt",    "#f5576c"});
        THEMES_CONFIG.put("Développement Mobile",    new String[]{"fas fa-mobile-alt",    "#f39c12"});
        THEMES_CONFIG.put("Intelligence Artificielle",new String[]{"fas fa-robot",        "#9b59b6"});
        THEMES_CONFIG.put("Design UI/UX",            new String[]{"fas fa-palette",       "#e91e63"});
        THEMES_CONFIG.put("Réseaux Informatiques",   new String[]{"fas fa-network-wired", "#00bcd4"});
        THEMES_CONFIG.put("Systèmes d''Exploitation",new String[]{"fab fa-linux",         "#ff9800"});
        THEMES_CONFIG.put("Data Science",            new String[]{"fas fa-chart-pie",     "#795548"});
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Vérification de la session et du rôle
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (!"joueur".equals(utilisateur.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        int utilisateurId = utilisateur.getId();

        // 2. Initialisation des DAO
        CoursDAO coursDAO = new CoursDAO();
        ProgressionCoursDAO progressionDAO = new ProgressionCoursDAO();

        // 3. Récupération de tous les cours publiés
        List<Cours> tousLesCours = coursDAO.listerPubliés();

        // 4. Construction de la liste des thèmes avec progression
        List<Map<String, Object>> themesList = new ArrayList<>();

        // Grouper les cours par thème
        Map<String, List<Cours>> coursParTheme = new LinkedHashMap<>();
        for (Cours cours : tousLesCours) {
            String theme = cours.getTheme();
            if (!coursParTheme.containsKey(theme)) {
                coursParTheme.put(theme, new ArrayList<>());
            }
            coursParTheme.get(theme).add(cours);
        }

        // Construire les données pour chaque thème
        for (Map.Entry<String, List<Cours>> entry : coursParTheme.entrySet()) {
            String nomTheme = entry.getKey();
            List<Cours> coursDuTheme = entry.getValue();

            // Récupérer la config (icône, couleur)
            String[] config = THEMES_CONFIG.getOrDefault(nomTheme,
                    new String[]{"fas fa-book", "#667eea"});

            Map<String, Object> themeData = new LinkedHashMap<>();
            themeData.put("nom", nomTheme);
            themeData.put("icone", config[0]);
            themeData.put("couleur", config[1]);

            int totalCours = coursDuTheme.size();
            int coursTermines = 0;
            int totalLecons = 0;
            int leconsTerminees = 0;

            // Compter les cours terminés
            for (Cours cours : coursDuTheme) {
                totalLecons += cours.getNombreTotalLecons() > 0 ? 
                    cours.getNombreTotalLecons() : 
                    progressionDAO.getNombreTotalLecons(cours.getId());
                
                leconsTerminees += progressionDAO.getNombreLeconsTerminees(utilisateurId, cours.getId());
                
                if (progressionDAO.estCoursTermine(utilisateurId, cours.getId())) {
                    coursTermines++;
                }
            }

            int pourcentageGlobal = (totalLecons > 0) ? (leconsTerminees * 100 / totalLecons) : 0;

            themeData.put("totalCours", totalCours);
            themeData.put("coursTermines", coursTermines);
            themeData.put("totalLecons", totalLecons);
            themeData.put("leconsTerminees", leconsTerminees);
            themeData.put("pourcentageProgression", pourcentageGlobal);

            themesList.add(themeData);
        }

        // 5. Statistiques globales
        int totalCoursPlateforme = tousLesCours.size();
        int totalCoursTermines = 0;
        int totalLeconsPlateforme = 0;
        int totalLeconsTerminees = 0;

        for (Cours cours : tousLesCours) {
            totalLeconsPlateforme += cours.getNombreTotalLecons() > 0 ?
                cours.getNombreTotalLecons() :
                progressionDAO.getNombreTotalLecons(cours.getId());
            
            totalLeconsTerminees += progressionDAO.getNombreLeconsTerminees(utilisateurId, cours.getId());
            
            if (progressionDAO.estCoursTermine(utilisateurId, cours.getId())) {
                totalCoursTermines++;
            }
        }

        int pourcentageGlobalPlateforme = (totalLeconsPlateforme > 0) ? 
            (totalLeconsTerminees * 100 / totalLeconsPlateforme) : 0;

        // 6. Passage des attributs à la JSP
        request.setAttribute("pageTitle", "Cours - AmijayQuiz");
        request.setAttribute("themes", themesList);
        request.setAttribute("totalCours", totalCoursPlateforme);
        request.setAttribute("totalCoursTermines", totalCoursTermines);
        request.setAttribute("totalLecons", totalLeconsPlateforme);
        request.setAttribute("totalLeconsTerminees", totalLeconsTerminees);
        request.setAttribute("pourcentageGlobal", pourcentageGlobalPlateforme);

        // 7. Affichage de la page
        request.getRequestDispatcher("/views/joueur/cours-themes.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}