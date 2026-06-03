// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/PaiementsAdminServlet.java
// RÔLE    : Contrôleur administrateur pour la validation
//           des paiements manuels
//           GET  : Affiche la liste des demandes en attente
//           POST : Valide ou refuse une demande de paiement
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.AccessPremiumDAO;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PaiementsAdminServlet - Gestion des paiements manuels par l'administrateur
 * 
 * GET  : Affiche toutes les demandes de paiement en attente
 * POST : Valide (active le premium) ou refuse une demande
 * 
 * URL : /admin/paiements
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "PaiementsAdminServlet", urlPatterns = {"/admin/paiements"})
public class PaiementsAdminServlet extends HttpServlet {

    // ===== REQUÊTES SQL =====
    private static final String SELECT_DEMANDES_EN_ATTENTE = 
        "SELECT pm.*, u.pseudo, u.email " +
        "FROM paiements_manuels pm " +
        "JOIN utilisateurs u ON pm.utilisateur_id = u.id " +
        "WHERE pm.statut = 'EN_ATTENTE' " +
        "ORDER BY pm.date_creation ASC";

    private static final String SELECT_DEMANDES_HISTORIQUE = 
        "SELECT pm.*, u.pseudo, u.email " +
        "FROM paiements_manuels pm " +
        "JOIN utilisateurs u ON pm.utilisateur_id = u.id " +
        "WHERE pm.statut != 'EN_ATTENTE' " +
        "ORDER BY pm.date_validation DESC " +
        "LIMIT 20";

    private static final String UPDATE_VALIDER = 
        "UPDATE paiements_manuels SET statut = 'VALIDE', date_validation = ? WHERE id = ?";

    private static final String UPDATE_REFUSER = 
        "UPDATE paiements_manuels SET statut = 'REFUSE', date_validation = ? WHERE id = ?";

    private static final String SELECT_DEMANDE_PAR_ID = 
        "SELECT * FROM paiements_manuels WHERE id = ?";

    // ============================================================
    // DOGET : AFFICHAGE DE LA PAGE ADMIN
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

        // 2. Récupération des demandes en attente
        List<Map<String, Object>> demandesEnAttente = listerDemandes(SELECT_DEMANDES_EN_ATTENTE);

        // 3. Récupération de l'historique récent
        List<Map<String, Object>> historique = listerDemandes(SELECT_DEMANDES_HISTORIQUE);

        // 4. Statistiques
        PaiementManuelDAO dao = new PaiementManuelDAO();
        int nbEnAttente = dao.compterDemandesEnAttente();
        int nbValideAujourdhui = dao.compterValideAujourdhui();

        // 5. Passage des attributs
        request.setAttribute("pageTitle", "Paiements en attente - Administration - AmijayQuiz");
        request.setAttribute("demandes", demandesEnAttente);
        request.setAttribute("historique", historique);
        request.setAttribute("nbEnAttente", nbEnAttente);
        request.setAttribute("nbValideAujourdhui", nbValideAujourdhui);

        // 6. Affichage
        request.getRequestDispatcher("/views/admin/paiements-admin.jsp").forward(request, response);
    }

    // ============================================================
    // DOPOST : VALIDATION OU REFUS D'UNE DEMANDE
    // ============================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

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
        String action = request.getParameter("action");
        String idParam = request.getParameter("id");

        if (action == null || idParam == null) {
            session.setAttribute("messageErreur", "Paramètres manquants.");
            response.sendRedirect(request.getContextPath() + "/admin/paiements");
            return;
        }

        int demandeId;
        try {
            demandeId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de demande invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/paiements");
            return;
        }

        // 3. Récupérer les infos de la demande
        Map<String, Object> demande = trouverDemandeParId(demandeId);
        if (demande == null || demande.isEmpty()) {
            session.setAttribute("messageErreur", "Demande introuvable.");
            response.sendRedirect(request.getContextPath() + "/admin/paiements");
            return;
        }

        int utilisateurId = (int) demande.get("utilisateur_id");
        String idTransaction = (String) demande.get("id_transaction");

        // 4. Traitement selon l'action
        if ("valider".equals(action)) {
            // VALIDER LA DEMANDE
            boolean valide = validerDemande(demandeId);

            if (valide) {
                // Activer le premium
                AccessPremiumDAO premiumDAO = new AccessPremiumDAO();
                boolean premiumActive = premiumDAO.activerPremium(utilisateurId, idTransaction, 4.00, "USD");

                if (premiumActive) {
                    session.setAttribute("messageSucces",
                        "Demande #" + demandeId + " validée avec succès. Premium activé pour l'utilisateur ID " + utilisateurId + ".");
                    System.out.println("✅ [Admin] Demande #" + demandeId + " validée - UserID: " + utilisateurId);
                } else {
                    session.setAttribute("messageErreur",
                        "Demande validée mais erreur lors de l'activation du premium. Vérifiez manuellement.");
                    System.err.println("❌ [Admin] Erreur activation premium - UserID: " + utilisateurId);
                }
            } else {
                session.setAttribute("messageErreur", "Erreur lors de la validation de la demande #" + demandeId + ".");
            }

        } else if ("refuser".equals(action)) {
            // REFUSER LA DEMANDE
            boolean refuse = refuserDemande(demandeId);

            if (refuse) {
                session.setAttribute("messageSucces",
                    "Demande #" + demandeId + " refusée.");
                System.out.println("⚠️ [Admin] Demande #" + demandeId + " refusée - UserID: " + utilisateurId);
            } else {
                session.setAttribute("messageErreur", "Erreur lors du refus de la demande #" + demandeId + ".");
            }

        } else {
            session.setAttribute("messageErreur", "Action invalide. Utilisez 'valider' ou 'refuser'.");
        }

        // 5. Redirection vers la page admin
        response.sendRedirect(request.getContextPath() + "/admin/paiements");
    }

    // ============================================================
    // MÉTHODES PRIVÉES
    // ============================================================

    /**
     * Liste les demandes selon une requête SQL donnée
     * 
     * @param sql Requête SQL SELECT
     * @return Liste de Map contenant les données de chaque demande
     */
    private List<Map<String, Object>> listerDemandes(String sql) {
        List<Map<String, Object>> liste = new ArrayList<>();
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return liste;

            preparedStatement = connexion.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", resultSet.getInt("id"));
                row.put("utilisateur_id", resultSet.getInt("utilisateur_id"));
                row.put("id_transaction", resultSet.getString("id_transaction"));
                row.put("operateur", resultSet.getString("operateur"));
                row.put("telephone", resultSet.getString("telephone"));
                row.put("statut", resultSet.getString("statut"));
                row.put("date_creation", resultSet.getTimestamp("date_creation"));
                row.put("date_validation", resultSet.getTimestamp("date_validation"));

                // Colonnes jointes de la table utilisateurs
                try {
                    row.put("pseudo", resultSet.getString("pseudo"));
                    row.put("email", resultSet.getString("email"));
                } catch (SQLException e) {
                    row.put("pseudo", "N/A");
                    row.put("email", "N/A");
                }

                liste.add(row);
            }

        } catch (SQLException e) {
            System.err.println("❌ [PaiementsAdminServlet] Erreur liste demandes : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return liste;
    }

    /**
     * Trouve une demande par son ID
     * 
     * @param id ID de la demande
     * @return Map contenant les données de la demande, ou null
     */
    private Map<String, Object> trouverDemandeParId(int id) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return null;

            preparedStatement = connexion.prepareStatement(SELECT_DEMANDE_PAR_ID);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", resultSet.getInt("id"));
                row.put("utilisateur_id", resultSet.getInt("utilisateur_id"));
                row.put("id_transaction", resultSet.getString("id_transaction"));
                row.put("operateur", resultSet.getString("operateur"));
                row.put("telephone", resultSet.getString("telephone"));
                row.put("statut", resultSet.getString("statut"));
                return row;
            }

        } catch (SQLException e) {
            System.err.println("❌ [PaiementsAdminServlet] Erreur recherche demande : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, resultSet);
        }

        return null;
    }

    /**
     * Valide une demande de paiement
     * 
     * @param demandeId ID de la demande
     * @return true si la mise à jour a réussi
     */
    private boolean validerDemande(int demandeId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(UPDATE_VALIDER);
            preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setInt(2, demandeId);

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ [PaiementsAdminServlet] Erreur validation : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
    }

    /**
     * Refuse une demande de paiement
     * 
     * @param demandeId ID de la demande
     * @return true si la mise à jour a réussi
     */
    private boolean refuserDemande(int demandeId) {
        Connection connexion = null;
        PreparedStatement preparedStatement = null;

        try {
            connexion = ConnexionDB.getConnexion();
            if (connexion == null) return false;

            preparedStatement = connexion.prepareStatement(UPDATE_REFUSER);
            preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setInt(2, demandeId);

            int lignes = preparedStatement.executeUpdate();
            return lignes > 0;

        } catch (SQLException e) {
            System.err.println("❌ [PaiementsAdminServlet] Erreur refus : " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerRessources(null, preparedStatement, null);
        }

        return false;
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
}