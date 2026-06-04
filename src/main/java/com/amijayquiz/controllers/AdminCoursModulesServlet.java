// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminCoursModulesServlet.java
// RÔLE    : Contrôleur pour la gestion des 15 modules d'un cours
//           GET : Affiche la grille des 15 modules avec statuts
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.CoursDAO;
import com.amijayquiz.dao.ExamenModuleDAO;
import com.amijayquiz.dao.ProgressionModuleDAO;
import com.amijayquiz.models.Cours;
import com.amijayquiz.models.ExamenModule;
import com.amijayquiz.models.ProgressionModule;
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
 * AdminCoursModulesServlet - Gestion des 15 modules d'un cours
 * 
 * Affiche une grille de 15 cartes modules (3 colonnes × 5 lignes).
 * Chaque carte montre :
 * - Numéro du module (1 à 15)
 * - Statut : Examen configuré / Aucun examen
 * - Nombre de questions dans l'examen
 * - Nombre d'apprenants ayant réussi ce module
 * - Boutons : Configurer/Modifier l'examen, Voir les questions
 * 
 * URL : /admin/cours/modules?coursId=X
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminCoursModulesServlet", urlPatterns = {"/admin/cours/modules"})
public class AdminCoursModulesServlet extends HttpServlet {

    // Nombre de modules par cours
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
        if (!"admin".equals(utilisateur.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        // 2. Récupération et validation du paramètre coursId
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

        // 3. Vérifier que le cours existe
        CoursDAO coursDAO = new CoursDAO();
        Cours cours = coursDAO.trouverParId(coursId);

        if (cours == null) {
            session.setAttribute("messageErreur", "Ce cours n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 4. Récupération de tous les examens du cours
        ExamenModuleDAO examenDAO = new ExamenModuleDAO();
        List<ExamenModule> examens = examenDAO.listerParCours(coursId);

        // 5. Récupération de la progression pour chaque module
        ProgressionModuleDAO progressionDAO = new ProgressionModuleDAO();

        // 6. Construction des données pour chaque module (1 à 15)
        List<Map<String, Object>> modulesData = new ArrayList<>();

        for (int moduleNumero = 1; moduleNumero <= NOMBRE_MODULES; moduleNumero++) {
            Map<String, Object> moduleData = new LinkedHashMap<>();
            moduleData.put("numero", moduleNumero);

            // Chercher l'examen de ce module
            ExamenModule examen = null;
            if (examens != null) {
                for (ExamenModule e : examens) {
                    if (e.getModuleNumero() == moduleNumero) {
                        examen = e;
                        break;
                    }
                }
            }
            moduleData.put("examen", examen);

            // Statut de l'examen
            boolean examenConfigure = examen != null;
            moduleData.put("examenConfigure", examenConfigure);

            // Nombre de questions si examen configuré
            int nbQuestions = 0;
            if (examen != null) {
                com.amijayquiz.dao.QuestionExamenDAO questionDAO = new com.amijayquiz.dao.QuestionExamenDAO();
                nbQuestions = questionDAO.compterParExamen(examen.getId());
            }
            moduleData.put("nbQuestions", nbQuestions);

            // Nombre d'apprenants ayant réussi ce module
            // (compteur approximatif basé sur progression_modules)
            int nbApprenantsReussis = 0;
            // Cette info pourrait être récupérée via une requête COUNT dans ProgressionModuleDAO
            moduleData.put("nbApprenantsReussis", nbApprenantsReussis);

            modulesData.add(moduleData);
        }

        // 7. Message de succès/erreur
        String messageSucces = (String) session.getAttribute("messageSucces");
        String messageErreur = (String) session.getAttribute("messageErreur");
        if (messageSucces != null) session.removeAttribute("messageSucces");
        if (messageErreur != null) session.removeAttribute("messageErreur");

        // 8. Statistiques globales
        int examensConfigured = 0;
        int totalQuestions = 0;
        for (Map<String, Object> md : modulesData) {
            if ((Boolean) md.get("examenConfigure")) {
                examensConfigured++;
                totalQuestions += (Integer) md.get("nbQuestions");
            }
        }

        // 9. Passage des attributs
        request.setAttribute("pageTitle", "Modules - " + cours.getTitre() + " - Administration");
        request.setAttribute("cours", cours);
        request.setAttribute("modules", modulesData);
        request.setAttribute("nombreModules", NOMBRE_MODULES);
        request.setAttribute("examensConfigured", examensConfigured);
        request.setAttribute("totalQuestions", totalQuestions);
        request.setAttribute("messageSucces", messageSucces);
        request.setAttribute("messageErreur", messageErreur);

        // 10. Affichage
        request.getRequestDispatcher("/views/admin/admin-cours-modules.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}