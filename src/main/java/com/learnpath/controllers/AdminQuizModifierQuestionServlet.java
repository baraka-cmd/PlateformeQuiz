// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/AdminQuizModifierQuestionServlet.java
// RÔLE    : Contrôleur pour modifier une question dans le quiz en modification
//           GET  : Affiche le formulaire pré-rempli avec la question
//           POST : Met à jour la question dans la session
// ============================================================

package com.learnpath.controllers;

import com.learnpath.models.Question;
import com.learnpath.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * AdminQuizModifierQuestionServlet - Modification d'une question
 * 
 * GET  : Affiche le formulaire pré-rempli avec la question existante
 *        ou un formulaire vide pour ajouter une nouvelle question
 * 
 * POST : Met à jour la question dans la liste en session
 *        ou ajoute une nouvelle question
 * 
 * URL : /admin/quiz/modifier-question?quizId=X&index=Y
 *       /admin/quiz/modifier-question?quizId=X&action=ajouter
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebServlet(name = "AdminQuizModifierQuestionServlet", urlPatterns = {"/admin/quiz/modifier-question"})
public class AdminQuizModifierQuestionServlet extends HttpServlet {

    // ============================================================
    // DOGET : AFFICHAGE DU FORMULAIRE
    // ============================================================
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

        // 2. Récupération des paramètres
        String quizIdStr = request.getParameter("quizId");
        String indexStr = request.getParameter("index");
        String action = request.getParameter("action");

        Integer quizId = (Integer) session.getAttribute("quizIdModification");

        if (quizId == null) {
            session.setAttribute("messageErreur", "Aucun quiz en cours de modification.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        // 3. Récupérer le quiz et les questions depuis la session
        com.learnpath.models.Quiz quiz = (com.learnpath.models.Quiz) session.getAttribute("quizEnModification");
        List<Question> questions = (List<Question>) session.getAttribute("questionsEnModification");

        if (questions == null) {
            questions = new java.util.ArrayList<>();
        }

        Question questionCourante = null;
        int indexQuestion = -1;
        boolean modeAjout = false;

        // 4. Déterminer le mode : modification ou ajout
        if ("ajouter".equals(action)) {
            // Mode ajout
            modeAjout = true;
            questionCourante = new Question();
            questionCourante.setBonneReponse(1);
        } else if (indexStr != null && !indexStr.trim().isEmpty()) {
            // Mode modification
            try {
                indexQuestion = Integer.parseInt(indexStr.trim());
                if (indexQuestion >= 0 && indexQuestion < questions.size()) {
                    questionCourante = questions.get(indexQuestion);
                } else {
                    session.setAttribute("messageErreurModification", "Question introuvable.");
                    response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
                    return;
                }
            } catch (NumberFormatException e) {
                session.setAttribute("messageErreurModification", "Index de question invalide.");
                response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
                return;
            }
        } else {
            session.setAttribute("messageErreurModification", "Parametres manquants.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
            return;
        }

        // 5. Message d'erreur
        String messageErreur = (String) session.getAttribute("messageErreurModification");
        if (messageErreur != null) session.removeAttribute("messageErreurModification");

        // 6. Passage des attributs
        String titrePage = modeAjout ? "Ajouter une question" : "Modifier la question";
        request.setAttribute("pageTitle", titrePage + " - " + (quiz != null ? quiz.getTitre() : "Quiz") + " - Administration");
        request.setAttribute("quiz", quiz);
        request.setAttribute("question", questionCourante);
        request.setAttribute("indexQuestion", indexQuestion);
        request.setAttribute("modeAjout", modeAjout);
        request.setAttribute("messageErreur", messageErreur);

        // 7. Affichage
        request.getRequestDispatcher("/views/admin/admin-quiz-modifier-question.jsp").forward(request, response);
    }

    // ============================================================
    // DOPOST : ENREGISTREMENT DE LA QUESTION
    // ============================================================
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

        // 2. Récupération des paramètres
        Integer quizId = (Integer) session.getAttribute("quizIdModification");
        String modeAjoutStr = request.getParameter("modeAjout");
        String indexStr = request.getParameter("indexQuestion");
        String texteQuestion = request.getParameter("texteQuestion");
        String proposition1 = request.getParameter("proposition1");
        String proposition2 = request.getParameter("proposition2");
        String proposition3 = request.getParameter("proposition3");
        String proposition4 = request.getParameter("proposition4");
        String bonneReponseStr = request.getParameter("bonneReponse");

        boolean modeAjout = "true".equals(modeAjoutStr);

        // 3. Validation
        if (texteQuestion == null || texteQuestion.trim().isEmpty() ||
            proposition1 == null || proposition1.trim().isEmpty() ||
            proposition2 == null || proposition2.trim().isEmpty() ||
            bonneReponseStr == null || bonneReponseStr.trim().isEmpty()) {

            session.setAttribute("messageErreurModification",
                "La question, les propositions A et B, et la bonne réponse sont obligatoires.");

            if (modeAjout) {
                response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier-question?quizId=" + quizId + "&action=ajouter");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier-question?quizId=" + quizId + "&index=" + indexStr);
            }
            return;
        }

        int bonneReponse;
        try {
            bonneReponse = Integer.parseInt(bonneReponseStr.trim());
            if (bonneReponse < 1 || bonneReponse > 4) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreurModification",
                "La bonne réponse doit être un nombre entre 1 et 4.");
            if (modeAjout) {
                response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier-question?quizId=" + quizId + "&action=ajouter");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier-question?quizId=" + quizId + "&index=" + indexStr);
            }
            return;
        }

        // 4. Récupérer la liste des questions depuis la session
        List<Question> questions = (List<Question>) session.getAttribute("questionsEnModification");
        if (questions == null) {
            questions = new java.util.ArrayList<>();
        }

        // 5. Créer ou mettre à jour la question
        Question question;
        if (modeAjout) {
            // Nouvelle question
            question = new Question(
                texteQuestion.trim(),
                proposition1.trim(),
                proposition2.trim(),
                proposition3 != null ? proposition3.trim() : "",
                proposition4 != null ? proposition4.trim() : "",
                bonneReponse
            );
            questions.add(question);
            session.setAttribute("messageSuccesModification",
                "Question ajoutee avec succes ! " + questions.size() + "/10.");
        } else {
            // Modification d'une question existante
            int index;
            try {
                index = Integer.parseInt(indexStr.trim());
                if (index < 0 || index >= questions.size()) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                session.setAttribute("messageErreurModification", "Index de question invalide.");
                response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
                return;
            }

            question = questions.get(index);
            question.setTexteQuestion(texteQuestion.trim());
            question.setProposition1(proposition1.trim());
            question.setProposition2(proposition2.trim());
            question.setProposition3(proposition3 != null ? proposition3.trim() : "");
            question.setProposition4(proposition4 != null ? proposition4.trim() : "");
            question.setBonneReponse(bonneReponse);

            session.setAttribute("messageSuccesModification",
                "Question modifiee avec succes !");
        }

        // 6. Mettre à jour la session
        session.setAttribute("questionsEnModification", questions);
        session.setAttribute("compteurQuestionsModification", questions.size());

        // 7. Redirection vers la page de modification du quiz
        response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
    }
}