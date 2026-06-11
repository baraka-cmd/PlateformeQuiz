// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/AdminCoursChapitreSupprimerServlet.java
// RÔLE    : Contrôleur pour supprimer un chapitre
//           POST : Supprime le chapitre et ses leçons associées
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.ChapitreDAO;
import com.learnpath.models.Chapitre;
import com.learnpath.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AdminCoursChapitreSupprimerServlet - Suppression d'un chapitre
 * 
 * POST : Supprime le chapitre et toutes ses leçons associées.
 *        La suppression est définitive.
 * 
 * URL : /admin/cours/chapitres/supprimer
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebServlet(name = "AdminCoursChapitreSupprimerServlet", urlPatterns = {"/admin/cours/chapitres/supprimer"})
public class AdminCoursChapitreSupprimerServlet extends HttpServlet {

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
        String coursIdStr = request.getParameter("coursId");

        if (idStr == null || idStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID du chapitre manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        int chapitreId;
        try {
            chapitreId = Integer.parseInt(idStr.trim());
            if (chapitreId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de chapitre invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 3. Récupération du chapitre
        ChapitreDAO chapitreDAO = new ChapitreDAO();
        Chapitre chapitre = chapitreDAO.trouverParId(chapitreId);

        if (chapitre == null) {
            session.setAttribute("messageErreur", "Ce chapitre n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        String titreChapitre = chapitre.getTitre();
        int coursId = chapitre.getCoursId();

        // Utiliser le coursId passé en paramètre si disponible
        if (coursIdStr != null && !coursIdStr.trim().isEmpty()) {
            try { coursId = Integer.parseInt(coursIdStr.trim()); } catch (NumberFormatException e) {}
        }

        // 4. Suppression du chapitre (et de ses leçons)
        boolean supprime = chapitreDAO.supprimer(chapitreId);

        // 5. Résultat
        if (supprime) {
            session.setAttribute("messageSucces", 
                "Chapitre \"" + titreChapitre + "\" supprimé avec succès !");

            System.out.println("✅ [AdminCoursChapitreSupprimer] Chapitre supprimé : ID=" + chapitreId + 
                " - Titre=" + titreChapitre + " - CoursID=" + coursId);

        } else {
            session.setAttribute("messageErreur", 
                "Erreur lors de la suppression du chapitre. Veuillez réessayer.");

            System.err.println("❌ [AdminCoursChapitreSupprimer] Échec suppression : ID=" + chapitreId);
        }

        // 6. Redirection vers la liste des chapitres du cours
        response.sendRedirect(request.getContextPath() + "/admin/cours/chapitres?coursId=" + coursId);
    }
}