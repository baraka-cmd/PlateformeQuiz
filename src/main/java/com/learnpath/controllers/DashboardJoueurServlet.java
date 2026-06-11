package com.learnpath.controllers;

import com.learnpath.dao.CertificatDAO;
import com.learnpath.dao.CoursDAO;
import com.learnpath.dao.PartieDAO;
import com.learnpath.dao.ProgressionCoursDAO;
import com.learnpath.dao.ProgressionJoueurDAO;
import com.learnpath.dao.QuizDAO;
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
import java.util.*;

/**
 * DashboardJoueurServlet - Tableau de bord unifié du joueur
 * 
 * Affiche la liste de tous les thèmes disponibles avec leur progression
 * pour les Quiz ET les Cours.
 * Chaque thème montre le pourcentage global et le niveau actuel du joueur.
 * Inclut les statistiques globales, les certificats et l'historique rapide.
 * 
 * La navigation vers les niveaux se fait via ThemeServlet et NiveauServlet.
 * La navigation vers les cours se fait via ThemeCoursServlet.
 * 
 * URL : /joueur/dashboard
 * 
 * @author LearnPath
 * @version 3.0
 */
@WebServlet(name = "DashboardJoueurServlet", urlPatterns = {"/joueur/dashboard"})
public class DashboardJoueurServlet extends HttpServlet {

    // Noms des niveaux dans l'ordre
    private static final String[] NIVEAUX = {"elementaire", "moyen", "avance"};

    // ===== CONFIGURATION DES THÈMES QUIZ =====
    private static final Map<String, String[]> THEMES_QUIZ_CONFIG = new LinkedHashMap<>();

    static {
        THEMES_QUIZ_CONFIG.put("Informatique Générale", new String[]{"fas fa-laptop", "#667eea"});
        THEMES_QUIZ_CONFIG.put("Réseaux", new String[]{"fas fa-network-wired", "#f093fb"});
        THEMES_QUIZ_CONFIG.put("Programmation", new String[]{"fas fa-code", "#4facfe"});
        THEMES_QUIZ_CONFIG.put("Base de données", new String[]{"fas fa-database", "#43e97b"});
        THEMES_QUIZ_CONFIG.put("Sécurité", new String[]{"fas fa-shield-alt", "#f5576c"});
        THEMES_QUIZ_CONFIG.put("Web", new String[]{"fas fa-globe", "#f39c12"});
    }

    // ===== CONFIGURATION DES THÈMES COURS =====
    private static final Map<String, String[]> THEMES_COURS_CONFIG = new LinkedHashMap<>();

    static {
        THEMES_COURS_CONFIG.put("Développement Web",       new String[]{"fas fa-globe",         "#667eea"});
        THEMES_COURS_CONFIG.put("Langage Java",            new String[]{"fas fa-coffee",        "#f093fb"});
        THEMES_COURS_CONFIG.put("Langage Python",          new String[]{"fab fa-python",        "#4facfe"});
        THEMES_COURS_CONFIG.put("Langage PHP",             new String[]{"fab fa-php",           "#8892bf"});
        THEMES_COURS_CONFIG.put("Bases de Données & SQL",  new String[]{"fas fa-database",      "#43e97b"});
        THEMES_COURS_CONFIG.put("Cybersécurité",           new String[]{"fas fa-shield-alt",    "#f5576c"});
        THEMES_COURS_CONFIG.put("Développement Mobile",    new String[]{"fas fa-mobile-alt",    "#f39c12"});
        THEMES_COURS_CONFIG.put("Intelligence Artificielle",new String[]{"fas fa-robot",        "#9b59b6"});
        THEMES_COURS_CONFIG.put("Design UI/UX",            new String[]{"fas fa-palette",       "#e91e63"});
        THEMES_COURS_CONFIG.put("Réseaux Informatiques",   new String[]{"fas fa-network-wired", "#00bcd4"});
        THEMES_COURS_CONFIG.put("Systèmes d''Exploitation",new String[]{"fab fa-linux",         "#ff9800"});
        THEMES_COURS_CONFIG.put("Data Science",            new String[]{"fas fa-chart-pie",     "#795548"});
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
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

        int joueurId = utilisateur.getId();

        // ============================================================
        // PARTIE 1 : DONNÉES QUIZ
        // ============================================================
        QuizDAO quizDAO = new QuizDAO();
        PartieDAO partieDAO = new PartieDAO();
        ProgressionJoueurDAO progressionQuizDAO = new ProgressionJoueurDAO();

        // Récupération des quiz organisés par thème et niveau
        Map<String, Map<String, List<Quiz>>> quizOrganises = quizDAO.listerQuizOrganises();

        // Construction de la liste des thèmes Quiz avec progression
        List<Map<String, Object>> themesQuizList = new ArrayList<>();

        if (quizOrganises != null && !quizOrganises.isEmpty()) {
            for (Map.Entry<String, Map<String, List<Quiz>>> entryTheme : quizOrganises.entrySet()) {
                String nomTheme = entryTheme.getKey();
                Map<String, List<Quiz>> niveauxMap = entryTheme.getValue();

                // Récupérer la config du thème (icône, couleur)
                String[] config = THEMES_QUIZ_CONFIG.getOrDefault(nomTheme,
                        new String[]{"fas fa-book", "#667eea"});

                Map<String, Object> themeData = new LinkedHashMap<>();
                themeData.put("nom", nomTheme);
                themeData.put("icone", config[0]);
                themeData.put("couleur", config[1]);

                // Calculer la progression globale du thème
                int totalPartiesTheme = 0;
                int partiesReussiesTheme = 0;

                // Construire les données des 3 niveaux
                List<Map<String, Object>> niveauxList = new ArrayList<>();

                for (String niveauStr : NIVEAUX) {
                    List<Quiz> quizDuNiveau = niveauxMap.getOrDefault(niveauStr, new ArrayList<>());
                    int totalParties = quizDuNiveau.size();
                    totalPartiesTheme += totalParties;

                    Map<String, Object> niveauData = construireResumeNiveau(
                            joueurId, nomTheme, niveauStr, totalParties,
                            progressionQuizDAO, quizDAO
                    );
                    partiesReussiesTheme += (int) niveauData.get("partiesReussies");
                    niveauxList.add(niveauData);
                }

                int pourcentageTheme = (totalPartiesTheme > 0)
                        ? (partiesReussiesTheme * 100 / totalPartiesTheme)
                        : 0;

                themeData.put("totalParties", totalPartiesTheme);
                themeData.put("partiesReussies", partiesReussiesTheme);
                themeData.put("pourcentageProgression", pourcentageTheme);
                themeData.put("niveaux", niveauxList);

                // Déterminer le niveau actif (le premier non terminé)
                String niveauActif = determinerNiveauActif(niveauxList);
                themeData.put("niveauActif", niveauActif);

                themesQuizList.add(themeData);
            }
        }

        // Statistiques Quiz
        int nombreParties = partieDAO.compterPartiesJoueur(joueurId);
        double meilleurScore = partieDAO.meilleurScoreJoueur(joueurId);
        double moyenneScore = partieDAO.moyenneScoreJoueur(joueurId);

        // Compter les niveaux Quiz terminés (tous thèmes confondus)
        int niveauxTermines = 0;
        for (Map<String, Object> theme : themesQuizList) {
            List<Map<String, Object>> niveaux = (List<Map<String, Object>>) theme.get("niveaux");
            for (Map<String, Object> niv : niveaux) {
                if ((Boolean) niv.get("termine")) {
                    niveauxTermines++;
                }
            }
        }

        // Dernières parties Quiz (5 max)
        List<Partie> dernieresParties = partieDAO.historiqueJoueur(joueurId);
        if (dernieresParties != null && dernieresParties.size() > 5) {
            dernieresParties = dernieresParties.subList(0, 5);
        }

        // ============================================================
        // PARTIE 2 : DONNÉES COURS
        // ============================================================
        CoursDAO coursDAO = new CoursDAO();
        ProgressionCoursDAO progressionCoursDAO = new ProgressionCoursDAO();
        CertificatDAO certificatDAO = new CertificatDAO();

        // Récupération de tous les cours publiés
        List<Cours> tousLesCours = coursDAO.listerPubliés();

        // Grouper les cours par thème
        Map<String, List<Cours>> coursParTheme = new LinkedHashMap<>();
        for (Cours cours : tousLesCours) {
            String theme = cours.getTheme();
            if (!coursParTheme.containsKey(theme)) {
                coursParTheme.put(theme, new ArrayList<>());
            }
            coursParTheme.get(theme).add(cours);
        }

        // Construction de la liste des thèmes Cours avec progression
        List<Map<String, Object>> themesCoursList = new ArrayList<>();
        int totalCoursPlateforme = tousLesCours.size();
        int totalCoursTerminesGlobal = 0;
        int totalLeconsPlateforme = 0;
        int totalLeconsTermineesGlobal = 0;

        for (Map.Entry<String, List<Cours>> entry : coursParTheme.entrySet()) {
            String nomTheme = entry.getKey();
            List<Cours> coursDuTheme = entry.getValue();

            // Récupérer la config du thème (icône, couleur)
            String[] config = THEMES_COURS_CONFIG.getOrDefault(nomTheme,
                    new String[]{"fas fa-book", "#667eea"});

            Map<String, Object> themeData = new LinkedHashMap<>();
            themeData.put("nom", nomTheme);
            themeData.put("icone", config[0]);
            themeData.put("couleur", config[1]);

            int totalCoursTheme = coursDuTheme.size();
            int coursTerminesTheme = 0;
            int totalLeconsTheme = 0;
            int leconsTermineesTheme = 0;

            for (Cours cours : coursDuTheme) {
                int totalLeconsCours = cours.getNombreTotalLecons() > 0 ?
                    cours.getNombreTotalLecons() :
                    progressionCoursDAO.getNombreTotalLecons(cours.getId());
                int leconsTermineesCours = progressionCoursDAO.getNombreLeconsTerminees(joueurId, cours.getId());

                totalLeconsTheme += totalLeconsCours;
                leconsTermineesTheme += leconsTermineesCours;

                if (progressionCoursDAO.estCoursTermine(joueurId, cours.getId())) {
                    coursTerminesTheme++;
                }
            }

            int pourcentageTheme = (totalLeconsTheme > 0) ?
                (leconsTermineesTheme * 100 / totalLeconsTheme) : 0;

            themeData.put("totalCours", totalCoursTheme);
            themeData.put("coursTermines", coursTerminesTheme);
            themeData.put("totalLecons", totalLeconsTheme);
            themeData.put("leconsTerminees", leconsTermineesTheme);
            themeData.put("pourcentageProgression", pourcentageTheme);

            themesCoursList.add(themeData);

            totalCoursTerminesGlobal += coursTerminesTheme;
            totalLeconsPlateforme += totalLeconsTheme;
            totalLeconsTermineesGlobal += leconsTermineesTheme;
        }

        int pourcentageCoursGlobal = (totalLeconsPlateforme > 0) ?
            (totalLeconsTermineesGlobal * 100 / totalLeconsPlateforme) : 0;

        // Nombre de certificats
        int totalCertificats = certificatDAO.compterParUtilisateur(joueurId);

        // ============================================================
        // PARTIE 3 : MESSAGE DE SUCCÈS
        // ============================================================
        String messageSucces = (String) session.getAttribute("messageSucces");
        if (messageSucces != null) {
            session.removeAttribute("messageSucces");
        }

        // ============================================================
        // PARTIE 4 : PASSAGE DES ATTRIBUTS À LA JSP
        // ============================================================

        // Attributs Quiz
        request.setAttribute("themesQuiz", themesQuizList);
        request.setAttribute("nombreParties", nombreParties);
        request.setAttribute("meilleurScore", meilleurScore);
        request.setAttribute("moyenneScore", moyenneScore);
        request.setAttribute("dernieresParties", dernieresParties);
        request.setAttribute("niveauxTermines", niveauxTermines);

        // Attributs Cours
        request.setAttribute("themesCours", themesCoursList);
        request.setAttribute("totalCours", totalCoursPlateforme);
        request.setAttribute("totalCoursTermines", totalCoursTerminesGlobal);
        request.setAttribute("totalLecons", totalLeconsPlateforme);
        request.setAttribute("totalLeconsTerminees", totalLeconsTermineesGlobal);
        request.setAttribute("pourcentageCoursGlobal", pourcentageCoursGlobal);
        request.setAttribute("totalCertificats", totalCertificats);

        // Message
        request.setAttribute("messageSucces", messageSucces);

        // 5. Affichage du dashboard
        request.getRequestDispatcher("/views/joueur/dashboard.jsp").forward(request, response);
    }

    /**
     * Construit un résumé d'un niveau pour l'affichage dans une carte thème Quiz
     */
    private Map<String, Object> construireResumeNiveau(
            int joueurId,
            String categorie,
            String niveauStr,
            int totalParties,
            ProgressionJoueurDAO progressionDAO,
            QuizDAO quizDAO) {

        Map<String, Object> niveauData = new LinkedHashMap<>();

        String[] infosNiveau = getInfosNiveau(niveauStr);
        niveauData.put("nom", niveauStr);
        niveauData.put("nomFormate", infosNiveau[0]);
        niveauData.put("icone", infosNiveau[1]);
        niveauData.put("couleur", infosNiveau[2]);
        niveauData.put("totalParties", totalParties);

        boolean debloque = false;
        boolean termine = false;
        int partiesReussies = 0;

        if (totalParties > 0) {
            // Niveau élémentaire toujours débloqué
            debloque = "elementaire".equals(niveauStr)
                    || progressionDAO.estNiveauDebloque(joueurId, categorie, niveauStr);

            partiesReussies = progressionDAO.compterPartiesReussiesNiveau(joueurId, categorie, niveauStr);
            termine = (partiesReussies >= totalParties);
        }

        niveauData.put("debloque", debloque);
        niveauData.put("termine", termine);
        niveauData.put("partiesReussies", partiesReussies);
        niveauData.put("pourcentageProgression",
                totalParties > 0 ? (partiesReussies * 100 / totalParties) : 0);

        return niveauData;
    }

    /**
     * Détermine le niveau actif (le premier non terminé)
     */
    private String determinerNiveauActif(List<Map<String, Object>> niveaux) {
        for (Map<String, Object> niveau : niveaux) {
            boolean termine = (Boolean) niveau.get("termine");
            if (!termine) {
                return (String) niveau.get("nomFormate");
            }
        }
        return "Terminé";
    }

    /**
     * Retourne les informations formatées pour un niveau donné
     */
    private String[] getInfosNiveau(String niveau) {
        switch (niveau) {
            case "elementaire":
                return new String[]{"Élémentaire", "fas fa-seedling", "#27ae60"};
            case "moyen":
                return new String[]{"Moyen", "fas fa-fire", "#f39c12"};
            case "avance":
                return new String[]{"Avancé", "fas fa-star", "#e74c3c"};
            default:
                return new String[]{"Inconnu", "fas fa-circle", "#888888"};
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}