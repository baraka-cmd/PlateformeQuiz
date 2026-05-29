package com.amijayquiz.controllers;

import com.amijayquiz.dao.CoursDAO;
import com.amijayquiz.models.Cours;
import com.amijayquiz.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/joueur/paiement/checkout")
public class CheckoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        String courseIdParam = request.getParameter("courseId");
        if (courseIdParam == null || courseIdParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        int courseId;
        try {
            courseId = Integer.parseInt(courseIdParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        CoursDAO coursDAO = new CoursDAO();
        Cours cours = coursDAO.trouverParId(courseId);

        if (cours == null) {
            session.setAttribute("messageErreur", "Cours non trouvé");
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        request.setAttribute("cours", cours);
        request.getRequestDispatcher("/views/joueur/paiement/checkout.jsp").forward(request, response);
    }
}