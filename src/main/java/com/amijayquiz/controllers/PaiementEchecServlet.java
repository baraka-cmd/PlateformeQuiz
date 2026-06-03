package com.amijayquiz.controllers;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "PaiementEchecServlet", urlPatterns = {"/paiement-echoue"})
public class PaiementEchecServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        String reference = request.getParameter("reference");

        // Nettoyer les attributs de session
        session.removeAttribute("referencePaiement");
        session.removeAttribute("telephonePaiement");
        session.removeAttribute("montantPaiement");
        session.removeAttribute("csrf_token_paiement");

        request.setAttribute("pageTitle", "Paiement échoué - AmijayQuiz");
        request.getRequestDispatcher("/views/joueur/paiement-echoue.jsp").forward(request, response);
    }
}