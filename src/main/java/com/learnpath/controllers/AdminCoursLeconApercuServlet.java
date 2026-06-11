// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/AdminCoursLeconApercuServlet.java
// RÔLE    : Contrôleur pour afficher l'aperçu d'une leçon
//           GET : Affiche la leçon comme elle apparaît côté apprenant
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

/**
 * AdminCoursLeconApercuServlet - Aperçu d'une leçon
 * 
 * Affiche la leçon en mode "vue apprenant" pour que l'admin
 * puisse vérifier le rendu avant publication.
 * 
 * URL : /admin/cours/lecons/apercu?id=X
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebServlet(name = "AdminCoursLeconApercuServlet", urlPatterns = {"/admin/cours/lecons/apercu"})
public class AdminCoursLeconApercuServlet extends HttpServlet {

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

        // 2. Récupération et validation de l'ID
        String leconIdStr = request.getParameter("id");
        if (leconIdStr == null || leconIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID de la leçon manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        int leconId;
        try {
            leconId = Integer.parseInt(leconIdStr.trim());
            if (leconId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de leçon invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 3. Récupération de la leçon avec ses ressources
        LeconDAO leconDAO = new LeconDAO();
        Lecon lecon = leconDAO.trouverParId(leconId);

        if (lecon == null) {
            session.setAttribute("messageErreur", "Cette leçon n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 4. Récupération du chapitre
        ChapitreDAO chapitreDAO = new ChapitreDAO();
        Chapitre chapitre = chapitreDAO.trouverParId(lecon.getChapitreId());

        if (chapitre == null) {
            session.setAttribute("messageErreur", "Le chapitre associé n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 5. Récupération du cours
        CoursDAO coursDAO = new CoursDAO();
        Cours cours = coursDAO.trouverParId(chapitre.getCoursId());

        // 6. Passage des attributs
        request.setAttribute("pageTitle", "Aperçu - " + lecon.getTitre() + " - Administration");
        request.setAttribute("cours", cours);
        request.setAttribute("chapitre", chapitre);
        request.setAttribute("lecon", lecon);
        request.setAttribute("estApercu", true);

        // 7. Affichage (utilise la même JSP que l'aperçu apprenant ou une version admin)
        request.getRequestDispatcher("/views/admin/admin-cours-lecon-apercu.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}