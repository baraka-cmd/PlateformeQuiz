// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/ChangerMotDePasseServlet.java
// RÔLE    : Gère le changement de mot de passe utilisateur
//           Vérifie l'ancien mot de passe, valide le nouveau,
//           le hashe et le met à jour dans la base de données
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.UtilisateurDAO;
import com.learnpath.models.Utilisateur;
import com.learnpath.utils.HashUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * ChangerMotDePasseServlet - Permet à l'utilisateur de changer son mot de passe
 * 
 * Reçoit l'ancien mot de passe, le nouveau mot de passe et sa confirmation.
 * Vérifie que l'ancien mot de passe est correct, valide le nouveau,
 * puis le met à jour dans la base de données après hachage.
 * 
 * URL : /changer-mot-de-passe
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebServlet(name = "ChangerMotDePasseServlet", urlPatterns = {"/changer-mot-de-passe"})
public class ChangerMotDePasseServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Vérifier que l'utilisateur est connecté
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        // Afficher la page de changement de mot de passe
        request.getRequestDispatcher("/views/changer-mot-de-passe.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        // 1. Vérifier que l'utilisateur est connecté
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");

        // 2. Récupération des paramètres
        String ancienMotDePasse = request.getParameter("ancienMotDePasse");
        String nouveauMotDePasse = request.getParameter("nouveauMotDePasse");
        String confirmation = request.getParameter("confirmation");

        // 3. Validation : champs non vides
        if (ancienMotDePasse == null || ancienMotDePasse.trim().isEmpty() ||
            nouveauMotDePasse == null || nouveauMotDePasse.trim().isEmpty() ||
            confirmation == null || confirmation.trim().isEmpty()) {

            session.setAttribute("messageErreur", "Tous les champs sont obligatoires !");
            response.sendRedirect(request.getContextPath() + "/changer-mot-de-passe");
            return;
        }

        // Nettoyage des espaces
        ancienMotDePasse = ancienMotDePasse.trim();
        nouveauMotDePasse = nouveauMotDePasse.trim();
        confirmation = confirmation.trim();

        // 4. Validation : longueur du nouveau mot de passe
        if (nouveauMotDePasse.length() < 8) {
            session.setAttribute("messageErreur", "Le nouveau mot de passe doit contenir au moins 8 caractères !");
            response.sendRedirect(request.getContextPath() + "/changer-mot-de-passe");
            return;
        }

        // 5. Validation : confirmation identique
        if (!nouveauMotDePasse.equals(confirmation)) {
            session.setAttribute("messageErreur", "Les nouveaux mots de passe ne correspondent pas !");
            response.sendRedirect(request.getContextPath() + "/changer-mot-de-passe");
            return;
        }

        // 6. Validation : force du mot de passe (au moins une majuscule, minuscule, chiffre, caractère spécial)
        if (!nouveauMotDePasse.matches(".*[A-Z].*")) {
            session.setAttribute("messageErreur", "Le mot de passe doit contenir au moins une majuscule !");
            response.sendRedirect(request.getContextPath() + "/changer-mot-de-passe");
            return;
        }
        if (!nouveauMotDePasse.matches(".*[a-z].*")) {
            session.setAttribute("messageErreur", "Le mot de passe doit contenir au moins une minuscule !");
            response.sendRedirect(request.getContextPath() + "/changer-mot-de-passe");
            return;
        }
        if (!nouveauMotDePasse.matches(".*[0-9].*")) {
            session.setAttribute("messageErreur", "Le mot de passe doit contenir au moins un chiffre !");
            response.sendRedirect(request.getContextPath() + "/changer-mot-de-passe");
            return;
        }
        if (!nouveauMotDePasse.matches(".*[!@#$%^&*(),.?\":{}|<>_\\-+=\\[\\]\\\\;:'/`~].*")) {
            session.setAttribute("messageErreur", "Le mot de passe doit contenir au moins un caractère spécial !");
            response.sendRedirect(request.getContextPath() + "/changer-mot-de-passe");
            return;
        }

        // 7. Vérifier que l'ancien mot de passe est correct
        String ancienMotDePasseHashe = HashUtil.hasher(ancienMotDePasse);

        if (ancienMotDePasseHashe == null) {
            session.setAttribute("messageErreur", "Une erreur est survenue. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/changer-mot-de-passe");
            return;
        }

        // Vérifier que l'ancien MDP correspond à celui en base
        if (!ancienMotDePasseHashe.equals(utilisateur.getMotDePasse())) {
            session.setAttribute("messageErreur", "L'ancien mot de passe est incorrect !");
            response.sendRedirect(request.getContextPath() + "/changer-mot-de-passe");
            return;
        }

        // 8. Vérifier que le nouveau MDP est différent de l'ancien
        if (ancienMotDePasse.equals(nouveauMotDePasse)) {
            session.setAttribute("messageErreur", "Le nouveau mot de passe doit être différent de l'ancien !");
            response.sendRedirect(request.getContextPath() + "/changer-mot-de-passe");
            return;
        }

        // 9. Hasher le nouveau mot de passe
        String nouveauMotDePasseHashe = HashUtil.hasher(nouveauMotDePasse);

        if (nouveauMotDePasseHashe == null) {
            session.setAttribute("messageErreur", "Une erreur est survenue lors du traitement. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/changer-mot-de-passe");
            return;
        }

        // 10. Mettre à jour le mot de passe dans la base de données
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        boolean modifie = utilisateurDAO.modifierMotDePasse(utilisateur.getId(), nouveauMotDePasseHashe);

        if (modifie) {
            // Mettre à jour l'utilisateur en session avec le nouveau mot de passe
            utilisateur.setMotDePasse(nouveauMotDePasseHashe);
            session.setAttribute("utilisateur", utilisateur);

            session.setAttribute("messageSucces", "Votre mot de passe a été changé avec succès ! ✅");
            
            // Rediriger selon le rôle
            if ("admin".equals(utilisateur.getRole())) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
            }
        } else {
            session.setAttribute("messageErreur", "Erreur lors de la modification du mot de passe. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/changer-mot-de-passe");
        }
    }
}