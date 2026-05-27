package com.amijayquiz.filters;

import com.amijayquiz.models.Utilisateur;
import com.amijayquiz.services.PaymentService;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filtre pour vérifier l'accès aux cours payants
 * Intercepte les requêtes vers les leçons et le plan du cours
 */
@WebFilter(urlPatterns = {"/joueur/cours/lecon", "/joueur/cours/plan"})
public class CourseAccessFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("[CourseAccessFilter] Initialisé");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        // Récupérer l'utilisateur connecté
        Utilisateur utilisateur = null;
        if (session != null && session.getAttribute("utilisateur") != null) {
            utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        }

        // Non connecté -> rediriger vers connexion
        if (utilisateur == null) {
            session.setAttribute("messageErreur", "Veuillez vous connecter pour accéder au cours");
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/connexion");
            return;
        }

        // Récupérer l'ID du cours depuis les paramètres
        String coursIdParam = httpRequest.getParameter("coursId");
        if (coursIdParam == null) {
            chain.doFilter(request, response);
            return;
        }

        int courseId;
        try {
            courseId = Integer.parseInt(coursIdParam);
        } catch (NumberFormatException e) {
            chain.doFilter(request, response);
            return;
        }

        // Vérifier si le cours est payant (prix > 0)
        double coursePrice = getCoursePrice(courseId);
        boolean isPaidCourse = coursePrice > 0;

        // Si cours gratuit ou premium -> accès direct
        if (!isPaidCourse) {
            chain.doFilter(request, response);
            return;
        }

        // Vérifier si l'utilisateur a déjà payé
        boolean hasAccess = PaymentService.canAccessCourse(utilisateur.getId(), courseId);

        if (hasAccess) {
            // Accès autorisé
            chain.doFilter(request, response);
        } else {
            // Accès refusé -> rediriger vers page d'achat
            session.setAttribute("messageErreur", 
                "Ce cours est payant. Veuillez acheter l'accès pour continuer.");
            httpResponse.sendRedirect(httpRequest.getContextPath() + 
                "/joueur/paiement/checkout?courseId=" + courseId);
        }
    }

    /**
     * Récupère le prix d'un cours
     * TODO: Adapter selon ta logique métier
     */
    private double getCoursePrice(int courseId) {
        // À remplacer par un appel à CourseService
        switch (courseId) {
            case 1: return 29.99;
            case 2: return 49.99;
            case 3: return 39.99;
            default: return 0.0;
        }
    }

    @Override
    public void destroy() {
        System.out.println("[CourseAccessFilter] Détruit");
    }
}