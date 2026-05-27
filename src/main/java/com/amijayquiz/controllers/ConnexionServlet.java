package com.amijayquiz.controllers;

import com.amijayquiz.dao.UtilisateurDAO;
import com.amijayquiz.models.Utilisateur;
import com.amijayquiz.utils.HashUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

@WebServlet(name = "ConnexionServlet", urlPatterns = {"/connexion"})
public class ConnexionServlet extends HttpServlet {
    
    // Durée de vie du cookie "Se souvenir de moi" : 30 jours
    private static final int REMEMBER_ME_DAYS = 30;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("views/connexion.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // Récupération des paramètres du formulaire
        String email = request.getParameter("email");
        String motDePasse = request.getParameter("motDePasse");
        String rememberMe = request.getParameter("rememberMe");
        
        HttpSession session = request.getSession();
        
        // Conserver l'email saisi en cas d'erreur
        session.setAttribute("oldEmail", email);
        
        // Validation 1 : Vérifier que les champs ne sont pas vides
        if (email == null || email.trim().isEmpty() ||
            motDePasse == null || motDePasse.trim().isEmpty()) {
            
            session.setAttribute("messageErreur", "Veuillez remplir tous les champs !");
            response.sendRedirect("connexion");
            return;
        }
        
        // Nettoyage des espaces
        email = email.trim();
        motDePasse = motDePasse.trim();
        
        // Validation 2 : Format de l'email
        String emailRegex = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
        if (!email.matches(emailRegex)) {
            session.setAttribute("messageErreur", "Veuillez entrer une adresse email valide !");
            response.sendRedirect("connexion");
            return;
        }
        
        // Hashage du mot de passe
        String motDePasseHashe = HashUtil.hasher(motDePasse);
        
        if (motDePasseHashe == null) {
            session.setAttribute("messageErreur", "Une erreur est survenue lors du traitement. Veuillez réessayer.");
            response.sendRedirect("connexion");
            return;
        }
        
        // Tentative de connexion par email
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        Utilisateur utilisateur = utilisateurDAO.connecterParEmail(email, motDePasseHashe);
        
        if (utilisateur != null) {
            // Connexion réussie
            session.setAttribute("utilisateur", utilisateur);
            session.setAttribute("idUtilisateur", utilisateur.getId());
            session.setAttribute("pseudoUtilisateur", utilisateur.getPseudo());
            session.setAttribute("roleUtilisateur", utilisateur.getRole());
            
            // Nettoyer l'email sauvegardé
            session.removeAttribute("oldEmail");
            
            // ===== GESTION "SE SOUVENIR DE MOI" =====
            if ("on".equals(rememberMe) || "true".equals(rememberMe)) {
                // Générer un token aléatoire sécurisé
                String token = genererRememberToken();
                
                // Sauvegarder le token dans la base de données
                utilisateurDAO.mettreAJourRememberToken(utilisateur.getId(), token);
                
                // Créer le cookie avec le token
                Cookie cookie = new Cookie("remember_token", token);
                cookie.setMaxAge(REMEMBER_ME_DAYS * 24 * 60 * 60); // 30 jours en secondes
                cookie.setHttpOnly(true); // Empêche l'accès JavaScript
                cookie.setPath("/"); // Disponible sur tout le site
                response.addCookie(cookie);
            }
            
            // Redirection selon le rôle
            if ("admin".equals(utilisateur.getRole())) {
                session.setAttribute("messageSucces", "Bienvenue Administrateur " + utilisateur.getPseudo() + " !");
                response.sendRedirect("admin/dashboard");
            } else {
                session.setAttribute("messageSucces", "Bienvenue " + utilisateur.getPseudo() + " !");
                response.sendRedirect("joueur/dashboard");
            }
            
        } else {
            session.setAttribute("messageErreur", "Email ou mot de passe incorrect !");
            response.sendRedirect("connexion");
        }
    }
    
    /**
     * Génère un token aléatoire sécurisé pour le cookie "Se souvenir de moi"
     * 
     * @return Token encodé en Base64 URL-safe
     */
    private String genererRememberToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 256 bits
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}