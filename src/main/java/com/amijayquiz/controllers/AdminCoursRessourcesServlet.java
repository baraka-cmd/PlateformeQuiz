// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminCoursRessourcesServlet.java
// RÔLE    : Contrôleur pour gérer les ressources d'une leçon
//           GET  : Liste les ressources de la leçon
//           POST : Ajouter ou supprimer une ressource
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.LeconDAO;
import com.amijayquiz.dao.RessourceDAO;
import com.amijayquiz.models.Lecon;
import com.amijayquiz.models.Ressource;
import com.amijayquiz.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * AdminCoursRessourcesServlet - Gestion des ressources d'une leçon
 * 
 * GET  : Affiche la liste des ressources existantes
 * POST : action=ajouter → Ajoute une nouvelle ressource
 *        action=supprimer → Supprime une ressource
 * 
 * URL : /admin/cours/ressources?leconId=X&coursId=Y
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminCoursRessourcesServlet", urlPatterns = {"/admin/cours/ressources"})
public class AdminCoursRessourcesServlet extends HttpServlet {

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

        // 2. Récupération et validation des paramètres
        String leconIdStr = request.getParameter("leconId");
        String coursIdStr = request.getParameter("coursId");

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

        int coursId = 0;
        if (coursIdStr != null && !coursIdStr.trim().isEmpty()) {
            try { coursId = Integer.parseInt(coursIdStr.trim()); } catch (NumberFormatException e) {}
        }

        // 3. Récupération de la leçon
        LeconDAO leconDAO = new LeconDAO();
        Lecon lecon = leconDAO.trouverParId(leconId);

        if (lecon == null) {
            session.setAttribute("messageErreur", "Cette leçon n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 4. Récupération des ressources existantes
        RessourceDAO ressourceDAO = new RessourceDAO();
        List<Ressource> ressources = ressourceDAO.listerParLecon(leconId);

        // 5. Message de succès/erreur
        String messageSucces = (String) session.getAttribute("messageSucces");
        String messageErreur = (String) session.getAttribute("messageErreur");
        if (messageSucces != null) session.removeAttribute("messageSucces");
        if (messageErreur != null) session.removeAttribute("messageErreur");

        // 6. Passage des attributs
        request.setAttribute("pageTitle", "Ressources - " + lecon.getTitre() + " - Administration");
        request.setAttribute("lecon", lecon);
        request.setAttribute("ressources", ressources);
        request.setAttribute("totalRessources", ressources != null ? ressources.size() : 0);
        request.setAttribute("messageSucces", messageSucces);
        request.setAttribute("messageErreur", messageErreur);

        // 7. Affichage
        request.getRequestDispatcher("/views/admin/admin-cours-ressources.jsp").forward(request, response);
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
        String action = request.getParameter("action");
        String leconIdStr = request.getParameter("leconId");
        String coursIdStr = request.getParameter("coursId");

        if (leconIdStr == null || leconIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID de la leçon manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        int leconId;
        int coursId = 0;
        try {
            leconId = Integer.parseInt(leconIdStr.trim());
            if (coursIdStr != null && !coursIdStr.isEmpty()) {
                coursId = Integer.parseInt(coursIdStr.trim());
            }
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        String redirectUrl = request.getContextPath() + "/admin/cours/ressources?leconId=" + leconId;
        if (coursId > 0) redirectUrl += "&coursId=" + coursId;

        // 3. Traitement selon l'action
        if ("ajouter".equals(action)) {
            traiterAjoutRessource(request, response, session, leconId, redirectUrl);
        } else if ("supprimer".equals(action)) {
            traiterSuppressionRessource(request, response, session, leconId, redirectUrl);
        } else {
            session.setAttribute("messageErreur", "Action invalide.");
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Ajoute une nouvelle ressource
     */
    private void traiterAjoutRessource(HttpServletRequest request, HttpServletResponse response,
                                        HttpSession session, int leconId, String redirectUrl) throws IOException {

        String titre = request.getParameter("titre");
        String type = request.getParameter("type");
        String urlFichier = request.getParameter("urlFichier");
        String description = request.getParameter("description");
        String taille = request.getParameter("taille");

        // Validation
        if (titre == null || titre.trim().isEmpty()) {
            session.setAttribute("messageErreur", "Le titre de la ressource est obligatoire.");
            response.sendRedirect(redirectUrl);
            return;
        }

        if (type == null || type.trim().isEmpty()) {
            session.setAttribute("messageErreur", "Le type de ressource est obligatoire.");
            response.sendRedirect(redirectUrl);
            return;
        }

        if (urlFichier == null || urlFichier.trim().isEmpty()) {
            session.setAttribute("messageErreur", "L'URL du fichier est obligatoire.");
            response.sendRedirect(redirectUrl);
            return;
        }

        // Créer la ressource
        Ressource ressource = new Ressource();
        ressource.setLeconId(leconId);
        ressource.setTitre(titre.trim());
        ressource.setType(type.trim());
        ressource.setUrlFichier(urlFichier.trim());
        ressource.setDescription(description != null ? description.trim() : "");
        ressource.setTaille(taille != null ? taille.trim() : "");

        RessourceDAO ressourceDAO = new RessourceDAO();
        boolean ajoute = ressourceDAO.ajouter(ressource);

        if (ajoute) {
            session.setAttribute("messageSucces", "Ressource \"" + titre + "\" ajoutée avec succès !");
        } else {
            session.setAttribute("messageErreur", "Erreur lors de l'ajout de la ressource.");
        }

        response.sendRedirect(redirectUrl);
    }

    /**
     * Supprime une ressource
     */
    private void traiterSuppressionRessource(HttpServletRequest request, HttpServletResponse response,
                                              HttpSession session, int leconId, String redirectUrl) throws IOException {

        String ressourceIdStr = request.getParameter("ressourceId");

        if (ressourceIdStr == null || ressourceIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID de la ressource manquant.");
            response.sendRedirect(redirectUrl);
            return;
        }

        int ressourceId;
        try {
            ressourceId = Integer.parseInt(ressourceIdStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de ressource invalide.");
            response.sendRedirect(redirectUrl);
            return;
        }

        RessourceDAO ressourceDAO = new RessourceDAO();
        boolean supprime = ressourceDAO.supprimer(ressourceId);

        if (supprime) {
            session.setAttribute("messageSucces", "Ressource supprimée avec succès !");
        } else {
            session.setAttribute("messageErreur", "Erreur lors de la suppression.");
        }

        response.sendRedirect(redirectUrl);
    }
}