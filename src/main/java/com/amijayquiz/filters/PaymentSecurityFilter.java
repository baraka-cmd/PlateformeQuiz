package com.amijayquiz.filters;

import com.amijayquiz.utils.HttpUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Filtre de sécurité pour les endpoints de paiement
 * - Vérification des IP PawaPay
 * - Rate limiting
 * - Validation des headers
 * @author BARAKA
 */
@WebFilter(urlPatterns = {"/api/paiements/*", "/joueur/paiement/*"})
public class PaymentSecurityFilter implements Filter {

    // IPs autorisées de PawaPay (production et sandbox)
    private static final Set<String> ALLOWED_IPS = new HashSet<>(Arrays.asList(
        "3.120.0.0/16",      // Plage IP PawaPay
        "18.184.0.0/16",     // Plage IP PawaPay
        "52.28.0.0/16",      // Plage IP PawaPay
        "127.0.0.1",         // Localhost pour test
        "::1"                // Localhost IPv6
    ));
    
    // Rate limiting: map (ip + endpoint) -> compteur
    private static final ConcurrentHashMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final int RATE_LIMIT_WINDOW_MS = 60000; // 1 minute
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("[PaymentSecurityFilter] Initialisé");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIp = HttpUtils.getClientIp(httpRequest);
        String requestUri = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        
        // 1. Vérification des IP pour le webhook (endpoint sensible)
        if (requestUri.contains("/api/paiements/webhook")) {
            if (!isIpAllowed(clientIp)) {
                System.err.println("[PaymentSecurityFilter] IP non autorisée: " + clientIp);
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.getWriter().write("{\"error\":\"IP non autorisée\"}");
                return;
            }
        }
        
        // 2. Rate limiting pour tous les endpoints de paiement
        if (!checkRateLimit(clientIp, requestUri)) {
            System.err.println("[PaymentSecurityFilter] Rate limit dépassé: " + clientIp);
            httpResponse.setStatus(429);
            httpResponse.getWriter().write("{\"error\":\"Trop de requêtes. Veuillez réessayer plus tard.\"}");
            return;
        }
        
        // 3. Vérification des headers pour les requêtes POST
        if ("POST".equalsIgnoreCase(method)) {
            String contentType = httpRequest.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpResponse.getWriter().write("{\"error\":\"Content-Type doit être application/json\"}");
                return;
            }
        }
        
        // 4. Logging des requêtes
        logRequest(httpRequest, clientIp);
        
        // Continuer le filtre
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        System.out.println("[PaymentSecurityFilter] Détruit");
        rateLimitMap.clear();
    }
    
    /**
     * Vérifie si l'IP est autorisée
     */
    private boolean isIpAllowed(String ip) {
        if (ip == null) return false;
        
        // Vérification exacte
        if (ALLOWED_IPS.contains(ip)) {
            return true;
        }
        
        // Vérification des plages CIDR (simplifiée)
        for (String allowedIp : ALLOWED_IPS) {
            if (allowedIp.contains("/")) {
                if (isIpInCidrRange(ip, allowedIp)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Vérifie si une IP appartient à une plage CIDR (simplifié)
     */
    private boolean isIpInCidrRange(String ip, String cidr) {
        // Implémentation simplifiée
        // En production, utiliser une librairie comme IPAddress ou Commons Net
        return ip.startsWith(cidr.split("/")[0].substring(0, 10));
    }
    
    /**
     * Vérifie le rate limiting
     */
    private synchronized boolean checkRateLimit(String clientIp, String endpoint) {
        String key = clientIp + ":" + endpoint;
        RateLimitInfo info = rateLimitMap.get(key);
        long now = System.currentTimeMillis();
        
        if (info == null) {
            info = new RateLimitInfo();
            info.count = 1;
            info.firstRequestTime = now;
            rateLimitMap.put(key, info);
            return true;
        }
        
        // Réinitialiser si la fenêtre est expirée
        if (now - info.firstRequestTime > RATE_LIMIT_WINDOW_MS) {
            info.count = 1;
            info.firstRequestTime = now;
            rateLimitMap.put(key, info);
            return true;
        }
        
        // Vérifier le compteur
        if (info.count >= MAX_REQUESTS_PER_MINUTE) {
            return false;
        }
        
        info.count++;
        rateLimitMap.put(key, info);
        return true;
    }
    
    /**
     * Journalise la requête
     */
    private void logRequest(HttpServletRequest request, String clientIp) {
        StringBuilder log = new StringBuilder();
        log.append("[PaymentSecurityFilter] ");
        log.append(request.getMethod()).append(" ");
        log.append(request.getRequestURI());
        log.append(" | IP: ").append(clientIp);
        log.append(" | User-Agent: ").append(request.getHeader("User-Agent"));
        
        System.out.println(log.toString());
    }
    
    /**
     * Classe interne pour les informations de rate limiting
     */
    private static class RateLimitInfo {
        int count;
        long firstRequestTime;
    }
}