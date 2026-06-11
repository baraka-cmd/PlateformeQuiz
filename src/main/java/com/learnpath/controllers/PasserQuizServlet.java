package com.learnpath.controllers;

import com.learnpath.models.Question;
import com.learnpath.models.Quiz;
import com.learnpath.models.Utilisateur;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "PasserQuizServlet", urlPatterns = {"/joueur/passer-quiz"})
public class PasserQuizServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Récupération de la session
        HttpSession session = request.getSession(false);
        
        // Vérifications
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        
        if (!"joueur".equals(utilisateur.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        // Vérifier qu'un quiz est en cours
        if (session.getAttribute("quizEnCours") == null) {
            response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
            return;
        }
        
        // Vérifier si le quiz est déjà terminé
        Boolean quizTermine = (Boolean) session.getAttribute("quizTermine");
        if (quizTermine != null && quizTermine) {
            response.sendRedirect(request.getContextPath() + "/joueur/resultat");
            return;
        }
        
        // Récupération des données du quiz en cours
        Quiz quiz = (Quiz) session.getAttribute("quizEnCours");
        List<Question> listeQuestions = (List<Question>) session.getAttribute("questionsQuiz");
        Integer indexQuestionCourante = (Integer) session.getAttribute("indexQuestionCourante");
        int[] reponsesJoueur = (int[]) session.getAttribute("reponsesJoueur");
        Long tempsDebutQuiz = (Long) session.getAttribute("tempsDebutQuiz");
        
        if (listeQuestions == null || indexQuestionCourante == null) {
            response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
            return;
        }
        
        // Vérifier si le temps est écoulé
        long tempsEcoule = System.currentTimeMillis() - tempsDebutQuiz;
        long tempsMaxMs = quiz.getDureeMaxMinutes() * 60 * 1000L;
        long tempsRestant = tempsMaxMs - tempsEcoule;
        
        if (tempsRestant <= 0) {
            // Temps écoulé, redirection vers la correction
            session.setAttribute("quizTermine", true);
            session.setAttribute("tempsRealise", tempsMaxMs);
            response.sendRedirect(request.getContextPath() + "/joueur/correction");
            return;
        }
        
        // Récupération de la question courante
        Question questionCourante = listeQuestions.get(indexQuestionCourante);
        int reponseActuelle = reponsesJoueur[indexQuestionCourante];
        
        // Passage des attributs à la JSP
        request.setAttribute("quiz", quiz);
        request.setAttribute("questionCourante", questionCourante);
        request.setAttribute("indexCourant", indexQuestionCourante);
        request.setAttribute("nombreTotalQuestions", listeQuestions.size());
        request.setAttribute("reponseActuelle", reponseActuelle);
        request.setAttribute("tempsRestantMs", tempsRestant);
        request.setAttribute("tempsMaxMs", tempsMaxMs);
        request.setAttribute("estPremiereQuestion", indexQuestionCourante == 0);
        request.setAttribute("estDerniereQuestion", indexQuestionCourante == listeQuestions.size() - 1);
        
        // Affichage de la page
        request.getRequestDispatcher("/views/joueur/passer-quiz.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Récupération de la session
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("quizEnCours") == null) {
            response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
            return;
        }
        
        List<Question> listeQuestions = (List<Question>) session.getAttribute("questionsQuiz");
        Integer indexQuestionCourante = (Integer) session.getAttribute("indexQuestionCourante");
        int[] reponsesJoueur = (int[]) session.getAttribute("reponsesJoueur");
        
        // Récupération de l'action
        String action = request.getParameter("action");
        String reponseParam = request.getParameter("reponse");
        
        // Sauvegarde de la réponse actuelle
        if (reponseParam != null && !reponseParam.trim().isEmpty()) {
            int reponse = Integer.parseInt(reponseParam);
            reponsesJoueur[indexQuestionCourante] = reponse;
            session.setAttribute("reponsesJoueur", reponsesJoueur);
        }
        
        // Gestion des actions
        if ("precedent".equals(action)) {
            // Question précédente
            if (indexQuestionCourante > 0) {
                session.setAttribute("indexQuestionCourante", indexQuestionCourante - 1);
            }
            response.sendRedirect(request.getContextPath() + "/joueur/passer-quiz");
            
        } else if ("suivant".equals(action)) {
            // Question suivante
            if (indexQuestionCourante < listeQuestions.size() - 1) {
                session.setAttribute("indexQuestionCourante", indexQuestionCourante + 1);
                response.sendRedirect(request.getContextPath() + "/joueur/passer-quiz");
            }
            
        } else if ("terminer".equals(action)) {
            // Fin du quiz
            Quiz quiz = (Quiz) session.getAttribute("quizEnCours");
            Long tempsDebutQuiz = (Long) session.getAttribute("tempsDebutQuiz");
            long tempsEcoule = System.currentTimeMillis() - tempsDebutQuiz;
            long tempsMaxMs = quiz.getDureeMaxMinutes() * 60 * 1000L;
            
            long tempsRealise;
            if (tempsEcoule > tempsMaxMs) {
                tempsRealise = tempsMaxMs;
            } else {
                tempsRealise = tempsEcoule;
            }
            
            session.setAttribute("quizTermine", true);
            session.setAttribute("tempsRealise", tempsRealise);
            response.sendRedirect(request.getContextPath() + "/joueur/correction");
            
        } else {
            // Par défaut, afficher la question courante
            response.sendRedirect(request.getContextPath() + "/joueur/passer-quiz");
        }
    }
}