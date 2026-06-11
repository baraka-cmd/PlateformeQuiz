// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/GoogleLoginServlet.java
// RÔLE    : Redirige l'utilisateur vers Google pour authentification OAuth 2.0
// ============================================================

package com.learnpath.controllers;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

/**
 * GoogleLoginServlet - Initie la connexion via Google OAuth 2.0
 * 
 * Redirige l'utilisateur vers la page d'authentification Google.
 * Google demande à l'utilisateur d'autoriser l'accès à ses infos.
 * Après autorisation, Google redirige vers GoogleCallbackServlet.
 * 
 * URL : /google-login
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebServlet(name = "GoogleLoginServlet", urlPatterns = {"/google-login"})
public class GoogleLoginServlet extends HttpServlet {

    // Identifiants OAuth 2.0 obtenus depuis Google Cloud Console
    private static final String CLIENT_ID = "527664599034-aop5vb2efmbknjribo7r4327i15sfkbh.apps.googleusercontent.com";
    private static final String REDIRECT_URI = "http://localhost:8080/learnpath/google-callback";

    // URL de base pour l'authentification Google
    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    
    // Paramètres de portée (scopes) : ce qu'on demande à Google
    private static final String SCOPE = "openid email profile";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Générer un état aléatoire pour prévenir les attaques CSRF
        String state = UUID.randomUUID().toString();
        
        // 2. Stocker l'état dans la session pour vérification ultérieure
        HttpSession session = request.getSession();
        session.setAttribute("oauth_state", state);

        // 3. Construire l'URL de redirection Google
        String authUrl = GOOGLE_AUTH_URL + "?" +
            "client_id=" + CLIENT_ID + "&" +
            "redirect_uri=" + REDIRECT_URI + "&" +
            "response_type=code&" +
            "scope=" + SCOPE + "&" +
            "state=" + state + "&" +
            "access_type=online&" +
            "prompt=select_account";

        // 4. Rediriger l'utilisateur vers Google
        response.sendRedirect(authUrl);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}