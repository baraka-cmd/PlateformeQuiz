// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/AdminThemeAjouterServlet.java
// RÔLE    : Contrôleur pour créer un nouveau thème
//           GET  : Affiche le formulaire de création
//           POST : Valide et enregistre le thème
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.ThemeDAO;
import com.learnpath.models.Theme;
import com.learnpath.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AdminThemeAjouterServlet - Création d'un thème
 * 
 * GET  : Affiche le formulaire de création
 * POST : Valide les données et crée le thème
 * 
 * URL : /admin/themes/creer
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebServlet(name = "AdminThemeAjouterServlet", urlPatterns = {"/admin/themes/creer"})
public class AdminThemeAjouterServlet extends HttpServlet {

    // Liste des icônes Font Awesome disponibles pour les thèmes
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

        // 2. Récupérer l'ordre suggéré (max + 1)
        ThemeDAO themeDAO = new ThemeDAO();
        int ordreSuggere = themeDAO.getOrdreMaximum() + 1;

        // 3. Message d'erreur éventuel
        String messageErreur = (String) session.getAttribute("messageErreurCreationTheme");
        if (messageErreur != null) session.removeAttribute("messageErreurCreationTheme");

        // 4. Passage des attributs
        request.setAttribute("pageTitle", "Créer un thème - Administration - LearnPath");
        request.setAttribute("iconesDisponibles", ICONES_DISPONIBLES);
        request.setAttribute("ordreSuggere", ordreSuggere);
        request.setAttribute("messageErreur", messageErreur);

        // 5. Affichage
        request.getRequestDispatcher("/views/admin/admin-theme-creer.jsp").forward(request, response);
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
    String nom = request.getParameter("nom");
    String description = request.getParameter("description");
    String icone = request.getParameter("icone");
    String couleur = request.getParameter("couleur");
    String ordreStr = request.getParameter("ordre");

    // DEBUG - Afficher les valeurs reçues
    System.out.println("📋 [AdminThemeAjouter] Données reçues :");
    System.out.println("   nom = '" + nom + "'");
    System.out.println("   description = '" + description + "'");
    System.out.println("   icone = '" + icone + "'");
    System.out.println("   couleur = '" + couleur + "'");
    System.out.println("   ordre = '" + ordreStr + "'");

    // 3. Validation
    if (nom == null || nom.trim().isEmpty()) {
        session.setAttribute("messageErreurCreationTheme", "Le nom du thème est obligatoire.");
        response.sendRedirect(request.getContextPath() + "/admin/themes/creer");
        return;
    }

    nom = nom.trim();

    if (nom.length() < 2) {
        session.setAttribute("messageErreurCreationTheme", "Le nom doit contenir au moins 2 caractères.");
        response.sendRedirect(request.getContextPath() + "/admin/themes/creer");
        return;
    }

    if (nom.length() > 100) {
        session.setAttribute("messageErreurCreationTheme", "Le nom ne peut pas dépasser 100 caractères.");
        response.sendRedirect(request.getContextPath() + "/admin/themes/creer");
        return;
    }

    // 4. Vérifier que le nom n'existe pas déjà
    ThemeDAO themeDAO = new ThemeDAO();
    if (themeDAO.trouverParNom(nom) != null) {
        session.setAttribute("messageErreurCreationTheme", 
            "Un thème avec ce nom existe déjà. Choisissez un nom différent.");
        response.sendRedirect(request.getContextPath() + "/admin/themes/creer");
        return;
    }

    // 5. Validation icône
    if (icone == null || icone.trim().isEmpty()) {
        icone = "fas fa-book";
    }
    icone = icone.trim();

    // 6. Validation couleur - CORRIGÉE
    if (couleur == null || couleur.trim().isEmpty()) {
        couleur = "667eea";
    }
    couleur = couleur.trim().replace("#", ""); // Enlever le # si présent

    // Vérifier que c'est un hexadécimal valide
    if (!couleur.matches("^[0-9a-fA-F]{6}$")) {
        System.out.println("⚠️ [AdminThemeAjouter] Couleur invalide : '" + couleur + "', utilisation de la valeur par défaut");
        couleur = "667eea";
    }

    // Ajouter le # pour le stockage
    String couleurComplete = "#" + couleur;

    // 7. Validation ordre
    int ordre = 1;
    if (ordreStr != null && !ordreStr.trim().isEmpty()) {
        try {
            ordre = Integer.parseInt(ordreStr.trim());
            if (ordre < 1) ordre = 1;
            if (ordre > 999) ordre = 999;
        } catch (NumberFormatException e) {
            System.out.println("⚠️ [AdminThemeAjouter] Ordre invalide : '" + ordreStr + "', utilisation de 1");
        }
    }

    // 8. Nettoyage de la description
    if (description != null) {
        description = description.trim();
    }

    // 9. Création du thème
    Theme theme = new Theme(nom, description, icone, couleurComplete, ordre);
    boolean ajoute = themeDAO.ajouter(theme);

    System.out.println("📋 [AdminThemeAjouter] Résultat ajout : " + (ajoute ? "SUCCÈS" : "ÉCHEC"));

    // 10. Résultat
    if (ajoute) {
        session.setAttribute("messageSucces", 
            "Thème \"" + nom + "\" créé avec succès !");
        response.sendRedirect(request.getContextPath() + "/admin/themes");
    } else {
        session.setAttribute("messageErreurCreationTheme", 
            "Erreur lors de la création du thème. Veuillez réessayer.");
        response.sendRedirect(request.getContextPath() + "/admin/themes/creer");
    }
}
}