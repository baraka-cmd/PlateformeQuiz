// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/AdminCoursModuleQuestionsServlet.java
// RÔLE    : Contrôleur pour gérer les questions d'un examen de module
//           GET  : Liste les questions existantes
//           POST : Ajouter, modifier ou supprimer une question
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.CoursDAO;
import com.learnpath.dao.ExamenModuleDAO;
import com.learnpath.dao.QuestionExamenDAO;
import com.learnpath.models.Cours;
import com.learnpath.models.ExamenModule;
import com.learnpath.models.QuestionExamen;
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
 * AdminCoursModuleQuestionsServlet - Gestion des questions d'un examen de module
 * 
 * GET  : Affiche la liste des questions de l'examen
 *        avec le nombre configuré / nombre requis
 * 
 * POST : action=ajouter → Ajoute une nouvelle question
 *        action=modifier → Modifie une question existante
 *        action=supprimer → Supprime une question
 * 
 * URL : /admin/cours/modules/questions?examenId=X&coursId=Y
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebServlet(name = "AdminCoursModuleQuestionsServlet", urlPatterns = {"/admin/cours/modules/questions"})
public class AdminCoursModuleQuestionsServlet extends HttpServlet {

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
        if (!"admin".equals(utilisateur.getRole()) && !"gestionnaire".equals(utilisateur.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        // 2. Récupération et validation des paramètres
        String examenIdStr = request.getParameter("examenId");
        String coursIdStr = request.getParameter("coursId");

        if (examenIdStr == null || examenIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID de l'examen manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        int examenId;
        try {
            examenId = Integer.parseInt(examenIdStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID d'examen invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        int coursId = 0;
        if (coursIdStr != null && !coursIdStr.trim().isEmpty()) {
            try { coursId = Integer.parseInt(coursIdStr.trim()); } catch (NumberFormatException e) {}
        }

        // 3. Récupération de l'examen
        ExamenModuleDAO examenDAO = new ExamenModuleDAO();
        ExamenModule examen = examenDAO.trouverParId(examenId);

        if (examen == null) {
            session.setAttribute("messageErreur", "Cet examen n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        if (coursId == 0) coursId = examen.getCoursId();

        // 4. Récupération du cours
        CoursDAO coursDAO = new CoursDAO();
        Cours cours = coursDAO.trouverParId(coursId);

        // 5. Récupération des questions existantes
        QuestionExamenDAO questionDAO = new QuestionExamenDAO();
        List<QuestionExamen> questions = questionDAO.listerParExamen(examenId);

        int nbQuestionsActuelles = questions != null ? questions.size() : 0;
        int nbQuestionsRequis = examen.getNombreQuestions();
        boolean quizComplet = nbQuestionsActuelles >= nbQuestionsRequis;

        // 6. Message de succès/erreur
        String messageSucces = (String) session.getAttribute("messageSucces");
        String messageErreur = (String) session.getAttribute("messageErreur");
        if (messageSucces != null) session.removeAttribute("messageSucces");
        if (messageErreur != null) session.removeAttribute("messageErreur");

        // 7. Passage des attributs
        request.setAttribute("pageTitle", "Questions - " + examen.getModuleFormate() + " - " + 
            (cours != null ? cours.getTitre() : "Cours") + " - Administration");
        request.setAttribute("cours", cours);
        request.setAttribute("examen", examen);
        request.setAttribute("questions", questions);
        request.setAttribute("nbQuestionsActuelles", nbQuestionsActuelles);
        request.setAttribute("nbQuestionsRequis", nbQuestionsRequis);
        request.setAttribute("quizComplet", quizComplet);
        request.setAttribute("messageSucces", messageSucces);
        request.setAttribute("messageErreur", messageErreur);

        // 8. Affichage
        request.getRequestDispatcher("/views/admin/admin-cours-module-questions.jsp").forward(request, response);
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
        String examenIdStr = request.getParameter("examenId");
        String coursIdStr = request.getParameter("coursId");

        if (examenIdStr == null || examenIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID de l'examen manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        int examenId;
        int coursId = 0;
        try {
            examenId = Integer.parseInt(examenIdStr.trim());
            if (coursIdStr != null && !coursIdStr.isEmpty()) {
                coursId = Integer.parseInt(coursIdStr.trim());
            }
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        String redirectUrl = request.getContextPath() + "/admin/cours/modules/questions?examenId=" + examenId;
        if (coursId > 0) redirectUrl += "&coursId=" + coursId;

        // 3. Traitement selon l'action
        if ("ajouter".equals(action)) {
            traiterAjoutQuestion(request, response, session, examenId, redirectUrl);
        } else if ("modifier".equals(action)) {
            traiterModificationQuestion(request, response, session, examenId, redirectUrl);
        } else if ("supprimer".equals(action)) {
            traiterSuppressionQuestion(request, response, session, examenId, redirectUrl);
        } else {
            session.setAttribute("messageErreur", "Action invalide.");
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Ajoute une nouvelle question à l'examen
     */
    private void traiterAjoutQuestion(HttpServletRequest request, HttpServletResponse response,
                                       HttpSession session, int examenId, String redirectUrl) throws IOException {

        String question = request.getParameter("question");
        String proposition1 = request.getParameter("proposition1");
        String proposition2 = request.getParameter("proposition2");
        String proposition3 = request.getParameter("proposition3");
        String proposition4 = request.getParameter("proposition4");
        String bonneReponseStr = request.getParameter("bonneReponse");

        // Validation
        if (question == null || question.trim().isEmpty() ||
            proposition1 == null || proposition1.trim().isEmpty() ||
            proposition2 == null || proposition2.trim().isEmpty() ||
            bonneReponseStr == null || bonneReponseStr.trim().isEmpty()) {

            session.setAttribute("messageErreur", "La question, les propositions A et B, et la bonne réponse sont obligatoires.");
            response.sendRedirect(redirectUrl);
            return;
        }

        int bonneReponse;
        try {
            bonneReponse = Integer.parseInt(bonneReponseStr.trim());
            if (bonneReponse < 1 || bonneReponse > 4) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "La bonne réponse doit être un nombre entre 1 et 4.");
            response.sendRedirect(redirectUrl);
            return;
        }

        // Créer la question
        QuestionExamenDAO questionDAO = new QuestionExamenDAO();
        QuestionExamen q = new QuestionExamen();
        q.setExamenId(examenId);
        q.setNumero(questionDAO.compterParExamen(examenId) + 1);
        q.setQuestion(question.trim());
        q.setProposition1(proposition1.trim());
        q.setProposition2(proposition2.trim());
        q.setProposition3(proposition3 != null ? proposition3.trim() : "");
        q.setProposition4(proposition4 != null ? proposition4.trim() : "");
        q.setBonneReponse(bonneReponse);

        boolean ajoute = questionDAO.ajouter(q);

        if (ajoute) {
            session.setAttribute("messageSucces", "Question ajoutée avec succès !");
        } else {
            session.setAttribute("messageErreur", "Erreur lors de l'ajout de la question.");
        }

        response.sendRedirect(redirectUrl);
    }

    /**
     * Modifie une question existante
     */
    private void traiterModificationQuestion(HttpServletRequest request, HttpServletResponse response,
                                              HttpSession session, int examenId, String redirectUrl) throws IOException {

        String questionIdStr = request.getParameter("questionId");

        if (questionIdStr == null || questionIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID de la question manquant.");
            response.sendRedirect(redirectUrl);
            return;
        }

        int questionId;
        try {
            questionId = Integer.parseInt(questionIdStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de question invalide.");
            response.sendRedirect(redirectUrl);
            return;
        }

        QuestionExamenDAO questionDAO = new QuestionExamenDAO();
        QuestionExamen q = questionDAO.trouverParId(questionId);

        if (q == null) {
            session.setAttribute("messageErreur", "Question introuvable.");
            response.sendRedirect(redirectUrl);
            return;
        }

        String question = request.getParameter("question");
        String proposition1 = request.getParameter("proposition1");
        String proposition2 = request.getParameter("proposition2");
        String proposition3 = request.getParameter("proposition3");
        String proposition4 = request.getParameter("proposition4");
        String bonneReponseStr = request.getParameter("bonneReponse");

        if (question == null || question.trim().isEmpty() ||
            proposition1 == null || proposition1.trim().isEmpty() ||
            proposition2 == null || proposition2.trim().isEmpty() ||
            bonneReponseStr == null || bonneReponseStr.trim().isEmpty()) {

            session.setAttribute("messageErreur", "La question, les propositions A et B, et la bonne réponse sont obligatoires.");
            response.sendRedirect(redirectUrl);
            return;
        }

        int bonneReponse;
        try {
            bonneReponse = Integer.parseInt(bonneReponseStr.trim());
            if (bonneReponse < 1 || bonneReponse > 4) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "La bonne réponse doit être un nombre entre 1 et 4.");
            response.sendRedirect(redirectUrl);
            return;
        }

        q.setQuestion(question.trim());
        q.setProposition1(proposition1.trim());
        q.setProposition2(proposition2.trim());
        q.setProposition3(proposition3 != null ? proposition3.trim() : "");
        q.setProposition4(proposition4 != null ? proposition4.trim() : "");
        q.setBonneReponse(bonneReponse);

        boolean modifie = questionDAO.modifier(q);

        if (modifie) {
            session.setAttribute("messageSucces", "Question modifiée avec succès !");
        } else {
            session.setAttribute("messageErreur", "Erreur lors de la modification.");
        }

        response.sendRedirect(redirectUrl);
    }

    /**
     * Supprime une question
     */
    private void traiterSuppressionQuestion(HttpServletRequest request, HttpServletResponse response,
                                             HttpSession session, int examenId, String redirectUrl) throws IOException {

        String questionIdStr = request.getParameter("questionId");

        if (questionIdStr == null || questionIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID de la question manquant.");
            response.sendRedirect(redirectUrl);
            return;
        }

        int questionId;
        try {
            questionId = Integer.parseInt(questionIdStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de question invalide.");
            response.sendRedirect(redirectUrl);
            return;
        }

        QuestionExamenDAO questionDAO = new QuestionExamenDAO();
        boolean supprime = questionDAO.supprimer(questionId);

        if (supprime) {
            session.setAttribute("messageSucces", "Question supprimée avec succès !");
        } else {
            session.setAttribute("messageErreur", "Erreur lors de la suppression.");
        }

        response.sendRedirect(redirectUrl);
    }
}