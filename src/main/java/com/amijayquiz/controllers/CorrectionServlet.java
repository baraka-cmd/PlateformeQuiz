package com.amijayquiz.controllers;

import com.amijayquiz.dao.PartieDAO;
import com.amijayquiz.dao.ProgressionJoueurDAO;
import com.amijayquiz.dao.QuizDAO;
import com.amijayquiz.models.Partie;
import com.amijayquiz.models.ProgressionJoueur;
import com.amijayquiz.models.Question;
import com.amijayquiz.models.Quiz;
import com.amijayquiz.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * CorrectionServlet - Correction et résultat d'un quiz terminé
 * 
 * Compare les réponses du joueur avec les bonnes réponses.
 * Calcule le score, enregistre la partie et met à jour la progression.
 * Gère le déblocage des niveaux supérieurs si le niveau est terminé.
 * 
 * URL : /joueur/correction
 * 
 * @author AmijayQuiz
 * @version 2.0
 */
@WebServlet(name = "CorrectionServlet", urlPatterns = {"/joueur/correction"})
public class CorrectionServlet extends HttpServlet {

    private static final int SEUIL_REUSSITE = 8;
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

        // 2. Vérifier que le quiz est bien terminé
        Boolean quizTermine = (Boolean) session.getAttribute("quizTermine");
        if (quizTermine == null || !quizTermine) {
            response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
            return;
        }

        // 3. Récupération des données du quiz depuis la session
        Quiz quiz = (Quiz) session.getAttribute("quizEnCours");
        List<Question> listeQuestions = (List<Question>) session.getAttribute("questionsQuiz");
        int[] reponsesJoueur = (int[]) session.getAttribute("reponsesJoueur");
        Long tempsRealise = (Long) session.getAttribute("tempsRealise");

        if (quiz == null || listeQuestions == null || reponsesJoueur == null) {
            response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
            return;
        }

        // 4. Correction : comparaison des réponses du joueur avec les bonnes réponses
        int score = 0;
        List<Boolean> corrections = new ArrayList<>();

        for (int i = 0; i < listeQuestions.size(); i++) {
            Question question = listeQuestions.get(i);
            int reponseJoueur = reponsesJoueur[i];
            boolean estCorrecte = question.estCorrecte(reponseJoueur);
            corrections.add(estCorrecte);

            if (estCorrecte) {
                score++;
            }
        }

        // 5. Calcul des statistiques
        int totalQuestions = listeQuestions.size();
        double pourcentage = (double) score / totalQuestions * 100;
        int tempsRealiseSecondes = (int) (tempsRealise / 1000);
        boolean partieReussie = score >= SEUIL_REUSSITE;

        // 6. Extraire la catégorie et le niveau du titre du quiz
        String titreQuiz = quiz.getTitre();
        String categorie = extraireCategorie(titreQuiz);
        String niveauQuiz = quiz.getNiveau();
        int partieNumero = quiz.getOrdrePartie();

        // 7. Enregistrement de la partie dans la table parties
        Partie partie = new Partie(utilisateur.getId(), quiz.getId(), score, totalQuestions, tempsRealiseSecondes);
        PartieDAO partieDAO = new PartieDAO();
        boolean enregistree = partieDAO.enregistrerPartie(partie);

        if (!enregistree) {
            System.err.println("Erreur : La partie n'a pas pu être enregistrée !");
        }

        // 8. Gestion de la progression
        ProgressionJoueurDAO progressionDAO = new ProgressionJoueurDAO();
        QuizDAO quizDAO = new QuizDAO();

        // Sauvegarder la progression du joueur pour cette partie
        ProgressionJoueur progression = new ProgressionJoueur();
        progression.setJoueurId(utilisateur.getId());
        progression.setQuizId(quiz.getId());
        progression.setNiveau(niveauQuiz);
        progression.setPartie(partieNumero);
        progression.setScore(score);
        progression.setTotalQuestions(totalQuestions);
        progression.setReussi(partieReussie);

        progressionDAO.enregistrerOuMettreAJour(progression);

        // 9. Vérifier si le niveau est terminé
        int totalPartiesNiveau = quizDAO.compterPartiesParNiveau(categorie, niveauQuiz);
        int partiesReussiesNiveau = progressionDAO.compterPartiesReussiesNiveau(
                utilisateur.getId(), categorie, niveauQuiz);
        boolean niveauTermine = (totalPartiesNiveau > 0) && (partiesReussiesNiveau >= totalPartiesNiveau);

        // 10. Si le niveau est terminé, déterminer le niveau supérieur
        String niveauSuperieur = null;
        Quiz premierQuizNiveauSuperieur = null;

        if (niveauTermine) {
            niveauSuperieur = getNiveauSuperieur(niveauQuiz);

            if (niveauSuperieur != null) {
                progressionDAO.debloquerNiveau(utilisateur.getId(), categorie, niveauSuperieur);
                premierQuizNiveauSuperieur = quizDAO.getPremierQuizNiveau(categorie, niveauSuperieur);
            }
        }

        // 11. Récupérer le quiz suivant dans le même niveau (si partie réussie)
        Quiz quizSuivant = null;
        if (partieReussie) {
            quizSuivant = quizDAO.getQuizSuivant(quiz.getId());
        }

        // 12. Détermination du niveau de réussite (libellé, icône, couleur)
        String[] niveauReussiteInfos = determinerNiveauReussite(pourcentage);

        // 13. Nettoyage des attributs de session liés au quiz terminé
        nettoyerSessionQuiz(session);

        // 14. Passage des attributs à la JSP
        request.setAttribute("quiz", quiz);
        request.setAttribute("listeQuestions", listeQuestions);
        request.setAttribute("reponsesJoueur", reponsesJoueur);
        request.setAttribute("corrections", corrections);
        request.setAttribute("score", score);
        request.setAttribute("totalQuestions", totalQuestions);
        request.setAttribute("pourcentage", pourcentage);
        request.setAttribute("tempsRealiseSecondes", tempsRealiseSecondes);
        request.setAttribute("niveauReussite", niveauReussiteInfos[0]);
        request.setAttribute("iconeNiveau", niveauReussiteInfos[1]);
        request.setAttribute("couleurNiveau", niveauReussiteInfos[2]);
        request.setAttribute("partieReussie", partieReussie);
        request.setAttribute("seuilReussite", SEUIL_REUSSITE);
        request.setAttribute("niveauTermine", niveauTermine);
        request.setAttribute("niveauSuperieur", niveauSuperieur);
        request.setAttribute("quizSuivant", quizSuivant);
        request.setAttribute("premierQuizNiveauSuperieur", premierQuizNiveauSuperieur);
        request.setAttribute("categorieQuiz", categorie);
        request.setAttribute("partiesReussiesNiveau", partiesReussiesNiveau);
        request.setAttribute("totalPartiesNiveau", totalPartiesNiveau);

        // 15. Affichage de la page de résultat
        request.getRequestDispatcher("/views/joueur/resultat.jsp").forward(request, response);
    }

    /**
     * Extrait la catégorie du titre du quiz
     * Format attendu : "Catégorie - Partie X"
     * 
     * @param titre Titre complet du quiz
     * @return La catégorie extraite, ou le titre complet si pas de séparateur
     */
    private String extraireCategorie(String titre) {
        if (titre == null || titre.isEmpty()) {
            return "";
        }
        int indexSeparation = titre.indexOf(" - ");
        if (indexSeparation > 0) {
            return titre.substring(0, indexSeparation);
        }
        return titre;
    }

    /**
     * Retourne le niveau supérieur en fonction du niveau actuel
     * 
     * @param niveauActuel Niveau actuel terminé
     * @return Le niveau supérieur, ou null si c'est le niveau avancé
     */
    private String getNiveauSuperieur(String niveauActuel) {
        if (NIVEAU_ELEMENTAIRE.equals(niveauActuel)) {
            return NIVEAU_MOYEN;
        } else if (NIVEAU_MOYEN.equals(niveauActuel)) {
            return NIVEAU_AVANCE;
        }
        return null; // Niveau avancé : pas de niveau supérieur
    }

    /**
     * Détermine le niveau de réussite basé sur le pourcentage
     * 
     * @param pourcentage Pourcentage de bonnes réponses
     * @return Tableau [libellé, icône, couleur]
     */
    private String[] determinerNiveauReussite(double pourcentage) {
        if (pourcentage == 100) {
            return new String[]{"Parfait", "fas fa-trophy", "#ffd700"};
        } else if (pourcentage >= 80) {
            return new String[]{"Excellent", "fas fa-star", "#27ae60"};
        } else if (pourcentage >= 60) {
            return new String[]{"Bien", "fas fa-thumbs-up", "#2980b9"};
        } else if (pourcentage >= 40) {
            return new String[]{"Moyen", "fas fa-book", "#f39c12"};
        } else if (pourcentage >= 20) {
            return new String[]{"Insuffisant", "fas fa-book-open", "#e67e22"};
        } else {
            return new String[]{"À réviser", "fas fa-dumbbell", "#e74c3c"};
        }
    }

    /**
     * Nettoie tous les attributs de session liés au quiz terminé
     */
    private void nettoyerSessionQuiz(HttpSession session) {
        session.removeAttribute("quizEnCours");
        session.removeAttribute("questionsQuiz");
        session.removeAttribute("indexQuestionCourante");
        session.removeAttribute("reponsesJoueur");
        session.removeAttribute("tempsDebutQuiz");
        session.removeAttribute("quizTermine");
        session.removeAttribute("tempsRealise");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}