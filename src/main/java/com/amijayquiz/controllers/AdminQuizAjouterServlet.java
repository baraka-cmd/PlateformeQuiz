// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminQuizAjouterServlet.java
// RÔLE    : Contrôleur administrateur pour l'ajout d'un quiz
//           Validation complète avant création
// ============================================================

package com.amijayquiz.controllers;

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
import java.util.List;

/**
 * AdminQuizAjouterServlet - Ajout d'un nouveau quiz
 * 
 * POST : Valide les données et crée le quiz
 *        Vérifie qu'aucun quiz avec le même thème/niveau/partie n'existe
 *        Le titre est généré automatiquement : "Thème - Partie X"
 * 
 * URL : /admin/quiz/ajouter
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminQuizAjouterServlet", urlPatterns = {"/admin/quiz/ajouter"})
public class AdminQuizAjouterServlet extends HttpServlet {

    // Niveaux valides
    private static final String[] NIVEAUX_VALIDES = {"elementaire", "moyen", "avance"};

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/admin/quiz");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // 1. Vérification authentification + rôle admin
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (!"admin".equals(utilisateur.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        // 2. Récupération des paramètres
        String theme = request.getParameter("theme");
        String niveau = request.getParameter("niveau");
        String ordrePartieStr = request.getParameter("ordrePartie");
        String description = request.getParameter("description");
        String dureeStr = request.getParameter("dureeMaxMinutes");

        // 3. Validation du thème
        if (theme == null || theme.trim().isEmpty()) {
            session.setAttribute("messageErreur", "Le thème du quiz est obligatoire.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }
        theme = theme.trim();

        // 4. Validation du niveau
        if (niveau == null || niveau.trim().isEmpty()) {
            session.setAttribute("messageErreur", "Le niveau du quiz est obligatoire.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }
        niveau = niveau.trim().toLowerCase();

        boolean niveauValide = false;
        for (String n : NIVEAUX_VALIDES) {
            if (n.equals(niveau)) {
                niveauValide = true;
                break;
            }
        }
        if (!niveauValide) {
            session.setAttribute("messageErreur", "Niveau invalide. Choisissez : élémentaire, moyen ou avancé.");
            response.sendRedirect(request.getContextPath() + "/admin/quiz");
            return;
        }

        // 5. Validation du numéro de partie
        int ordrePartie = 1;
        if (ordrePartieStr != null && !ordrePartieStr.trim().isEmpty()) {
            try {
                ordrePartie = Integer.parseInt(ordrePartieStr.trim());
                if (ordrePartie < 1) {
                    session.setAttribute("messageErreur", "Le numéro de partie doit être supérieur ou égal à 1.");
                    response.sendRedirect(request.getContextPath() + "/admin/quiz");
                    return;
                }
                if (ordrePartie > 20) {
                    session.setAttribute("messageErreur", "Le numéro de partie ne peut pas dépasser 20.");
                    response.sendRedirect(request.getContextPath() + "/admin/quiz");
                    return;
                }
            } catch (NumberFormatException e) {
                session.setAttribute("messageErreur", "Le numéro de partie doit être un nombre valide.");
                response.sendRedirect(request.getContextPath() + "/admin/quiz");
                return;
            }
        }

        // 6. Validation de la durée
        int dureeMaxMinutes = 10;
        if (dureeStr != null && !dureeStr.trim().isEmpty()) {
            try {
                dureeMaxMinutes = Integer.parseInt(dureeStr.trim());
                if (dureeMaxMinutes < 1) {
                    session.setAttribute("messageErreur", "La durée doit être d'au moins 1 minute.");
                    response.sendRedirect(request.getContextPath() + "/admin/quiz");
                    return;
                }
                if (dureeMaxMinutes > 120) {
                    session.setAttribute("messageErreur", "La durée ne peut pas dépasser 120 minutes.");
                    response.sendRedirect(request.getContextPath() + "/admin/quiz");
                    return;
                }
            } catch (NumberFormatException e) {
                session.setAttribute("messageErreur", "La durée doit être un nombre valide.");
                response.sendRedirect(request.getContextPath() + "/admin/quiz");
                return;
            }
        }

        // 7. Vérifier qu'un quiz avec le même thème/niveau/partie n'existe pas déjà
        QuizDAO quizDAO = new QuizDAO();
        List<Quiz> quizExistant = quizDAO.listerQuizParCategorieEtNiveau(theme, niveau);

        for (Quiz q : quizExistant) {
            if (q.getOrdrePartie() == ordrePartie) {
                session.setAttribute("messageErreur",
                    "Un quiz existe déjà pour le thème \"" + theme + "\", niveau \"" +
                    getNomFormateNiveau(niveau) + "\", Partie " + ordrePartie + ".");
                response.sendRedirect(request.getContextPath() + "/admin/quiz");
                return;
            }
        }

        // 8. Génération du titre au format "Thème - Partie X"
        String titre = theme + " - Partie " + ordrePartie;

        // 9. Création du quiz
        Quiz quiz = new Quiz();
        quiz.setTitre(titre);
        quiz.setDescription(description != null ? description.trim() : "");
        quiz.setDureeMaxMinutes(dureeMaxMinutes);
        quiz.setNiveau(niveau);
        quiz.setOrdrePartie(ordrePartie);

        boolean ajoute = quizDAO.ajouter(quiz);

        if (ajoute) {
            session.setAttribute("messageSucces",
                "Quiz \"" + titre + "\" créé avec succès dans le niveau " +
                getNomFormateNiveau(niveau) + " !");
        } else {
            session.setAttribute("messageErreur",
                "Erreur lors de la création du quiz. Vérifiez les données et réessayez.");
        }

        // 10. Redirection
        response.sendRedirect(request.getContextPath() + "/admin/quiz");
    }

    /**
     * Retourne le nom formaté d'un niveau
     */
    private String getNomFormateNiveau(String niveau) {
        if (niveau == null) return "Inconnu";
        switch (niveau.toLowerCase()) {
            case "elementaire": return "Élémentaire";
            case "moyen":       return "Moyen";
            case "avance":      return "Avancé";
            default:            return niveau;
        }
    }
}