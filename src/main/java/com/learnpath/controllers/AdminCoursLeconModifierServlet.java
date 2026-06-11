// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/AdminCoursLeconModifierServlet.java
// RÔLE    : Contrôleur pour modifier une leçon existante
//           GET  : Affiche le formulaire pré-rempli
//           POST : Valide et met à jour la leçon
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
 * AdminCoursLeconModifierServlet - Modification d'une leçon
 * 
 * GET  : Affiche le formulaire pré-rempli avec les données de la leçon
 * POST : Valide et met à jour la leçon
 * 
 * URL : /admin/cours/lecons/modifier?id=X&chapitreId=Y&coursId=Z
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebServlet(name = "AdminCoursLeconModifierServlet", urlPatterns = {"/admin/cours/lecons/modifier"})
public class AdminCoursLeconModifierServlet extends HttpServlet {

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

        // 2. Récupération et validation de l'ID de la leçon
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

        // 3. Récupération de la leçon
        LeconDAO leconDAO = new LeconDAO();
        Lecon lecon = leconDAO.trouverParId(leconId);

        if (lecon == null) {
            session.setAttribute("messageErreur", "Cette leçon n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 4. Récupération du chapitre
        int chapitreId = lecon.getChapitreId();
        String chapitreIdStr = request.getParameter("chapitreId");
        if (chapitreIdStr != null && !chapitreIdStr.trim().isEmpty()) {
            try { chapitreId = Integer.parseInt(chapitreIdStr.trim()); } catch (NumberFormatException e) {}
        }

        ChapitreDAO chapitreDAO = new ChapitreDAO();
        Chapitre chapitre = chapitreDAO.trouverParId(chapitreId);

        if (chapitre == null) {
            session.setAttribute("messageErreur", "Le chapitre associé n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 5. Récupération du cours
        int coursId = chapitre.getCoursId();
        String coursIdStr = request.getParameter("coursId");
        if (coursIdStr != null && !coursIdStr.trim().isEmpty()) {
            try { coursId = Integer.parseInt(coursIdStr.trim()); } catch (NumberFormatException e) {}
        }

        CoursDAO coursDAO = new CoursDAO();
        Cours cours = coursDAO.trouverParId(coursId);

        // 6. Message d'erreur éventuel
        String messageErreur = (String) session.getAttribute("messageErreurModificationLecon");
        if (messageErreur != null) session.removeAttribute("messageErreurModificationLecon");

        // 7. Passage des attributs
        request.setAttribute("pageTitle", "Modifier la leçon - " + lecon.getTitre() + " - Administration");
        request.setAttribute("cours", cours);
        request.setAttribute("chapitre", chapitre);
        request.setAttribute("lecon", lecon);
        request.setAttribute("ordreSuggere", lecon.getOrdre());
        request.setAttribute("messageErreur", messageErreur);
        request.setAttribute("modeModification", true);

        // 8. Affichage (même JSP que la création)
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
        String leconIdStr = request.getParameter("leconId");
        String chapitreIdStr = request.getParameter("chapitreId");
        String coursIdStr = request.getParameter("coursId");
        String titre = request.getParameter("titre");
        String contenuTexte = request.getParameter("contenuTexte");
        String codeSource = request.getParameter("codeSource");
        String ordreStr = request.getParameter("ordre");
        String acces = request.getParameter("acces");

        // 3. Validation leconId
        if (leconIdStr == null || leconIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID de la leçon manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        int leconId;
        try {
            leconId = Integer.parseInt(leconIdStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de leçon invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 4. Vérifier que la leçon existe
        LeconDAO leconDAO = new LeconDAO();
        Lecon lecon = leconDAO.trouverParId(leconId);

        if (lecon == null) {
            session.setAttribute("messageErreur", "Cette leçon n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 5. Récupération chapitreId et coursId
        int chapitreId = lecon.getChapitreId();
        if (chapitreIdStr != null && !chapitreIdStr.trim().isEmpty()) {
            try { chapitreId = Integer.parseInt(chapitreIdStr.trim()); } catch (NumberFormatException e) {}
        }

        int coursId = 0;
        if (coursIdStr != null && !coursIdStr.trim().isEmpty()) {
            try { coursId = Integer.parseInt(coursIdStr.trim()); } catch (NumberFormatException e) {}
        }

        // 6. Validation titre
        if (titre == null || titre.trim().isEmpty()) {
            session.setAttribute("messageErreurModificationLecon", "Le titre de la leçon est obligatoire.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/lecons/modifier?id=" + leconId + "&chapitreId=" + chapitreId + "&coursId=" + coursId);
            return;
        }
        titre = titre.trim();

        if (titre.length() < 2) {
            session.setAttribute("messageErreurModificationLecon", "Le titre doit contenir au moins 2 caractères.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/lecons/modifier?id=" + leconId + "&chapitreId=" + chapitreId + "&coursId=" + coursId);
            return;
        }

        // 7. Validation ordre
        int ordre = lecon.getOrdre();
        if (ordreStr != null && !ordreStr.trim().isEmpty()) {
            try {
                ordre = Integer.parseInt(ordreStr.trim());
                if (ordre < 1) ordre = 1;
            } catch (NumberFormatException e) {}
        }

        // 8. Type d'accès
        boolean gratuit = !"premium".equals(acces);

        // 9. Nettoyage
        if (contenuTexte != null) contenuTexte = contenuTexte.trim();
        if (codeSource != null) codeSource = codeSource.trim();

        // 10. Mise à jour
        lecon.setTitre(titre);
        lecon.setContenuTexte(contenuTexte);
        lecon.setCodeSource(codeSource);
        lecon.setOrdre(ordre);
        lecon.setGratuit(gratuit);

        boolean modifie = leconDAO.modifier(lecon);

        // 11. Résultat
        if (modifie) {
            session.setAttribute("messageSucces", 
                "Leçon \"" + titre + "\" modifiée avec succès !");

            System.out.println("✅ [AdminCoursLeconModifier] Leçon modifiée : ID=" + leconId + " - Titre=" + titre);

            response.sendRedirect(request.getContextPath() + "/admin/cours/lecons?chapitreId=" + chapitreId + "&coursId=" + coursId);
        } else {
            session.setAttribute("messageErreurModificationLecon", 
                "Erreur lors de la modification de la leçon.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/lecons/modifier?id=" + leconId + "&chapitreId=" + chapitreId + "&coursId=" + coursId);
        }
    }
}