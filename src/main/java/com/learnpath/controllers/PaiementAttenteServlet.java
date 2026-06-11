package com.learnpath.controllers;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "PaiementAttenteServlet", urlPatterns = {"/paiement-attente"})
public class PaiementAttenteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        String referencePaiement = (String) session.getAttribute("referencePaiement");
        if (referencePaiement == null || referencePaiement.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }

        request.setAttribute("pageTitle", "Paiement en cours - LearnPath");
        request.getRequestDispatcher("/views/joueur/paiement-attente.jsp").forward(request, response);
    }
}