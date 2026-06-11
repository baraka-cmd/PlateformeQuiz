package com.learnpath.controllers;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "PaiementSuccesServlet", urlPatterns = {"/paiement-succes"})
public class PaiementSuccesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        // Nettoyer les attributs de session liés au paiement
        session.removeAttribute("referencePaiement");
        session.removeAttribute("telephonePaiement");
        session.removeAttribute("montantPaiement");
        session.removeAttribute("csrf_token_paiement");
        session.removeAttribute("urlRedirectionApresPaiement");
        session.removeAttribute("_cachePremium");
        session.removeAttribute("_cachePremiumTimestamp");

        request.setAttribute("pageTitle", "Paiement réussi - LearnPath");
        request.getRequestDispatcher("/views/joueur/paiement-succes.jsp").forward(request, response);
    }
}