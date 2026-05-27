// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/CertificatServlet.java
// RÔLE    : Contrôleur des certificats de réussite
//           Affiche le certificat + vérification publique
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.CertificatDAO;
import com.amijayquiz.dao.CoursDAO;
import com.amijayquiz.dao.UtilisateurDAO;
import com.amijayquiz.models.Certificat;
import com.amijayquiz.models.Cours;
import com.amijayquiz.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * CertificatServlet - Gestion des certificats de réussite
 * 
 * Deux modes de fonctionnement :
 * 1. Mode connecté : Affiche le certificat d'un utilisateur pour un cours
 * 2. Mode public : Vérifie l'authenticité d'un certificat via son code
 * 
 * URL (connecté) : /joueur/cours/certificat?coursId=1
 * URL (public)   : /verifier-certificat?code=AMJ-XXXX-XXXX-XXXX
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "CertificatServlet", urlPatterns = {"/joueur/cours/certificat", "/verifier-certificat"})
public class CertificatServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();

        // Déterminer le mode : vérification publique ou certificat personnel
        if (requestURI.contains("/verifier-certificat")) {
            // MODE PUBLIC : Vérification par code
            traiterVerificationPublique(request, response);
        } else {
            // MODE CONNECTÉ : Certificat personnel
            traiterCertificatPersonnel(request, response);
        }
    }

    /**
     * Mode public : Vérification d'un certificat par son code unique
     * Accessible sans connexion
     */
    private void traiterVerificationPublique(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String code = request.getParameter("code");

        // 1. Validation du code
        if (code == null || code.trim().isEmpty()) {
            request.setAttribute("messageErreur", "Aucun code de vérification fourni.");
            request.getRequestDispatcher("/views/verifier-certificat.jsp").forward(request, response);
            return;
        }

        code = code.trim().toUpperCase();

        // 2. Recherche du certificat
        CertificatDAO certificatDAO = new CertificatDAO();
        Certificat certificat = certificatDAO.trouverParCode(code);

        if (certificat == null) {
            request.setAttribute("messageErreur", 
                "❌ Aucun certificat trouvé avec ce code. Vérifiez le code et réessayez.");
            request.setAttribute("codeRecherche", code);
            request.getRequestDispatcher("/views/verifier-certificat.jsp").forward(request, response);
            return;
        }

        // 3. Récupération des infos associées
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        Utilisateur utilisateur = utilisateurDAO.trouverParId(certificat.getUtilisateurId());
        
        CoursDAO coursDAO = new CoursDAO();
        Cours cours = coursDAO.trouverParId(certificat.getCoursId());

        // 4. Passage des attributs
        request.setAttribute("pageTitle", "Vérification Certificat - AmijayQuiz");
        request.setAttribute("certificat", certificat);
        request.setAttribute("utilisateur", utilisateur);
        request.setAttribute("cours", cours);
        request.setAttribute("estVerification", true);

        // 5. Affichage
        request.getRequestDispatcher("/views/certificat-public.jsp").forward(request, response);
    }

    /**
     * Mode connecté : Affiche le certificat personnel de l'utilisateur
     */
    private void traiterCertificatPersonnel(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Vérification de la session et du rôle
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (!"joueur".equals(utilisateur.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        // 2. Récupération du paramètre coursId
        String coursIdParam = request.getParameter("coursId");
        if (coursIdParam == null || coursIdParam.trim().isEmpty()) {
            // Pas de coursId : afficher la liste des certificats de l'utilisateur
            traiterListeCertificats(request, response, utilisateur);
            return;
        }

        int coursId;
        try {
            coursId = Integer.parseInt(coursIdParam);
        } catch (NumberFormatException e) {
            traiterListeCertificats(request, response, utilisateur);
            return;
        }

        // 3. Recherche du certificat
        CertificatDAO certificatDAO = new CertificatDAO();
        Certificat certificat = certificatDAO.trouverParUtilisateurEtCours(utilisateur.getId(), coursId);

        if (certificat == null) {
            session.setAttribute("messageErreur", 
                "Vous n'avez pas encore obtenu de certificat pour ce cours. " +
                "Terminez tous les chapitres et réussissez l'examen final !");
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        // 4. Récupération des infos du cours
        CoursDAO coursDAO = new CoursDAO();
        Cours cours = coursDAO.trouverParId(coursId);

        // 5. Passage des attributs
        request.setAttribute("pageTitle", "Mon Certificat - " + 
            (cours != null ? cours.getTitre() : "Cours") + " - AmijayQuiz");
        request.setAttribute("certificat", certificat);
        request.setAttribute("utilisateur", utilisateur);
        request.setAttribute("cours", cours);
        request.setAttribute("estVerification", false);

        // 6. Affichage
        request.getRequestDispatcher("/views/joueur/cours-certificat.jsp").forward(request, response);
    }

    /**
     * Affiche la liste de tous les certificats de l'utilisateur
     */
    private void traiterListeCertificats(HttpServletRequest request, HttpServletResponse response,
                                          Utilisateur utilisateur)
            throws ServletException, IOException {

        CertificatDAO certificatDAO = new CertificatDAO();
        CoursDAO coursDAO = new CoursDAO();

        // Récupérer tous les certificats
        java.util.List<Certificat> certificats = certificatDAO.listerParUtilisateur(utilisateur.getId());

        // Charger les infos de cours pour chaque certificat
        java.util.List<java.util.Map<String, Object>> certificatsAvecCours = new java.util.ArrayList<>();
        
        for (Certificat cert : certificats) {
            java.util.Map<String, Object> data = new java.util.LinkedHashMap<>();
            data.put("certificat", cert);
            Cours cours = coursDAO.trouverParId(cert.getCoursId());
            data.put("cours", cours);
            certificatsAvecCours.add(data);
        }

        int totalCertificats = certificats.size();
        int totalCoursDisponibles = coursDAO.compterTous();

        request.setAttribute("pageTitle", "Mes Certificats - AmijayQuiz");
        request.setAttribute("certificats", certificatsAvecCours);
        request.setAttribute("totalCertificats", totalCertificats);
        request.setAttribute("totalCoursDisponibles", totalCoursDisponibles);
        request.setAttribute("utilisateur", utilisateur);

        request.getRequestDispatcher("/views/joueur/cours-mes-certificats.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}