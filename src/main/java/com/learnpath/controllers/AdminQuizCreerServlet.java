// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/AdminQuizCreerServlet.java
// RÔLE    : Contrôleur de création de quiz en 2 étapes
//           GET  : Affiche le formulaire ou la liste des questions
//           POST : Ajoute une question ou enregistre le quiz final
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.QuizDAO;
import com.learnpath.dao.QuestionDAO;
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
 * AdminQuizCreerServlet - Création d'un quiz avec ses questions
 * 
 * GET  : Affiche la page de création du quiz
 *        avec la liste des questions déjà ajoutées
 * 
 * POST : action=ajouterQuestion → Ajoute une question à la liste temporaire
 *        action=enregistrer     → Crée le quiz + questions + associations
 * 
 * Les données sont stockées en session pendant la création.
 * Le quiz n'est enregistré QUE lorsque 10 questions sont prêtes.
 * 
 * URL : /admin/quiz/creer
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebServlet(name = "AdminQuizCreerServlet", urlPatterns = {"/admin/quiz/creer"})
public class AdminQuizCreerServlet extends HttpServlet {

    // Constantes
    private static final int NB_QUESTIONS_REQUISES = 10;
    private static final String[] NIVEAUX_VALIDES = {"elementaire", "moyen", "avance"};

    // ============================================================
    // DOGET : AFFICHAGE DE LA PAGE DE CRÉATION
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

        // 2. Récupérer les données de création en session (ou initialiser)
        Quiz quizEnCreation = (Quiz) session.getAttribute("quizEnCreation");
        List<Question> questionsEnCreation = (List<Question>) session.getAttribute("questionsEnCreation");
        Integer compteurQuestions = (Integer) session.getAttribute("compteurQuestions");

        // Initialiser si première visite
        if (quizEnCreation == null) {
            quizEnCreation = new Quiz();
            quizEnCreation.setDureeMaxMinutes(10);
            quizEnCreation.setNiveau("elementaire");
            quizEnCreation.setOrdrePartie(1);
        }

        if (questionsEnCreation == null) {
            questionsEnCreation = new ArrayList<>();
        }

        if (compteurQuestions == null) {
            compteurQuestions = questionsEnCreation.size();
        }

        // 3. Stocker en session
        session.setAttribute("quizEnCreation", quizEnCreation);
        session.setAttribute("questionsEnCreation", questionsEnCreation);
        session.setAttribute("compteurQuestions", compteurQuestions);

        // 4. Message de succès/erreur
        String messageSucces = (String) session.getAttribute("messageSuccesCreation");
        String messageErreur = (String) session.getAttribute("messageErreurCreation");
        if (messageSucces != null) session.removeAttribute("messageSuccesCreation");
        if (messageErreur != null) session.removeAttribute("messageErreurCreation");

        // 5. Passage des attributs
        request.setAttribute("pageTitle", "Créer un quiz - Administration - LearnPath");
        request.setAttribute("quiz", quizEnCreation);
        request.setAttribute("questions", questionsEnCreation);
        request.setAttribute("compteur", compteurQuestions);
        request.setAttribute("nbRequis", NB_QUESTIONS_REQUISES);
        request.setAttribute("quizComplet", compteurQuestions >= NB_QUESTIONS_REQUISES);
        request.setAttribute("messageSucces", messageSucces);
        request.setAttribute("messageErreur", messageErreur);

        // 6. Affichage
        request.getRequestDispatcher("/views/admin/admin-quiz-creer.jsp").forward(request, response);
    }

    // ============================================================
    // DOPOST : AJOUT DE QUESTION OU ENREGISTREMENT FINAL
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

        if ("enregistrerInfo".equals(action)) {
            traiterEnregistrementInfo(request, response, session);
        } else if ("ajouterQuestion".equals(action)) {
            traiterAjoutQuestion(request, response, session);
        } else if ("enregistrerQuiz".equals(action)) {
            traiterEnregistrementFinal(request, response, session);
        } else if ("supprimerQuestion".equals(action)) {
            traiterSuppressionQuestion(request, response, session);
        } else if ("reinitialiser".equals(action)) {
            traiterReinitialisation(request, response, session);
        } else {
            session.setAttribute("messageErreurCreation", "Action invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
        }
    }

    // ============================================================
    // TRAITEMENT : ENREGISTRER LES INFOS DU QUIZ
    // ============================================================
    private void traiterEnregistrementInfo(HttpServletRequest request, HttpServletResponse response,
                                            HttpSession session) throws IOException {

        String theme = request.getParameter("theme");
        String niveau = request.getParameter("niveau");
        String ordrePartieStr = request.getParameter("ordrePartie");
        String description = request.getParameter("description");
        String dureeStr = request.getParameter("dureeMaxMinutes");

        // Validation
        if (theme == null || theme.trim().isEmpty()) {
            session.setAttribute("messageErreurCreation", "Le thème est obligatoire.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
            return;
        }

        if (niveau == null || niveau.trim().isEmpty()) {
            session.setAttribute("messageErreurCreation", "Le niveau est obligatoire.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
            return;
        }

        int ordrePartie = 1;
        try {
            ordrePartie = Integer.parseInt(ordrePartieStr.trim());
            if (ordrePartie < 1) ordrePartie = 1;
            if (ordrePartie > 20) ordrePartie = 20;
        } catch (NumberFormatException e) {
            // Garder 1 par défaut
        }

        int dureeMaxMinutes = 10;
        try {
            dureeMaxMinutes = Integer.parseInt(dureeStr.trim());
            if (dureeMaxMinutes < 1) dureeMaxMinutes = 1;
            if (dureeMaxMinutes > 120) dureeMaxMinutes = 120;
        } catch (NumberFormatException e) {
            // Garder 10 par défaut
        }

        // Mettre à jour le quiz en session
        Quiz quiz = (Quiz) session.getAttribute("quizEnCreation");
        if (quiz == null) quiz = new Quiz();

        quiz.setTitre(theme.trim() + " - Partie " + ordrePartie);
        quiz.setNiveau(niveau.trim().toLowerCase());
        quiz.setOrdrePartie(ordrePartie);
        quiz.setDureeMaxMinutes(dureeMaxMinutes);
        quiz.setDescription(description != null ? description.trim() : "");

        session.setAttribute("quizEnCreation", quiz);
        session.setAttribute("messageSuccesCreation", "Informations du quiz enregistrées. Ajoutez maintenant les questions.");

        response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
    }

    // ============================================================
    // TRAITEMENT : AJOUTER UNE QUESTION
    // ============================================================
    private void traiterAjoutQuestion(HttpServletRequest request, HttpServletResponse response,
                                       HttpSession session) throws IOException {

        // Récupérer la liste existante
        List<Question> questions = (List<Question>) session.getAttribute("questionsEnCreation");
        if (questions == null) questions = new ArrayList<>();

        Integer compteur = (Integer) session.getAttribute("compteurQuestions");
        if (compteur == null) compteur = questions.size();

        // Vérifier qu'on n'a pas déjà 10 questions
        if (compteur >= NB_QUESTIONS_REQUISES) {
            session.setAttribute("messageErreurCreation",
                "Vous avez déjà " + NB_QUESTIONS_REQUISES + " questions. Supprimez-en une avant d'en ajouter.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
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

            session.setAttribute("messageErreurCreation",
                "La question, les propositions A et B, et la bonne réponse sont obligatoires.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
            return;
        }

        int bonneReponse;
        try {
            bonneReponse = Integer.parseInt(bonneReponseStr.trim());
            if (bonneReponse < 1 || bonneReponse > 4) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreurCreation",
                "La bonne réponse doit être un nombre entre 1 et 4.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
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

        // Mettre à jour la session
        session.setAttribute("questionsEnCreation", questions);
        session.setAttribute("compteurQuestions", compteur);

        session.setAttribute("messageSuccesCreation",
            "Question " + compteur + "/" + NB_QUESTIONS_REQUISES + " ajoutée avec succès !");

        response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
    }

    // ============================================================
    // TRAITEMENT : SUPPRIMER UNE QUESTION DE LA LISTE
    // ============================================================
    private void traiterSuppressionQuestion(HttpServletRequest request, HttpServletResponse response,
                                             HttpSession session) throws IOException {

        String indexStr = request.getParameter("index");

        if (indexStr == null || indexStr.trim().isEmpty()) {
            session.setAttribute("messageErreurCreation", "Index de question manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
            return;
        }

        int index;
        try {
            index = Integer.parseInt(indexStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreurCreation", "Index de question invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
            return;
        }

        List<Question> questions = (List<Question>) session.getAttribute("questionsEnCreation");
        if (questions == null || index < 0 || index >= questions.size()) {
            session.setAttribute("messageErreurCreation", "Question introuvable.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
            return;
        }

        questions.remove(index);
        int compteur = questions.size();

        session.setAttribute("questionsEnCreation", questions);
        session.setAttribute("compteurQuestions", compteur);

        session.setAttribute("messageSuccesCreation",
            "Question supprimée. " + compteur + "/" + NB_QUESTIONS_REQUISES + " restantes.");

        response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
    }

    // ============================================================
    // TRAITEMENT : ENREGISTREMENT FINAL DU QUIZ
    // ============================================================
    private void traiterEnregistrementFinal(HttpServletRequest request, HttpServletResponse response,
                                             HttpSession session) throws IOException {

        Quiz quiz = (Quiz) session.getAttribute("quizEnCreation");
        List<Question> questions = (List<Question>) session.getAttribute("questionsEnCreation");

        // Vérifications
        if (quiz == null || quiz.getTitre() == null || quiz.getTitre().trim().isEmpty()) {
            session.setAttribute("messageErreurCreation",
                "Veuillez d'abord remplir les informations du quiz.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
            return;
        }

        if (questions == null || questions.size() < NB_QUESTIONS_REQUISES) {
            session.setAttribute("messageErreurCreation",
                "Vous devez ajouter " + NB_QUESTIONS_REQUISES + " questions avant d'enregistrer. " +
                "Actuellement : " + (questions != null ? questions.size() : 0) + "/" + NB_QUESTIONS_REQUISES + ".");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
            return;
        }

        // 1. Créer le quiz
        QuizDAO quizDAO = new QuizDAO();
        boolean quizCree = quizDAO.ajouter(quiz);

        if (!quizCree) {
            session.setAttribute("messageErreurCreation",
                "Erreur lors de la création du quiz. Vérifiez qu'aucun quiz identique n'existe déjà.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
            return;
        }

        // 2. Récupérer l'ID du quiz créé (dernier quiz ajouté)
        List<Quiz> tousLesQuiz = quizDAO.listerQuizDisponibles();
        Quiz quizCreeMaintenant = null;
        for (Quiz q : tousLesQuiz) {
            if (q.getTitre() != null && q.getTitre().equals(quiz.getTitre()) &&
                q.getNiveau() != null && q.getNiveau().equals(quiz.getNiveau()) &&
                q.getOrdrePartie() == quiz.getOrdrePartie()) {
                quizCreeMaintenant = q;
                break;
            }
        }

        if (quizCreeMaintenant == null) {
            session.setAttribute("messageErreurCreation",
                "Quiz créé mais impossible de le retrouver. Contactez le support.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
            return;
        }

        int quizId = quizCreeMaintenant.getId();

        // 3. Enregistrer les questions et les associer au quiz
        QuestionDAO questionDAO = new QuestionDAO();
        QuizQuestionDAO quizQuestionDAO = new QuizQuestionDAO();

        int questionsAjoutees = 0;
        for (Question q : questions) {
            // Créer la question dans la base
            boolean questionAjoutee = questionDAO.ajouter(q);
            if (!questionAjoutee) continue;

            // Récupérer l'ID de la question créée
            List<Question> toutesLesQuestions = questionDAO.listerToutes();
            Question derniereQuestion = null;
            if (toutesLesQuestions != null && !toutesLesQuestions.isEmpty()) {
                derniereQuestion = toutesLesQuestions.get(0); // La plus récente (tri par ID DESC)
            }

            if (derniereQuestion != null) {
                // Associer la question au quiz
                boolean associee = quizQuestionDAO.associerQuestionAuQuiz(quizId, derniereQuestion.getId());
                if (associee) {
                    questionsAjoutees++;
                }
            }
        }

        // 4. Nettoyer la session
        session.removeAttribute("quizEnCreation");
        session.removeAttribute("questionsEnCreation");
        session.removeAttribute("compteurQuestions");
        session.removeAttribute("messageSuccesCreation");
        session.removeAttribute("messageErreurCreation");

        // 5. Message de succès
        session.setAttribute("messageSucces",
            "Quiz \"" + quiz.getTitre() + "\" créé avec succès ! " +
            questionsAjoutees + " question(s) associée(s).");

        System.out.println("✅ [AdminQuizCreer] Quiz créé - ID: " + quizId +
            " - Titre: " + quiz.getTitre() + " - Questions: " + questionsAjoutees);

        // 6. Redirection vers la liste des quiz
        response.sendRedirect(request.getContextPath() + "/admin/quiz");
    }

    // ============================================================
    // TRAITEMENT : RÉINITIALISER LA CRÉATION
    // ============================================================
    private void traiterReinitialisation(HttpServletRequest request, HttpServletResponse response,
                                          HttpSession session) throws IOException {

        session.removeAttribute("quizEnCreation");
        session.removeAttribute("questionsEnCreation");
        session.removeAttribute("compteurQuestions");
        session.removeAttribute("messageSuccesCreation");
        session.removeAttribute("messageErreurCreation");

        session.setAttribute("messageSucces", "Création de quiz réinitialisée.");

        response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
    }
}