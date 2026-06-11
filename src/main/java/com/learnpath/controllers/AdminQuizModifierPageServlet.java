// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/AdminQuizModifierPageServlet.java
// RÔLE    : Contrôleur de la page de modification d'un quiz
//           GET  : Charge le quiz existant et ses questions
//           POST : Traite les actions de modification
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.QuestionDAO;
import com.learnpath.dao.QuizDAO;
import com.learnpath.dao.QuizQuestionDAO;
import com.learnpath.models.Quiz;
import com.learnpath.models.Question;
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

/**
 * AdminQuizModifierPageServlet - Page de modification d'un quiz existant
 * 
 * GET  : Charge le quiz et ses questions depuis la base
 *        et les stocke en session pour modification
 * 
 * POST : action=enregistrerInfo       → Met à jour les infos du quiz en session
 *        action=ajouterQuestion       → Ajoute une question à la liste temporaire
 *        action=supprimerQuestion     → Supprime une question de la liste
 *        action=enregistrerModifications → Enregistre tout en base
 *        action=reinitialiser         → Recharge depuis la base
 * 
 * URL : /admin/quiz/modifier
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebServlet(name = "AdminQuizModifierPageServlet", urlPatterns = {"/admin/quiz/modifier"})
public class AdminQuizModifierPageServlet extends HttpServlet {

    // Constantes
    private static final int NB_QUESTIONS_REQUISES = 10;

    // ============================================================
    // DOGET : CHARGEMENT DU QUIZ ET AFFICHAGE
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

    // 3. Vérifier si des données modifiées existent en session
    Quiz quizEnSession = (Quiz) session.getAttribute("quizEnModification");
    Integer quizIdSession = (Integer) session.getAttribute("quizIdModification");
    List<Question> questionsEnSession = (List<Question>) session.getAttribute("questionsEnModification");

    Quiz quiz;
    List<Question> questions;

    // 🔑 Si les données en session correspondent au quiz demandé, les utiliser
    if (quizEnSession != null && quizIdSession != null && quizIdSession == quizId && questionsEnSession != null) {
        quiz = quizEnSession;
        questions = questionsEnSession;
    } else {
        // Sinon, charger depuis la base
        QuizDAO quizDAO = new QuizDAO();
        quiz = quizDAO.trouverParId(quizId);

        if (quiz == null) {
            session.setAttribute("messageErreur", "Le quiz #" + quizId + " n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        QuestionDAO questionDAO = new QuestionDAO();
        questions = questionDAO.listerQuestionsParQuiz(quizId);

        if (questions == null) {
            questions = new ArrayList<>();
        }

        // Stocker en session pour modification
        session.setAttribute("quizEnModification", quiz);
        session.setAttribute("questionsEnModification", questions);
        session.setAttribute("quizIdModification", quizId);
    }

    int compteur = questions.size();

    // 4. Message de succès/erreur
    String messageSucces = (String) session.getAttribute("messageSuccesModification");
    String messageErreur = (String) session.getAttribute("messageErreurModification");
    if (messageSucces != null) session.removeAttribute("messageSuccesModification");
    if (messageErreur != null) session.removeAttribute("messageErreurModification");

    // 5. Passage des attributs
    request.setAttribute("pageTitle", "Modifier le quiz - " + quiz.getTitre() + " - Administration");
    request.setAttribute("quiz", quiz);
    request.setAttribute("questions", questions);
    request.setAttribute("compteur", compteur);
    request.setAttribute("nbRequis", NB_QUESTIONS_REQUISES);
    request.setAttribute("quizComplet", compteur >= NB_QUESTIONS_REQUISES);
    request.setAttribute("messageSucces", messageSucces);
    request.setAttribute("messageErreur", messageErreur);

    // 6. Affichage
    request.getRequestDispatcher("/views/admin/admin-quiz-modifier.jsp").forward(request, response);
}

    // ============================================================
    // DOPOST : TRAITEMENT DES ACTIONS DE MODIFICATION
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

        // 2. Récupération de l'action
        String action = request.getParameter("action");
        Integer quizId = (Integer) session.getAttribute("quizIdModification");

        if (quizId == null) {
            session.setAttribute("messageErreur", "Aucun quiz en cours de modification.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        // 3. Traitement selon l'action
        if ("enregistrerInfo".equals(action)) {
            traiterEnregistrementInfo(request, response, session);
        } else if ("ajouterQuestion".equals(action)) {
            traiterAjoutQuestion(request, response, session);
        } else if ("supprimerQuestion".equals(action)) {
            traiterSuppressionQuestion(request, response, session);
        } else if ("enregistrerModifications".equals(action)) {
            traiterEnregistrementFinal(request, response, session);
        } else if ("reinitialiser".equals(action)) {
            traiterReinitialisation(request, response, session);
        } else {
            session.setAttribute("messageErreurModification", "Action invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
        }
    }

    // ============================================================
    // TRAITEMENT : ENREGISTRER LES INFOS DU QUIZ
    // ============================================================
    private void traiterEnregistrementInfo(HttpServletRequest request, HttpServletResponse response,
                                            HttpSession session) throws IOException {

        Integer quizId = (Integer) session.getAttribute("quizIdModification");
        Quiz quiz = (Quiz) session.getAttribute("quizEnModification");

        if (quiz == null) {
            session.setAttribute("messageErreurModification", "Aucun quiz en cours de modification.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        String theme = request.getParameter("theme");
        String niveau = request.getParameter("niveau");
        String ordrePartieStr = request.getParameter("ordrePartie");
        String description = request.getParameter("description");
        String dureeStr = request.getParameter("dureeMaxMinutes");

        // Validation
        if (theme == null || theme.trim().isEmpty()) {
            session.setAttribute("messageErreurModification", "Le thème est obligatoire.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
            return;
        }

        if (niveau == null || niveau.trim().isEmpty()) {
            session.setAttribute("messageErreurModification", "Le niveau est obligatoire.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
            return;
        }

        int ordrePartie = quiz.getOrdrePartie();
        try {
            if (ordrePartieStr != null && !ordrePartieStr.trim().isEmpty()) {
                ordrePartie = Integer.parseInt(ordrePartieStr.trim());
                if (ordrePartie < 1) ordrePartie = 1;
                if (ordrePartie > 20) ordrePartie = 20;
            }
        } catch (NumberFormatException e) {
            // Garder la valeur actuelle
        }

        int dureeMaxMinutes = quiz.getDureeMaxMinutes();
        try {
            if (dureeStr != null && !dureeStr.trim().isEmpty()) {
                dureeMaxMinutes = Integer.parseInt(dureeStr.trim());
                if (dureeMaxMinutes < 1) dureeMaxMinutes = 1;
                if (dureeMaxMinutes > 120) dureeMaxMinutes = 120;
            }
        } catch (NumberFormatException e) {
            // Garder la valeur actuelle
        }

        // Mettre à jour le quiz en session
        quiz.setTitre(theme.trim() + " - Partie " + ordrePartie);
        quiz.setNiveau(niveau.trim().toLowerCase());
        quiz.setOrdrePartie(ordrePartie);
        quiz.setDureeMaxMinutes(dureeMaxMinutes);
        quiz.setDescription(description != null ? description.trim() : "");

        session.setAttribute("quizEnModification", quiz);
        session.setAttribute("messageSuccesModification", "Informations du quiz mises à jour.");

        response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
    }

    // ============================================================
    // TRAITEMENT : AJOUTER UNE QUESTION
    // ============================================================
    private void traiterAjoutQuestion(HttpServletRequest request, HttpServletResponse response,
                                       HttpSession session) throws IOException {

        Integer quizId = (Integer) session.getAttribute("quizIdModification");
        List<Question> questions = (List<Question>) session.getAttribute("questionsEnModification");
        if (questions == null) questions = new ArrayList<>();

        Integer compteur = (Integer) session.getAttribute("compteurQuestionsModification");
        if (compteur == null) compteur = questions.size();

        // Vérifier qu'on n'a pas déjà 10 questions
        if (compteur >= NB_QUESTIONS_REQUISES) {
            session.setAttribute("messageErreurModification",
                "Vous avez déjà " + NB_QUESTIONS_REQUISES + " questions. Supprimez-en une avant d'en ajouter.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
            return;
        }

        // Récupération des champs
        String texteQuestion = request.getParameter("texteQuestion");
        String proposition1 = request.getParameter("proposition1");
        String proposition2 = request.getParameter("proposition2");
        String proposition3 = request.getParameter("proposition3");
        String proposition4 = request.getParameter("proposition4");
        String bonneReponseStr = request.getParameter("bonneReponse");

        // Validation
        if (texteQuestion == null || texteQuestion.trim().isEmpty() ||
            proposition1 == null || proposition1.trim().isEmpty() ||
            proposition2 == null || proposition2.trim().isEmpty() ||
            bonneReponseStr == null || bonneReponseStr.trim().isEmpty()) {

            session.setAttribute("messageErreurModification",
                "La question, les propositions A et B, et la bonne réponse sont obligatoires.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
            return;
        }

        int bonneReponse;
        try {
            bonneReponse = Integer.parseInt(bonneReponseStr.trim());
            if (bonneReponse < 1 || bonneReponse > 4) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreurModification", "La bonne réponse doit être un nombre entre 1 et 4.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
            return;
        }

        // Créer la question
        Question question = new Question(
            texteQuestion.trim(),
            proposition1.trim(),
            proposition2.trim(),
            proposition3 != null ? proposition3.trim() : "",
            proposition4 != null ? proposition4.trim() : "",
            bonneReponse
        );

        // Ajouter à la liste
        questions.add(question);
        compteur = questions.size();

        session.setAttribute("questionsEnModification", questions);
        session.setAttribute("compteurQuestionsModification", compteur);
        session.setAttribute("messageSuccesModification", "Question ajoutée. " + compteur + "/" + NB_QUESTIONS_REQUISES + ".");

        response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
    }

    // ============================================================
    // TRAITEMENT : SUPPRIMER UNE QUESTION
    // ============================================================
    private void traiterSuppressionQuestion(HttpServletRequest request, HttpServletResponse response,
                                             HttpSession session) throws IOException {

        Integer quizId = (Integer) session.getAttribute("quizIdModification");
        String indexStr = request.getParameter("index");

        if (indexStr == null || indexStr.trim().isEmpty()) {
            session.setAttribute("messageErreurModification", "Index de question manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
            return;
        }

        int index;
        try {
            index = Integer.parseInt(indexStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreurModification", "Index de question invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
            return;
        }

        List<Question> questions = (List<Question>) session.getAttribute("questionsEnModification");
        if (questions == null || index < 0 || index >= questions.size()) {
            session.setAttribute("messageErreurModification", "Question introuvable.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
            return;
        }

        questions.remove(index);
        int compteur = questions.size();

        session.setAttribute("questionsEnModification", questions);
        session.setAttribute("compteurQuestionsModification", compteur);
        session.setAttribute("messageSuccesModification", "Question supprimée. " + compteur + "/" + NB_QUESTIONS_REQUISES + ".");

        response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
    }

    // ============================================================
    // TRAITEMENT : ENREGISTREMENT FINAL
    // ============================================================
    private void traiterEnregistrementFinal(HttpServletRequest request, HttpServletResponse response,
                                             HttpSession session) throws IOException {

        Integer quizId = (Integer) session.getAttribute("quizIdModification");
        Quiz quiz = (Quiz) session.getAttribute("quizEnModification");
        List<Question> questions = (List<Question>) session.getAttribute("questionsEnModification");

        // Vérifications
        if (quiz == null || quiz.getTitre() == null || quiz.getTitre().trim().isEmpty()) {
            session.setAttribute("messageErreurModification", "Les informations du quiz sont incomplètes.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
            return;
        }

        if (questions == null || questions.size() < NB_QUESTIONS_REQUISES) {
            session.setAttribute("messageErreurModification",
                "Vous devez avoir " + NB_QUESTIONS_REQUISES + " questions. Actuellement : " + 
                (questions != null ? questions.size() : 0) + "/" + NB_QUESTIONS_REQUISES + ".");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
            return;
        }

        // Vérifier que chaque question est complète
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            if (q.getTexteQuestion() == null || q.getTexteQuestion().trim().isEmpty() ||
                q.getProposition1() == null || q.getProposition1().trim().isEmpty() ||
                q.getProposition2() == null || q.getProposition2().trim().isEmpty()) {
                session.setAttribute("messageErreurModification",
                    "La question " + (i + 1) + " est incomplète. Vérifiez le texte et les propositions A et B.");
                response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
                return;
            }
        }

        // 1. Mettre à jour le quiz
        QuizDAO quizDAO = new QuizDAO();
        quiz.setId(quizId);
        boolean quizModifie = quizDAO.modifier(quiz);

        if (!quizModifie) {
            session.setAttribute("messageErreurModification", "Erreur lors de la modification du quiz.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
            return;
        }

        // 2. Supprimer toutes les anciennes associations quiz_questions
        QuizQuestionDAO quizQuestionDAO = new QuizQuestionDAO();
        quizQuestionDAO.supprimerToutesAssociations(quizId);

        // 3. Mettre à jour les questions existantes et en créer de nouvelles
        QuestionDAO questionDAO = new QuestionDAO();
        int questionsModifiees = 0;

        for (Question q : questions) {
            if (q.getId() > 0) {
                // Question existante → modifier
                questionDAO.modifier(q);
            } else {
                // Nouvelle question → créer
                questionDAO.ajouter(q);
                // Récupérer l'ID de la dernière question créée
                List<Question> toutesLesQuestions = questionDAO.listerToutes();
                if (toutesLesQuestions != null && !toutesLesQuestions.isEmpty()) {
                    q.setId(toutesLesQuestions.get(0).getId());
                }
            }

            // Associer la question au quiz
            if (q.getId() > 0) {
                quizQuestionDAO.associerQuestionAuQuiz(quizId, q.getId());
                questionsModifiees++;
            }
        }

        // 4. Nettoyer la session
        session.removeAttribute("quizEnModification");
        session.removeAttribute("questionsEnModification");
        session.removeAttribute("quizIdModification");
        session.removeAttribute("compteurQuestionsModification");

        // 5. Message de succès
        session.setAttribute("messageSucces",
            "Quiz \"" + quiz.getTitre() + "\" modifié avec succès ! " + questionsModifiees + " questions associées.");

        // 6. Redirection
        response.sendRedirect(request.getContextPath() + "/admin/quiz");
    }

    // ============================================================
    // TRAITEMENT : RÉINITIALISER (RECHARGER DEPUIS LA BASE)
    // ============================================================
    private void traiterReinitialisation(HttpServletRequest request, HttpServletResponse response,
                                          HttpSession session) throws IOException {

        Integer quizId = (Integer) session.getAttribute("quizIdModification");

        session.removeAttribute("quizEnModification");
        session.removeAttribute("questionsEnModification");
        session.removeAttribute("compteurQuestionsModification");

        session.setAttribute("messageSuccesModification", "Modifications annulées. Données rechargées.");

        if (quizId != null) {
            response.sendRedirect(request.getContextPath() + "/admin/quiz/modifier?quizId=" + quizId);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
        }
    }
}