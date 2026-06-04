// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminResultatsJoueurServlet.java
// RÔLE    : Contrôleur administrateur pour le détail d'un joueur
//           - Statistiques personnelles du joueur
//           - Historique complet de ses parties
//           - Progression par thème
// ============================================================

package com.amijayquiz.controllers;

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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminResultatsJoueurServlet - Détail d'un joueur
 * 
 * GET : Affiche les statistiques et l'historique complet
 *       d'un joueur spécifique
 * 
 * URL : /admin/resultats/joueur?id=X
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminResultatsJoueurServlet", urlPatterns = {"/admin/resultats/joueur"})
public class AdminResultatsJoueurServlet extends HttpServlet {

    // ============================================================
    // DOGET : AFFICHAGE DU DÉTAIL JOUEUR
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

        // 2. Récupération et validation de l'ID du joueur
        String joueurIdStr = request.getParameter("id");
        if (joueurIdStr == null || joueurIdStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID du joueur manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/resultats");
            return;
        }

        int joueurId;
        try {
            joueurId = Integer.parseInt(joueurIdStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de joueur invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/resultats");
            return;
        }

        // 3. Récupération des informations du joueur
        Map<String, Object> infosJoueur = getInfosJoueur(joueurId);
        if (infosJoueur == null || infosJoueur.isEmpty()) {
            session.setAttribute("messageErreur", "Joueur introuvable.");
            response.sendRedirect(request.getContextPath() + "/admin/resultats");
            return;
        }

        // 4. Statistiques personnelles du joueur
        Map<String, Object> statsJoueur = getStatsJoueur(joueurId);

        // 5. Progression par thème
        List<Map<String, Object>> progressionThemes = getProgressionParTheme(joueurId);

        // 6. Historique complet des parties
        List<Map<String, Object>> historique = getHistoriqueJoueur(joueurId);

        // 7. Passage des attributs
        request.setAttribute("pageTitle", "Détail joueur - " + infosJoueur.get("pseudo") + " - Administration");
        request.setAttribute("infosJoueur", infosJoueur);
        request.setAttribute("statsJoueur", statsJoueur);
        request.setAttribute("progressionThemes", progressionThemes);
        request.setAttribute("historique", historique);

        // 8. Affichage
        request.getRequestDispatcher("/views/admin/admin-resultats-joueur.jsp").forward(request, response);
    }

    // ============================================================
    // MÉTHODES PRIVÉES
    // ============================================================

    /**
     * Récupère les informations de base du joueur
     */
    private Map<String, Object> getInfosJoueur(int joueurId) {
        Map<String, Object> infos = new LinkedHashMap<>();
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return infos;

            String sql = "SELECT id, pseudo, email, date_inscription, premium FROM utilisateurs WHERE id = ? AND role = 'joueur'";
            ps = connexion.prepareStatement(sql);
            ps.setInt(1, joueurId);
            rs = ps.executeQuery();

            if (rs.next()) {
                infos.put("id", rs.getInt("id"));
                infos.put("pseudo", rs.getString("pseudo"));
                infos.put("email", rs.getString("email"));
                infos.put("date_inscription", rs.getTimestamp("date_inscription"));
                infos.put("premium", rs.getBoolean("premium"));
            }

        } catch (SQLException e) {
            System.err.println("❌ [AdminResultatsJoueur] Erreur infos joueur : " + e.getMessage());
        } finally {
            fermerRessources(null, ps, rs);
        }

        return infos;
    }

    /**
     * Calcule les statistiques personnelles du joueur
     */
    private Map<String, Object> getStatsJoueur(int joueurId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return stats;

            // Total parties
            ps = connexion.prepareStatement("SELECT COUNT(*) FROM parties WHERE id_joueur = ?");
            ps.setInt(1, joueurId);
            rs = ps.executeQuery();
            stats.put("totalParties", rs.next() ? rs.getInt(1) : 0);
            rs.close(); ps.close();

            // Parties réussies
            ps = connexion.prepareStatement("SELECT COUNT(*) FROM parties WHERE id_joueur = ? AND score >= 8");
            ps.setInt(1, joueurId);
            rs = ps.executeQuery();
            int reussies = rs.next() ? rs.getInt(1) : 0;
            stats.put("partiesReussies", reussies);
            rs.close(); ps.close();

            // Taux de réussite
            int total = (int) stats.get("totalParties");
            double tauxReussite = total > 0 ? (reussies * 100.0 / total) : 0;
            stats.put("tauxReussite", Math.round(tauxReussite * 10.0) / 10.0);
            rs.close(); ps.close();

            // Meilleur score
            ps = connexion.prepareStatement(
                "SELECT MAX((score * 100.0) / total_questions) FROM parties WHERE id_joueur = ?");
            ps.setInt(1, joueurId);
            rs = ps.executeQuery();
            stats.put("meilleurScore", rs.next() ? Math.round(rs.getDouble(1) * 10.0) / 10.0 : 0);
            rs.close(); ps.close();

            // Temps moyen
            ps = connexion.prepareStatement(
                "SELECT AVG(temps_realise_secondes) FROM parties WHERE id_joueur = ?");
            ps.setInt(1, joueurId);
            rs = ps.executeQuery();
            double tempsMoyen = rs.next() ? rs.getDouble(1) : 0;
            int minutes = (int) (tempsMoyen / 60);
            int secondes = (int) (tempsMoyen % 60);
            stats.put("tempsMoyen", minutes + " min " + secondes + " s");
            rs.close(); ps.close();

            // Nombre de thèmes joués
            ps = connexion.prepareStatement(
                "SELECT COUNT(DISTINCT SUBSTRING_INDEX(q.titre, ' - Partie', 1)) " +
                "FROM parties p JOIN quiz q ON p.id_quiz = q.id WHERE p.id_joueur = ?");
            ps.setInt(1, joueurId);
            rs = ps.executeQuery();
            stats.put("nbThemes", rs.next() ? rs.getInt(1) : 0);

        } catch (SQLException e) {
            System.err.println("❌ [AdminResultatsJoueur] Erreur stats joueur : " + e.getMessage());
        } finally {
            fermerRessources(null, ps, rs);
        }

        return stats;
    }

    /**
     * Calcule la progression du joueur par thème
     */
    private List<Map<String, Object>> getProgressionParTheme(int joueurId) {
        List<Map<String, Object>> progression = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return progression;

            String sql = 
                "SELECT " +
                "  SUBSTRING_INDEX(q.titre, ' - Partie', 1) AS theme, " +
                "  COUNT(*) AS total_parties, " +
                "  SUM(CASE WHEN p.score >= 8 THEN 1 ELSE 0 END) AS parties_reussies, " +
                "  ROUND(AVG((p.score * 100.0) / p.total_questions), 1) AS moyenne " +
                "FROM parties p " +
                "JOIN quiz q ON p.id_quiz = q.id " +
                "WHERE p.id_joueur = ? " +
                "GROUP BY theme " +
                "ORDER BY theme";

            ps = connexion.prepareStatement(sql);
            ps.setInt(1, joueurId);
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("theme", rs.getString("theme"));
                row.put("totalParties", rs.getInt("total_parties"));
                row.put("partiesReussies", rs.getInt("parties_reussies"));
                row.put("moyenne", rs.getDouble("moyenne"));

                int total = rs.getInt("total_parties");
                int reussies = rs.getInt("parties_reussies");
                row.put("pourcentage", total > 0 ? (reussies * 100 / total) : 0);

                progression.add(row);
            }

        } catch (SQLException e) {
            System.err.println("❌ [AdminResultatsJoueur] Erreur progression : " + e.getMessage());
        } finally {
            fermerRessources(null, ps, rs);
        }

        return progression;
    }

    /**
     * Récupère l'historique complet des parties du joueur
     */
    private List<Map<String, Object>> getHistoriqueJoueur(int joueurId) {
        List<Map<String, Object>> historique = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return historique;

            String sql = 
                "SELECT p.id, p.id_quiz, p.score, p.total_questions, " +
                "p.temps_realise_secondes, p.date_partie, " +
                "q.titre AS titre_quiz, q.niveau AS niveau_quiz " +
                "FROM parties p " +
                "JOIN quiz q ON p.id_quiz = q.id " +
                "WHERE p.id_joueur = ? " +
                "ORDER BY p.date_partie DESC " +
                "LIMIT 50";

            ps = connexion.prepareStatement(sql);
            ps.setInt(1, joueurId);
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("id_quiz", rs.getInt("id_quiz"));
                row.put("score", rs.getInt("score"));
                row.put("total_questions", rs.getInt("total_questions"));
                row.put("temps_realise_secondes", rs.getInt("temps_realise_secondes"));
                row.put("date_partie", rs.getTimestamp("date_partie"));
                row.put("titre_quiz", rs.getString("titre_quiz"));
                row.put("niveau_quiz", rs.getString("niveau_quiz"));

                int score = rs.getInt("score");
                int total = rs.getInt("total_questions");
                double pourcentage = (total > 0) ? (score * 100.0 / total) : 0;
                row.put("pourcentage", Math.round(pourcentage * 10.0) / 10.0);
                row.put("reussi", score >= 8);

                int temps = rs.getInt("temps_realise_secondes");
                int minutes = temps / 60;
                int secondes = temps % 60;
                row.put("temps_formate", minutes + " min " + secondes + " s");

                historique.add(row);
            }

        } catch (SQLException e) {
            System.err.println("❌ [AdminResultatsJoueur] Erreur historique : " + e.getMessage());
        } finally {
            fermerRessources(null, ps, rs);
        }

        return historique;
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