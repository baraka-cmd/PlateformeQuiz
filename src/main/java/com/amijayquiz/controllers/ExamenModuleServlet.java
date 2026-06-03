// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/ExamenModuleServlet.java
// RÔLE    : Contrôleur de l'examen d'un module
//           MODIFIÉ : Bloque examens des modules 2+ si non premium
//           MODIFIÉ : Autorise le repassage avec parametre ?repasser=true
//           MODIFIÉ : Suppression des messages d'alerte session
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.AccessPremiumDAO;
import com.amijayquiz.dao.ExamenModuleDAO;
import com.amijayquiz.dao.QuestionExamenDAO;
import com.amijayquiz.dao.ProgressionModuleDAO;
import com.amijayquiz.dao.CoursDAO;
import com.amijayquiz.models.ExamenModule;
import com.amijayquiz.models.QuestionExamen;
import com.amijayquiz.models.ProgressionModule;
import com.amijayquiz.models.Cours;
import com.amijayquiz.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ExamenModuleServlet - Gestion de l'examen d'un module
 * 
 * GET  : Affiche les 20 questions de l'examen du module
 * POST : Corrige les réponses, calcule le score,
 *        enregistre la progression et débloque le module suivant si réussi
 * 
 * 🔒 MODIFIÉ : Module 1 accessible gratuitement.
 *              Modules 2+ nécessitent un compte premium.
 * 🔄 MODIFIÉ : Autorise le repassage avec le parametre ?repasser=true
 * 🧹 MODIFIÉ : Suppression des messages d'alerte session pour le repassage
 * 
 * URL : /joueur/cours/examen-module?coursId=1&module=1
 * URL repassage : /joueur/cours/examen-module?coursId=1&module=1&repasser=true
 * 
 * @author AmijayQuiz
 * @version 1.3
 */
@WebServlet(name = "ExamenModuleServlet", urlPatterns = {"/joueur/cours/examen-module"})
public class ExamenModuleServlet extends HttpServlet {

    // Constantes
    private static final int NOMBRE_QUESTIONS = 20;
    private static final double SEUIL_REUSSITE = 75.0;
    private static final int MODULE_GRATUIT_MAX = 1;

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

        // 2. Récupération et validation des paramètres
        String coursIdParam = request.getParameter("coursId");
        String moduleParam = request.getParameter("module");
        String repasserParam = request.getParameter("repasser");

        if (coursIdParam == null || coursIdParam.trim().isEmpty() ||
            moduleParam == null || moduleParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        int coursId;
        int moduleNumero;
        try {
            coursId = Integer.parseInt(coursIdParam);
            moduleNumero = Integer.parseInt(moduleParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        if (coursId <= 0 || moduleNumero <= 0) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        // Savoir si l'utilisateur veut repasser l'examen
        boolean estRepassage = "true".equals(repasserParam);

        // ===== 3. VÉRIFICATION PREMIUM POUR MODULES 2+ =====
        AccessPremiumDAO premiumDAO = new AccessPremiumDAO();
        boolean estPremium = premiumDAO.estPremium(utilisateurId);

        if (moduleNumero > MODULE_GRATUIT_MAX && !estPremium) {
            session.setAttribute("messageErreur",
                "🔒 Les examens des modules " + (MODULE_GRATUIT_MAX + 1) + " et plus nécessitent un compte Premium. " +
                "Débloquez tous les niveaux pour seulement 4$ !");
            session.setAttribute("urlRedirectionApresPaiement",
                request.getContextPath() + "/joueur/cours/examen-module?coursId=" + coursId + "&module=" + moduleNumero);
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }

        // 4. Initialisation des DAO
        CoursDAO coursDAO = new CoursDAO();
        ExamenModuleDAO examenDAO = new ExamenModuleDAO();
        QuestionExamenDAO questionDAO = new QuestionExamenDAO();
        ProgressionModuleDAO progressionDAO = new ProgressionModuleDAO();

        // 5. Vérifier que le cours existe
        Cours cours = coursDAO.trouverParId(coursId);
        if (cours == null) {
            session.setAttribute("messageErreur", "Ce cours n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        // 6. Vérifier que le module est débloqué
        if (!progressionDAO.estModuleDebloque(utilisateurId, coursId, moduleNumero)) {
            session.setAttribute("messageErreur", 
                "Ce module n'est pas encore débloqué. Réussissez d'abord le module " + (moduleNumero - 1) + ".");
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + coursId);
            return;
        }

        // 7. Récupérer l'examen du module
        ExamenModule examen = examenDAO.trouverParCoursEtModule(coursId, moduleNumero);
        if (examen == null) {
            session.setAttribute("messageErreur", "Aucun examen trouvé pour ce module.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + coursId);
            return;
        }

        // 8. Récupérer les questions de l'examen
        List<QuestionExamen> questions = questionDAO.listerParExamen(examen.getId());
        if (questions == null || questions.isEmpty()) {
            session.setAttribute("messageErreur", "Cet examen ne contient pas encore de questions.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + coursId);
            return;
        }

        // 9. Vérifier si l'utilisateur a déjà réussi ce module
        ProgressionModule progressionExistante = progressionDAO.getProgression(utilisateurId, coursId, moduleNumero);
        
        // Si le module est déjà réussi ET que ce n'est PAS un repassage, rediriger vers le plan
        if (progressionExistante != null && progressionExistante.isReussi() && !estRepassage) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + coursId);
            return;
        }

        // 10. Passage des attributs à la JSP
        request.setAttribute("pageTitle", "Examen " + examen.getModuleFormate() + " - " + cours.getTitre());
        request.setAttribute("cours", cours);
        request.setAttribute("examen", examen);
        request.setAttribute("questions", questions);
        request.setAttribute("nombreQuestions", questions.size());
        request.setAttribute("seuilReussite", examen.getSeuilReussite());
        request.setAttribute("scoreMinimum", examen.getScoreMinimum());
        request.setAttribute("progressionExistante", progressionExistante);
        request.setAttribute("estPremium", estPremium);
        request.setAttribute("estRepassage", estRepassage);

        // 11. Affichage de la page d'examen
        request.getRequestDispatcher("/views/joueur/cours-examen-module.jsp").forward(request, response);
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

        // 2. Récupération des paramètres
        String coursIdParam = request.getParameter("coursId");
        String moduleParam = request.getParameter("module");

        if (coursIdParam == null || moduleParam == null) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        int coursId;
        int moduleNumero;
        try {
            coursId = Integer.parseInt(coursIdParam);
            moduleNumero = Integer.parseInt(moduleParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        // ===== 3. VÉRIFICATION PREMIUM POUR MODULES 2+ (POST aussi) =====
        AccessPremiumDAO premiumDAO = new AccessPremiumDAO();
        boolean estPremium = premiumDAO.estPremium(utilisateurId);

        if (moduleNumero > MODULE_GRATUIT_MAX && !estPremium) {
            session.setAttribute("messageErreur",
                "🔒 Les examens des modules " + (MODULE_GRATUIT_MAX + 1) + " et plus nécessitent un compte Premium.");
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }

        // 4. Initialisation des DAO
        CoursDAO coursDAO = new CoursDAO();
        ExamenModuleDAO examenDAO = new ExamenModuleDAO();
        QuestionExamenDAO questionDAO = new QuestionExamenDAO();
        ProgressionModuleDAO progressionDAO = new ProgressionModuleDAO();

        // 5. Vérifier que le cours existe
        Cours cours = coursDAO.trouverParId(coursId);
        if (cours == null) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        // 6. Vérifier que le module est débloqué
        if (!progressionDAO.estModuleDebloque(utilisateurId, coursId, moduleNumero)) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + coursId);
            return;
        }

        // 7. Récupérer l'examen et les questions
        ExamenModule examen = examenDAO.trouverParCoursEtModule(coursId, moduleNumero);
        if (examen == null) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + coursId);
            return;
        }

        List<QuestionExamen> questions = questionDAO.listerParExamen(examen.getId());
        if (questions == null || questions.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + coursId);
            return;
        }

        // 8. Correction des réponses
        int score = 0;
        List<Boolean> corrections = new ArrayList<>();
        int[] reponsesJoueur = new int[questions.size()];

        for (int i = 0; i < questions.size(); i++) {
            QuestionExamen question = questions.get(i);
            String reponseParam = request.getParameter("reponse_" + i);
            int reponseJoueur = 0;

            if (reponseParam != null && !reponseParam.trim().isEmpty()) {
                try {
                    reponseJoueur = Integer.parseInt(reponseParam);
                } catch (NumberFormatException e) {
                    reponseJoueur = 0;
                }
            }

            reponsesJoueur[i] = reponseJoueur;
            boolean estCorrecte = question.estCorrecte(reponseJoueur);
            corrections.add(estCorrecte);

            if (estCorrecte) {
                score++;
            }
        }

        // 9. Calcul du pourcentage
        int totalQuestions = questions.size();
        double pourcentage = (double) score / totalQuestions * 100;
        pourcentage = Math.round(pourcentage * 100.0) / 100.0;
        boolean estReussi = pourcentage >= SEUIL_REUSSITE;

        // 10. Enregistrer la progression
        ProgressionModule progression = new ProgressionModule(
            utilisateurId, coursId, moduleNumero, score, totalQuestions
        );
        progression.setPourcentage(pourcentage);
        progression.setReussi(estReussi);

        boolean enregistree = progressionDAO.enregistrerOuMettreAJour(progression);

        if (!enregistree) {
            System.err.println("⚠️ Erreur : La progression du module " + moduleNumero + " n'a pas pu être enregistrée.");
        }

        // 11. Déterminer le module suivant
        int moduleSuivant = moduleNumero + 1;
        boolean moduleSuivantDebloque = estReussi && progressionDAO.estModuleDebloque(utilisateurId, coursId, moduleSuivant);

        // 12. Passage des attributs à la JSP
        request.setAttribute("pageTitle", "Résultat " + examen.getModuleFormate() + " - " + cours.getTitre());
        request.setAttribute("cours", cours);
        request.setAttribute("examen", examen);
        request.setAttribute("questions", questions);
        request.setAttribute("reponsesJoueur", reponsesJoueur);
        request.setAttribute("corrections", corrections);
        request.setAttribute("score", score);
        request.setAttribute("totalQuestions", totalQuestions);
        request.setAttribute("pourcentage", pourcentage);
        request.setAttribute("estReussi", estReussi);
        request.setAttribute("seuilReussite", SEUIL_REUSSITE);
        request.setAttribute("scoreMinimum", examen.getScoreMinimum());
        request.setAttribute("moduleSuivant", moduleSuivant);
        request.setAttribute("moduleSuivantDebloque", moduleSuivantDebloque);
        request.setAttribute("estPremium", estPremium);

        // 13. Message de succès si réussi
        if (estReussi) {
            session.setAttribute("messageSucces", 
                "🎉 Félicitations ! Vous avez réussi le Module " + moduleNumero + 
                " avec " + String.format("%.0f%%", pourcentage) + " !");
        }

        // 14. Affichage de la page de résultat
        request.getRequestDispatcher("/views/joueur/cours-resultat-module.jsp").forward(request, response);
    }
}