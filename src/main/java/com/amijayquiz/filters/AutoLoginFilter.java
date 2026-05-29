// ============================================================
// FICHIER : src/main/java/com/amijayquiz/filters/AutoLoginFilter.java
// RÔLE    : Filtre d'auto-connexion par cookie "Se souvenir de moi"
//           Vérifie la présence du cookie remember_token à chaque requête.
//           Si trouvé, reconnecte automatiquement l'utilisateur.
//           S'exécute avant chaque servlet.
// ============================================================

package com.amijayquiz.filters;

import com.amijayquiz.dao.UtilisateurDAO;
import com.amijayquiz.models.Utilisateur;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AutoLoginFilter - Filtre d'auto-connexion
 * 
 * S'exécute à chaque requête HTTP.
 * Vérifie si l'utilisateur n'est PAS déjà connecté ET si un cookie "remember_token" existe.
 * Si le cookie est valide, reconnecte automatiquement l'utilisateur.
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebFilter(urlPatterns = {"/*"}) // S'applique à toutes les URLs
public class AutoLoginFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Rien à initialiser
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, 
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession(false);

        // 1. Vérifier si l'utilisateur est déjà connecté
        boolean estDejaConnecte = (session != null && session.getAttribute("utilisateur") != null);

        // 2. Si PAS connecté, chercher le cookie "Se souvenir de moi"
        if (!estDejaConnecte) {
            String rememberToken = getCookieValue(request, "remember_token");

            if (rememberToken != null && !rememberToken.isEmpty()) {
                // 3. Chercher l'utilisateur correspondant au token
                UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
                Utilisateur utilisateur = utilisateurDAO.trouverParRememberToken(rememberToken);

                if (utilisateur != null && utilisateur.isActif()) {
                    // 4. Reconnecter automatiquement l'utilisateur
                    session = request.getSession(true);
                    session.setAttribute("utilisateur", utilisateur);
                    session.setAttribute("idUtilisateur", utilisateur.getId());
                    session.setAttribute("pseudoUtilisateur", utilisateur.getPseudo());
                    session.setAttribute("roleUtilisateur", utilisateur.getRole());

                    // 5. Rediriger vers la page demandée UNIQUEMENT si on est sur la page d'accueil
                    //    ou si on essaie d'accéder à connexion/inscription
                    String uri = request.getRequestURI();
                    String contextPath = request.getContextPath();
                    
                    if (uri.equals(contextPath + "/") || 
                        uri.endsWith("/index.jsp") ||
                        uri.equals(contextPath + "/connexion") ||
                        uri.equals(contextPath + "/inscription")) {
                        
                        // Rediriger vers le dashboard approprié
                        if ("admin".equals(utilisateur.getRole())) {
                            response.sendRedirect(contextPath + "/admin/dashboard");
                        } else {
                            response.sendRedirect(contextPath + "/joueur/dashboard");
                        }
                        return; // Important : arrêter le filtre après redirection
                    }
                } else {
                    // Token invalide ou utilisateur désactivé : supprimer le cookie
                    supprimerCookieRemember(response);
                    
                    // Si l'utilisateur existe mais est désactivé, supprimer le token de la base
                    if (utilisateur != null && !utilisateur.isActif()) {
                        utilisateurDAO.supprimerRememberToken(utilisateur.getId());
                    }
                }
            }
        }

        // 6. Continuer la chaîne de filtres
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Rien à détruire
    }

    /**
     * Récupère la valeur d'un cookie par son nom
     * 
     * @param request La requête HTTP
     * @param name    Le nom du cookie
     * @return La valeur du cookie ou null si non trouvé
     */
    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Supprime le cookie "Se souvenir de moi" en le mettant à expirer immédiatement
     * 
     * @param response La réponse HTTP
     */
    private void supprimerCookieRemember(HttpServletResponse response) {
        Cookie cookie = new Cookie("remember_token", "");
        cookie.setMaxAge(0); // Expire immédiatement
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}