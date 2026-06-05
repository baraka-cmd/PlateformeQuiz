// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminThemeSupprimerServlet.java
// RÔLE    : Contrôleur pour supprimer un thème
//           POST : Supprime le thème après vérification
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.ThemeDAO;
import com.amijayquiz.models.Theme;
import com.amijayquiz.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AdminThemeSupprimerServlet - Suppression d'un thème
 * 
 * POST : Supprime le thème s'il n'a aucun cours associé
 *        Refuse la suppression si des cours utilisent ce thème
 * 
 * URL : /admin/themes/supprimer
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminThemeSupprimerServlet", urlPatterns = {"/admin/themes/supprimer"})
public class AdminThemeSupprimerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // GET non autorisé
        response.sendRedirect(request.getContextPath() + "/admin/themes");
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
        if (idStr == null || idStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID du thème manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/themes");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr.trim());
            if (id <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de thème invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/themes");
            return;
        }

        // 3. Vérifier que le thème existe
        ThemeDAO themeDAO = new ThemeDAO();
        Theme theme = themeDAO.trouverParId(id);

        if (theme == null) {
            session.setAttribute("messageErreur", "Ce thème n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/themes");
            return;
        }

        // 4. Vérifier qu'aucun cours n'utilise ce thème
        int nbCours = themeDAO.compterCoursParTheme(theme.getNom());
        if (nbCours > 0) {
            session.setAttribute("messageErreur", 
                "Impossible de supprimer le thème \"" + theme.getNom() + "\". " +
                nbCours + " cours utilisent ce thème. Supprimez ou réassignez ces cours d'abord.");
            response.sendRedirect(request.getContextPath() + "/admin/themes");
            return;
        }

        // 5. Suppression
        boolean supprime = themeDAO.supprimer(id);

        // 6. Résultat
        if (supprime) {
            session.setAttribute("messageSucces", 
                "Thème \"" + theme.getNom() + "\" supprimé avec succès !");
        } else {
            session.setAttribute("messageErreur", 
                "Erreur lors de la suppression du thème. Veuillez réessayer.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/themes");
    }
}