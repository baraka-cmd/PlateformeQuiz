package com.learnpath.controllers;

import com.learnpath.dao.CertificatDAO;
import com.learnpath.dao.CoursDAO;
import com.learnpath.dao.PartieDAO;
import com.learnpath.dao.ProgressionCoursDAO;
import com.learnpath.dao.ProgressionJoueurDAO;
import com.learnpath.dao.QuizDAO;
import com.learnpath.models.Certificat;
import com.learnpath.models.Cours;
import com.learnpath.models.Partie;
import com.learnpath.models.Quiz;
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

@WebServlet(name = "HistoriqueServlet", urlPatterns = {"/joueur/historique"})
public class HistoriqueServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
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
        
        int utilisateurId = utilisateur.getId();
        
        PartieDAO partieDAO = new PartieDAO();
        ProgressionJoueurDAO progressionDAO = new ProgressionJoueurDAO();
        QuizDAO quizDAO = new QuizDAO();
        ProgressionCoursDAO progressionCoursDAO = new ProgressionCoursDAO();
        CoursDAO coursDAO = new CoursDAO();
        CertificatDAO certificatDAO = new CertificatDAO();
        
        // ============================================================
        // PARTIE 1 : DONNÉES QUIZ
        // ============================================================
        
        String filtreTheme = request.getParameter("theme");
        String filtreNiveau = request.getParameter("niveau");
        String filtreReussite = request.getParameter("reussite");
        
        List<Partie> toutesLesParties = partieDAO.historiqueJoueur(utilisateurId);
        
        // Appliquer les filtres
        List<Partie> partiesFiltrees = new ArrayList<>();
        for (Partie partie : toutesLesParties) {
            boolean garder = true;
            
            if (filtreTheme != null && !filtreTheme.isEmpty() && !"tous".equals(filtreTheme)) {
                String titreQuiz = partie.getTitreQuiz();
                if (titreQuiz != null) {
                    String categorie = titreQuiz.contains(" - ") ? 
                        titreQuiz.substring(0, titreQuiz.indexOf(" - ")) : titreQuiz;
                    if (!categorie.equalsIgnoreCase(filtreTheme)) {
                        garder = false;
                    }
                }
            }
            
            if (filtreNiveau != null && !filtreNiveau.isEmpty() && !"tous".equals(filtreNiveau)) {
                String titreQuiz = partie.getTitreQuiz();
                if (titreQuiz != null) {
                    String niveauFormate = "";
                    if ("elementaire".equals(filtreNiveau)) niveauFormate = "Elementaire";
                    else if ("moyen".equals(filtreNiveau)) niveauFormate = "Moyen";
                    else if ("avance".equals(filtreNiveau)) niveauFormate = "Avance";
                    
                    if (!titreQuiz.toLowerCase().contains(filtreNiveau) && 
                        !titreQuiz.contains(niveauFormate)) {
                        garder = false;
                    }
                }
            }
            
            if (filtreReussite != null && !filtreReussite.isEmpty() && !"tous".equals(filtreReussite)) {
                double pourcentage = partie.getPourcentage();
                if ("reussies".equals(filtreReussite) && pourcentage < 80) {
                    garder = false;
                } else if ("echouees".equals(filtreReussite) && pourcentage >= 80) {
                    garder = false;
                }
            }
            
            if (garder) {
                partiesFiltrees.add(partie);
            }
        }
        
        // Statistiques Quiz
        int totalParties = toutesLesParties.size();
        int partiesReussies = 0;
        int partiesEchouees = 0;
        double sommeScores = 0;
        int sommeTemps = 0;
        int meilleurScoreGlobal = 0;
        String meilleurQuiz = "";
        
        for (Partie partie : toutesLesParties) {
            double pourcentage = partie.getPourcentage();
            sommeScores += pourcentage;
            sommeTemps += partie.getTempsRealiseSecondes();
            
            if (pourcentage >= 80) {
                partiesReussies++;
            } else {
                partiesEchouees++;
            }
            
            if (pourcentage > meilleurScoreGlobal) {
                meilleurScoreGlobal = (int) pourcentage;
                meilleurQuiz = partie.getTitreQuiz();
            }
        }
        
        double tauxReussite = totalParties > 0 ? (double) partiesReussies / totalParties * 100 : 0;
        double moyenneGenerale = totalParties > 0 ? sommeScores / totalParties : 0;
        int tempsTotalMinutes = sommeTemps / 60;
        
        // Thèmes pour le filtre
        List<Quiz> tousLesQuiz = quizDAO.listerQuizDisponibles();
        List<String> themes = new ArrayList<>();
        for (Quiz quiz : tousLesQuiz) {
            String titre = quiz.getTitre();
            if (titre != null && titre.contains(" - ")) {
                String theme = titre.substring(0, titre.indexOf(" - "));
                if (!themes.contains(theme)) {
                    themes.add(theme);
                }
            }
        }
        
        // Pagination Quiz
        int page = 1;
        int partiesParPage = 20;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                page = Integer.parseInt(pageParam);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        
        int totalPartiesFiltrees = partiesFiltrees.size();
        int totalPages = (int) Math.ceil((double) totalPartiesFiltrees / partiesParPage);
        if (page > totalPages) page = totalPages;
        if (page < 1) page = 1;
        
        int debut = (page - 1) * partiesParPage;
        int fin = Math.min(debut + partiesParPage, totalPartiesFiltrees);
        
        List<Partie> partiesPage = new ArrayList<>();
        if (totalPartiesFiltrees > 0 && debut < totalPartiesFiltrees) {
            partiesPage = partiesFiltrees.subList(debut, fin);
        }
        
        // ============================================================
        // PARTIE 2 : DONNÉES COURS
        // ============================================================
        
        List<Cours> tousLesCours = coursDAO.listerPubliés();
        List<Map<String, Object>> progressionCours = new ArrayList<>();
        int totalCoursCommences = 0;
        int totalCoursTermines = 0;
        int totalLeconsTermineesHisto = 0;
        
        if (tousLesCours != null) {
            for (Cours cours : tousLesCours) {
                int leconsTerminees = progressionCoursDAO.getNombreLeconsTerminees(utilisateurId, cours.getId());
                
                if (leconsTerminees > 0) {
                    totalCoursCommences++;
                    
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("titre", cours.getTitre());
                    data.put("theme", cours.getTheme());
                    data.put("niveau", cours.getNiveauFormate());
                    data.put("coursId", cours.getId());
                    
                    int totalLecons = progressionCoursDAO.getNombreTotalLecons(cours.getId());
                    int pourcentage = totalLecons > 0 ? (leconsTerminees * 100 / totalLecons) : 0;
                    data.put("pourcentage", pourcentage);
                    
                    boolean estTermine = progressionCoursDAO.estCoursTermine(utilisateurId, cours.getId());
                    data.put("termine", estTermine);
                    data.put("aCertificat", certificatDAO.trouverParUtilisateurEtCours(utilisateurId, cours.getId()) != null);
                    
                    progressionCours.add(data);
                    totalLeconsTermineesHisto += leconsTerminees;
                    
                    if (estTermine) totalCoursTermines++;
                }
            }
        }
        
        // Certificats récents
        List<Certificat> certificats = certificatDAO.listerParUtilisateur(utilisateurId);
        List<Map<String, Object>> certificatsRecents = new ArrayList<>();
        if (certificats != null) {
            for (Certificat cert : certificats) {
                Map<String, Object> certData = new LinkedHashMap<>();
                certData.put("certificat", cert);
                Cours c = coursDAO.trouverParId(cert.getCoursId());
                certData.put("titreCours", c != null ? c.getTitre() : "Cours");
                certificatsRecents.add(certData);
            }
        }
        
        int totalCertificatsObtenus = certificats != null ? certificats.size() : 0;
        
        // ============================================================
        // PARTIE 3 : ATTRIBUTS POUR LA JSP
        // ============================================================
        
        // Quiz
        request.setAttribute("parties", partiesPage);
        request.setAttribute("totalParties", totalParties);
        request.setAttribute("partiesReussies", partiesReussies);
        request.setAttribute("partiesEchouees", partiesEchouees);
        request.setAttribute("tauxReussite", tauxReussite);
        request.setAttribute("moyenneGenerale", moyenneGenerale);
        request.setAttribute("meilleurScoreGlobal", meilleurScoreGlobal);
        request.setAttribute("meilleurQuiz", meilleurQuiz);
        request.setAttribute("tempsTotalMinutes", tempsTotalMinutes);
        request.setAttribute("themes", themes);
        request.setAttribute("pageActuelle", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalPartiesFiltrees", totalPartiesFiltrees);
        request.setAttribute("filtreTheme", filtreTheme);
        request.setAttribute("filtreNiveau", filtreNiveau);
        request.setAttribute("filtreReussite", filtreReussite);
        
        // Cours
        request.setAttribute("progressionCours", progressionCours);
        request.setAttribute("totalCoursCommences", totalCoursCommences);
        request.setAttribute("totalCoursTermines", totalCoursTermines);
        request.setAttribute("totalLeconsTerminees", totalLeconsTermineesHisto);
        request.setAttribute("totalCertificatsObtenus", totalCertificatsObtenus);
        request.setAttribute("certificatsRecents", certificatsRecents);
        
        request.setAttribute("pageTitle", "Historique complet - LearnPath");
        
        request.getRequestDispatcher("/views/joueur/historique.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}