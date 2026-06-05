// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminCoursChapitreModifierServlet.java
// RÔLE    : Contrôleur pour modifier un chapitre existant
//           GET  : Affiche le formulaire pré-rempli
//           POST : Valide et met à jour le chapitre
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.ChapitreDAO;
import com.amijayquiz.dao.CoursDAO;
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

/**
 * AdminCoursChapitreModifierServlet - Modification d'un chapitre
 * 
 * GET  : Affiche le formulaire pré-rempli avec les données du chapitre
 * POST : Valide et met à jour le chapitre
 * 
 * URL : /admin/cours/chapitres/modifier?id=X&coursId=Y
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminCoursChapitreModifierServlet", urlPatterns = {"/admin/cours/chapitres/modifier"})
public class AdminCoursChapitreModifierServlet extends HttpServlet {

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

        // 2. Récupération et validation de l'ID du chapitre
        String idStr = request.getParameter("id");
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

        // 4. Récupération du cours associé
        int coursId = chapitre.getCoursId();
        String coursIdStr = request.getParameter("coursId");
        if (coursIdStr != null && !coursIdStr.trim().isEmpty()) {
            try { coursId = Integer.parseInt(coursIdStr.trim()); } catch (NumberFormatException e) {}
        }

        CoursDAO coursDAO = new CoursDAO();
        Cours cours = coursDAO.trouverParId(coursId);

        if (cours == null) {
            session.setAttribute("messageErreur", "Le cours associé n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 5. Message d'erreur éventuel
        String messageErreur = (String) session.getAttribute("messageErreurModificationChapitre");
        if (messageErreur != null) session.removeAttribute("messageErreurModificationChapitre");

        // 6. Passage des attributs
        request.setAttribute("pageTitle", "Modifier le chapitre - " + chapitre.getTitre() + " - Administration");
        request.setAttribute("cours", cours);
        request.setAttribute("coursId", coursId);
        request.setAttribute("chapitre", chapitre);
        request.setAttribute("modulePreselectionne", chapitre.getModuleNumero());
        request.setAttribute("nombreModules", NOMBRE_MODULES);
        request.setAttribute("ordreSuggere", chapitre.getOrdre());
        request.setAttribute("messageErreur", messageErreur);
        request.setAttribute("modeModification", true);

        // 7. Affichage
        request.getRequestDispatcher("/views/admin/admin-cours-chapitre-creer.jsp").forward(request, response);
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
        if (!"admin".equals(utilisateur.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        // 2. Récupération des paramètres
        String chapitreIdStr = request.getParameter("chapitreId");
        String coursIdStr = request.getParameter("coursId");
        String moduleStr = request.getParameter("module");
        String titre = request.getParameter("titre");
        String description = request.getParameter("description");
        String objectifs = request.getParameter("objectifs");
        String ordreStr = request.getParameter("ordre");

        // 3. Validation chapitreId
        if (chapitreIdStr == null || chapitreIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID du chapitre manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        int chapitreId;
        try {
            chapitreId = Integer.parseInt(chapitreIdStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de chapitre invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 4. Vérifier que le chapitre existe
        ChapitreDAO chapitreDAO = new ChapitreDAO();
        Chapitre chapitre = chapitreDAO.trouverParId(chapitreId);

        if (chapitre == null) {
            session.setAttribute("messageErreur", "Ce chapitre n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 5. Validation coursId
        int coursId = chapitre.getCoursId();
        if (coursIdStr != null && !coursIdStr.trim().isEmpty()) {
            try { coursId = Integer.parseInt(coursIdStr.trim()); } catch (NumberFormatException e) {}
        }

        // 6. Validation titre
        if (titre == null || titre.trim().isEmpty()) {
            session.setAttribute("messageErreurModificationChapitre", "Le titre du chapitre est obligatoire.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/chapitres/modifier?id=" + chapitreId + "&coursId=" + coursId);
            return;
        }
        titre = titre.trim();

        if (titre.length() < 2) {
            session.setAttribute("messageErreurModificationChapitre", "Le titre doit contenir au moins 2 caractères.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/chapitres/modifier?id=" + chapitreId + "&coursId=" + coursId);
            return;
        }

        // 7. Validation module
        int module = chapitre.getModuleNumero();
        if (moduleStr != null && !moduleStr.trim().isEmpty()) {
            try {
                module = Integer.parseInt(moduleStr.trim());
                if (module < 1) module = 1;
                if (module > NOMBRE_MODULES) module = NOMBRE_MODULES;
            } catch (NumberFormatException e) {}
        }

        // 8. Validation ordre
        int ordre = chapitre.getOrdre();
        if (ordreStr != null && !ordreStr.trim().isEmpty()) {
            try {
                ordre = Integer.parseInt(ordreStr.trim());
                if (ordre < 1) ordre = 1;
            } catch (NumberFormatException e) {}
        }

        // 9. Nettoyage
        if (description != null) description = description.trim();
        if (objectifs != null) objectifs = objectifs.trim();

        // 10. Mise à jour
        chapitre.setModuleNumero(module);
        chapitre.setTitre(titre);
        chapitre.setDescription(description);
        chapitre.setObjectifs(objectifs);
        chapitre.setOrdre(ordre);

        boolean modifie = chapitreDAO.modifier(chapitre);

        // 11. Résultat
        if (modifie) {
            session.setAttribute("messageSucces", 
                "Chapitre \"" + titre + "\" modifié avec succès !");

            System.out.println("✅ [AdminCoursChapitreModifier] Chapitre modifié : ID=" + chapitreId + " - Titre=" + titre);

            response.sendRedirect(request.getContextPath() + "/admin/cours/chapitres?coursId=" + coursId);
        } else {
            session.setAttribute("messageErreurModificationChapitre", 
                "Erreur lors de la modification du chapitre.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/chapitres/modifier?id=" + chapitreId + "&coursId=" + coursId);
        }
    }
}