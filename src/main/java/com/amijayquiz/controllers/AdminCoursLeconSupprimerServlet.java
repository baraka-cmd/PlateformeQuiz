// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminCoursLeconSupprimerServlet.java
// RÔLE    : Contrôleur pour supprimer une leçon
//           POST : Supprime la leçon et ses ressources
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.LeconDAO;
import com.amijayquiz.models.Lecon;
import com.amijayquiz.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AdminCoursLeconSupprimerServlet - Suppression d'une leçon
 * 
 * POST : Supprime la leçon et toutes ses ressources associées.
 *        La suppression est définitive.
 * 
 * URL : /admin/cours/lecons/supprimer
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminCoursLeconSupprimerServlet", urlPatterns = {"/admin/cours/lecons/supprimer"})
public class AdminCoursLeconSupprimerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/admin/cours");
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

        // 2. Récupération et validation de l'ID
        String idStr = request.getParameter("id");
        String chapitreIdStr = request.getParameter("chapitreId");
        String coursIdStr = request.getParameter("coursId");

        if (idStr == null || idStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID de la leçon manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        int leconId;
        try {
            leconId = Integer.parseInt(idStr.trim());
            if (leconId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de leçon invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 3. Récupération de la leçon
        LeconDAO leconDAO = new LeconDAO();
        Lecon lecon = leconDAO.trouverParId(leconId);

        if (lecon == null) {
            session.setAttribute("messageErreur", "Cette leçon n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        String titreLecon = lecon.getTitre();
        int chapitreId = lecon.getChapitreId();
        int coursId = 0;

        if (chapitreIdStr != null && !chapitreIdStr.trim().isEmpty()) {
            try { chapitreId = Integer.parseInt(chapitreIdStr.trim()); } catch (NumberFormatException e) {}
        }
        if (coursIdStr != null && !coursIdStr.trim().isEmpty()) {
            try { coursId = Integer.parseInt(coursIdStr.trim()); } catch (NumberFormatException e) {}
        }

        // 4. Suppression de la leçon (et de ses ressources)
        boolean supprime = leconDAO.supprimer(leconId);

        // 5. Résultat
        if (supprime) {
            session.setAttribute("messageSucces", 
                "Leçon \"" + titreLecon + "\" supprimée avec succès !");

            System.out.println("✅ [AdminCoursLeconSupprimer] Leçon supprimée : ID=" + leconId + 
                " - Titre=" + titreLecon + " - ChapitreID=" + chapitreId);

        } else {
            session.setAttribute("messageErreur", 
                "Erreur lors de la suppression de la leçon. Veuillez réessayer.");

            System.err.println("❌ [AdminCoursLeconSupprimer] Échec suppression : ID=" + leconId);
        }

        // 6. Redirection vers la liste des leçons du chapitre
        String redirectUrl = "/admin/cours/lecons?chapitreId=" + chapitreId;
        if (coursId > 0) redirectUrl += "&coursId=" + coursId;
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }
}