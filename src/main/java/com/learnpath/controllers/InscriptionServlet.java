package com.learnpath.controllers;

import com.learnpath.dao.UtilisateurDAO;
import com.learnpath.models.Utilisateur;
import com.learnpath.utils.EmailUtil;
import com.learnpath.utils.HashUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "InscriptionServlet", urlPatterns = {"/inscription"})
public class InscriptionServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("views/inscription.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // Récupération des paramètres du formulaire
        String pseudo = request.getParameter("pseudo");
        String email = request.getParameter("email");
        String motDePasse = request.getParameter("motDePasse");
        String confirmation = request.getParameter("confirmation");
        
        HttpSession session = request.getSession();
        
        // Conserver les valeurs saisies en cas d'erreur
        session.setAttribute("oldPseudo", pseudo);
        session.setAttribute("oldEmail", email);
        
        // ===== VALIDATION 1 : Champs obligatoires =====
        if (pseudo == null || pseudo.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            motDePasse == null || motDePasse.trim().isEmpty() ||
            confirmation == null || confirmation.trim().isEmpty()) {
            
            session.setAttribute("messageErreur", "Veuillez remplir tous les champs !");
            response.sendRedirect("inscription");
            return;
        }
        
        // Nettoyage des espaces
        pseudo = pseudo.trim();
        email = email.trim().toLowerCase();
        motDePasse = motDePasse.trim();
        confirmation = confirmation.trim();
        
        // ===== VALIDATION 2 : Longueur du pseudo =====
        if (pseudo.length() < 3) {
            session.setAttribute("messageErreur", "Le pseudo doit contenir au moins 3 caractères !");
            response.sendRedirect("inscription");
            return;
        }
        
        if (pseudo.length() > 50) {
            session.setAttribute("messageErreur", "Le pseudo ne peut pas dépasser 50 caractères !");
            response.sendRedirect("inscription");
            return;
        }
        
        // ===== VALIDATION 3 : Format de l'email =====
        String emailRegex = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
        if (!email.matches(emailRegex)) {
            session.setAttribute("messageErreur", "Veuillez entrer une adresse email valide !");
            response.sendRedirect("inscription");
            return;
        }
        
        // ===== VALIDATION 4 : Longueur du mot de passe =====
        if (motDePasse.length() < 8) {
            session.setAttribute("messageErreur", "Le mot de passe doit contenir au moins 8 caractères !");
            response.sendRedirect("inscription");
            return;
        }
        
        // ===== VALIDATION 5 : Force du mot de passe =====
        if (!motDePasse.matches(".*[A-Z].*")) {
            session.setAttribute("messageErreur", "Le mot de passe doit contenir au moins une majuscule !");
            response.sendRedirect("inscription");
            return;
        }
        
        if (!motDePasse.matches(".*[a-z].*")) {
            session.setAttribute("messageErreur", "Le mot de passe doit contenir au moins une minuscule !");
            response.sendRedirect("inscription");
            return;
        }
        
        if (!motDePasse.matches(".*[0-9].*")) {
            session.setAttribute("messageErreur", "Le mot de passe doit contenir au moins un chiffre !");
            response.sendRedirect("inscription");
            return;
        }
        
        // ===== VALIDATION 6 : Correspondance des mots de passe =====
        if (!motDePasse.equals(confirmation)) {
            session.setAttribute("messageErreur", "Les mots de passe ne correspondent pas !");
            response.sendRedirect("inscription");
            return;
        }
        
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        
        // ===== VALIDATION 7 : Pseudo déjà utilisé ? =====
        if (utilisateurDAO.existePseudo(pseudo)) {
            session.setAttribute("messageErreur", "Ce pseudo est déjà utilisé ! Veuillez en choisir un autre.");
            response.sendRedirect("inscription");
            return;
        }
        
        // ===== VALIDATION 8 : Email déjà utilisé ? =====
        if (utilisateurDAO.existeEmail(email)) {
            session.setAttribute("messageErreur", "Cet email est déjà utilisé ! Veuillez en choisir un autre.");
            response.sendRedirect("inscription");
            return;
        }
        
        // Hashage du mot de passe
        String motDePasseHashe = HashUtil.hasher(motDePasse);
        
        if (motDePasseHashe == null) {
            session.setAttribute("messageErreur", "Une erreur est survenue lors du traitement. Veuillez réessayer.");
            response.sendRedirect("inscription");
            return;
        }
        
        // Création de l'objet utilisateur
        Utilisateur utilisateur = new Utilisateur(pseudo, motDePasseHashe);
        utilisateur.setEmail(email);
        utilisateur.setRole("joueur");
        utilisateur.setActif(true);
        
        // Inscription dans la base de données
        boolean inscriptionReussie = utilisateurDAO.inscrire(utilisateur);
        
        // Nettoyer les anciennes valeurs
        session.removeAttribute("oldPseudo");
        session.removeAttribute("oldEmail");
        
        if (inscriptionReussie) {
            // Envoyer l'email de bienvenue (optionnel, ne bloque pas l'inscription)
            try {
                EmailUtil.envoyerBienvenue(email, pseudo);
            } catch (Exception e) {
                System.err.println("⚠️ Email de bienvenue non envoyé à " + email + " : " + e.getMessage());
            }
            
            session.setAttribute("messageSucces", "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            response.sendRedirect("connexion");
        } else {
            session.setAttribute("messageErreur", "Erreur lors de l'inscription. Veuillez réessayer.");
            response.sendRedirect("inscription");
        }
    }
}