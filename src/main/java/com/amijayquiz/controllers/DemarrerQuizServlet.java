package com.amijayquiz.controllers;

import com.amijayquiz.dao.QuestionDAO;
import com.amijayquiz.dao.QuizDAO;
import com.amijayquiz.models.Question;
import com.amijayquiz.models.Quiz;
import com.amijayquiz.models.Utilisateur;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "DemarrerQuizServlet", urlPatterns = {"/joueur/demarrer-quiz"})
public class DemarrerQuizServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Récupération de la session
        HttpSession session = request.getSession(false);
        
        // Vérification que l'utilisateur est connecté et est un joueur
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        
        if (!"joueur".equals(utilisateur.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        // Récupération de l'ID du quiz
        String idQuizParam = request.getParameter("id");
        
        if (idQuizParam == null || idQuizParam.trim().isEmpty()) {
            session.setAttribute("messageErreur", "Aucun quiz sélectionné !");
            response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
            return;
        }
        
        int idQuiz;
        try {
            idQuiz = Integer.parseInt(idQuizParam);
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "Quiz invalide !");
            response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
            return;
        }
        
        // Récupération des informations du quiz
        QuizDAO quizDAO = new QuizDAO();
        Quiz quiz = quizDAO.trouverParId(idQuiz);
        
        if (quiz == null) {
            session.setAttribute("messageErreur", "Ce quiz n'existe pas !");
            response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
            return;
        }
        
        // Récupération des questions du quiz
        QuestionDAO questionDAO = new QuestionDAO();
        List<Question> listeQuestions = questionDAO.listerQuestionsParQuiz(idQuiz);
        
        if (listeQuestions == null || listeQuestions.isEmpty()) {
            session.setAttribute("messageErreur", "Ce quiz ne contient aucune question !");
            response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
            return;
        }
        
        // Passage des attributs à la JSP
        request.setAttribute("quiz", quiz);
        request.setAttribute("nombreQuestions", listeQuestions.size());
        
        // Redirection vers la page de présentation du quiz
        request.getRequestDispatcher("/views/joueur/demarrer-quiz.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Récupération de la session
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        // Récupération de l'ID du quiz
        String idQuizParam = request.getParameter("idQuiz");
        
        if (idQuizParam == null || idQuizParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
            return;
        }
        
        int idQuiz = Integer.parseInt(idQuizParam);
        
        // Récupération des questions
        QuestionDAO questionDAO = new QuestionDAO();
        List<Question> listeQuestions = questionDAO.listerQuestionsParQuiz(idQuiz);
        
        // Récupération du quiz pour la durée
        QuizDAO quizDAO = new QuizDAO();
        Quiz quiz = quizDAO.trouverParId(idQuiz);
        
        // Initialisation des variables de session pour le quiz
        session.setAttribute("quizEnCours", quiz);
        session.setAttribute("questionsQuiz", listeQuestions);
        session.setAttribute("indexQuestionCourante", 0);
        session.setAttribute("reponsesJoueur", new int[listeQuestions.size()]);
        session.setAttribute("tempsDebutQuiz", System.currentTimeMillis());
        session.setAttribute("quizTermine", false);
        
        // Redirection vers la page pour passer le quiz
        response.sendRedirect(request.getContextPath() + "/joueur/passer-quiz");
    }
}