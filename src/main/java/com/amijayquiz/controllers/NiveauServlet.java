// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/NiveauServlet.java
// RÔLE    : Contrôleur de la page niveau Quiz
//           MODIFIÉ : Bloque niveaux moyen/avancé si non premium
//                     Limite à 5 parties pour niveau élémentaire gratuit
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.AccessPremiumDAO;
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

/**
 * NiveauServlet - Page d'un niveau de quiz
 * 
 * 🔒 RÈGLES D'ACCÈS :
 *   - Niveau élémentaire : gratuit (parties 1 à 5 uniquement si non premium)
 *   - Niveau moyen/avancé : premium requis
 *   - Si premium : toutes les parties de tous les niveaux
 * 
 * URL : /joueur/niveau?categorie=Informatique+Generale&niveau=elementaire
 * 
 * @author AmijayQuiz
 * @version 2.1
 */
@WebServlet(name = "NiveauServlet", urlPatterns = {"/joueur/niveau"})
public class NiveauServlet extends HttpServlet {

    private static final String NIVEAU_ELEMENTAIRE = "elementaire";
    private static final String NIVEAU_MOYEN = "moyen";
    private static final String NIVEAU_AVANCE = "avance";

    // Nombre maximum de parties gratuites pour le niveau élémentaire
    private static final int MAX_PARTIES_GRATUITES = 5;

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
        AccessPremiumDAO premiumDAO = new AccessPremiumDAO();
        int joueurId = utilisateur.getId();

        // ===== 🆕 5. VÉRIFICATION PREMIUM =====
        boolean estPremium = premiumDAO.estPremium(joueurId);

        // Bloquer niveaux moyen/avancé si non premium
        if (!NIVEAU_ELEMENTAIRE.equals(niveau) && !estPremium) {
            session.setAttribute("messageErreur",
                "🔒 Le niveau <strong>" + getNomFormateNiveau(niveau) + "</strong> nécessite un compte Premium. " +
                "Débloquez tous les niveaux pour seulement 4$ !");
            session.setAttribute("urlRedirectionApresPaiement",
                request.getContextPath() + "/joueur/niveau?categorie=" +
                java.net.URLEncoder.encode(categorie, "UTF-8") + "&niveau=" + niveau);
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }

        // 6. Vérification que le niveau est débloqué (progression)
        boolean debloque = NIVEAU_ELEMENTAIRE.equals(niveau)
                || progressionDAO.estNiveauDebloque(joueurId, categorie, niveau);

        if (!debloque) {
            session.setAttribute("messageErreur",
                    "Ce niveau n'est pas encore débloqué. Terminez d'abord le niveau précédent.");
            response.sendRedirect(request.getContextPath()
                    + "/joueur/theme?categorie=" + java.net.URLEncoder.encode(categorie, "UTF-8"));
            return;
        }

        // 7. Récupération des quiz du niveau
        List<Quiz> quizDuNiveau = quizDAO.listerQuizParCategorieEtNiveau(categorie, niveau);

        if (quizDuNiveau == null || quizDuNiveau.isEmpty()) {
            session.setAttribute("messageErreur", "Ce niveau ne contient aucune partie pour le moment.");
            response.sendRedirect(request.getContextPath()
                    + "/joueur/theme?categorie=" + java.net.URLEncoder.encode(categorie, "UTF-8"));
            return;
        }

        // ===== 🆕 8. LIMITER À 5 PARTIES SI NIVEAU ÉLÉMENTAIRE ET NON PREMIUM =====
        List<Quiz> quizAccessibles = new ArrayList<>();
        List<Quiz> quizBloques = new ArrayList<>();

        if (NIVEAU_ELEMENTAIRE.equals(niveau) && !estPremium) {
            // Séparer les parties accessibles (1-5) et bloquées (6+)
            for (Quiz quiz : quizDuNiveau) {
                if (quiz.getOrdrePartie() <= MAX_PARTIES_GRATUITES) {
                    quizAccessibles.add(quiz);
                } else {
                    quizBloques.add(quiz);
                }
            }
        } else {
            // Premium ou niveau déjà vérifié : tout accessible
            quizAccessibles.addAll(quizDuNiveau);
        }

        // 9. Construction de la liste des parties avec leur statut
        int partiesReussies = 0;
        Quiz prochainQuiz = null;
        boolean prochainTrouve = false;

        List<Map<String, Object>> partiesList = new ArrayList<>();

        // Ajouter les parties accessibles
        for (Quiz quiz : quizAccessibles) {
            Map<String, Object> partieData = new LinkedHashMap<>();
            partieData.put("quiz", quiz);
            partieData.put("ordrePartie", quiz.getOrdrePartie());
            partieData.put("accessible", true);

            boolean reussie = progressionDAO.estPartieReussie(joueurId, quiz.getId());
            partieData.put("reussie", reussie);

            if (reussie) {
                partiesReussies++;
            }

            boolean estProchaine = !reussie && !prochainTrouve;
            partieData.put("estProchaine", estProchaine);

            if (estProchaine) {
                prochainQuiz = quiz;
                prochainTrouve = true;
            }

            partiesList.add(partieData);
        }

        // ===== 🆕 10. AJOUTER LES PARTIES BLOQUÉES (NIVEAU ÉLÉMENTAIRE GRATUIT) =====
        for (Quiz quiz : quizBloques) {
            Map<String, Object> partieData = new LinkedHashMap<>();
            partieData.put("quiz", quiz);
            partieData.put("ordrePartie", quiz.getOrdrePartie());
            partieData.put("accessible", false);
            partieData.put("reussie", false);
            partieData.put("estProchaine", false);
            partieData.put("bloque", true);

            partiesList.add(partieData);
        }

        // 11. Statut du niveau
        int totalPartiesAffichees = partiesList.size();
        boolean termine = (quizAccessibles.size() > 0) && (partiesReussies >= quizAccessibles.size());
        int pourcentageProgression = (totalPartiesAffichees > 0)
                ? (partiesReussies * 100 / totalPartiesAffichees)
                : 0;

        // 12. Informations formatées du niveau
        String[] infosNiveau = getInfosNiveau(niveau);

        // 13. Passage des attributs à la JSP
        request.setAttribute("nomCategorie", categorie);
        request.setAttribute("nomNiveau", niveau);
        request.setAttribute("nomNiveauFormate", infosNiveau[0]);
        request.setAttribute("iconeNiveau", infosNiveau[1]);
        request.setAttribute("couleurNiveau", infosNiveau[2]);
        request.setAttribute("parties", partiesList);
        request.setAttribute("totalParties", totalPartiesAffichees);
        request.setAttribute("partiesReussies", partiesReussies);
        request.setAttribute("pourcentageProgression", pourcentageProgression);
        request.setAttribute("prochainQuiz", prochainQuiz);
        request.setAttribute("niveauTermine", termine);
        request.setAttribute("niveauDebloque", debloque);

        // ===== 🆕 ATTRIBUTS PREMIUM =====
        request.setAttribute("estPremium", estPremium);
        request.setAttribute("maxPartiesGratuites", MAX_PARTIES_GRATUITES);

        if (!estPremium && NIVEAU_ELEMENTAIRE.equals(niveau)) {
            request.setAttribute("messagePremium",
                "🎯 Accès gratuit limité aux 5 premières parties. " +
                "<a href='" + request.getContextPath() + "/paiement' style='color: #f39c12; font-weight: 600;'>" +
                "Passez Premium pour débloquer tout (4$) ✨</a>");
        }

        // 14. Affichage de la page niveau
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

    private String getNomFormateNiveau(String niveau) {
        switch (niveau) {
            case NIVEAU_ELEMENTAIRE: return "Élémentaire";
            case NIVEAU_MOYEN:       return "Moyen";
            case NIVEAU_AVANCE:      return "Avancé";
            default:                 return niveau;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}