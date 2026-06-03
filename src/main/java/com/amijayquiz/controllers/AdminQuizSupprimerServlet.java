// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminQuizSupprimerServlet.java
// RÔLE    : Contrôleur administrateur pour la suppression d'un quiz
//           Supprime le quiz et ses associations
//           Conserve l'historique des parties et la progression
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.QuizDAO;
import com.amijayquiz.dao.QuizQuestionDAO;
import com.amijayquiz.models.Quiz;
import com.amijayquiz.models.Utilisateur;
import com.amijayquiz.utils.ConnexionDB;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * AdminQuizSupprimerServlet - Suppression d'un quiz
 * 
 * POST : Supprime le quiz et ses associations (quiz_questions)
 *        Conserve les parties jouées et les progressions
 *        pour ne pas casser l'historique des utilisateurs
 * 
 * URL : /admin/quiz/supprimer
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminQuizSupprimerServlet", urlPatterns = {"/admin/quiz/supprimer"})
public class AdminQuizSupprimerServlet extends HttpServlet {

    // Requête pour dissocier les questions du quiz
    private static final String DISSOCIER_QUESTIONS = 
        "DELETE FROM quiz_questions WHERE id_quiz = ?";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/admin/quiz");
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

        // 2. Récupération et validation de l'ID
        String idStr = request.getParameter("id");

        if (idStr == null || idStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID du quiz manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr.trim());
            if (id <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de quiz invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        // 3. Vérifier que le quiz existe
        QuizDAO quizDAO = new QuizDAO();
        Quiz quiz = quizDAO.trouverParId(id);

        if (quiz == null) {
            session.setAttribute("messageErreur", "Le quiz #" + id + " n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        String titreQuiz = quiz.getTitre();
        String niveauQuiz = quiz.getNiveauFormate();

        // 4. Étape 1 : Dissocier les questions du quiz
        //    Les questions elles-mêmes restent dans la base pour être réutilisées
        boolean questionsDissociees = dissocierQuestions(id);

        if (!questionsDissociees) {
            System.err.println("⚠️ [AdminQuizSupprimer] Erreur dissociation questions - QuizID: " + id);
        }

        // 5. Étape 2 : Supprimer le quiz
        //    Les parties (table "parties") sont CONSERVÉES pour l'historique
        //    Les progressions (table "progression_joueur") sont CONSERVÉES
        //    Seul le quiz et ses associations sont supprimés
        boolean supprime = quizDAO.supprimer(id);

        // 6. Message de résultat
        if (supprime) {
            session.setAttribute("messageSucces",
                "Quiz \"" + titreQuiz + "\" (Niveau " + niveauQuiz + ") supprimé avec succès ! " +
                "Les questions ont été dissociées et peuvent être réutilisées. " +
                "L'historique des parties et les progressions des utilisateurs sont conservés.");

            System.out.println("✅ [AdminQuizSupprimer] Quiz supprimé - ID: " + id +
                " - Titre: " + titreQuiz + " - Niveau: " + niveauQuiz);

        } else {
            session.setAttribute("messageErreur",
                "Erreur lors de la suppression du quiz #" + id + ". Veuillez réessayer.");

            System.err.println("❌ [AdminQuizSupprimer] Erreur suppression - QuizID: " + id);
        }

        // 7. Redirection
        response.sendRedirect(request.getContextPath() + "/admin/quiz");
    }

    /**
     * Dissocie toutes les questions du quiz (supprime les lignes dans quiz_questions)
     * Les questions elles-mêmes ne sont pas supprimées
     * 
     * @param quizId ID du quiz
     * @return true si la dissociation a réussi
     */
    private boolean dissocierQuestions(int quizId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(DISSOCIER_QUESTIONS);
            preparedStatement.setInt(1, quizId);

            int lignes = preparedStatement.executeUpdate();

            System.out.println("📋 [AdminQuizSupprimer] Questions dissociées - QuizID: " + quizId +
                " - " + lignes + " association(s) supprimée(s)");

            return true;

        } catch (SQLException e) {
            System.err.println("❌ [AdminQuizSupprimer] Erreur dissociation questions : " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (preparedStatement != null) {
                try { preparedStatement.close(); } catch (SQLException e) { /* Ignorer */ }
            }
            if (connexion != null) {
                try { connexion.close(); } catch (SQLException e) { /* Ignorer */ }
            }
        }
    }
}