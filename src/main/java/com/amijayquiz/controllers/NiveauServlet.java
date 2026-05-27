package com.amijayquiz.controllers;

import com.amijayquiz.dao.ProgressionJoueurDAO;
import com.amijayquiz.dao.QuizDAO;
import com.amijayquiz.models.Quiz;
import com.amijayquiz.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

@WebServlet(name = "NiveauServlet", urlPatterns = {"/joueur/niveau"})
public class NiveauServlet extends HttpServlet {

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

        // 2. Récupération et validation des paramètres
        String categorie = request.getParameter("categorie");
        String niveau = request.getParameter("niveau");

        if (categorie == null || categorie.trim().isEmpty()
                || niveau == null || niveau.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
            return;
        }

        categorie = categorie.trim();
        niveau = niveau.trim().toLowerCase();

        // 3. Validation du niveau
        if (!NIVEAU_ELEMENTAIRE.equals(niveau)
                && !NIVEAU_MOYEN.equals(niveau)
                && !NIVEAU_AVANCE.equals(niveau)) {
            response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
            return;
        }

        // 4. Initialisation des DAO
        QuizDAO quizDAO = new QuizDAO();
        ProgressionJoueurDAO progressionDAO = new ProgressionJoueurDAO();
        int joueurId = utilisateur.getId();

        // 5. Vérification que le niveau est débloqué
        boolean debloque = NIVEAU_ELEMENTAIRE.equals(niveau)
                || progressionDAO.estNiveauDebloque(joueurId, categorie, niveau);

        if (!debloque) {
            session.setAttribute("messageErreur",
                    "Ce niveau n'est pas encore débloqué. Terminez d'abord le niveau précédent.");
            response.sendRedirect(request.getContextPath()
                    + "/joueur/theme?categorie=" + java.net.URLEncoder.encode(categorie, "UTF-8"));
            return;
        }

        // 6. Récupération des quiz du niveau
        List<Quiz> quizDuNiveau = quizDAO.listerQuizParCategorieEtNiveau(categorie, niveau);

        if (quizDuNiveau == null || quizDuNiveau.isEmpty()) {
            session.setAttribute("messageErreur", "Ce niveau ne contient aucune partie pour le moment.");
            response.sendRedirect(request.getContextPath()
                    + "/joueur/theme?categorie=" + java.net.URLEncoder.encode(categorie, "UTF-8"));
            return;
        }

        // 7. Construction de la liste des parties avec leur statut
        int partiesReussies = 0;
        Quiz prochainQuiz = null;
        boolean prochainTrouve = false;

        List<Map<String, Object>> partiesList = new ArrayList<>();

        for (Quiz quiz : quizDuNiveau) {
            Map<String, Object> partieData = new LinkedHashMap<>();
            partieData.put("quiz", quiz);
            partieData.put("ordrePartie", quiz.getOrdrePartie());

            boolean reussie = progressionDAO.estPartieReussie(joueurId, quiz.getId());
            partieData.put("reussie", reussie);

            if (reussie) {
                partiesReussies++;
            }

            // La première partie non réussie est la prochaine à jouer
            boolean estProchaine = !reussie && !prochainTrouve;
            partieData.put("estProchaine", estProchaine);

            if (estProchaine) {
                prochainQuiz = quiz;
                prochainTrouve = true;
            }

            partiesList.add(partieData);
        }

        // 8. Statut du niveau
        int totalParties = quizDuNiveau.size();
        boolean termine = (totalParties > 0) && (partiesReussies >= totalParties);
        int pourcentageProgression = (totalParties > 0)
                ? (partiesReussies * 100 / totalParties)
                : 0;

        // 9. Informations formatées du niveau
        String[] infosNiveau = getInfosNiveau(niveau);

        // 10. Passage des attributs à la JSP
        request.setAttribute("nomCategorie", categorie);
        request.setAttribute("nomNiveau", niveau);
        request.setAttribute("nomNiveauFormate", infosNiveau[0]);
        request.setAttribute("iconeNiveau", infosNiveau[1]);
        request.setAttribute("couleurNiveau", infosNiveau[2]);
        request.setAttribute("parties", partiesList);
        request.setAttribute("totalParties", totalParties);
        request.setAttribute("partiesReussies", partiesReussies);
        request.setAttribute("pourcentageProgression", pourcentageProgression);
        request.setAttribute("prochainQuiz", prochainQuiz);
        request.setAttribute("niveauTermine", termine);
        request.setAttribute("niveauDebloque", debloque);

        // 11. Affichage de la page niveau
        request.getRequestDispatcher("/views/joueur/niveau.jsp").forward(request, response);
    }

    private String[] getInfosNiveau(String niveau) {
        switch (niveau) {
            case NIVEAU_ELEMENTAIRE:
                return new String[]{"Élémentaire", "fas fa-seedling", "#27ae60"};
            case NIVEAU_MOYEN:
                return new String[]{"Moyen", "fas fa-fire", "#f39c12"};
            case NIVEAU_AVANCE:
                return new String[]{"Avancé", "fas fa-star", "#e74c3c"};
            default:
                return new String[]{"Niveau", "fas fa-circle", "#888888"};
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}