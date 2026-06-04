// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminCoursLeconCreerServlet.java
// RÔLE    : Contrôleur pour créer une nouvelle leçon
//           GET  : Affiche le formulaire avec éditeur
//           POST : Valide et crée la leçon
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.ChapitreDAO;
import com.amijayquiz.dao.CoursDAO;
import com.amijayquiz.dao.LeconDAO;
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

/**
 * AdminCoursLeconCreerServlet - Création d'une leçon
 * 
 * GET  : Affiche le formulaire avec éditeur de texte enrichi
 *        - Titre, contenu texte (HTML), code source
 *        - Type d'accès (gratuit/premium)
 *        - Ordre auto-suggéré
 * 
 * POST : Valide et crée la leçon
 * 
 * URL : /admin/cours/lecons/creer?chapitreId=X&coursId=Y
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminCoursLeconCreerServlet", urlPatterns = {"/admin/cours/lecons/creer"})
public class AdminCoursLeconCreerServlet extends HttpServlet {

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

        // 5. Calculer l'ordre suggéré
        LeconDAO leconDAO = new LeconDAO();
        int ordreSuggere = leconDAO.compterParChapitre(chapitreId) + 1;

        // 6. Message d'erreur éventuel
        String messageErreur = (String) session.getAttribute("messageErreurCreationLecon");
        if (messageErreur != null) session.removeAttribute("messageErreurCreationLecon");

        // 7. Passage des attributs
        request.setAttribute("pageTitle", "Créer une leçon - " + chapitre.getTitre() + " - Administration");
        request.setAttribute("cours", cours);
        request.setAttribute("chapitre", chapitre);
        request.setAttribute("ordreSuggere", ordreSuggere);
        request.setAttribute("messageErreur", messageErreur);
        request.setAttribute("modeModification", false);

        // 8. Affichage
        request.getRequestDispatcher("/views/admin/admin-cours-lecon-creer.jsp").forward(request, response);
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
        String titre = request.getParameter("titre");
        String contenuTexte = request.getParameter("contenuTexte");
        String codeSource = request.getParameter("codeSource");
        String ordreStr = request.getParameter("ordre");
        String acces = request.getParameter("acces"); // "gratuit" ou "premium"

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

        int coursId = 0;
        if (coursIdStr != null && !coursIdStr.trim().isEmpty()) {
            try { coursId = Integer.parseInt(coursIdStr.trim()); } catch (NumberFormatException e) {}
        }

        // 4. Validation titre
        if (titre == null || titre.trim().isEmpty()) {
            session.setAttribute("messageErreurCreationLecon", "Le titre de la leçon est obligatoire.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/lecons/creer?chapitreId=" + chapitreId + "&coursId=" + coursId);
            return;
        }
        titre = titre.trim();

        if (titre.length() < 2) {
            session.setAttribute("messageErreurCreationLecon", "Le titre doit contenir au moins 2 caractères.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/lecons/creer?chapitreId=" + chapitreId + "&coursId=" + coursId);
            return;
        }

        // 5. Validation ordre
        int ordre = 1;
        if (ordreStr != null && !ordreStr.trim().isEmpty()) {
            try {
                ordre = Integer.parseInt(ordreStr.trim());
                if (ordre < 1) ordre = 1;
            } catch (NumberFormatException e) {}
        }

        // 6. Type d'accès
        boolean gratuit = !"premium".equals(acces);

        // 7. Nettoyage
        if (contenuTexte != null) contenuTexte = contenuTexte.trim();
        if (codeSource != null) codeSource = codeSource.trim();

        // 8. Création de la leçon
        Lecon lecon = new Lecon();
        lecon.setChapitreId(chapitreId);
        lecon.setTitre(titre);
        lecon.setContenuTexte(contenuTexte);
        lecon.setCodeSource(codeSource);
        lecon.setOrdre(ordre);
        lecon.setGratuit(gratuit);

        LeconDAO leconDAO = new LeconDAO();
        boolean ajoute = leconDAO.ajouter(lecon);

        // 9. Résultat
        if (ajoute) {
            session.setAttribute("messageSucces", 
                "Leçon \"" + titre + "\" créée avec succès !");

            System.out.println("✅ [AdminCoursLeconCreer] Leçon créée : " + titre + 
                " (ChapitreID: " + chapitreId + ", Ordre: " + ordre + ")");

            response.sendRedirect(request.getContextPath() + "/admin/cours/lecons?chapitreId=" + chapitreId + "&coursId=" + coursId);
        } else {
            session.setAttribute("messageErreurCreationLecon", 
                "Erreur lors de la création de la leçon. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/lecons/creer?chapitreId=" + chapitreId + "&coursId=" + coursId);
        }
    }
}