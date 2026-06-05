// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminCoursChapitreCreerServlet.java
// RÔLE    : Contrôleur pour créer un nouveau chapitre
//           GET  : Affiche le formulaire de création
//           POST : Valide et crée le chapitre
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
 * AdminCoursChapitreCreerServlet - Création d'un chapitre
 * 
 * GET  : Affiche le formulaire avec :
 *        - Cours présélectionné
 *        - Module présélectionné (si passé en paramètre)
 *        - Ordre auto-suggéré
 * 
 * POST : Valide et crée le chapitre
 * 
 * URL : /admin/cours/chapitres/creer?coursId=X&module=Y
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminCoursChapitreCreerServlet", urlPatterns = {"/admin/cours/chapitres/creer"})
public class AdminCoursChapitreCreerServlet extends HttpServlet {

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

        // 2. Récupération et validation du coursId
        String coursIdStr = request.getParameter("coursId");
        if (coursIdStr == null || coursIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID du cours manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        int coursId;
        try {
            coursId = Integer.parseInt(coursIdStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de cours invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 3. Vérifier que le cours existe
        CoursDAO coursDAO = new CoursDAO();
        Cours cours = coursDAO.trouverParId(coursId);

        if (cours == null) {
            session.setAttribute("messageErreur", "Ce cours n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 4. Module présélectionné (optionnel)
        int modulePreselectionne = 1;
        String moduleStr = request.getParameter("module");
        if (moduleStr != null && !moduleStr.trim().isEmpty()) {
            try {
                modulePreselectionne = Integer.parseInt(moduleStr.trim());
                if (modulePreselectionne < 1) modulePreselectionne = 1;
                if (modulePreselectionne > NOMBRE_MODULES) modulePreselectionne = NOMBRE_MODULES;
            } catch (NumberFormatException e) {}
        }

        // 5. Calculer l'ordre suggéré (dernier ordre + 1 pour ce module)
        ChapitreDAO chapitreDAO = new ChapitreDAO();
        int ordreSuggere = calculerOrdreSuggere(chapitreDAO, coursId, modulePreselectionne);

        // 6. Message d'erreur éventuel
        String messageErreur = (String) session.getAttribute("messageErreurCreationChapitre");
        if (messageErreur != null) session.removeAttribute("messageErreurCreationChapitre");

        // 7. Passage des attributs
        request.setAttribute("pageTitle", "Créer un chapitre - " + cours.getTitre() + " - Administration");
        request.setAttribute("cours", cours);
        request.setAttribute("coursId", coursId);
        request.setAttribute("modulePreselectionne", modulePreselectionne);
        request.setAttribute("nombreModules", NOMBRE_MODULES);
        request.setAttribute("ordreSuggere", ordreSuggere);
        request.setAttribute("messageErreur", messageErreur);
        request.setAttribute("modeModification", false);

        // 8. Affichage
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
        String coursIdStr = request.getParameter("coursId");
        String moduleStr = request.getParameter("module");
        String titre = request.getParameter("titre");
        String description = request.getParameter("description");
        String objectifs = request.getParameter("objectifs");
        String ordreStr = request.getParameter("ordre");

        // 3. Validation coursId
        if (coursIdStr == null || coursIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID du cours manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        int coursId;
        try {
            coursId = Integer.parseInt(coursIdStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de cours invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 4. Validation module
        int module = 1;
        if (moduleStr != null && !moduleStr.trim().isEmpty()) {
            try {
                module = Integer.parseInt(moduleStr.trim());
                if (module < 1) module = 1;
                if (module > NOMBRE_MODULES) module = NOMBRE_MODULES;
            } catch (NumberFormatException e) {}
        }

        // 5. Validation titre
        if (titre == null || titre.trim().isEmpty()) {
            session.setAttribute("messageErreurCreationChapitre", "Le titre du chapitre est obligatoire.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/chapitres/creer?coursId=" + coursId + "&module=" + module);
            return;
        }
        titre = titre.trim();

        if (titre.length() < 2) {
            session.setAttribute("messageErreurCreationChapitre", "Le titre doit contenir au moins 2 caractères.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/chapitres/creer?coursId=" + coursId + "&module=" + module);
            return;
        }

        // 6. Validation ordre
        int ordre = 1;
        if (ordreStr != null && !ordreStr.trim().isEmpty()) {
            try {
                ordre = Integer.parseInt(ordreStr.trim());
                if (ordre < 1) ordre = 1;
            } catch (NumberFormatException e) {}
        }

        // 7. Nettoyage des champs optionnels
        if (description != null) description = description.trim();
        if (objectifs != null) objectifs = objectifs.trim();

        // 8. Création du chapitre
        Chapitre chapitre = new Chapitre();
        chapitre.setCoursId(coursId);
        chapitre.setModuleNumero(module);
        chapitre.setTitre(titre);
        chapitre.setDescription(description);
        chapitre.setObjectifs(objectifs);
        chapitre.setOrdre(ordre);

        ChapitreDAO chapitreDAO = new ChapitreDAO();
        boolean ajoute = chapitreDAO.ajouter(chapitre);

        // 9. Résultat
        if (ajoute) {
            session.setAttribute("messageSucces", 
                "Chapitre \"" + titre + "\" créé avec succès dans le Module " + module + " !");

            System.out.println("✅ [AdminCoursChapitreCreer] Chapitre créé : " + titre + 
                " (CoursID: " + coursId + ", Module: " + module + ")");

            response.sendRedirect(request.getContextPath() + "/admin/cours/chapitres?coursId=" + coursId);
        } else {
            session.setAttribute("messageErreurCreationChapitre", 
                "Erreur lors de la création du chapitre. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/chapitres/creer?coursId=" + coursId + "&module=" + module);
        }
    }

    /**
     * Calcule l'ordre suggéré pour un nouveau chapitre dans un module
     */
    private int calculerOrdreSuggere(ChapitreDAO chapitreDAO, int coursId, int module) {
        java.util.List<Chapitre> chapitres = chapitreDAO.listerParCours(coursId);
        int maxOrdre = 0;
        if (chapitres != null) {
            for (Chapitre c : chapitres) {
                if (c.getModuleNumero() == module && c.getOrdre() > maxOrdre) {
                    maxOrdre = c.getOrdre();
                }
            }
        }
        return maxOrdre + 1;
    }
}