// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/AdminQuizQuestionsServlet.java
// RÔLE    : Contrôleur administrateur pour la gestion des questions
//           d'un quiz spécifique
//           - Voir les questions associées à un quiz
//           - Ajouter des questions existantes au quiz
//           - Retirer une question du quiz
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.QuestionDAO;
import com.learnpath.dao.QuizDAO;
import com.learnpath.dao.QuizQuestionDAO;
import com.learnpath.models.Question;
import com.learnpath.models.Quiz;
import com.learnpath.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminQuizQuestionsServlet - Gestion des questions d'un quiz
 * 
 * GET  : Affiche les questions associées au quiz
 *        avec possibilité d'en ajouter depuis la base
 * 
 * POST : Action = ajouter → Associe des questions sélectionnées au quiz
 *        Action = retirer → Dissocie une question du quiz
 * 
 * URL : /admin/quiz/questions
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebServlet(name = "AdminQuizQuestionsServlet", urlPatterns = {"/admin/quiz/questions"})
public class AdminQuizQuestionsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Vérification authentification + rôle admin
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

        // 2. Récupération et validation de l'ID du quiz
        String quizIdStr = request.getParameter("quizId");
        if (quizIdStr == null || quizIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID du quiz manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        int quizId;
        try {
            quizId = Integer.parseInt(quizIdStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de quiz invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        // 3. Vérifier que le quiz existe
        QuizDAO quizDAO = new QuizDAO();
        Quiz quiz = quizDAO.trouverParId(quizId);

        if (quiz == null) {
            session.setAttribute("messageErreur", "Le quiz #" + quizId + " n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        // 4. Récupération des questions associées au quiz
        QuestionDAO questionDAO = new QuestionDAO();
        List<Question> questionsDuQuiz = questionDAO.listerQuestionsParQuiz(quizId);

        // 5. Récupération de toutes les questions disponibles (pour l'ajout)
        List<Question> toutesLesQuestions = questionDAO.listerToutes();

        // 6. Filtrer : ne garder que les questions qui ne sont PAS déjà dans le quiz
        List<Integer> idsQuestionsDuQuiz = questionsDuQuiz.stream()
            .map(Question::getId)
            .collect(Collectors.toList());

        List<Question> questionsDisponibles = toutesLesQuestions.stream()
            .filter(q -> !idsQuestionsDuQuiz.contains(q.getId()))
            .collect(Collectors.toList());

        // 7. Récupération du paramètre de recherche pour les questions disponibles
        String recherche = request.getParameter("recherche");
        if (recherche != null && !recherche.trim().isEmpty()) {
            String filtre = recherche.toLowerCase().trim();
            questionsDisponibles = questionsDisponibles.stream()
                .filter(q -> q.getTexteQuestion() != null && 
                       q.getTexteQuestion().toLowerCase().contains(filtre))
                .collect(Collectors.toList());
        }

        // 8. Message de succès/erreur
        String messageSucces = (String) session.getAttribute("messageSucces");
        String messageErreur = (String) session.getAttribute("messageErreur");
        if (messageSucces != null) session.removeAttribute("messageSucces");
        if (messageErreur != null) session.removeAttribute("messageErreur");

        // 9. Passage des attributs
        request.setAttribute("pageTitle", "Questions du quiz - " + quiz.getTitre() + " - Administration");
        request.setAttribute("quiz", quiz);
        request.setAttribute("questionsDuQuiz", questionsDuQuiz);
        request.setAttribute("questionsDisponibles", questionsDisponibles);
        request.setAttribute("totalQuestionsQuiz", questionsDuQuiz.size());
        request.setAttribute("totalDisponibles", questionsDisponibles.size());
        request.setAttribute("recherche", recherche);
        request.setAttribute("messageSucces", messageSucces);
        request.setAttribute("messageErreur", messageErreur);

        // 10. Affichage
        request.getRequestDispatcher("/views/admin/admin-quiz-questions.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // 1. Vérification authentification + rôle admin
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

        // 2. Récupération de l'action
        String action = request.getParameter("action");
        String quizIdStr = request.getParameter("quizId");

        if (quizIdStr == null || quizIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID du quiz manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        int quizId;
        try {
            quizId = Integer.parseInt(quizIdStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de quiz invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        // 3. Traitement selon l'action
        if ("ajouter".equals(action)) {
            traiterAjoutQuestions(request, response, session, quizId);
        } else if ("retirer".equals(action)) {
            traiterRetraitQuestion(request, response, session, quizId);
        } else {
            session.setAttribute("messageErreur", "Action invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/questions?quizId=" + quizId);
        }
    }

    /**
     * Ajoute des questions sélectionnées au quiz
     */
    private void traiterAjoutQuestions(HttpServletRequest request, HttpServletResponse response,
                                        HttpSession session, int quizId) throws IOException {

        String[] idsQuestions = request.getParameterValues("idsQuestions");

        if (idsQuestions == null || idsQuestions.length == 0) {
            session.setAttribute("messageErreur", "Aucune question sélectionnée.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/questions?quizId=" + quizId);
            return;
        }

        QuizQuestionDAO quizQuestionDAO = new QuizQuestionDAO();
        int ajoutees = 0;
        int dejaPresentes = 0;

        for (String idStr : idsQuestions) {
            try {
                int questionId = Integer.parseInt(idStr.trim());

                // Vérifier si l'association existe déjà
                if (quizQuestionDAO.associationExiste(quizId, questionId)) {
                    dejaPresentes++;
                    continue;
                }

                // Créer l'association
                boolean associe = quizQuestionDAO.associerQuestionAuQuiz(quizId, questionId);
                if (associe) {
                    ajoutees++;
                }

            } catch (NumberFormatException e) {
                // Ignorer les IDs invalides
            }
        }

        if (ajoutees > 0) {
            session.setAttribute("messageSucces",
                ajoutees + " question(s) ajoutée(s) au quiz avec succès !" +
                (dejaPresentes > 0 ? " " + dejaPresentes + " déjà présente(s)." : ""));
        } else if (dejaPresentes > 0) {
            session.setAttribute("messageErreur",
                "Toutes les questions sélectionnées sont déjà dans ce quiz.");
        } else {
            session.setAttribute("messageErreur", "Aucune question n'a pu être ajoutée.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/quiz/questions?quizId=" + quizId);
    }

    /**
     * Retire une question du quiz
     */
    private void traiterRetraitQuestion(HttpServletRequest request, HttpServletResponse response,
                                         HttpSession session, int quizId) throws IOException {

        String questionIdStr = request.getParameter("questionId");

        if (questionIdStr == null || questionIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID de la question manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/questions?quizId=" + quizId);
            return;
        }

        int questionId;
        try {
            questionId = Integer.parseInt(questionIdStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de question invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/questions?quizId=" + quizId);
            return;
        }

        QuizQuestionDAO quizQuestionDAO = new QuizQuestionDAO();
        boolean retire = quizQuestionDAO.retirerQuestionDuQuiz(quizId, questionId);

        if (retire) {
            session.setAttribute("messageSucces", "Question retirée du quiz avec succès !");
        } else {
            session.setAttribute("messageErreur", "Erreur lors du retrait de la question.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/quiz/questions?quizId=" + quizId);
    }
}