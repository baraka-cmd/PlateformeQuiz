// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/AdminCoursModuleExamenServlet.java
// RÔLE    : Contrôleur pour créer ou modifier l'examen d'un module
//           GET  : Affiche le formulaire (création ou modification)
//           POST : Valide et enregistre/crée l'examen
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.CoursDAO;
import com.learnpath.dao.ExamenModuleDAO;
import com.learnpath.models.Cours;
import com.learnpath.models.ExamenModule;
import com.learnpath.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AdminCoursModuleExamenServlet - Création/Modification d'un examen de module
 * 
 * GET avec paramètre "id" : Mode modification (examen existant)
 * GET avec paramètres "coursId" et "module" : Mode création
 * POST : Enregistre l'examen (création ou mise à jour)
 * 
 * URL création  : /admin/cours/modules/creer?coursId=X&module=Y
 * URL modification : /admin/cours/modules/modifier?id=X&coursId=Y
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebServlet(name = "AdminCoursModuleExamenServlet", urlPatterns = {
    "/admin/cours/modules/creer",
    "/admin/cours/modules/modifier"
})
public class AdminCoursModuleExamenServlet extends HttpServlet {

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
        String requestURI = request.getRequestURI();
        boolean isModification = requestURI.contains("/modifier");

        ExamenModuleDAO examenDAO = new ExamenModuleDAO();
        CoursDAO coursDAO = new CoursDAO();

        ExamenModule examen = null;
        Cours cours = null;
        int moduleNumero = 0;

        if (isModification) {
            // ===== MODE MODIFICATION =====
            String idStr = request.getParameter("id");
            String coursIdStr = request.getParameter("coursId");

            if (idStr == null || idStr.trim().isEmpty()) {
                session.setAttribute("messageErreur", "ID de l'examen manquant.");
                response.sendRedirect(request.getContextPath() + "/admin/cours");
                return;
            }

            int examenId;
            try {
                examenId = Integer.parseInt(idStr.trim());
            } catch (NumberFormatException e) {
                session.setAttribute("messageErreur", "ID d'examen invalide.");
                response.sendRedirect(request.getContextPath() + "/admin/cours");
                return;
            }

            examen = examenDAO.trouverParId(examenId);
            if (examen == null) {
                session.setAttribute("messageErreur", "Cet examen n'existe pas.");
                response.sendRedirect(request.getContextPath() + "/admin/cours");
                return;
            }

            // Récupérer le cours associé
            int coursId = examen.getCoursId();
            if (coursIdStr != null && !coursIdStr.isEmpty()) {
                try { coursId = Integer.parseInt(coursIdStr); } catch (NumberFormatException e) {}
            }
            cours = coursDAO.trouverParId(coursId);
            moduleNumero = examen.getModuleNumero();

            request.setAttribute("pageTitle", "Modifier l'examen - " + examen.getModuleFormate() + " - Administration");
            request.setAttribute("modeModification", true);

        } else {
            // ===== MODE CRÉATION =====
            String coursIdStr = request.getParameter("coursId");
            String moduleStr = request.getParameter("module");

            if (coursIdStr == null || coursIdStr.trim().isEmpty() || moduleStr == null || moduleStr.trim().isEmpty()) {
                session.setAttribute("messageErreur", "Paramètres manquants (coursId et module requis).");
                response.sendRedirect(request.getContextPath() + "/admin/cours");
                return;
            }

            int coursId;
            try {
                coursId = Integer.parseInt(coursIdStr.trim());
                moduleNumero = Integer.parseInt(moduleStr.trim());
            } catch (NumberFormatException e) {
                session.setAttribute("messageErreur", "Paramètres invalides.");
                response.sendRedirect(request.getContextPath() + "/admin/cours");
                return;
            }

            cours = coursDAO.trouverParId(coursId);
            if (cours == null) {
                session.setAttribute("messageErreur", "Ce cours n'existe pas.");
                response.sendRedirect(request.getContextPath() + "/admin/cours");
                return;
            }

            if (moduleNumero < 1 || moduleNumero > 15) {
                session.setAttribute("messageErreur", "Numéro de module invalide (1 à 15).");
                response.sendRedirect(request.getContextPath() + "/admin/cours/modules?coursId=" + coursId);
                return;
            }

            // Vérifier qu'aucun examen n'existe déjà pour ce module
            ExamenModule existant = examenDAO.trouverParCoursEtModule(coursId, moduleNumero);
            if (existant != null) {
                session.setAttribute("messageErreur", "Un examen existe déjà pour ce module. Utilisez la modification.");
                response.sendRedirect(request.getContextPath() + "/admin/cours/modules?coursId=" + coursId);
                return;
            }

            // Créer un examen vide pour le formulaire
            examen = new ExamenModule();
            examen.setCoursId(coursId);
            examen.setModuleNumero(moduleNumero);
            examen.setNombreQuestions(20);
            examen.setSeuilReussite(75);

            request.setAttribute("pageTitle", "Créer l'examen - Module " + moduleNumero + " - Administration");
            request.setAttribute("modeModification", false);
        }

        // Message d'erreur éventuel
        String messageErreur = (String) session.getAttribute("messageErreurExamen");
        if (messageErreur != null) session.removeAttribute("messageErreurExamen");

        // Attributs communs
        request.setAttribute("cours", cours);
        request.setAttribute("examen", examen);
        request.setAttribute("moduleNumero", moduleNumero);
        request.setAttribute("messageErreur", messageErreur);

        // Affichage
        request.getRequestDispatcher("/views/admin/admin-cours-module-examen.jsp").forward(request, response);
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
        String modeModificationStr = request.getParameter("modeModification");
        String examenIdStr = request.getParameter("examenId");
        String coursIdStr = request.getParameter("coursId");
        String moduleStr = request.getParameter("module");
        String titre = request.getParameter("titre");
        String description = request.getParameter("description");
        String nbQuestionsStr = request.getParameter("nombreQuestions");
        String seuilStr = request.getParameter("seuilReussite");

        boolean isModification = "true".equals(modeModificationStr);

        // 3. Validation des paramètres communs
        if (coursIdStr == null || coursIdStr.trim().isEmpty() || moduleStr == null || moduleStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "Paramètres manquants.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        int coursId;
        int moduleNumero;
        try {
            coursId = Integer.parseInt(coursIdStr.trim());
            moduleNumero = Integer.parseInt(moduleStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "Paramètres invalides.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 4. Validation du titre
        if (titre == null || titre.trim().isEmpty()) {
            titre = "Examen Module " + moduleNumero;
        }
        titre = titre.trim();

        // 5. Validation du nombre de questions
        int nbQuestions = 20;
        if (nbQuestionsStr != null && !nbQuestionsStr.trim().isEmpty()) {
            try {
                nbQuestions = Integer.parseInt(nbQuestionsStr.trim());
                if (nbQuestions < 5) nbQuestions = 5;
                if (nbQuestions > 50) nbQuestions = 50;
            } catch (NumberFormatException e) {}
        }

        // 6. Validation du seuil de réussite
        int seuil = 75;
        if (seuilStr != null && !seuilStr.trim().isEmpty()) {
            try {
                seuil = Integer.parseInt(seuilStr.trim());
                if (seuil < 50) seuil = 50;
                if (seuil > 100) seuil = 100;
            } catch (NumberFormatException e) {}
        }

        // 7. Nettoyage description
        if (description != null) description = description.trim();

        // 8. Enregistrement
        ExamenModuleDAO examenDAO = new ExamenModuleDAO();
        boolean success;

        if (isModification) {
            // Mode modification
            if (examenIdStr == null || examenIdStr.trim().isEmpty()) {
                session.setAttribute("messageErreur", "ID d'examen manquant.");
                response.sendRedirect(request.getContextPath() + "/admin/cours/modules?coursId=" + coursId);
                return;
            }

            int examenId;
            try {
                examenId = Integer.parseInt(examenIdStr.trim());
            } catch (NumberFormatException e) {
                session.setAttribute("messageErreur", "ID d'examen invalide.");
                response.sendRedirect(request.getContextPath() + "/admin/cours/modules?coursId=" + coursId);
                return;
            }

            ExamenModule examen = examenDAO.trouverParId(examenId);
            if (examen == null) {
                session.setAttribute("messageErreur", "Examen introuvable.");
                response.sendRedirect(request.getContextPath() + "/admin/cours/modules?coursId=" + coursId);
                return;
            }

            examen.setTitre(titre);
            examen.setDescription(description);
            examen.setNombreQuestions(nbQuestions);
            examen.setSeuilReussite(seuil);

            success = examenDAO.modifier(examen);

        } else {
            // Mode création
            ExamenModule examen = new ExamenModule();
            examen.setCoursId(coursId);
            examen.setModuleNumero(moduleNumero);
            examen.setTitre(titre);
            examen.setDescription(description);
            examen.setNombreQuestions(nbQuestions);
            examen.setSeuilReussite(seuil);

            success = examenDAO.ajouter(examen);
        }

        // 9. Résultat
        if (success) {
            session.setAttribute("messageSucces", 
                "Examen du Module " + moduleNumero + " " + (isModification ? "modifié" : "créé") + " avec succès !");
        } else {
            session.setAttribute("messageErreur", 
                "Erreur lors de l'enregistrement de l'examen.");
        }

        // 10. Redirection vers la liste des modules
        response.sendRedirect(request.getContextPath() + "/admin/cours/modules?coursId=" + coursId);
    }
}