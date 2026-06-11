// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/AdminQuizServlet.java
// RÔLE    : Contrôleur administrateur pour la gestion des quiz
//           - Liste paginée avec filtres et recherche
//           - Statistiques par thème et niveau
//           - Regroupement des quiz par thème
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.QuizDAO;
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
import java.util.stream.Collectors;

/**
 * AdminQuizServlet - Gestion des quiz par l'administrateur
 * 
 * GET  : Affiche la liste paginée des quiz avec filtres,
 *        recherche, statistiques et regroupement par thème.
 * 
 * URL : /admin/quiz
 * 
 * @author LearnPath
 * @version 2.0
 */
@WebServlet(name = "AdminQuizServlet", urlPatterns = {"/admin/quiz"})
public class AdminQuizServlet extends HttpServlet {

    // Nombre de quiz par page
    private static final int QUIZ_PAR_PAGE = 10;

    // ============================================================
    // DOGET : AFFICHAGE DE LA LISTE DES QUIZ
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
        if (!"admin".equals(utilisateur.getRole()) && !"gestionnaire".equals(utilisateur.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        // 2. Récupération des paramètres
        String recherche = request.getParameter("recherche");
        String filtreNiveau = request.getParameter("niveau");
        String filtreTheme = request.getParameter("theme");
        String pageStr = request.getParameter("page");
        String tri = request.getParameter("tri");

        // Valeurs par défaut
        if (filtreNiveau == null) filtreNiveau = "tous";
        if (filtreTheme == null) filtreTheme = "tous";
        int page = 1;
        try {
            if (pageStr != null && !pageStr.isEmpty()) {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        // 3. Initialisation du DAO
        QuizDAO quizDAO = new QuizDAO();

        // 4. Récupération de tous les quiz
        List<Quiz> tousLesQuiz = quizDAO.listerQuizDisponibles();

        // 5. Extraction des thèmes (depuis le titre)
        List<String> tousLesThemes = extraireThemes(tousLesQuiz);

        // 6. Regroupement des quiz par thème et niveau
        Map<String, Map<String, List<Quiz>>> quizParTheme = regrouperParTheme(tousLesQuiz);

        // 7. Application des filtres
            List<Quiz> quizFiltres = new ArrayList<>(tousLesQuiz);

            // Filtre par niveau
            if (!"tous".equals(filtreNiveau)) {
                final String filtreNiveauFinal = filtreNiveau;
                quizFiltres = quizFiltres.stream()
                    .filter(q -> filtreNiveauFinal.equals(q.getNiveau()))
                    .collect(Collectors.toList());
            }

            // Filtre par thème
            if (!"tous".equals(filtreTheme)) {
                final String filtreThemeFinal = filtreTheme;
                quizFiltres = quizFiltres.stream()
                    .filter(q -> extraireTheme(q.getTitre()).equals(filtreThemeFinal))
                    .collect(Collectors.toList());
            }

            // Recherche textuelle
            if (recherche != null && !recherche.trim().isEmpty()) {
                final String filtreRecherche = recherche.toLowerCase().trim();
                quizFiltres = quizFiltres.stream()
                    .filter(q -> 
                        (q.getTitre() != null && q.getTitre().toLowerCase().contains(filtreRecherche)) ||
                        (q.getDescription() != null && q.getDescription().toLowerCase().contains(filtreRecherche))
                    )
                    .collect(Collectors.toList());
            }

        // Recherche textuelle
        if (recherche != null && !recherche.trim().isEmpty()) {
            String filtre = recherche.toLowerCase().trim();
            quizFiltres = quizFiltres.stream()
                .filter(q -> 
                    (q.getTitre() != null && q.getTitre().toLowerCase().contains(filtre)) ||
                    (q.getDescription() != null && q.getDescription().toLowerCase().contains(filtre))
                )
                .collect(Collectors.toList());
        }

        // 8. Tri
        if ("titre".equals(tri)) {
            quizFiltres.sort((a, b) -> a.getTitre().compareToIgnoreCase(b.getTitre()));
        } else if ("niveau".equals(tri)) {
            quizFiltres.sort((a, b) -> a.getNiveau().compareTo(b.getNiveau()));
        } else if ("questions".equals(tri)) {
            quizFiltres.sort((a, b) -> Integer.compare(b.getNombreQuestions(), a.getNombreQuestions()));
        }
        // Par défaut : tri par ID

        // 9. Pagination
        int totalQuiz = quizFiltres.size();
        int totalPages = (int) Math.ceil((double) totalQuiz / QUIZ_PAR_PAGE);
        if (page > totalPages && totalPages > 0) page = totalPages;

        int debut = (page - 1) * QUIZ_PAR_PAGE;
        int fin = Math.min(debut + QUIZ_PAR_PAGE, totalQuiz);
        List<Quiz> quizPage = quizFiltres.subList(debut, fin);

        // 10. Calcul des statistiques
        Map<String, Integer> stats = calculerStatistiques(tousLesQuiz);

        // 11. Message de succès/erreur
        String messageSucces = (String) session.getAttribute("messageSucces");
        String messageErreur = (String) session.getAttribute("messageErreur");
        if (messageSucces != null) session.removeAttribute("messageSucces");
        if (messageErreur != null) session.removeAttribute("messageErreur");

        // 12. Construction de l'URL de pagination
        StringBuilder urlPagination = new StringBuilder("/admin/quiz?");
        if (recherche != null && !recherche.isEmpty()) urlPagination.append("recherche=").append(recherche).append("&");
        if (!"tous".equals(filtreNiveau)) urlPagination.append("niveau=").append(filtreNiveau).append("&");
        if (!"tous".equals(filtreTheme)) urlPagination.append("theme=").append(filtreTheme).append("&");
        if (tri != null && !tri.isEmpty()) urlPagination.append("tri=").append(tri).append("&");

        // 13. Passage des attributs à la JSP
        request.setAttribute("pageTitle", "Gestion des quiz - Administration - LearnPath");
        request.setAttribute("quizPage", quizPage);
        request.setAttribute("totalQuiz", totalQuiz);
        request.setAttribute("totalQuizGlobal", tousLesQuiz.size());
        request.setAttribute("page", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("recherche", recherche);
        request.setAttribute("filtreNiveau", filtreNiveau);
        request.setAttribute("filtreTheme", filtreTheme);
        request.setAttribute("tri", tri != null ? tri : "id");
        request.setAttribute("tousLesThemes", tousLesThemes);
        request.setAttribute("quizParTheme", quizParTheme);
        request.setAttribute("stats", stats);
        request.setAttribute("urlPagination", urlPagination.toString());
        request.setAttribute("messageSucces", messageSucces);
        request.setAttribute("messageErreur", messageErreur);

        // 14. Affichage
        request.getRequestDispatcher("/views/admin/admin-quiz.jsp").forward(request, response);
    }

    // ============================================================
    // MÉTHODES PRIVÉES
    // ============================================================

    /**
     * Extrait le thème depuis le titre du quiz
     * Format attendu : "Thème - Partie X"
     * 
     * @param titre Titre complet du quiz
     * @return Le thème extrait, ou le titre complet si pas de séparateur
     */
    private String extraireTheme(String titre) {
        if (titre == null || titre.isEmpty()) return "Sans thème";
        
        // Chercher " - Partie" ou " - partie"
        int indexPartie = titre.lastIndexOf(" - Partie");
        if (indexPartie == -1) indexPartie = titre.lastIndexOf(" - partie");
        
        if (indexPartie > 0) {
            return titre.substring(0, indexPartie).trim();
        }
        
        // Fallback : chercher " - "
        int indexSep = titre.lastIndexOf(" - ");
        if (indexSep > 0) {
            return titre.substring(0, indexSep).trim();
        }
        
        return titre.trim();
    }

    /**
     * Extrait la liste de tous les thèmes distincts
     */
    private List<String> extraireThemes(List<Quiz> quizList) {
        return quizList.stream()
            .map(q -> extraireTheme(q.getTitre()))
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * Regroupe les quiz par thème puis par niveau
     * 
     * @return Map<Thème, Map<Niveau, List<Quiz>>>
     */
    private Map<String, Map<String, List<Quiz>>> regrouperParTheme(List<Quiz> quizList) {
        Map<String, Map<String, List<Quiz>>> groupe = new LinkedHashMap<>();

        for (Quiz quiz : quizList) {
            String theme = extraireTheme(quiz.getTitre());
            String niveau = quiz.getNiveau() != null ? quiz.getNiveau() : "inconnu";

            groupe
                .computeIfAbsent(theme, k -> new LinkedHashMap<>())
                .computeIfAbsent(niveau, k -> new ArrayList<>())
                .add(quiz);
        }

        return groupe;
    }

    /**
     * Calcule les statistiques globales des quiz
     * 
     * @return Map contenant les compteurs
     */
    private Map<String, Integer> calculerStatistiques(List<Quiz> quizList) {
        Map<String, Integer> stats = new LinkedHashMap<>();

        stats.put("total", quizList.size());
        stats.put("themes", extraireThemes(quizList).size());

        int elementaire = 0, moyen = 0, avance = 0;
        for (Quiz q : quizList) {
            if ("elementaire".equals(q.getNiveau())) elementaire++;
            else if ("moyen".equals(q.getNiveau())) moyen++;
            else if ("avance".equals(q.getNiveau())) avance++;
        }

        stats.put("elementaire", elementaire);
        stats.put("moyen", moyen);
        stats.put("avance", avance);

        return stats;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}