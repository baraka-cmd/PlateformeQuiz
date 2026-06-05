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
        
        String email = request.getParameter("email");
        String motDePasse = request.getParameter("motDePasse");
        String rememberMe = request.getParameter("rememberMe");
        
        HttpSession session = request.getSession();
        
        session.setAttribute("oldEmail", email);
        
        // Validation
        if (email == null || email.trim().isEmpty() ||
            motDePasse == null || motDePasse.trim().isEmpty()) {
            
            session.setAttribute("messageErreur", "Veuillez remplir tous les champs !");
            response.sendRedirect("connexion");
            return;
        }
        
        email = email.trim();
        motDePasse = motDePasse.trim();
        
        String emailRegex = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
        if (!email.matches(emailRegex)) {
            session.setAttribute("messageErreur", "Veuillez entrer une adresse email valide !");
            response.sendRedirect("connexion");
            return;
        }
        
        String motDePasseHashe = HashUtil.hasher(motDePasse);
        
        if (motDePasseHashe == null) {
            session.setAttribute("messageErreur", "Une erreur est survenue lors du traitement. Veuillez réessayer.");
            response.sendRedirect("connexion");
            return;
        }
        
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        Utilisateur utilisateur = utilisateurDAO.connecterParEmail(email, motDePasseHashe);
        
        if (utilisateur != null) {
            
            // Vérifier si le compte est actif
            if (!utilisateur.isActif()) {
                session.setAttribute("messageErreur", "Votre compte a été désactivé. Contactez l'administrateur.");
                response.sendRedirect("connexion");
                return;
            }
            
            // Connexion réussie
            session.setAttribute("utilisateur", utilisateur);
            session.setAttribute("idUtilisateur", utilisateur.getId());
            session.setAttribute("pseudoUtilisateur", utilisateur.getPseudo());
            session.setAttribute("roleUtilisateur", utilisateur.getRole());
            
            session.removeAttribute("oldEmail");
            
            // Se souvenir de moi
            if ("on".equals(rememberMe) || "true".equals(rememberMe)) {
                String token = genererRememberToken();
                utilisateurDAO.mettreAJourRememberToken(utilisateur.getId(), token);
                
                Cookie cookie = new Cookie("remember_token", token);
                cookie.setMaxAge(REMEMBER_ME_DAYS * 24 * 60 * 60);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
            
            
            // Redirection selon le rôle
            String roleUtilisateur = utilisateur.getRole();
            String contextPath = request.getContextPath();
            
            if ("admin".equals(roleUtilisateur) || "gestionnaire".equals(roleUtilisateur)) {
                session.setAttribute("messageSucces", "Bienvenue " + utilisateur.getPseudo() + " !");
                response.sendRedirect(contextPath + "/admin/dashboard");
            } else {
                session.setAttribute("messageSucces", "Bienvenue " + utilisateur.getPseudo() + " !");
                response.sendRedirect(contextPath + "/joueur/dashboard");
            }
            
        } else {
            session.setAttribute("messageErreur", "Email ou mot de passe incorrect !");
            response.sendRedirect("connexion");
        }
    }
    
    private String genererRememberToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}