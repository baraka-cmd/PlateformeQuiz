// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminDashboardServlet.java
// RÔLE    : Contrôleur du tableau de bord administrateur
//           Affiche les statistiques globales de la plateforme
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.AccessPremiumDAO;
import com.amijayquiz.dao.CoursDAO;
import com.amijayquiz.dao.PaiementManuelDAO;
import com.amijayquiz.dao.UtilisateurDAO;
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
 * AdminDashboardServlet - Tableau de bord administrateur
 * 
 * Affiche les statistiques globales :
 * - Nombre total d'utilisateurs
 * - Nombre de cours disponibles
 * - Nombre de paiements en attente
 * - Nombre de paiements validés aujourd'hui
 * - Nombre d'utilisateurs premium
 * - Dernières inscriptions
 * - Paiements en attente récents
 * 
 * URL : /admin/dashboard
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {

    // ===== REQUÊTES SQL =====
    private static final String COUNT_UTILISATEURS = 
        "SELECT COUNT(*) FROM utilisateurs WHERE role = 'joueur'";

    private static final String COUNT_UTILISATEURS_TOTAL = 
        "SELECT COUNT(*) FROM utilisateurs";

    private static final String COUNT_QUESTIONS = 
        "SELECT COUNT(*) FROM questions";

    private static final String COUNT_QUIZ = 
        "SELECT COUNT(*) FROM quiz";

    private static final String DERNIERES_INSCRIPTIONS = 
        "SELECT id, pseudo, email, date_inscription FROM utilisateurs " +
        "WHERE role = 'joueur' ORDER BY date_inscription DESC LIMIT 5";

    private static final String COUNT_PARTIES_JOUES = 
        "SELECT COUNT(*) FROM parties";

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

        // 2. Initialisation des DAO
        CoursDAO coursDAO = new CoursDAO();
        PaiementManuelDAO paiementManuelDAO = new PaiementManuelDAO();
        AccessPremiumDAO premiumDAO = new AccessPremiumDAO();

        // 3. Récupération des statistiques
        int totalUtilisateurs = compter(COUNT_UTILISATEURS_TOTAL);
        int totalJoueurs = compter(COUNT_UTILISATEURS);
        int totalCours = coursDAO.compterTous();
        int totalQuestions = compter(COUNT_QUESTIONS);
        int totalQuiz = compter(COUNT_QUIZ);
        int totalPartiesJouees = compter(COUNT_PARTIES_JOUES);
        int paiementsEnAttente = paiementManuelDAO.compterDemandesEnAttente();
        int paiementsValidesAujourdhui = paiementManuelDAO.compterValideAujourdhui();
        int utilisateursPremium = premiumDAO.compterPremiumActifs();

        // 4. Dernières inscriptions
        List<Map<String, Object>> dernieresInscriptions = listerDernieresInscriptions();

        // 5. Passage des attributs
        request.setAttribute("pageTitle", "Dashboard - Administration - AmijayQuiz");
        request.setAttribute("totalUtilisateurs", totalUtilisateurs);
        request.setAttribute("totalJoueurs", totalJoueurs);
        request.setAttribute("totalCours", totalCours);
        request.setAttribute("totalQuestions", totalQuestions);
        request.setAttribute("totalQuiz", totalQuiz);
        request.setAttribute("totalPartiesJouees", totalPartiesJouees);
        request.setAttribute("paiementsEnAttente", paiementsEnAttente);
        request.setAttribute("paiementsValidesAujourdhui", paiementsValidesAujourdhui);
        request.setAttribute("utilisateursPremium", utilisateursPremium);
        request.setAttribute("dernieresInscriptions", dernieresInscriptions);

        // 6. Affichage
        request.getRequestDispatcher("/views/admin/admin-dashboard.jsp").forward(request, response);
    }

    // ============================================================
    // MÉTHODES PRIVÉES
    // ============================================================

    /**
     * Exécute une requête COUNT et retourne le résultat
     */
    private int compter(String sql) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return 0;

            preparedStatement = connexion.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ [AdminDashboard] Erreur comptage : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return 0;
    }

    /**
     * Récupère les dernières inscriptions
     */
    private List<Map<String, Object>> listerDernieresInscriptions() {
        List<Map<String, Object>> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return liste;

            preparedStatement = connexion.prepareStatement(DERNIERES_INSCRIPTIONS);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", resultSet.getInt("id"));
                row.put("pseudo", resultSet.getString("pseudo"));
                row.put("email", resultSet.getString("email"));
                row.put("date_inscription", resultSet.getTimestamp("date_inscription"));
                liste.add(row);
            }

        } catch (SQLException e) {
            System.err.println("❌ [AdminDashboard] Erreur inscriptions : " + e.getMessage());
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Ferme les ressources JDBC
     */
    private void fermerRessources(Connection connexion, PreparedStatement preparedStatement, 
                                   ResultSet resultSet) {
        if (resultSet != null) {
            try { resultSet.close(); } catch (SQLException e) { /* Ignorer */ }
        }
        if (preparedStatement != null) {
            try { preparedStatement.close(); } catch (SQLException e) { /* Ignorer */ }
        }
        if (connexion != null) {
            try { connexion.close(); } catch (SQLException e) { /* Ignorer */ }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}