package com.learnpath.controllers;

import com.learnpath.dao.ProgressionJoueurDAO;
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
import java.util.*;

/**
 * ThemeServlet - Page d'un thème spécifique
 * 
 * Affiche les 3 niveaux (élémentaire, moyen, avancé) d'un thème donné.
 * Détermine pour chaque niveau son statut : débloqué, en cours, terminé.
 * 
 * URL : /joueur/theme?categorie=Informatique+Generale
 * 
 * @author LearnPath
 * @version 2.0
 */
@WebServlet(name = "ThemeServlet", urlPatterns = {"/joueur/theme"})
public class ThemeServlet extends HttpServlet {

    // Constantes des niveaux
    private static final String[] NIVEAUX = {"elementaire", "moyen", "avance"};
    private static final String NIVEAU_ELEMENTAIRE = "elementaire";
    private static final String NIVEAU_MOYEN = "moyen";
    private static final String NIVEAU_AVANCE = "avance";

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

        // 2. Récupération et validation du paramètre catégorie
        String categorie = request.getParameter("categorie");
        if (categorie == null || categorie.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
            return;
        }
        categorie = categorie.trim();

        // 3. Initialisation des DAO
        QuizDAO quizDAO = new QuizDAO();
        ProgressionJoueurDAO progressionDAO = new ProgressionJoueurDAO();
        int joueurId = utilisateur.getId();

        // 4. Détermination des couleurs et icônes du thème
        Map<String, ThemeInfo> themesConfig = getThemesConfiguration();
        ThemeInfo themeInfo = themesConfig.getOrDefault(categorie,
                new ThemeInfo("fas fa-book", "#667eea"));

        // 5. Récupération de tous les quiz du thème, groupés par niveau
        Map<String, List<Quiz>> quizParNiveau = new LinkedHashMap<>();
        int totalPartiesTheme = 0;
        int partiesReussiesTheme = 0;

        for (String niveauStr : NIVEAUX) {
            List<Quiz> quizList = quizDAO.listerQuizParCategorieEtNiveau(categorie, niveauStr);
            quizParNiveau.put(niveauStr, quizList);
            totalPartiesTheme += quizList.size();

            if (!quizList.isEmpty()) {
                partiesReussiesTheme += progressionDAO.compterPartiesReussiesNiveau(joueurId, categorie, niveauStr);
            }
        }

        // 6. Calcul du pourcentage global du thème
        int pourcentageGlobal = (totalPartiesTheme > 0)
                ? (partiesReussiesTheme * 100 / totalPartiesTheme)
                : 0;

        // 7. Construction de la liste des 3 niveaux avec leur statut
        List<Map<String, Object>> niveauxList = new ArrayList<>();

        for (String niveauStr : NIVEAUX) {
            Map<String, Object> niveauData = construireNiveauData(
                    joueurId, categorie, niveauStr,
                    quizParNiveau.get(niveauStr),
                    progressionDAO, quizDAO
            );
            niveauxList.add(niveauData);
        }

        // 8. Passage des attributs à la JSP
        request.setAttribute("nomCategorie", categorie);
        request.setAttribute("iconeCategorie", themeInfo.icone);
        request.setAttribute("couleurCategorie", themeInfo.couleur);
        request.setAttribute("totalPartiesTheme", totalPartiesTheme);
        request.setAttribute("partiesReussiesTheme", partiesReussiesTheme);
        request.setAttribute("pourcentageProgressionTheme", pourcentageGlobal);
        request.setAttribute("niveaux", niveauxList);

        // 9. Affichage de la page thème
        request.getRequestDispatcher("/views/joueur/theme.jsp").forward(request, response);
    }

    /**
     * Construit les données complètes d'un niveau pour la vue
     * 
     * @param joueurId        ID du joueur
     * @param categorie       Catégorie (thème)
     * @param niveauStr       Identifiant du niveau (elementaire/moyen/avance)
     * @param quizDuNiveau    Liste des quiz du niveau
     * @param progressionDAO  DAO de progression
     * @param quizDAO         DAO de quiz
     * @return Map contenant toutes les données du niveau
     */
    private Map<String, Object> construireNiveauData(
            int joueurId,
            String categorie,
            String niveauStr,
            List<Quiz> quizDuNiveau,
            ProgressionJoueurDAO progressionDAO,
            QuizDAO quizDAO) {

        Map<String, Object> niveauData = new LinkedHashMap<>();

        // Nom formaté, icône et couleur selon le niveau
        String[] infosNiveau = getInfosNiveau(niveauStr);
        niveauData.put("nom", niveauStr);
        niveauData.put("nomFormate", infosNiveau[0]);
        niveauData.put("icone", infosNiveau[1]);
        niveauData.put("couleur", infosNiveau[2]);

        int totalParties = (quizDuNiveau != null) ? quizDuNiveau.size() : 0;
        niveauData.put("totalParties", totalParties);

        boolean debloque = false;
        boolean termine = false;
        int partiesReussies = 0;
        Quiz prochainQuiz = null;

        if (totalParties > 0) {
            // Le niveau élémentaire est toujours débloqué
            if (NIVEAU_ELEMENTAIRE.equals(niveauStr)) {
                debloque = true;
            } else {
                debloque = progressionDAO.estNiveauDebloque(joueurId, categorie, niveauStr);
            }

            // Compter les parties réussies
            partiesReussies = progressionDAO.compterPartiesReussiesNiveau(joueurId, categorie, niveauStr);

            // Vérifier si le niveau est terminé
            termine = (partiesReussies >= totalParties);

            // Récupérer le prochain quiz à jouer si le niveau est débloqué mais pas terminé
            if (debloque && !termine) {
                prochainQuiz = quizDAO.getPartieSuivante(joueurId, categorie, niveauStr);
            }
        }

        int pourcentageProgression = (totalParties > 0)
                ? (partiesReussies * 100 / totalParties)
                : 0;

        niveauData.put("debloque", debloque);
        niveauData.put("termine", termine);
        niveauData.put("partiesReussies", partiesReussies);
        niveauData.put("pourcentageProgression", pourcentageProgression);
        niveauData.put("prochainQuiz", prochainQuiz);
        niveauData.put("quizDuNiveau", quizDuNiveau);

        return niveauData;
    }

    /**
     * Retourne les informations formatées pour un niveau donné
     * 
     * @param niveau Identifiant du niveau
     * @return Tableau [nomFormate, icone, couleur]
     */
    private String[] getInfosNiveau(String niveau) {
        switch (niveau) {
            case NIVEAU_ELEMENTAIRE:
                return new String[]{"Élémentaire", "fas fa-seedling", "#27ae60"};
            case NIVEAU_MOYEN:
                return new String[]{"Moyen", "fas fa-fire", "#f39c12"};
            case NIVEAU_AVANCE:
                return new String[]{"Avancé", "fas fa-star", "#e74c3c"};
            default:
                return new String[]{"Inconnu", "fas fa-circle", "#888888"};
        }
    }

    /**
     * Retourne la configuration des thèmes (icônes et couleurs)
     * 
     * @return Map associant nom du thème → ThemeInfo
     */
    private Map<String, ThemeInfo> getThemesConfiguration() {
        Map<String, ThemeInfo> config = new LinkedHashMap<>();
        config.put("Informatique Générale", new ThemeInfo("fas fa-laptop", "#667eea"));
        config.put("Réseaux", new ThemeInfo("fas fa-network-wired", "#f093fb"));
        config.put("Programmation", new ThemeInfo("fas fa-code", "#4facfe"));
        config.put("Base de données", new ThemeInfo("fas fa-database", "#43e97b"));
        config.put("Sécurité", new ThemeInfo("fas fa-shield-alt", "#f5576c"));
        config.put("Web", new ThemeInfo("fas fa-globe", "#f39c12"));
        return config;
    }

    /**
     * Classe interne pour stocker les informations d'un thème
     */
    private static class ThemeInfo {
        final String icone;
        final String couleur;

        ThemeInfo(String icone, String couleur) {
            this.icone = icone;
            this.couleur = couleur;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}