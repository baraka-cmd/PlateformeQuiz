package com.learnpath.controllers;

import com.learnpath.dao.UtilisateurDAO;
import com.learnpath.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "DeconnexionServlet", urlPatterns = {"/deconnexion"})
public class DeconnexionServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Récupération de la session
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            // Récupérer l'utilisateur avant de détruire la session
            Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
            
            // Supprimer le token "Se souvenir de moi" de la base de données
            if (utilisateur != null) {
                UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
                utilisateurDAO.supprimerRememberToken(utilisateur.getId());
            }
            
            // Invalidation de la session
            session.invalidate();
        }
        
        // Supprimer le cookie "Se souvenir de moi"
        Cookie cookie = new Cookie("remember_token", "");
        cookie.setMaxAge(0); // Expire immédiatement
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
        
        // Redirection vers la page d'accueil
        response.sendRedirect(request.getContextPath() + "/");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}