// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/GoogleCallbackServlet.java
// RÔLE    : Reçoit le code de Google, échange contre les infos utilisateur,
//           connecte ou crée le compte automatiquement
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.UtilisateurDAO;
import com.learnpath.models.Utilisateur;
import com.learnpath.utils.HashUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;

/**
 * GoogleCallbackServlet - Callback OAuth 2.0 après authentification Google
 * 
 * Reçoit le code d'autorisation de Google, l'échange contre un token,
 * récupère les informations du profil utilisateur (email, nom, photo),
 * puis connecte ou crée le compte automatiquement.
 * 
 * URL : /google-callback
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebServlet(name = "GoogleCallbackServlet", urlPatterns = {"/google-callback"})
public class GoogleCallbackServlet extends HttpServlet {

    // Identifiants OAuth 2.0
    private static final String CLIENT_ID = "527664599034-aop5vb2efmbknjribo7r4327i15sfkbh.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String REDIRECT_URI = "http://localhost:8080/learnpath/google-callback";
    // URL pour échanger le code contre un token
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    
    // URL pour récupérer les infos utilisateur
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    
    // Transport HTTP et Factory JSON
    private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String contextPath = request.getContextPath();

        // 1. Vérifier le paramètre state (anti-CSRF)
        String state = request.getParameter("state");
        String sessionState = (String) session.getAttribute("oauth_state");
        
        if (state == null || !state.equals(sessionState)) {
            session.setAttribute("messageErreur", "Erreur de sécurité. Veuillez réessayer.");
            response.sendRedirect(contextPath + "/connexion");
            return;
        }
        
        // Nettoyer le state de la session
        session.removeAttribute("oauth_state");

        // 2. Vérifier si Google a renvoyé une erreur
        String error = request.getParameter("error");
        if (error != null) {
            session.setAttribute("messageErreur", "L'authentification Google a été annulée.");
            response.sendRedirect(contextPath + "/connexion");
            return;
        }

        // 3. Récupérer le code d'autorisation
        String code = request.getParameter("code");
        if (code == null || code.trim().isEmpty()) {
            session.setAttribute("messageErreur", "Aucun code d'autorisation reçu de Google.");
            response.sendRedirect(contextPath + "/connexion");
            return;
        }

        try {
            // 4. Échanger le code contre un token d'accès
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    HTTP_TRANSPORT,
                    JSON_FACTORY,
                    CLIENT_ID,
                    CLIENT_SECRET,
                    code,
                    REDIRECT_URI
            ).execute();

            String accessToken = tokenResponse.getAccessToken();

            // 5. Récupérer les informations du profil utilisateur Google
            String userInfoJson = getUserInfo(accessToken);

            if (userInfoJson == null) {
                session.setAttribute("messageErreur", "Impossible de récupérer vos informations Google.");
                response.sendRedirect(contextPath + "/connexion");
                return;
            }

            // 6. Parser les informations utilisateur
            ObjectMapper mapper = new ObjectMapper();
            JsonNode userInfo = mapper.readTree(userInfoJson);

            String email = userInfo.has("email") ? userInfo.get("email").asText() : null;
            String nom = userInfo.has("name") ? userInfo.get("name").asText() : "Utilisateur Google";
            String prenom = userInfo.has("given_name") ? userInfo.get("given_name").asText() : "";
            String photo = userInfo.has("picture") ? userInfo.get("picture").asText() : null;

            if (email == null || email.trim().isEmpty()) {
                session.setAttribute("messageErreur", "Votre compte Google n'a pas d'adresse email associée.");
                response.sendRedirect(contextPath + "/connexion");
                return;
            }

            // 7. Chercher ou créer l'utilisateur dans la base de données
            UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
            Utilisateur utilisateur = utilisateurDAO.trouverParEmail(email);

            if (utilisateur == null) {
                // Créer un nouveau compte
                utilisateur = new Utilisateur();
                
                // Générer un pseudo à partir du nom Google
                String pseudo = genererPseudoDepuisEmail(email, nom);
                
                // Vérifier que le pseudo n'existe pas déjà
                int compteur = 1;
                String pseudoBase = pseudo;
                while (utilisateurDAO.existePseudo(pseudo)) {
                    pseudo = pseudoBase + compteur;
                    compteur++;
                }
                
                utilisateur.setPseudo(pseudo);
                utilisateur.setEmail(email);
                
                // Générer un mot de passe aléatoire sécurisé (l'utilisateur ne l'utilisera pas)
                String motDePasseAleatoire = genererMotDePasseAleatoire();
                String motDePasseHashe = HashUtil.hasher(motDePasseAleatoire);
                utilisateur.setMotDePasse(motDePasseHashe);
                
                utilisateur.setRole("joueur");
                utilisateur.setActif(true);

                boolean inscrit = utilisateurDAO.inscrire(utilisateur);
                
                if (!inscrit) {
                    session.setAttribute("messageErreur", "Erreur lors de la création de votre compte.");
                    response.sendRedirect(contextPath + "/connexion");
                    return;
                }
                
                // Recharger l'utilisateur pour avoir son ID
                utilisateur = utilisateurDAO.trouverParEmail(email);
            }

            // 8. Connecter l'utilisateur
            if (utilisateur != null && utilisateur.isActif()) {
                session.setAttribute("utilisateur", utilisateur);
                session.setAttribute("idUtilisateur", utilisateur.getId());
                session.setAttribute("pseudoUtilisateur", utilisateur.getPseudo());
                session.setAttribute("roleUtilisateur", utilisateur.getRole());

                String messageBienvenue = "Bienvenue " + utilisateur.getPseudo() + " ! Connecté via Google ✅";
                session.setAttribute("messageSucces", messageBienvenue);

                // 9. Rediriger selon le rôle
                if ("admin".equals(utilisateur.getRole())) {
                    response.sendRedirect(contextPath + "/admin/dashboard");
                } else {
                    response.sendRedirect(contextPath + "/joueur/dashboard");
                }
            } else {
                session.setAttribute("messageErreur", "Votre compte est désactivé. Contactez l'administrateur.");
                response.sendRedirect(contextPath + "/connexion");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'authentification Google : " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("messageErreur", "Une erreur est survenue lors de la connexion avec Google.");
            response.sendRedirect(contextPath + "/connexion");
        }
    }

    /**
     * Récupère les informations du profil utilisateur depuis Google
     * 
     * @param accessToken Le token d'accès obtenu
     * @return JSON contenant les infos utilisateur, ou null si erreur
     */
    private String getUserInfo(String accessToken) {
        try {
            URL url = new URL(USERINFO_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.err.println("❌ Erreur API Google UserInfo : " + responseCode);
                return null;
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return response.toString();

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération infos Google : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Génère un pseudo à partir de l'email Google
     * Exemple : "jean.dupont@gmail.com" → "jean_dupont"
     * 
     * @param email L'adresse email Google
     * @param nom   Le nom complet Google
     * @return Un pseudo nettoyé
     */
    private String genererPseudoDepuisEmail(String email, String nom) {
        if (nom != null && !nom.trim().isEmpty()) {
            // Remplacer les espaces et caractères spéciaux
            String pseudo = nom.trim()
                    .replaceAll("[^a-zA-Z0-9À-ÿ\\s]", "")
                    .replaceAll("\\s+", "_")
                    .toLowerCase();
            if (pseudo.length() >= 3 && pseudo.length() <= 50) {
                return pseudo;
            }
        }
        // Fallback : utiliser la partie avant @ de l'email
        String partieLocale = email.substring(0, email.indexOf('@'));
        return partieLocale.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
    }

    /**
     * Génère un mot de passe aléatoire sécurisé
     * 
     * @return Mot de passe de 16 caractères
     */
    private String genererMotDePasseAleatoire() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
        SecureRandom random = new SecureRandom();
        StringBuilder mdp = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            mdp.append(chars.charAt(random.nextInt(chars.length())));
        }
        return mdp.toString();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}