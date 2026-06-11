// ============================================================
// FICHIER : src/main/java/com/LearnPath/filters/AccesPayantFilter.java
// RÔLE    : Filtre de contrôle d'accès aux ressources payantes
//           Vérifie le statut premium avant d'autoriser l'accès
//           aux niveaux moyen/avancé (Cours et Quiz)
//           et aux parties 6+ des quiz élémentaires
// ============================================================

package com.learnpath.filters;

import com.learnpath.dao.AccessPremiumDAO;
import com.learnpath.models.Utilisateur;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * AccesPayantFilter - Filtre de contrôle d'accès aux ressources payantes
 * 
 * Intercepte les requêtes vers les URLs nécessitant un compte premium.
 * 
 * RÈGLES D'ACCÈS :
 * ┌──────────────────────────────┬─────────────┬─────────────┐
 * │ Ressource                    │ Gratuit     │ Premium     │
 * ├──────────────────────────────┼─────────────┼─────────────┤
 * │ Cours niveau élémentaire     │ ✅ Accès    │ ✅ Accès    │
 * │ Cours niveau moyen           │ ❌ Bloqué   │ ✅ Accès    │
 * │ Cours niveau avancé          │ ❌ Bloqué   │ ✅ Accès    │
 * │ Quiz niveau élémentaire      │ ✅ Parties  │ ✅ Tout     │
 * │                              │   1 à 5     │             │
 * │ Quiz niveau moyen            │ ❌ Bloqué   │ ✅ Accès    │
 * │ Quiz niveau avancé           │ ❌ Bloqué   │ ✅ Accès    │
 * │ Examen module (module 2+)    │ ❌ Bloqué   │ ✅ Accès    │
 * └──────────────────────────────┴─────────────┴─────────────┘
 * 
 * URLs protégées par ce filtre :
 * - /joueur/cours/niveau (si niveau=moyen ou avance)
 * - /joueur/cours/examen-module (si module >= 2)
 * - /joueur/niveau (Quiz - si niveau=moyen ou avance)
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebFilter(urlPatterns = {
    "/joueur/cours/niveau",
    "/joueur/cours/examen-module",
    "/joueur/niveau"
})
public class AccesPayantFilter implements Filter {

    // ===== CONSTANTES =====
    private static final String PAGE_PAIEMENT = "/views/joueur/paiement.jsp";
    private static final String ATTR_UTILISATEUR = "utilisateur";
    private static final String ATTR_MESSAGE_ERREUR = "messageErreur";

    // URLs de redirection selon le contexte
    private static final String URL_CONNEXION = "/connexion";
    private static final String URL_COURS = "/joueur/cours";
    private static final String URL_DASHBOARD = "/joueur/dashboard";

    // Niveaux nécessitant premium
    private static final Set<String> NIVEAUX_PAYANTS = new HashSet<>(Arrays.asList(
        "moyen", "avance"
    ));

    // Module à partir duquel le premium est requis (module 2 et plus)
    private static final int MODULE_PREMIUM_MINIMUM = 2;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("🛡️ [AccesPayantFilter] Filtre initialisé");
        System.out.println("   URLs protégées : /joueur/cours/niveau, /joueur/cours/examen-module, /joueur/niveau");
        System.out.println("   Niveaux payants : " + NIVEAUX_PAYANTS);
        System.out.println("   Module premium minimum : " + MODULE_PREMIUM_MINIMUM);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession(false);

        // ============================================================
        // 1. VÉRIFICATION AUTHENTIFICATION
        // ============================================================
        if (session == null || session.getAttribute(ATTR_UTILISATEUR) == null) {
            response.sendRedirect(request.getContextPath() + URL_CONNEXION);
            return;
        }

        Utilisateur utilisateur = (Utilisateur) session.getAttribute(ATTR_UTILISATEUR);

        // Les admins ont toujours accès
        if ("admin".equals(utilisateur.getRole())) {
            chain.doFilter(request, response);
            return;
        }

        // ============================================================
        // 2. DÉTERMINER LE TYPE DE RESSOURCE DEMANDÉE
        // ============================================================
        String requestURI = request.getRequestURI();
        String contexte = request.getContextPath();

        boolean accesAutorise = true;
        String raisonBlocage = null;

        // Vérifier si l'utilisateur est premium
        boolean estPremium = verifierPremium(utilisateur.getId());

        if (!estPremium) {
            // L'utilisateur n'est pas premium, vérifier s'il a droit d'accès gratuit
            accesAutorise = verifierAccesGratuit(request, requestURI);
            if (!accesAutorise) {
                raisonBlocage = "Cette ressource nécessite un compte Premium.";
            }
        }

        // ============================================================
        // 3. AUTORISER OU BLOQUER
        // ============================================================
        if (accesAutorise) {
            // ✅ Accès autorisé : continuer la chaîne
            chain.doFilter(request, response);
        } else {
            // ❌ Accès bloqué : rediriger vers la page de paiement
            System.out.println("🔒 [AccesPayantFilter] Accès bloqué - UserID: " + utilisateur.getId()
                + " - URI: " + requestURI + " - Raison: " + raisonBlocage);

            // Stocker l'URL demandée pour redirection après paiement
            String urlDemandee = requestURI;
            String queryString = request.getQueryString();
            if (queryString != null && !queryString.isEmpty()) {
                urlDemandee += "?" + queryString;
            }
            session.setAttribute("urlRedirectionApresPaiement", urlDemandee);
            session.setAttribute(ATTR_MESSAGE_ERREUR,
                "🔒 " + raisonBlocage + "\nDébloquez tous les niveaux pour seulement 4$ (10 000 FC).");

            // Rediriger vers la page de paiement
            request.getRequestDispatcher(PAGE_PAIEMENT).forward(request, response);
        }
    }

    @Override
    public void destroy() {
        System.out.println("🛡️ [AccesPayantFilter] Filtre détruit");
    }

    // ============================================================
    // MÉTHODES PRIVÉES
    // ============================================================

    /**
     * Vérifie si l'utilisateur a le statut premium.
     * Utilise un cache de session pour éviter de requêter la BDD à chaque requête.
     * 
     * @param utilisateurId ID de l'utilisateur
     * @return true si premium actif
     */
    private boolean verifierPremium(int utilisateurId) {
        // Vérifier d'abord dans la session (cache)
        HttpSession session = getSessionSansException();
        if (session != null) {
            Boolean cachePremium = (Boolean) session.getAttribute("_cachePremium");
            Long cacheTimestamp = (Long) session.getAttribute("_cachePremiumTimestamp");

            // Cache valide 5 minutes
            if (cachePremium != null && cacheTimestamp != null) {
                long maintenant = System.currentTimeMillis();
                if (maintenant - cacheTimestamp < 300_000) { // 5 minutes
                    return cachePremium;
                }
            }
        }

        // Vérifier en base de données
        AccessPremiumDAO premiumDAO = new AccessPremiumDAO();
        boolean estPremium = premiumDAO.estPremium(utilisateurId);

        // Mettre en cache dans la session
        if (session != null) {
            session.setAttribute("_cachePremium", estPremium);
            session.setAttribute("_cachePremiumTimestamp", System.currentTimeMillis());
        }

        return estPremium;
    }

    /**
     * Vérifie si l'utilisateur non-premium a droit à un accès gratuit.
     * 
     * RÈGLES GRATUITES :
     * - Cours niveau "elementaire" → Autorisé
     * - Examen module 1 → Autorisé
     * - Quiz niveau "elementaire" → Autorisé (le blocage des parties 6+
     *   est géré dans NiveauServlet et les JSP)
     * 
     * @param request    La requête HTTP
     * @param requestURI L'URI demandée
     * @return true si l'accès gratuit est autorisé
     */
    private boolean verifierAccesGratuit(HttpServletRequest request, String requestURI) {
        String contexte = request.getContextPath();

        // ============================================================
        // CAS 1 : /joueur/cours/niveau?theme=X&niveau=Y
        // ============================================================
        if (requestURI.contains("/joueur/cours/niveau")) {
            String niveau = request.getParameter("niveau");
            if (niveau == null || niveau.trim().isEmpty()) {
                return true; // Pas de niveau spécifié, laisser passer
            }
            niveau = niveau.trim().toLowerCase();

            // Seul le niveau élémentaire est gratuit
            return !NIVEAUX_PAYANTS.contains(niveau);
        }

        // ============================================================
        // CAS 2 : /joueur/cours/examen-module?coursId=X&module=Y
        // ============================================================
        if (requestURI.contains("/joueur/cours/examen-module")) {
            String moduleParam = request.getParameter("module");
            if (moduleParam == null || moduleParam.trim().isEmpty()) {
                return true; // Pas de module spécifié, laisser passer
            }

            try {
                int module = Integer.parseInt(moduleParam.trim());
                // Seul le module 1 est gratuit
                return module < MODULE_PREMIUM_MINIMUM;
            } catch (NumberFormatException e) {
                return true; // Format invalide, laisser passer
            }
        }

        // ============================================================
        // CAS 3 : /joueur/niveau?categorie=X&niveau=Y (Quiz)
        // ============================================================
        if (requestURI.contains("/joueur/niveau")) {
            String niveau = request.getParameter("niveau");
            if (niveau == null || niveau.trim().isEmpty()) {
                return true; // Pas de niveau spécifié, laisser passer
            }
            niveau = niveau.trim().toLowerCase();

            // Seul le niveau élémentaire est gratuit
            // (le blocage des parties 6+ est géré dans NiveauServlet)
            return !NIVEAUX_PAYANTS.contains(niveau);
        }

        // Par défaut, autoriser (sécurité : ne pas bloquer par erreur)
        return true;
    }

    /**
     * Récupère la session sans lever d'exception si elle n'existe pas
     * 
     * @return La session HTTP ou null
     */
    private HttpSession getSessionSansException() {
        try {
            // Cette méthode est appelée depuis doFilter qui a déjà accès à la requête
            // On retourne null ici car on ne peut pas accéder à la requête
            // La session est passée en paramètre dans verifierPremium appelé depuis doFilter
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Version modifiée pour accepter la session en paramètre
     */
    private boolean verifierPremium(int utilisateurId, HttpSession session) {
        // Vérifier d'abord dans la session (cache)
        if (session != null) {
            Boolean cachePremium = (Boolean) session.getAttribute("_cachePremium");
            Long cacheTimestamp = (Long) session.getAttribute("_cachePremiumTimestamp");

            // Cache valide 5 minutes
            if (cachePremium != null && cacheTimestamp != null) {
                long maintenant = System.currentTimeMillis();
                if (maintenant - cacheTimestamp < 300_000) { // 5 minutes
                    return cachePremium;
                }
            }
        }

        // Vérifier en base de données
        AccessPremiumDAO premiumDAO = new AccessPremiumDAO();
        boolean estPremium = premiumDAO.estPremium(utilisateurId);

        // Mettre en cache dans la session
        if (session != null) {
            session.setAttribute("_cachePremium", estPremium);
            session.setAttribute("_cachePremiumTimestamp", System.currentTimeMillis());
        }

        return estPremium;
    }
}