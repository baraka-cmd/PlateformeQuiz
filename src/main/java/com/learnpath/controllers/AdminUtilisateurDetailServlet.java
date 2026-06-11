// ============================================================
// FICHIER : src/main/java/com/learnpath/controllers/AdminUtilisateurDetailServlet.java
// RÔLE    : Contrôleur pour le détail complet d'un utilisateur
//           GET  : Affiche la fiche détaillée avec toutes les stats
//           SÉCURISÉ : Seul le Super Admin peut accéder
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.AccessPremiumDAO;
import com.learnpath.dao.CertificatDAO;
import com.learnpath.dao.CoursDAO;
import com.learnpath.dao.PartieDAO;
import com.learnpath.dao.ProgressionCoursDAO;
import com.learnpath.dao.ProgressionJoueurDAO;
import com.learnpath.dao.UtilisateurDAO;
import com.learnpath.models.Cours;
import com.learnpath.models.Partie;
import com.learnpath.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AdminUtilisateurDetailServlet", urlPatterns = {"/admin/utilisateurs/detail"})
public class AdminUtilisateurDetailServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        Utilisateur adminConnecte = (Utilisateur) session.getAttribute("utilisateur");

        if (!"admin".equals(adminConnecte.getRole())) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        int adminId = adminConnecte.getId();

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

        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        Utilisateur utilisateur = utilisateurDAO.trouverParId(utilisateurId);

        if (utilisateur == null) {
            session.setAttribute("messageErreur", "Cet utilisateur n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/utilisateurs");
            return;
        }

        AccessPremiumDAO premiumDAO = new AccessPremiumDAO();
        PartieDAO partieDAO = new PartieDAO();
        CertificatDAO certificatDAO = new CertificatDAO();
        ProgressionJoueurDAO progressionJoueurDAO = new ProgressionJoueurDAO();
        ProgressionCoursDAO progressionCoursDAO = new ProgressionCoursDAO();
        CoursDAO coursDAO = new CoursDAO();

        // Statut Premium
        boolean estPremium = premiumDAO.estPremium(utilisateurId);
        Map<String, Object> infosPremium = premiumDAO.getInfosPremium(utilisateurId);

        // ===== STATISTIQUES QUIZ =====
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

        // ===== STATISTIQUES COURS =====
        Map<String, Object> statsCours = new LinkedHashMap<>();
        
        List<Cours> tousLesCours = coursDAO.listerPubliés();
        int totalCoursCommences = 0;
        int totalCoursTermines = 0;
        int totalLeconsTerminees = 0;

        if (tousLesCours != null) {
            for (Cours cours : tousLesCours) {
                int leconsTermineesCours = progressionCoursDAO.getNombreLeconsTerminees(utilisateurId, cours.getId());
                
                if (leconsTermineesCours > 0) {
                    totalCoursCommences++;
                    totalLeconsTerminees += leconsTermineesCours;
                    
                    if (progressionCoursDAO.estCoursTermine(utilisateurId, cours.getId())) {
                        totalCoursTermines++;
                    }
                }
            }
        }

        int totalCertificats = certificatDAO.compterParUtilisateur(utilisateurId);

        statsCours.put("totalCoursCommences", totalCoursCommences);
        statsCours.put("totalCoursTermines", totalCoursTermines);
        statsCours.put("totalLeconsTerminees", totalLeconsTerminees);
        statsCours.put("totalCertificats", totalCertificats);

        
        // Progression détaillée des cours pour l'affichage
        List<Map<String, Object>> progressionCoursDetail = new ArrayList<>();
        if (tousLesCours != null) {
            for (Cours cours : tousLesCours) {
                int leconsTermineesCours = progressionCoursDAO.getNombreLeconsTerminees(utilisateurId, cours.getId());

                if (leconsTermineesCours > 0) {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("titre", cours.getTitre());
                    data.put("theme", cours.getTheme());
                    data.put("coursId", cours.getId());
                    int totalLeconsCours = progressionCoursDAO.getNombreTotalLecons(cours.getId());
                    data.put("pourcentage", totalLeconsCours > 0 ? (leconsTermineesCours * 100 / totalLeconsCours) : 0);
                    data.put("termine", progressionCoursDAO.estCoursTermine(utilisateurId, cours.getId()));
                    progressionCoursDetail.add(data);
                }
            }
        }
        request.setAttribute("progressionCoursDetail", progressionCoursDetail);


        // Dernières parties (10 max)
        List<Partie> dernieresParties = partieDAO.historiqueJoueur(utilisateurId);
        if (dernieresParties != null && dernieresParties.size() > 10) {
            dernieresParties = dernieresParties.subList(0, 10);
        }

        // Derniers certificats (5 max)
        List<com.learnpath.models.Certificat> certificats = certificatDAO.listerParUtilisateur(utilisateurId);
        if (certificats != null && certificats.size() > 5) {
            certificats = certificats.subList(0, 5);
        }

        // Actions possibles
        boolean estSuperAdmin = "admin".equals(utilisateur.getRole());
        boolean estGestionnaire = "gestionnaire".equals(utilisateur.getRole());
        boolean estJoueur = "joueur".equals(utilisateur.getRole());
        boolean estMoi = utilisateurId == adminId;

        boolean peutActiver = !utilisateur.isActif() && !estSuperAdmin;
        boolean peutDesactiver = utilisateur.isActif() && !estMoi && !estSuperAdmin;
        boolean peutPromouvoir = estJoueur && !estMoi;
        boolean peutRetrograder = estGestionnaire && !estMoi;
        boolean peutReinitialiserMDP = utilisateur.getEmail() != null && !utilisateur.getEmail().isEmpty();

        // Messages flash
        String messageSucces = (String) session.getAttribute("messageSucces");
        String messageErreur = (String) session.getAttribute("messageErreur");
        if (messageSucces != null) session.removeAttribute("messageSucces");
        if (messageErreur != null) session.removeAttribute("messageErreur");

        // Passage des attributs
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
        request.setAttribute("estSuperAdmin", estSuperAdmin);
        request.setAttribute("estGestionnaire", estGestionnaire);
        request.setAttribute("adminId", adminId);
        request.setAttribute("messageSucces", messageSucces);
        request.setAttribute("messageErreur", messageErreur);

        request.getRequestDispatcher("/views/admin/admin-utilisateur-detail.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}