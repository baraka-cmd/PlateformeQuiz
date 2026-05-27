package com.amijayquiz.controllers;

import com.amijayquiz.dao.PartieDAO;
import com.amijayquiz.dao.ProgressionJoueurDAO;
import com.amijayquiz.dao.QuizDAO;
import com.amijayquiz.models.Partie;
import com.amijayquiz.models.Quiz;
import com.amijayquiz.models.Utilisateur;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
        
        PartieDAO partieDAO = new PartieDAO();
        ProgressionJoueurDAO progressionDAO = new ProgressionJoueurDAO();
        QuizDAO quizDAO = new QuizDAO();
        
        // Récupérer le filtre éventuel
        String filtreTheme = request.getParameter("theme");
        String filtreNiveau = request.getParameter("niveau");
        String filtreReussite = request.getParameter("reussite");
        
        // Récupérer toutes les parties du joueur
        List<Partie> toutesLesParties = partieDAO.historiqueJoueur(utilisateur.getId());
        
        // Appliquer les filtres si présents
        List<Partie> partiesFiltrees = new ArrayList<>();
        for (Partie partie : toutesLesParties) {
            boolean garder = true;
            
            // Filtre par thème
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
            
            // Filtre par niveau
            if (filtreNiveau != null && !filtreNiveau.isEmpty() && !"tous".equals(filtreNiveau)) {
                // On vérifie si le titre contient le niveau
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
            
            // Filtre par réussite
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
        
        // Statistiques globales
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
        
        // Récupérer la liste des thèmes pour le filtre
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
        
        // Pagination
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
        
        // Attributs pour la JSP
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
        
        request.getRequestDispatcher("/views/joueur/historique.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}