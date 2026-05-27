// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/ChapitreCoursServlet.java
// RÔLE    : Contrôleur de la page d'un chapitre de cours
//           Affiche le détail du chapitre avec la liste des leçons
//           + contexte du module pour la navigation
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.ChapitreDAO;
import com.amijayquiz.dao.CoursDAO;
import com.amijayquiz.dao.ProgressionCoursDAO;
import com.amijayquiz.dao.ProgressionModuleDAO;
import com.amijayquiz.models.Chapitre;
import com.amijayquiz.models.Cours;
import com.amijayquiz.models.Lecon;
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
 * ChapitreCoursServlet - Page d'un chapitre de cours
 * 
 * Affiche le titre, la description, les objectifs du chapitre
 * et la liste des leçons avec leur statut (terminée, en cours, verrouillée).
 * Navigation vers le chapitre précédent/suivant et vers l'examen du module.
 * 
 * Les leçons sont accessibles si le module parent est débloqué.
 * 
 * URL : /joueur/cours/chapitre?coursId=1&chapitreId=1
 * 
 * @author AmijayQuiz
 * @version 2.0
 */
@WebServlet(name = "ChapitreCoursServlet", urlPatterns = {"/joueur/cours/chapitre"})
public class ChapitreCoursServlet extends HttpServlet {

    private static final int NOMBRE_MODULES = 15;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ============================================================
        // 1. VÉRIFICATION DE LA SESSION ET DU RÔLE
        // ============================================================
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (!"joueur".equals(utilisateur.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        int utilisateurId = utilisateur.getId();

        // ============================================================
        // 2. RÉCUPÉRATION ET VALIDATION DES PARAMÈTRES
        // ============================================================
        String coursIdParam = request.getParameter("coursId");
        String chapitreIdParam = request.getParameter("chapitreId");

        if (coursIdParam == null || coursIdParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        int coursId;
        int chapitreId = 0;
        try {
            coursId = Integer.parseInt(coursIdParam);
            if (chapitreIdParam != null && !chapitreIdParam.trim().isEmpty()) {
                chapitreId = Integer.parseInt(chapitreIdParam);
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        // ============================================================
        // 3. INITIALISATION DES DAO
        // ============================================================
        CoursDAO coursDAO = new CoursDAO();
        ChapitreDAO chapitreDAO = new ChapitreDAO();
        ProgressionCoursDAO progressionCoursDAO = new ProgressionCoursDAO();
        ProgressionModuleDAO progressionModuleDAO = new ProgressionModuleDAO();

        // ============================================================
        // 4. RÉCUPÉRATION DU COURS
        // ============================================================
        Cours cours = coursDAO.trouverParId(coursId);
        if (cours == null) {
            session.setAttribute("messageErreur", "Ce cours n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        // ============================================================
        // 5. SI CHAPITREID EST 0 OU MANQUANT → PLAN DU COURS
        // ============================================================
        if (chapitreId == 0) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + coursId);
            return;
        }

        // ============================================================
        // 6. RÉCUPÉRATION DU CHAPITRE
        // ============================================================
        Chapitre chapitre = chapitreDAO.trouverParId(chapitreId);
        if (chapitre == null || chapitre.getCoursId() != coursId) {
            session.setAttribute("messageErreur", "Ce chapitre n'existe pas dans ce cours.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + coursId);
            return;
        }

        // ============================================================
        // 7. DÉTERMINER LE MODULE ACTIF
        // ============================================================
        int moduleActifNumero = chapitre.getModuleNumero();
        if (moduleActifNumero <= 0) {
            moduleActifNumero = 1;
        }

        // Vérifier si le module est débloqué
        boolean moduleDebloque = progressionModuleDAO.estModuleDebloque(utilisateurId, coursId, moduleActifNumero);
        boolean moduleReussi = progressionModuleDAO.estModuleReussi(utilisateurId, coursId, moduleActifNumero);

        // Si le module n'est ni débloqué ni réussi, rediriger
        if (!moduleDebloque && !moduleReussi) {
            session.setAttribute("messageErreur", 
                "Ce module n'est pas encore débloqué. Réussissez d'abord le module " + (moduleActifNumero - 1) + ".");
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + coursId);
            return;
        }

        // ============================================================
        // 8. RÉCUPÉRATION DES LEÇONS DU CHAPITRE AVEC PROGRESSION
        // ============================================================
        List<Lecon> lecons = chapitre.getLecons();
        if (lecons == null) {
            lecons = new ArrayList<>();
        }

        List<Map<String, Object>> leconsAvecProgression = new ArrayList<>();
        int leconsTerminees = 0;

        for (int i = 0; i < lecons.size(); i++) {
            Lecon lecon = lecons.get(i);
            Map<String, Object> leconData = new LinkedHashMap<>();
            leconData.put("lecon", lecon);

            boolean estTerminee = progressionCoursDAO.estLeconTerminee(utilisateurId, lecon.getId());
            leconData.put("estTerminee", estTerminee);

            // Si le module est débloqué ou réussi, toutes les leçons sont accessibles
            boolean estAccessible = moduleDebloque || moduleReussi;
            leconData.put("estAccessible", estAccessible);

            // Une leçon est "active" si elle n'est pas terminée et qu'elle est accessible
            leconData.put("estActive", !estTerminee && estAccessible);

            if (estTerminee) {
                leconsTerminees++;
            }

            leconsAvecProgression.add(leconData);
        }

        // ============================================================
        // 9. STATUT DU CHAPITRE
        // ============================================================
        int totalLecons = lecons.size();
        boolean chapitreTermine = progressionCoursDAO.estChapitreTermine(utilisateurId, chapitreId);
        int pourcentageChapitre = (totalLecons > 0) ? (leconsTerminees * 100 / totalLecons) : 0;

        // ============================================================
        // 10. NAVIGATION : CHAPITRE PRÉCÉDENT ET SUIVANT DANS LE MODULE
        // ============================================================
        List<Chapitre> tousLesChapitres = cours.getChapitres();
        if (tousLesChapitres == null || tousLesChapitres.isEmpty()) {
            tousLesChapitres = chapitreDAO.listerParCours(coursId);
            cours.setChapitres(tousLesChapitres);
        }

        // Filtrer les chapitres du module actif
        List<Chapitre> chapitresDuModule = new ArrayList<>();
        for (Chapitre chap : tousLesChapitres) {
            int chapModule = chap.getModuleNumero() > 0 ? chap.getModuleNumero() : 1;
            if (chapModule == moduleActifNumero) {
                chapitresDuModule.add(chap);
            }
        }

        // Trouver le chapitre précédent et suivant dans le module
        Chapitre chapitrePrecedent = null;
        Chapitre chapitreSuivant = null;

        for (int i = 0; i < chapitresDuModule.size(); i++) {
            Chapitre chap = chapitresDuModule.get(i);
            if (chap.getId() == chapitreId) {
                if (i > 0) {
                    chapitrePrecedent = chapitresDuModule.get(i - 1);
                }
                if (i < chapitresDuModule.size() - 1) {
                    chapitreSuivant = chapitresDuModule.get(i + 1);
                }
                break;
            }
        }

        // ============================================================
        // 11. VÉRIFIER SI C'EST LE DERNIER CHAPITRE DU MODULE
        // ============================================================
        boolean estDernierChapitreDuModule = (chapitreSuivant == null);

        // ============================================================
        // 12. VÉRIFIER SI TOUS LES MODULES SONT RÉUSSIS
        // ============================================================
        boolean tousModulesReussis = true;
        for (int m = 1; m <= NOMBRE_MODULES; m++) {
            if (!progressionModuleDAO.estModuleReussi(utilisateurId, coursId, m)) {
                tousModulesReussis = false;
                break;
            }
        }

        // ============================================================
        // 13. PASSAGE DES ATTRIBUTS À LA JSP
        // ============================================================
        request.setAttribute("pageTitle", chapitre.getTitre() + " - " + cours.getTitre() + " - AmijayQuiz");
        request.setAttribute("cours", cours);
        request.setAttribute("chapitre", chapitre);
        request.setAttribute("lecons", leconsAvecProgression);
        request.setAttribute("totalLecons", totalLecons);
        request.setAttribute("leconsTerminees", leconsTerminees);
        request.setAttribute("pourcentageChapitre", pourcentageChapitre);
        request.setAttribute("chapitreTermine", chapitreTermine);
        request.setAttribute("chapitrePrecedent", chapitrePrecedent);
        request.setAttribute("chapitreSuivant", chapitreSuivant);
        request.setAttribute("chapitreSuivantDebloque", (moduleDebloque || moduleReussi));
        request.setAttribute("totalChapitres", chapitresDuModule.size());
        request.setAttribute("tousChapitresTermines", tousModulesReussis);
        request.setAttribute("moduleActifNumero", moduleActifNumero);
        request.setAttribute("moduleDebloque", moduleDebloque);
        request.setAttribute("moduleReussi", moduleReussi);
        request.setAttribute("estDernierChapitreDuModule", estDernierChapitreDuModule);

        // ============================================================
        // 14. AFFICHAGE DE LA PAGE
        // ============================================================
        request.getRequestDispatcher("/views/joueur/cours-chapitre.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}