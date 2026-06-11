// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/AdminCoursLeconsServlet.java
// RÔLE    : Contrôleur pour la liste des leçons d'un chapitre
//           GET : Affiche toutes les leçons avec leurs métadonnées
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.ChapitreDAO;
import com.learnpath.dao.CoursDAO;
import com.learnpath.dao.LeconDAO;
import com.learnpath.models.Chapitre;
import com.learnpath.models.Cours;
import com.learnpath.models.Lecon;
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

/**
 * AdminCoursLeconsServlet - Liste des leçons d'un chapitre
 * 
 * Affiche toutes les leçons avec :
 * - Titre, ordre, temps de lecture estimé
 * - Badges : Gratuit/Premium, Code source, Ressources
 * - Actions : Modifier, Aperçu, Ressources, Supprimer
 * 
 * URL : /admin/cours/lecons?chapitreId=X&coursId=Y
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebServlet(name = "AdminCoursLeconsServlet", urlPatterns = {"/admin/cours/lecons"})
public class AdminCoursLeconsServlet extends HttpServlet {

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

        // 2. Récupération et validation des paramètres
        String chapitreIdStr = request.getParameter("chapitreId");
        String coursIdStr = request.getParameter("coursId");

        if (chapitreIdStr == null || chapitreIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID du chapitre manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        int chapitreId;
        try {
            chapitreId = Integer.parseInt(chapitreIdStr.trim());
            if (chapitreId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de chapitre invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        int coursId = 0;
        if (coursIdStr != null && !coursIdStr.trim().isEmpty()) {
            try { coursId = Integer.parseInt(coursIdStr.trim()); } catch (NumberFormatException e) {}
        }

        // 3. Récupération du chapitre
        ChapitreDAO chapitreDAO = new ChapitreDAO();
        Chapitre chapitre = chapitreDAO.trouverParId(chapitreId);

        if (chapitre == null) {
            session.setAttribute("messageErreur", "Ce chapitre n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        if (coursId == 0) coursId = chapitre.getCoursId();

        // 4. Récupération du cours
        CoursDAO coursDAO = new CoursDAO();
        Cours cours = coursDAO.trouverParId(coursId);

        // 5. Récupération des leçons
        LeconDAO leconDAO = new LeconDAO();
        List<Lecon> lecons = leconDAO.listerParChapitre(chapitreId);

        // 6. Enrichir avec les métadonnées
        List<Map<String, Object>> leconsAvecMeta = new ArrayList<>();
        if (lecons != null) {
            for (Lecon lecon : lecons) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("lecon", lecon);
                data.put("hasCodeSource", lecon.hasCodeSource());
                data.put("hasRessources", lecon.hasRessources());
                data.put("nbRessources", lecon.getNombreRessources());
                data.put("tempsLecture", lecon.getTempsLectureFormate());
                data.put("nombreMots", lecon.getNombreMots());
                leconsAvecMeta.add(data);
            }
        }

        int totalLecons = lecons != null ? lecons.size() : 0;
        int leconsGratuites = leconDAO.compterGratuitesParChapitre(chapitreId);

        // 7. Message de succès/erreur
        String messageSucces = (String) session.getAttribute("messageSucces");
        String messageErreur = (String) session.getAttribute("messageErreur");
        if (messageSucces != null) session.removeAttribute("messageSucces");
        if (messageErreur != null) session.removeAttribute("messageErreur");

        // 8. Passage des attributs
        request.setAttribute("pageTitle", "Leçons - " + chapitre.getTitre() + " - Administration");
        request.setAttribute("cours", cours);
        request.setAttribute("chapitre", chapitre);
        request.setAttribute("lecons", leconsAvecMeta);
        request.setAttribute("totalLecons", totalLecons);
        request.setAttribute("leconsGratuites", leconsGratuites);
        request.setAttribute("leconsPremium", totalLecons - leconsGratuites);
        request.setAttribute("messageSucces", messageSucces);
        request.setAttribute("messageErreur", messageErreur);

        // 9. Affichage
        request.getRequestDispatcher("/views/admin/admin-cours-lecons.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}