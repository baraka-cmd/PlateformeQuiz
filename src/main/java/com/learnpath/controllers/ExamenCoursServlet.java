// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/ExamenCoursServlet.java
// RÔLE    : Contrôleur de l'examen final d'un cours
//           Vérifie les prérequis, affiche l'examen, corrige et délivre le certificat
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.CertificatDAO;
import com.learnpath.dao.ChapitreDAO;
import com.learnpath.dao.CoursDAO;
import com.learnpath.dao.ProgressionCoursDAO;
import com.learnpath.dao.QuestionDAO;
import com.learnpath.models.Certificat;
import com.learnpath.models.Chapitre;
import com.learnpath.models.Cours;
import com.learnpath.models.Question;
import com.learnpath.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * ExamenCoursServlet - Gestion de l'examen final d'un cours
 * 
 * Vérifie que tous les chapitres sont terminés avant d'afficher l'examen.
 * L'examen est composé de questions aléatoires venant de tous les quiz du cours.
 * Si réussi (≥80%), génère et délivre un certificat.
 * 
 * URL : /joueur/cours/examen?coursId=1
 * 
 * @author LearnPath
 * @version 1.1
 */
@WebServlet(name = "ExamenCoursServlet", urlPatterns = {"/joueur/cours/examen"})
public class ExamenCoursServlet extends HttpServlet {

    private static final double SEUIL_REUSSITE = 80.0;
    private static final int NOMBRE_QUESTIONS_EXAMEN = 20;

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

        int utilisateurId = utilisateur.getId();

        // 2. Récupération et validation du paramètre
        String coursIdParam = request.getParameter("coursId");
        if (coursIdParam == null || coursIdParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        int coursId;
        try {
            coursId = Integer.parseInt(coursIdParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        // 3. Initialisation des DAO
        CoursDAO coursDAO = new CoursDAO();
        ChapitreDAO chapitreDAO = new ChapitreDAO();
        ProgressionCoursDAO progressionDAO = new ProgressionCoursDAO();
        CertificatDAO certificatDAO = new CertificatDAO();
        QuestionDAO questionDAO = new QuestionDAO();

        // 4. Vérifier que le cours existe
        Cours cours = coursDAO.trouverParId(coursId);
        if (cours == null) {
            session.setAttribute("messageErreur", "Ce cours n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        // 5. Vérifier que l'utilisateur n'a pas déjà un certificat pour ce cours
        Certificat existant = certificatDAO.trouverParUtilisateurEtCours(utilisateurId, coursId);
        if (existant != null) {
            request.setAttribute("certificatExistant", existant);
            request.setAttribute("cours", cours);
            request.getRequestDispatcher("/views/joueur/cours-deja-certifie.jsp").forward(request, response);
            return;
        }

        // 6. Vérifier que tous les chapitres sont terminés
        List<Chapitre> chapitres = chapitreDAO.listerParCours(coursId);
        if (chapitres.isEmpty()) {
            session.setAttribute("messageErreur", "Ce cours ne contient aucun chapitre.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + coursId);
            return;
        }

        boolean tousChapitresTermines = true;
        List<Chapitre> chapitresNonTermines = new ArrayList<>();

        for (Chapitre chapitre : chapitres) {
            if (!progressionDAO.estChapitreTermine(utilisateurId, chapitre.getId())) {
                tousChapitresTermines = false;
                chapitresNonTermines.add(chapitre);
            }
        }

        if (!tousChapitresTermines) {
            request.setAttribute("cours", cours);
            request.setAttribute("chapitresNonTermines", chapitresNonTermines);
            request.getRequestDispatcher("/views/joueur/cours-examen-bloque.jsp").forward(request, response);
            return;
        }

        // 7. Récupérer des questions aléatoires de tous les quiz du cours
        List<Question> questionsExamen = new ArrayList<>();
        List<Integer> idsQuestionsUtilisees = new ArrayList<>();

        for (Chapitre chapitre : chapitres) {
            if (chapitre.hasQuiz()) {
                List<Question> questionsQuiz = questionDAO.listerQuestionsParQuiz(chapitre.getQuizId());
                for (Question q : questionsQuiz) {
                    if (!idsQuestionsUtilisees.contains(q.getId())) {
                        questionsExamen.add(q);
                        idsQuestionsUtilisees.add(q.getId());
                    }
                }
            }
        }

        // Si pas assez de questions, prendre des questions aléatoires de la base
        if (questionsExamen.size() < NOMBRE_QUESTIONS_EXAMEN) {
            List<Question> toutesLesQuestions = questionDAO.listerToutes();
            for (Question q : toutesLesQuestions) {
                if (!idsQuestionsUtilisees.contains(q.getId()) && 
                    questionsExamen.size() < NOMBRE_QUESTIONS_EXAMEN) {
                    questionsExamen.add(q);
                    idsQuestionsUtilisees.add(q.getId());
                }
            }
        }

        // Limiter au nombre requis et mélanger
        if (questionsExamen.size() > NOMBRE_QUESTIONS_EXAMEN) {
            // Mélanger et prendre les N premiers
            java.util.Collections.shuffle(questionsExamen, new SecureRandom());
            questionsExamen = new ArrayList<>(questionsExamen.subList(0, NOMBRE_QUESTIONS_EXAMEN));
        }

        // 8. Stocker les questions dans la session pour la correction
        session.setAttribute("questionsExamen", questionsExamen);
        session.setAttribute("coursExamenId", coursId);
        session.setAttribute("tempsDebutExamen", System.currentTimeMillis());

        // 9. Passage des attributs à la JSP
        request.setAttribute("pageTitle", "Examen Final - " + cours.getTitre() + " - LearnPath");
        request.setAttribute("cours", cours);
        request.setAttribute("questions", questionsExamen);
        request.setAttribute("nombreQuestions", questionsExamen.size());
        request.setAttribute("seuilReussite", SEUIL_REUSSITE);

        // 10. Affichage de la page d'examen
        request.getRequestDispatcher("/views/joueur/cours-examen.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // 1. Vérification de la session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        int utilisateurId = utilisateur.getId();

        // 2. Récupération des questions depuis la session
        List<Question> questionsExamen = (List<Question>) session.getAttribute("questionsExamen");
        Integer coursId = (Integer) session.getAttribute("coursExamenId");

        if (questionsExamen == null || coursId == null) {
            session.setAttribute("messageErreur", "Session d'examen expirée. Veuillez recommencer.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + 
                (coursId != null ? coursId : ""));
            return;
        }

        // 3. Correction de l'examen
        int score = 0;
        List<Boolean> corrections = new ArrayList<>();

        for (int i = 0; i < questionsExamen.size(); i++) {
            Question question = questionsExamen.get(i);
            String reponseParam = request.getParameter("reponse_" + i);
            int reponseJoueur = 0;

            if (reponseParam != null && !reponseParam.isEmpty()) {
                try {
                    reponseJoueur = Integer.parseInt(reponseParam);
                } catch (NumberFormatException e) {
                    reponseJoueur = 0;
                }
            }

            boolean estCorrecte = question.estCorrecte(reponseJoueur);
            corrections.add(estCorrecte);

            if (estCorrecte) {
                score++;
            }
        }

        // 4. Calcul du pourcentage
        int totalQuestions = questionsExamen.size();
        double pourcentage = (double) score / totalQuestions * 100;
        boolean estReussi = pourcentage >= SEUIL_REUSSITE;

        // 5. Nettoyer la session
        session.removeAttribute("questionsExamen");
        session.removeAttribute("coursExamenId");
        session.removeAttribute("tempsDebutExamen");

        // 6. Générer le certificat si réussi
        Certificat certificat = null;
        if (estReussi) {
            String codeVerification = genererCodeVerification();
            certificat = new Certificat(utilisateurId, coursId, pourcentage, codeVerification);

            CertificatDAO certificatDAO = new CertificatDAO();
            boolean ajoute = certificatDAO.ajouter(certificat);

            if (!ajoute) {
                // Le certificat existe peut-être déjà, essayer de le récupérer
                certificat = certificatDAO.trouverParUtilisateurEtCours(utilisateurId, coursId);
            }
        }

        // 7. Charger les infos du cours pour l'affichage
        CoursDAO coursDAO = new CoursDAO();
        Cours cours = coursDAO.trouverParId(coursId);

        // 8. Passage des attributs à la JSP
        request.setAttribute("pageTitle", "Résultat Examen - " + 
            (cours != null ? cours.getTitre() : "Cours") + " - LearnPath");
        request.setAttribute("cours", cours);
        request.setAttribute("questions", questionsExamen);
        request.setAttribute("corrections", corrections);
        request.setAttribute("score", score);
        request.setAttribute("totalQuestions", totalQuestions);
        request.setAttribute("pourcentage", pourcentage);
        request.setAttribute("estReussi", estReussi);
        request.setAttribute("seuilReussite", SEUIL_REUSSITE);
        request.setAttribute("certificat", certificat);

        // 9. Affichage du résultat
        request.getRequestDispatcher("/views/joueur/cours-resultat-examen.jsp").forward(request, response);
    }

    /**
     * Génère un code de vérification unique pour le certificat
     * Format : AMJ-XXXX-XXXX-XXXX (16 caractères aléatoires)
     * 
     * @return Code de vérification
     */
    private String genererCodeVerification() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        // Formater en groupes de 4 caractères
        return "AMJ-" + code.substring(0, 4).toUpperCase() + "-" + 
               code.substring(4, 8).toUpperCase() + "-" + 
               code.substring(8, 12).toUpperCase();
    }
}