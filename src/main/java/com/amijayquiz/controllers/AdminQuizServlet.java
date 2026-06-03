// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminQuizServlet.java
// RÔLE    : Contrôleur administrateur pour la gestion des quiz
//           GET  : Liste tous les quiz avec recherche
//           POST : Crée, modifie ou supprime un quiz
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.QuizDAO;
import com.amijayquiz.models.Quiz;
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
 * AdminQuizServlet - Gestion des quiz par l'administrateur
 * 
 * GET  : Affiche la liste de tous les quiz
 *        avec recherche par titre ou description
 * 
 * POST : Action = ajouter → Crée un nouveau quiz
 *        Action = modifier → Modifie un quiz existant
 *        Action = supprimer → Supprime un quiz
 * 
 * URL : /admin/quiz
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminQuizServlet", urlPatterns = {"/admin/quiz"})
public class AdminQuizServlet extends HttpServlet {

    // Constantes des niveaux
    private static final String[] NIVEAUX = {"elementaire", "moyen", "avance"};

    // ============================================================
    // DOGET : AFFICHAGE DE LA LISTE DES QUIZ
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
        String filtreNiveau = request.getParameter("niveau");

        // 3. Initialisation du DAO
        QuizDAO quizDAO = new QuizDAO();
        List<Quiz> quizList;

        // 4. Chargement des quiz
        if (filtreNiveau != null && !filtreNiveau.isEmpty() && !"tous".equals(filtreNiveau)) {
            // Filtrer par niveau
            quizList = quizDAO.listerQuizParNiveau(filtreNiveau);
        } else {
            // Tous les quiz
            quizList = quizDAO.listerQuizDisponibles();
        }

        // 5. Filtrage par recherche textuelle
        if (recherche != null && !recherche.trim().isEmpty()) {
            String filtre = recherche.toLowerCase().trim();
            quizList.removeIf(q -> 
                (q.getTitre() == null || !q.getTitre().toLowerCase().contains(filtre)) &&
                (q.getDescription() == null || !q.getDescription().toLowerCase().contains(filtre))
            );
        }

        // 6. Passage des attributs à la JSP
        request.setAttribute("pageTitle", "Gestion des quiz - Administration - AmijayQuiz");
        request.setAttribute("quizList", quizList);
        request.setAttribute("totalQuiz", quizList != null ? quizList.size() : 0);
        request.setAttribute("recherche", recherche);
        request.setAttribute("filtreNiveau", filtreNiveau != null ? filtreNiveau : "tous");

        // 7. Message de succès/erreur
        String messageSucces = (String) session.getAttribute("messageSucces");
        String messageErreur = (String) session.getAttribute("messageErreur");
        if (messageSucces != null) session.removeAttribute("messageSucces");
        if (messageErreur != null) session.removeAttribute("messageErreur");
        request.setAttribute("messageSucces", messageSucces);
        request.setAttribute("messageErreur", messageErreur);

        // 8. Affichage
        request.getRequestDispatcher("/views/admin/admin-quiz.jsp").forward(request, response);
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
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
        }
    }

    // ============================================================
    // TRAITEMENT AJOUT
    // ============================================================
    private void traiterAjout(HttpServletRequest request, HttpServletResponse response, 
                               HttpSession session) throws IOException {

        String titre = request.getParameter("titre");
        String description = request.getParameter("description");
        String dureeStr = request.getParameter("dureeMaxMinutes");
        String niveau = request.getParameter("niveau");
        String ordrePartieStr = request.getParameter("ordrePartie");

        // Validation
        if (titre == null || titre.trim().isEmpty()) {
            session.setAttribute("messageErreur", "Le titre du quiz est obligatoire.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        if (niveau == null || niveau.trim().isEmpty()) {
            session.setAttribute("messageErreur", "Le niveau du quiz est obligatoire.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        int dureeMaxMinutes = 10;
        if (dureeStr != null && !dureeStr.trim().isEmpty()) {
            try {
                dureeMaxMinutes = Integer.parseInt(dureeStr);
                if (dureeMaxMinutes < 1) dureeMaxMinutes = 10;
            } catch (NumberFormatException e) {
                session.setAttribute("messageErreur", "La durée doit être un nombre valide.");
                response.sendRedirect(request.getContextPath() + "/admin/quiz");
                return;
            }
        }

        int ordrePartie = 1;
        if (ordrePartieStr != null && !ordrePartieStr.trim().isEmpty()) {
            try {
                ordrePartie = Integer.parseInt(ordrePartieStr);
                if (ordrePartie < 1) ordrePartie = 1;
            } catch (NumberFormatException e) {
                // Garder la valeur par défaut
            }
        }

        // Création du quiz
        Quiz quiz = new Quiz(
            titre.trim(),
            description != null ? description.trim() : "",
            dureeMaxMinutes,
            niveau.trim(),
            ordrePartie
        );

        QuizDAO dao = new QuizDAO();
        boolean ajoute = dao.ajouter(quiz);

        if (ajoute) {
            session.setAttribute("messageSucces", "Quiz ajoute avec succes !");
        } else {
            session.setAttribute("messageErreur", "Erreur lors de l'ajout du quiz.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/quiz");
    }

    // ============================================================
    // TRAITEMENT MODIFICATION
    // ============================================================
    private void traiterModification(HttpServletRequest request, HttpServletResponse response, 
                                      HttpSession session) throws IOException {

        String idStr = request.getParameter("id");
        String titre = request.getParameter("titre");
        String description = request.getParameter("description");
        String dureeStr = request.getParameter("dureeMaxMinutes");
        String niveau = request.getParameter("niveau");
        String ordrePartieStr = request.getParameter("ordrePartie");

        if (idStr == null || idStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID du quiz manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de quiz invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        int dureeMaxMinutes = 10;
        if (dureeStr != null && !dureeStr.trim().isEmpty()) {
            try {
                dureeMaxMinutes = Integer.parseInt(dureeStr);
                if (dureeMaxMinutes < 1) dureeMaxMinutes = 10;
            } catch (NumberFormatException e) {
                // Garder la valeur par défaut
            }
        }

        int ordrePartie = 1;
        if (ordrePartieStr != null && !ordrePartieStr.trim().isEmpty()) {
            try {
                ordrePartie = Integer.parseInt(ordrePartieStr);
                if (ordrePartie < 1) ordrePartie = 1;
            } catch (NumberFormatException e) {
                // Garder la valeur par défaut
            }
        }

        Quiz quiz = new Quiz();
        quiz.setId(id);
        quiz.setTitre(titre != null ? titre.trim() : "");
        quiz.setDescription(description != null ? description.trim() : "");
        quiz.setDureeMaxMinutes(dureeMaxMinutes);
        quiz.setNiveau(niveau != null ? niveau.trim() : "elementaire");
        quiz.setOrdrePartie(ordrePartie);

        QuizDAO dao = new QuizDAO();
        boolean modifie = dao.modifier(quiz);

        if (modifie) {
            session.setAttribute("messageSucces", "Quiz modifie avec succes !");
        } else {
            session.setAttribute("messageErreur", "Erreur lors de la modification du quiz.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/quiz");
    }

    // ============================================================
    // TRAITEMENT SUPPRESSION
    // ============================================================
    private void traiterSuppression(HttpServletRequest request, HttpServletResponse response, 
                                     HttpSession session) throws IOException {

        String idStr = request.getParameter("id");

        if (idStr == null || idStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID du quiz manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de quiz invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        QuizDAO dao = new QuizDAO();
        boolean supprime = dao.supprimer(id);

        if (supprime) {
            session.setAttribute("messageSucces", "Quiz supprime avec succes !");
        } else {
            session.setAttribute("messageErreur", "Erreur lors de la suppression du quiz.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/quiz");
    }
}