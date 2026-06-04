// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminResultatsQuizServlet.java
// RÔLE    : Contrôleur administrateur pour le détail d'un quiz
//           - Statistiques du quiz
//           - Distribution des scores
//           - Classement des joueurs
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.QuizDAO;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminResultatsQuizServlet - Détail d'un quiz
 * 
 * GET : Affiche les statistiques et le classement
 *       des joueurs pour un quiz spécifique
 * 
 * URL : /admin/resultats/quiz?id=X
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminResultatsQuizServlet", urlPatterns = {"/admin/resultats/quiz"})
public class AdminResultatsQuizServlet extends HttpServlet {

    // ============================================================
    // DOGET : AFFICHAGE DU DÉTAIL QUIZ
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

        Utilisateur admin = (Utilisateur) session.getAttribute("utilisateur");
        if (!"admin".equals(admin.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        // 2. Récupération et validation de l'ID du quiz
        String quizIdStr = request.getParameter("id");
        if (quizIdStr == null || quizIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID du quiz manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/resultats");
            return;
        }

        int quizId;
        try {
            quizId = Integer.parseInt(quizIdStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de quiz invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/resultats");
            return;
        }

        // 3. Vérifier que le quiz existe
        QuizDAO quizDAO = new QuizDAO();
        Quiz quiz = quizDAO.trouverParId(quizId);

        if (quiz == null) {
            session.setAttribute("messageErreur", "Le quiz #" + quizId + " n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/resultats");
            return;
        }

        // 4. Statistiques du quiz
        Map<String, Object> statsQuiz = getStatsQuiz(quizId);

        // 5. Distribution des scores
        List<Map<String, Object>> distributionScores = getDistributionScores(quizId);

        // 6. Classement des joueurs
        List<Map<String, Object>> classement = getClassement(quizId);

        // 7. Passage des attributs
        request.setAttribute("pageTitle", "Détail quiz - " + quiz.getTitre() + " - Administration");
        request.setAttribute("quiz", quiz);
        request.setAttribute("statsQuiz", statsQuiz);
        request.setAttribute("distributionScores", distributionScores);
        request.setAttribute("classement", classement);

        // 8. Affichage
        request.getRequestDispatcher("/views/admin/admin-resultats-quiz.jsp").forward(request, response);
    }

    // ============================================================
    // MÉTHODES PRIVÉES
    // ============================================================

    /**
     * Calcule les statistiques globales du quiz
     */
    private Map<String, Object> getStatsQuiz(int quizId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return stats;

            // Nombre de joueurs distincts
            ps = connexion.prepareStatement(
                "SELECT COUNT(DISTINCT id_joueur) FROM parties WHERE id_quiz = ?");
            ps.setInt(1, quizId);
            rs = ps.executeQuery();
            stats.put("nbJoueurs", rs.next() ? rs.getInt(1) : 0);
            rs.close(); ps.close();

            // Taux de réussite
            ps = connexion.prepareStatement(
                "SELECT ROUND(AVG(CASE WHEN score >= 8 THEN 100.0 ELSE 0 END), 1) FROM parties WHERE id_quiz = ?");
            ps.setInt(1, quizId);
            rs = ps.executeQuery();
            stats.put("tauxReussite", rs.next() ? rs.getDouble(1) : 0);
            rs.close(); ps.close();

            // Score moyen
            ps = connexion.prepareStatement(
                "SELECT ROUND(AVG((score * 100.0) / total_questions), 1) FROM parties WHERE id_quiz = ?");
            ps.setInt(1, quizId);
            rs = ps.executeQuery();
            stats.put("scoreMoyen", rs.next() ? rs.getDouble(1) : 0);
            rs.close(); ps.close();

            // Meilleur score
            ps = connexion.prepareStatement(
                "SELECT MAX(score) FROM parties WHERE id_quiz = ?");
            ps.setInt(1, quizId);
            rs = ps.executeQuery();
            stats.put("meilleurScore", rs.next() ? rs.getInt(1) : 0);
            rs.close(); ps.close();

            // Taux d'échec
            double tauxReussite = (double) stats.getOrDefault("tauxReussite", 0.0);
            stats.put("tauxEchec", Math.round((100.0 - tauxReussite) * 10.0) / 10.0);

        } catch (SQLException e) {
            System.err.println("❌ [AdminResultatsQuiz] Erreur stats quiz : " + e.getMessage());
        } finally {
            fermerRessources(null, ps, rs);
        }

        return stats;
    }

    /**
     * Calcule la distribution des scores (combien de joueurs pour chaque score)
     */
    private List<Map<String, Object>> getDistributionScores(int quizId) {
        List<Map<String, Object>> distribution = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return distribution;

            String sql = 
                "SELECT score, COUNT(*) AS nb_joueurs " +
                "FROM parties WHERE id_quiz = ? " +
                "GROUP BY score ORDER BY score DESC";

            ps = connexion.prepareStatement(sql);
            ps.setInt(1, quizId);
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("score", rs.getInt("score"));
                row.put("nbJoueurs", rs.getInt("nb_joueurs"));
                distribution.add(row);
            }

        } catch (SQLException e) {
            System.err.println("❌ [AdminResultatsQuiz] Erreur distribution : " + e.getMessage());
        } finally {
            fermerRessources(null, ps, rs);
        }

        return distribution;
    }

    /**
     * Récupère le classement des joueurs pour ce quiz
     */
    private List<Map<String, Object>> getClassement(int quizId) {
        List<Map<String, Object>> classement = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return classement;

            String sql = 
                "SELECT p.id_joueur, u.pseudo, p.score, p.total_questions, " +
                "p.temps_realise_secondes, p.date_partie " +
                "FROM parties p " +
                "JOIN utilisateurs u ON p.id_joueur = u.id " +
                "WHERE p.id_quiz = ? " +
                "ORDER BY p.score DESC, p.temps_realise_secondes ASC " +
                "LIMIT 100";

            ps = connexion.prepareStatement(sql);
            ps.setInt(1, quizId);
            rs = ps.executeQuery();

            int position = 1;
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("position", position);
                row.put("id_joueur", rs.getInt("id_joueur"));
                row.put("pseudo", rs.getString("pseudo"));
                row.put("score", rs.getInt("score"));
                row.put("total_questions", rs.getInt("total_questions"));
                row.put("temps_realise_secondes", rs.getInt("temps_realise_secondes"));
                row.put("date_partie", rs.getTimestamp("date_partie"));

                int score = rs.getInt("score");
                int total = rs.getInt("total_questions");
                double pourcentage = (total > 0) ? (score * 100.0 / total) : 0;
                row.put("pourcentage", Math.round(pourcentage * 10.0) / 10.0);
                row.put("reussi", score >= 8);

                int temps = rs.getInt("temps_realise_secondes");
                int minutes = temps / 60;
                int secondes = temps % 60;
                row.put("temps_formate", minutes + " min " + secondes + " s");

                classement.add(row);
                position++;
            }

        } catch (SQLException e) {
            System.err.println("❌ [AdminResultatsQuiz] Erreur classement : " + e.getMessage());
        } finally {
            fermerRessources(null, ps, rs);
        }

        return classement;
    }

    /**
     * Ferme les ressources JDBC
     */
    private void fermerRessources(Connection connexion, PreparedStatement ps, ResultSet rs) {
        if (rs != null) { try { rs.close(); } catch (SQLException e) { /* Ignorer */ } }
        if (ps != null) { try { ps.close(); } catch (SQLException e) { /* Ignorer */ } }
        if (connexion != null) { try { connexion.close(); } catch (SQLException e) { /* Ignorer */ } }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}