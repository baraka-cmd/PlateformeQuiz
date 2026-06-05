// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminCoursChapitresServlet.java
// RÔLE    : Contrôleur pour la liste des chapitres d'un cours
//           GET : Affiche les chapitres groupés par module
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.ChapitreDAO;
import com.amijayquiz.dao.CoursDAO;
import com.amijayquiz.dao.LeconDAO;
import com.amijayquiz.models.Chapitre;
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
 * AdminCoursChapitresServlet - Liste des chapitres d'un cours
 * 
 * Affiche tous les chapitres groupés par module (1 à 15).
 * Pour chaque chapitre : titre, nombre de leçons, actions.
 * 
 * URL : /admin/cours/chapitres?coursId=X
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminCoursChapitresServlet", urlPatterns = {"/admin/cours/chapitres"})
public class AdminCoursChapitresServlet extends HttpServlet {

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
        if (!"admin".equals(utilisateur.getRole()) && !"gestionnaire".equals(utilisateur.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        // 2. Récupération et validation du paramètre
        String coursIdStr = request.getParameter("coursId");
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

        // 4. Récupération de tous les chapitres
        ChapitreDAO chapitreDAO = new ChapitreDAO();
        LeconDAO leconDAO = new LeconDAO();
        List<Chapitre> tousLesChapitres = chapitreDAO.listerParCours(coursId);

        // 5. Grouper les chapitres par module
        Map<Integer, List<Chapitre>> chapitresParModule = new LinkedHashMap<>();
        for (int i = 1; i <= NOMBRE_MODULES; i++) {
            chapitresParModule.put(i, new ArrayList<>());
        }

        if (tousLesChapitres != null) {
            for (Chapitre chapitre : tousLesChapitres) {
                int module = chapitre.getModuleNumero();
                if (module < 1) module = 1;
                if (module > NOMBRE_MODULES) module = NOMBRE_MODULES;
                chapitresParModule.get(module).add(chapitre);
            }
        }

        // 6. Ajouter le nombre de leçons pour chaque chapitre
        List<Map<String, Object>> modulesData = new ArrayList<>();
        int totalChapitres = 0;
        int totalLecons = 0;

        for (int moduleNumero = 1; moduleNumero <= NOMBRE_MODULES; moduleNumero++) {
            List<Chapitre> chapitresDuModule = chapitresParModule.get(moduleNumero);
            if (chapitresDuModule == null) chapitresDuModule = new ArrayList<>();

            List<Map<String, Object>> chapitresData = new ArrayList<>();
            int nbLeconsModule = 0;

            for (Chapitre chapitre : chapitresDuModule) {
                Map<String, Object> chapitreData = new LinkedHashMap<>();
                chapitreData.put("chapitre", chapitre);
                int nbLecons = leconDAO.compterParChapitre(chapitre.getId());
                chapitreData.put("nbLecons", nbLecons);
                chapitresData.add(chapitreData);
                nbLeconsModule += nbLecons;
            }

            Map<String, Object> moduleData = new LinkedHashMap<>();
            moduleData.put("numero", moduleNumero);
            moduleData.put("chapitres", chapitresData);
            moduleData.put("nbChapitres", chapitresDuModule.size());
            moduleData.put("nbLecons", nbLeconsModule);

            modulesData.add(moduleData);

            totalChapitres += chapitresDuModule.size();
            totalLecons += nbLeconsModule;
        }

        // 7. Message de succès/erreur
        String messageSucces = (String) session.getAttribute("messageSucces");
        String messageErreur = (String) session.getAttribute("messageErreur");
        if (messageSucces != null) session.removeAttribute("messageSucces");
        if (messageErreur != null) session.removeAttribute("messageErreur");

        // 8. Passage des attributs
        request.setAttribute("pageTitle", "Chapitres - " + cours.getTitre() + " - Administration");
        request.setAttribute("cours", cours);
        request.setAttribute("modules", modulesData);
        request.setAttribute("nombreModules", NOMBRE_MODULES);
        request.setAttribute("totalChapitres", totalChapitres);
        request.setAttribute("totalLecons", totalLecons);
        request.setAttribute("messageSucces", messageSucces);
        request.setAttribute("messageErreur", messageErreur);

        // 9. Affichage
        request.getRequestDispatcher("/views/admin/admin-cours-chapitres.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}