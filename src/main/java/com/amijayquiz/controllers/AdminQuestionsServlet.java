// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminQuestionsServlet.java
// RÔLE    : Contrôleur administrateur pour la gestion des questions
//           GET  : Liste toutes les questions avec recherche
//           POST : Crée, modifie ou supprime une question
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.QuestionDAO;
import com.amijayquiz.models.Question;
import com.amijayquiz.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * AdminQuestionsServlet - Gestion des questions par l'administrateur
 * 
 * GET  : Affiche la liste de toutes les questions
 *        avec recherche par mot-clé
 * 
 * POST : Action = ajouter → Crée une nouvelle question
 *        Action = modifier → Modifie une question existante
 *        Action = supprimer → Supprime une question
 * 
 * URL : /admin/questions
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminQuestionsServlet", urlPatterns = {"/admin/questions"})
public class AdminQuestionsServlet extends HttpServlet {

    // ============================================================
    // DOGET : AFFICHAGE DE LA LISTE DES QUESTIONS
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

        // 2. Récupération du paramètre de recherche
        String recherche = request.getParameter("recherche");

        // 3. Initialisation du DAO
        QuestionDAO questionDAO = new QuestionDAO();
        List<Question> questions;

        // 4. Chargement des questions (toutes ou filtrées)
        if (recherche != null && !recherche.trim().isEmpty()) {
            // Recherche filtrée
            questions = questionDAO.listerToutes();
            String filtre = recherche.toLowerCase().trim();
            questions.removeIf(q -> 
                q.getTexteQuestion() == null || 
                !q.getTexteQuestion().toLowerCase().contains(filtre)
            );
        } else {
            // Toutes les questions
            questions = questionDAO.listerToutes();
        }

        // 5. Passage des attributs à la JSP
        request.setAttribute("pageTitle", "Gestion des questions - Administration - AmijayQuiz");
        request.setAttribute("questions", questions);
        request.setAttribute("totalQuestions", questions != null ? questions.size() : 0);
        request.setAttribute("recherche", recherche);

        // 6. Message de succès/erreur
        String messageSucces = (String) session.getAttribute("messageSucces");
        String messageErreur = (String) session.getAttribute("messageErreur");
        if (messageSucces != null) session.removeAttribute("messageSucces");
        if (messageErreur != null) session.removeAttribute("messageErreur");
        request.setAttribute("messageSucces", messageSucces);
        request.setAttribute("messageErreur", messageErreur);

        // 7. Affichage
        request.getRequestDispatcher("/views/admin/admin-questions.jsp").forward(request, response);
    }

    // ============================================================
    // DOPOST : AJOUT, MODIFICATION OU SUPPRESSION
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

        if ("ajouter".equals(action)) {
            traiterAjout(request, response, session);
        } else if ("modifier".equals(action)) {
            traiterModification(request, response, session);
        } else if ("supprimer".equals(action)) {
            traiterSuppression(request, response, session);
        } else {
            session.setAttribute("messageErreur", "Action invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/questions");
        }
    }

    // ============================================================
    // TRAITEMENT AJOUT
    // ============================================================
    private void traiterAjout(HttpServletRequest request, HttpServletResponse response, 
                               HttpSession session) throws IOException {

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

            session.setAttribute("messageErreur", 
                "La question, les propositions 1 et 2, et la bonne réponse sont obligatoires.");
            response.sendRedirect(request.getContextPath() + "/admin/questions");
            return;
        }

        int bonneReponse;
        try {
            bonneReponse = Integer.parseInt(bonneReponseStr);
            if (bonneReponse < 1 || bonneReponse > 4) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "La bonne réponse doit être un nombre entre 1 et 4.");
            response.sendRedirect(request.getContextPath() + "/admin/questions");
            return;
        }

        // Création de la question
        Question question = new Question(
            texteQuestion.trim(),
            proposition1.trim(),
            proposition2.trim(),
            proposition3 != null ? proposition3.trim() : "",
            proposition4 != null ? proposition4.trim() : "",
            bonneReponse
        );

        QuestionDAO dao = new QuestionDAO();
        boolean ajoute = dao.ajouter(question);

        if (ajoute) {
            session.setAttribute("messageSucces", "Question ajoutée avec succès !");
        } else {
            session.setAttribute("messageErreur", "Erreur lors de l'ajout de la question.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/questions");
    }

    // ============================================================
    // TRAITEMENT MODIFICATION
    // ============================================================
    private void traiterModification(HttpServletRequest request, HttpServletResponse response, 
                                      HttpSession session) throws IOException {

        String idStr = request.getParameter("id");
        String texteQuestion = request.getParameter("texteQuestion");
        String proposition1 = request.getParameter("proposition1");
        String proposition2 = request.getParameter("proposition2");
        String proposition3 = request.getParameter("proposition3");
        String proposition4 = request.getParameter("proposition4");
        String bonneReponseStr = request.getParameter("bonneReponse");

        if (idStr == null || idStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID de la question manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/questions");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de question invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/questions");
            return;
        }

        int bonneReponse;
        try {
            bonneReponse = Integer.parseInt(bonneReponseStr);
            if (bonneReponse < 1 || bonneReponse > 4) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "La bonne réponse doit être un nombre entre 1 et 4.");
            response.sendRedirect(request.getContextPath() + "/admin/questions");
            return;
        }

        Question question = new Question(
            id,
            texteQuestion != null ? texteQuestion.trim() : "",
            proposition1 != null ? proposition1.trim() : "",
            proposition2 != null ? proposition2.trim() : "",
            proposition3 != null ? proposition3.trim() : "",
            proposition4 != null ? proposition4.trim() : "",
            bonneReponse
        );

        QuestionDAO dao = new QuestionDAO();
        boolean modifie = dao.modifier(question);

        if (modifie) {
            session.setAttribute("messageSucces", "Question modifiée avec succès !");
        } else {
            session.setAttribute("messageErreur", "Erreur lors de la modification de la question.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/questions");
    }

    // ============================================================
    // TRAITEMENT SUPPRESSION
    // ============================================================
    private void traiterSuppression(HttpServletRequest request, HttpServletResponse response, 
                                     HttpSession session) throws IOException {

        String idStr = request.getParameter("id");

        if (idStr == null || idStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID de la question manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/questions");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de question invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/questions");
            return;
        }

        QuestionDAO dao = new QuestionDAO();
        boolean supprime = dao.supprimer(id);

        if (supprime) {
            session.setAttribute("messageSucces", "Question supprimée avec succès !");
        } else {
            session.setAttribute("messageErreur", "Erreur lors de la suppression de la question.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/questions");
    }
}