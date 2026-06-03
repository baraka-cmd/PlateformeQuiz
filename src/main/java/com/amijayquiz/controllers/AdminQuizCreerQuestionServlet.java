package com.amijayquiz.controllers;

import com.amijayquiz.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "AdminQuizCreerQuestionServlet", urlPatterns = {"/admin/quiz/creer/ajouter-question"})
public class AdminQuizCreerQuestionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (!"admin".equals(utilisateur.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        com.amijayquiz.models.Quiz quiz = (com.amijayquiz.models.Quiz) session.getAttribute("quizEnCreation");
        
        if (quiz == null || quiz.getTitre() == null || quiz.getTitre().isEmpty()) {
            session.setAttribute("messageErreurCreation", "Veuillez d'abord remplir les informations du quiz.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
            return;
        }

        String messageErreur = (String) session.getAttribute("messageErreurCreation");
        if (messageErreur != null) session.removeAttribute("messageErreurCreation");

        request.setAttribute("quiz", quiz);
        request.setAttribute("messageErreur", messageErreur);
        request.setAttribute("pageTitle", "Ajouter une question - " + quiz.getTitre() + " - Administration");

        request.getRequestDispatcher("/views/admin/admin-quiz-ajouter-question.jsp").forward(request, response);
    }
}