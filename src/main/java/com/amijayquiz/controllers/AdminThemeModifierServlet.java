// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminThemeModifierServlet.java
// RÔLE    : Contrôleur pour modifier un thème existant
//           GET  : Affiche le formulaire pré-rempli
//           POST : Valide et met à jour le thème
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
 * AdminThemeModifierServlet - Modification d'un thème
 * 
 * GET  : Affiche le formulaire pré-rempli
 * POST : Valide les données et met à jour le thème
 * 
 * URL : /admin/themes/modifier
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminThemeModifierServlet", urlPatterns = {"/admin/themes/modifier"})
public class AdminThemeModifierServlet extends HttpServlet {

    // Icônes disponibles (même liste que pour la création)
    private static final String[] ICONES_DISPONIBLES = {
        "fas fa-globe", "fas fa-coffee", "fab fa-python", "fab fa-php",
        "fas fa-database", "fas fa-shield-alt", "fas fa-mobile-alt",
        "fas fa-robot", "fas fa-palette", "fas fa-network-wired",
        "fab fa-linux", "fas fa-chart-pie", "fas fa-book",
        "fas fa-laptop-code", "fas fa-server", "fas fa-cloud",
        "fas fa-cogs", "fas fa-microchip", "fas fa-gamepad",
        "fas fa-camera", "fas fa-music", "fas fa-calculator",
        "fas fa-flask", "fas fa-atom", "fas fa-dna"
    };

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

        // 3. Récupération du thème
        ThemeDAO themeDAO = new ThemeDAO();
        Theme theme = themeDAO.trouverParId(id);

        if (theme == null) {
            session.setAttribute("messageErreur", "Ce thème n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/themes");
            return;
        }

        // 4. Message d'erreur éventuel
        String messageErreur = (String) session.getAttribute("messageErreurModificationTheme");
        if (messageErreur != null) session.removeAttribute("messageErreurModificationTheme");

        // 5. Passage des attributs
        request.setAttribute("pageTitle", "Modifier le thème - " + theme.getNom() + " - Administration");
        request.setAttribute("theme", theme);
        request.setAttribute("iconesDisponibles", ICONES_DISPONIBLES);
        request.setAttribute("messageErreur", messageErreur);

        // 6. Affichage
        request.getRequestDispatcher("/views/admin/admin-theme-modifier.jsp").forward(request, response);
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
    String idStr = request.getParameter("id");
    String nom = request.getParameter("nom");
    String description = request.getParameter("description");
    String icone = request.getParameter("icone");
    String couleur = request.getParameter("couleur");
    String ordreStr = request.getParameter("ordre");

    // 3. Validation ID
    if (idStr == null || idStr.trim().isEmpty()) {
        session.setAttribute("messageErreur", "ID du thème manquant.");
        response.sendRedirect(request.getContextPath() + "/admin/themes");
        return;
    }

    int id;
    try {
        id = Integer.parseInt(idStr.trim());
    } catch (NumberFormatException e) {
        session.setAttribute("messageErreur", "ID de thème invalide.");
        response.sendRedirect(request.getContextPath() + "/admin/themes");
        return;
    }

    // 4. Vérifier que le thème existe
    ThemeDAO themeDAO = new ThemeDAO();
    Theme theme = themeDAO.trouverParId(id);

    if (theme == null) {
        session.setAttribute("messageErreur", "Ce thème n'existe pas.");
        response.sendRedirect(request.getContextPath() + "/admin/themes");
        return;
    }

    // 5. Validation nom (seulement s'il n'est pas verrouillé)
    boolean nomModifiable = themeDAO.compterCoursParTheme(theme.getNom()) == 0;
    
    if (nomModifiable) {
        if (nom == null || nom.trim().isEmpty()) {
            session.setAttribute("messageErreurModificationTheme", "Le nom du thème est obligatoire.");
            response.sendRedirect(request.getContextPath() + "/admin/themes/modifier?id=" + id);
            return;
        }
        nom = nom.trim();
        if (nom.length() < 2) {
            session.setAttribute("messageErreurModificationTheme", "Le nom doit contenir au moins 2 caractères.");
            response.sendRedirect(request.getContextPath() + "/admin/themes/modifier?id=" + id);
            return;
        }
        if (nom.length() > 100) {
            session.setAttribute("messageErreurModificationTheme", "Le nom ne peut pas dépasser 100 caractères.");
            response.sendRedirect(request.getContextPath() + "/admin/themes/modifier?id=" + id);
            return;
        }
    } else {
        // Nom verrouillé, on garde l'ancien
        nom = theme.getNom();
    }

    // 6. Validation icône
    if (icone == null || icone.trim().isEmpty()) {
        icone = theme.getIcone();
    }
    icone = icone.trim();

    // 7. Validation couleur - CORRIGÉE
    if (couleur == null || couleur.trim().isEmpty()) {
        couleur = theme.getCouleur().replace("#", "");
    }
    couleur = couleur.trim().replace("#", ""); // Enlever le # si présent

    // Vérifier que c'est bien un hexadécimal à 6 caractères
    if (!couleur.matches("^[0-9a-fA-F]{6}$")) {
        // Si la couleur est invalide, garder l'ancienne
        couleur = theme.getCouleur().replace("#", "");
    }

    // Ajouter le # pour le stockage
    String couleurComplete = "#" + couleur;

    // 8. Validation ordre
    int ordre = theme.getOrdre();
    if (ordreStr != null && !ordreStr.trim().isEmpty()) {
        try {
            ordre = Integer.parseInt(ordreStr.trim());
            if (ordre < 1) ordre = 1;
            if (ordre > 999) ordre = 999;
        } catch (NumberFormatException e) {
            // Garder l'ancien ordre
        }
    }

    // 9. Nettoyage description
    if (description != null) {
        description = description.trim();
    }

    // 10. Mise à jour du thème
    theme.setNom(nom);
    theme.setDescription(description);
    theme.setIcone(icone);
    theme.setCouleur(couleurComplete);
    theme.setOrdre(ordre);

    boolean modifie = themeDAO.modifier(theme);

    // 11. Résultat
    if (modifie) {
        // ✅ Supprimer le brouillon après modification réussie
        session.setAttribute("messageSucces", 
            "Thème \"" + nom + "\" modifié avec succès !");
        response.sendRedirect(request.getContextPath() + "/admin/themes");
    } else {
        session.setAttribute("messageErreurModificationTheme", 
            "Erreur lors de la modification. Vérifiez que le nom n'est pas déjà utilisé.");
        response.sendRedirect(request.getContextPath() + "/admin/themes/modifier?id=" + id);
    }
}
}