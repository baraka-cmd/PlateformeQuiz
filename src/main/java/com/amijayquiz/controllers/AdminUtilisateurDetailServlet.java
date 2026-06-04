// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminUtilisateurDetailServlet.java
// RÔLE    : Contrôleur pour le détail complet d'un utilisateur
//           GET  : Affiche la fiche détaillée avec toutes les stats
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.AccessPremiumDAO;
import com.amijayquiz.dao.CertificatDAO;
import com.amijayquiz.dao.PartieDAO;
import com.amijayquiz.dao.ProgressionCoursDAO;
import com.amijayquiz.dao.ProgressionJoueurDAO;
import com.amijayquiz.dao.UtilisateurDAO;
import com.amijayquiz.models.Partie;
import com.amijayquiz.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminUtilisateurDetailServlet - Détail complet d'un utilisateur
 * 
 * Affiche :
 * - Informations personnelles (pseudo, email, rôle, date inscription, statut)
 * - Statut Premium (actif/inactif, date activation, référence paiement)
 * - Statistiques Quiz (nb parties, taux réussite, meilleur score, niveaux)
 * - Statistiques Cours (leçons vues, cours commencés, certificats)
 * - Dernières parties jouées (10 max)
 * - Derniers certificats obtenus (5 max)
 * - Actions : Activer/Désactiver, Changer rôle, Réinitialiser MDP
 * 
 * URL : /admin/utilisateurs/detail?id=X
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminUtilisateurDetailServlet", urlPatterns = {"/admin/utilisateurs/detail"})
public class AdminUtilisateurDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Vérification authentification + rôle admin
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        Utilisateur adminConnecte = (Utilisateur) session.getAttribute("utilisateur");
        if (!"admin".equals(adminConnecte.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        int adminId = adminConnecte.getId();

        // 2. Récupération et validation de l'ID
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID de l'utilisateur manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/utilisateurs");
            return;
        }

        int utilisateurId;
        try {
            utilisateurId = Integer.parseInt(idStr.trim());
            if (utilisateurId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID d'utilisateur invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/utilisateurs");
            return;
        }

        // 3. Récupération de l'utilisateur
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        Utilisateur utilisateur = utilisateurDAO.trouverParId(utilisateurId);

        if (utilisateur == null) {
            session.setAttribute("messageErreur", "Cet utilisateur n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/utilisateurs");
            return;
        }

        // 4. Initialisation des DAO
        AccessPremiumDAO premiumDAO = new AccessPremiumDAO();
        PartieDAO partieDAO = new PartieDAO();
        CertificatDAO certificatDAO = new CertificatDAO();
        ProgressionJoueurDAO progressionJoueurDAO = new ProgressionJoueurDAO();
        ProgressionCoursDAO progressionCoursDAO = new ProgressionCoursDAO();

        // 5. Statut Premium
        boolean estPremium = premiumDAO.estPremium(utilisateurId);
        Map<String, Object> infosPremium = premiumDAO.getInfosPremium(utilisateurId);

        // 6. Statistiques Quiz
        Map<String, Object> statsQuiz = new LinkedHashMap<>();
        int totalParties = partieDAO.compterPartiesJoueur(utilisateurId);
        double meilleurScore = partieDAO.meilleurScoreJoueur(utilisateurId);
        double moyenneScore = partieDAO.moyenneScoreJoueur(utilisateurId);
        int totalPartiesReussies = progressionJoueurDAO.compterTotalPartiesReussies(utilisateurId);

        statsQuiz.put("totalParties", totalParties);
        statsQuiz.put("meilleurScore", Math.round(meilleurScore * 10.0) / 10.0);
        statsQuiz.put("moyenneScore", Math.round(moyenneScore * 10.0) / 10.0);
        statsQuiz.put("partiesReussies", totalPartiesReussies);
        statsQuiz.put("tauxReussite", totalParties > 0 ? 
            Math.round((totalPartiesReussies * 100.0 / totalParties) * 10.0) / 10.0 : 0.0);

        // 7. Statistiques Cours
        Map<String, Object> statsCours = new LinkedHashMap<>();
        int totalCertificats = certificatDAO.compterParUtilisateur(utilisateurId);

        statsCours.put("totalCertificats", totalCertificats);

        // 8. Dernières parties (10 max)
        List<Partie> dernieresParties = partieDAO.historiqueJoueur(utilisateurId);
        if (dernieresParties != null && dernieresParties.size() > 10) {
            dernieresParties = dernieresParties.subList(0, 10);
        }

        // 9. Derniers certificats (5 max)
        java.util.List<com.amijayquiz.models.Certificat> certificats = certificatDAO.listerParUtilisateur(utilisateurId);
        if (certificats != null && certificats.size() > 5) {
            certificats = certificats.subList(0, 5);
        }

        // 10. Déterminer les actions possibles
        boolean peutActiver = !utilisateur.isActif();
        boolean peutDesactiver = utilisateur.isActif() && utilisateurId != adminId;
        boolean peutPromouvoir = "joueur".equals(utilisateur.getRole()) && utilisateurId != adminId;
        boolean peutRetrograder = "admin".equals(utilisateur.getRole()) && utilisateurId != adminId;
        boolean peutReinitialiserMDP = utilisateur.getEmail() != null && !utilisateur.getEmail().isEmpty();

        // 11. Message de succès/erreur
        String messageSucces = (String) session.getAttribute("messageSucces");
        String messageErreur = (String) session.getAttribute("messageErreur");
        if (messageSucces != null) session.removeAttribute("messageSucces");
        if (messageErreur != null) session.removeAttribute("messageErreur");

        // 12. Passage des attributs
        request.setAttribute("pageTitle", "Détail - " + utilisateur.getPseudo() + " - Administration");
        request.setAttribute("utilisateur", utilisateur);
        request.setAttribute("estPremium", estPremium);
        request.setAttribute("infosPremium", infosPremium);
        request.setAttribute("statsQuiz", statsQuiz);
        request.setAttribute("statsCours", statsCours);
        request.setAttribute("dernieresParties", dernieresParties);
        request.setAttribute("certificats", certificats);
        request.setAttribute("peutActiver", peutActiver);
        request.setAttribute("peutDesactiver", peutDesactiver);
        request.setAttribute("peutPromouvoir", peutPromouvoir);
        request.setAttribute("peutRetrograder", peutRetrograder);
        request.setAttribute("peutReinitialiserMDP", peutReinitialiserMDP);
        request.setAttribute("adminId", adminId);
        request.setAttribute("messageSucces", messageSucces);
        request.setAttribute("messageErreur", messageErreur);

        // 13. Affichage
        request.getRequestDispatcher("/views/admin/admin-utilisateur-detail.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}