// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/NiveauCoursServlet.java
// RÔLE    : Contrôleur de la page niveau d'un thème de cours
//           Affiche la liste des cours disponibles pour un
//           thème et un niveau donnés (élémentaire, moyen, avancé)
//           MODIFIÉ : Vérification premium pour niveaux moyen/avancé
//           MODIFIÉ : Vérification progression niveau précédent
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.AccessPremiumDAO;
import com.amijayquiz.dao.ChapitreDAO;
import com.amijayquiz.dao.CoursDAO;
import com.amijayquiz.dao.ProgressionCoursDAO;
import com.amijayquiz.models.Chapitre;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NiveauCoursServlet - Page niveau d'un thème de cours
 * 
 * Affiche la liste de tous les cours disponibles pour un thème
 * et un niveau donnés. Chaque cours est présenté sous forme de carte
 * avec sa progression et un bouton pour voir son plan détaillé.
 * 
 * 🔒 MODIFIÉ : Bloque l'accès aux niveaux moyen/avancé si non premium.
 * 🔒 MODIFIÉ : Vérifie que le niveau précédent est terminé avant d'accéder.
 * 
 * URL : /joueur/cours/niveau?theme=Développement+Web&niveau=elementaire
 * 
 * @author AmijayQuiz
 * @version 1.2
 */
@WebServlet(name = "NiveauCoursServlet", urlPatterns = {"/joueur/cours/niveau"})
public class NiveauCoursServlet extends HttpServlet {

    // Constantes des niveaux
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

        int utilisateurId = utilisateur.getId();

        // 2. Récupération et validation des paramètres
        String theme = request.getParameter("theme");
        String niveau = request.getParameter("niveau");

        if (theme == null || theme.trim().isEmpty() ||
            niveau == null || niveau.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        theme = theme.trim();
        niveau = niveau.trim().toLowerCase();

        // 3. Validation du niveau
        if (!NIVEAU_ELEMENTAIRE.equals(niveau) && 
            !NIVEAU_MOYEN.equals(niveau) && 
            !NIVEAU_AVANCE.equals(niveau)) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        // ===== 4. VÉRIFICATION PREMIUM POUR NIVEAUX MOYEN/AVANCÉ =====
        AccessPremiumDAO premiumDAO = new AccessPremiumDAO();
        boolean estPremium = premiumDAO.estPremium(utilisateurId);

        // Si niveau non élémentaire et utilisateur non premium → rediriger vers paiement
        if (!NIVEAU_ELEMENTAIRE.equals(niveau) && !estPremium) {
            session.setAttribute("messageErreur",
                "Le niveau " + getNomFormateNiveau(niveau) + " necessite un compte Premium. " +
                "Debloquez tous les niveaux pour seulement 4$ !");
            session.setAttribute("urlRedirectionApresPaiement", 
                request.getContextPath() + "/joueur/cours/niveau?theme=" + 
                java.net.URLEncoder.encode(theme, "UTF-8") + "&niveau=" + niveau);
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }

        // ===== 5. VÉRIFIER QUE LE NIVEAU PRÉCÉDENT EST TERMINÉ =====
        if (!NIVEAU_ELEMENTAIRE.equals(niveau)) {
            CoursDAO coursDAOVerif = new CoursDAO();
            ProgressionCoursDAO progressionDAOVerif = new ProgressionCoursDAO();

            String niveauPrecedent = NIVEAU_AVANCE.equals(niveau) ? NIVEAU_MOYEN : NIVEAU_ELEMENTAIRE;

            List<Cours> coursNiveauPrecedent = coursDAOVerif.listerParThemeEtNiveau(theme, niveauPrecedent);
            boolean niveauPrecedentTermine = true;

            if (coursNiveauPrecedent != null && !coursNiveauPrecedent.isEmpty()) {
                for (Cours coursPrec : coursNiveauPrecedent) {
                    if (!progressionDAOVerif.estCoursTermine(utilisateurId, coursPrec.getId())) {
                        niveauPrecedentTermine = false;
                        break;
                    }
                }

                if (!niveauPrecedentTermine) {
                    session.setAttribute("messageErreur",
                        "Vous devez terminer le niveau " + getNomFormateNiveau(niveauPrecedent) +
                        " avant d'acceder au niveau " + getNomFormateNiveau(niveau) + ".");
                    response.sendRedirect(request.getContextPath() +
                        "/joueur/cours/theme?theme=" + java.net.URLEncoder.encode(theme, "UTF-8"));
                    return;
                }
            }
        }

        // 6. Initialisation des DAO
        CoursDAO coursDAO = new CoursDAO();
        ChapitreDAO chapitreDAO = new ChapitreDAO();
        ProgressionCoursDAO progressionDAO = new ProgressionCoursDAO();

        // 7. Récupérer les cours du thème et niveau
        List<Cours> coursDuNiveau = coursDAO.listerParThemeEtNiveau(theme, niveau);

        if (coursDuNiveau.isEmpty()) {
            session.setAttribute("messageErreur", "Aucun cours trouvé pour ce niveau.");
            response.sendRedirect(request.getContextPath() + 
                "/joueur/cours/theme?theme=" + java.net.URLEncoder.encode(theme, "UTF-8"));
            return;
        }

        // 8. Informations formatées du niveau
        String[] infosNiveau = getInfosNiveau(niveau);

        // 9. Construire les données de chaque cours avec progression
        List<Map<String, Object>> coursAvecProgression = new ArrayList<>();
        int totalChapitresNiveau = 0;
        int totalLeconsNiveau = 0;
        int totalChapitresTerminesNiveau = 0;
        int totalCoursTerminesNiveau = 0;

        for (Cours cours : coursDuNiveau) {
            Map<String, Object> coursData = new LinkedHashMap<>();
            
            // Charger les chapitres du cours
            List<Chapitre> chapitres = chapitreDAO.listerParCours(cours.getId());
            cours.setChapitres(chapitres);
            cours.setNombreChapitres(chapitres.size());
            cours.setNombreLecons(cours.getNombreTotalLecons());
            
            coursData.put("cours", cours);
            
            // Calculer la progression
            int totalLecons = cours.getNombreTotalLecons();
            if (totalLecons == 0) {
                totalLecons = progressionDAO.getNombreTotalLecons(cours.getId());
            }
            
            int leconsTerminees = progressionDAO.getNombreLeconsTerminees(utilisateurId, cours.getId());
            boolean estTermine = progressionDAO.estCoursTermine(utilisateurId, cours.getId());
            
            // Compter les chapitres terminés
            int chapitresTermines = 0;
            for (Chapitre chapitre : chapitres) {
                if (progressionDAO.estChapitreTermine(utilisateurId, chapitre.getId())) {
                    chapitresTermines++;
                }
            }
            
            int pourcentage = (totalLecons > 0) ? (leconsTerminees * 100 / totalLecons) : 0;
            
            coursData.put("nbChapitres", chapitres.size());
            coursData.put("nbLecons", totalLecons);
            coursData.put("leconsTerminees", leconsTerminees);
            coursData.put("chapitresTermines", chapitresTermines);
            coursData.put("estTermine", estTermine);
            coursData.put("pourcentage", pourcentage);
            
            // Déterminer le premier chapitre accessible
            int premierChapitreId = 0;
            if (!chapitres.isEmpty()) {
                premierChapitreId = chapitres.get(0).getId();
            }
            coursData.put("premierChapitreId", premierChapitreId);
            
            // Statut du cours
            String statutCours;
            String iconeStatutCours;
            String couleurStatutCours;
            if (estTermine) {
                statutCours = "Termine";
                iconeStatutCours = "fas fa-check-circle";
                couleurStatutCours = "#27ae60";
            } else if (pourcentage > 0) {
                statutCours = "En cours";
                iconeStatutCours = "fas fa-spinner";
                couleurStatutCours = "#667eea";
            } else {
                statutCours = "A commencer";
                iconeStatutCours = "fas fa-play-circle";
                couleurStatutCours = "#f39c12";
            }
            coursData.put("statutCours", statutCours);
            coursData.put("iconeStatutCours", iconeStatutCours);
            coursData.put("couleurStatutCours", couleurStatutCours);
            
            coursAvecProgression.add(coursData);
            
            totalChapitresNiveau += chapitres.size();
            totalLeconsNiveau += totalLecons;
            totalChapitresTerminesNiveau += chapitresTermines;
            if (estTermine) totalCoursTerminesNiveau++;
        }

        // 10. Calcul du pourcentage global du niveau
        int pourcentageNiveau = (totalLeconsNiveau > 0) ? 
            (totalChapitresTerminesNiveau * 100 / Math.max(totalChapitresNiveau, 1)) : 0;

        // 11. Déterminer le statut global du niveau
        boolean niveauTermine = (coursDuNiveau.size() > 0 && 
            totalCoursTerminesNiveau >= coursDuNiveau.size());

        // 12. Passage des attributs à la JSP
        request.setAttribute("pageTitle", infosNiveau[0] + " - " + theme + " - AmijayQuiz");
        request.setAttribute("nomTheme", theme);
        request.setAttribute("nomNiveau", niveau);
        request.setAttribute("nomNiveauFormate", infosNiveau[0]);
        request.setAttribute("iconeNiveau", infosNiveau[1]);
        request.setAttribute("couleurNiveau", infosNiveau[2]);
        request.setAttribute("coursAvecProgression", coursAvecProgression);
        request.setAttribute("totalCours", coursDuNiveau.size());
        request.setAttribute("totalCoursTermines", totalCoursTerminesNiveau);
        request.setAttribute("totalChapitres", totalChapitresNiveau);
        request.setAttribute("totalLecons", totalLeconsNiveau);
        request.setAttribute("pourcentageNiveau", pourcentageNiveau);
        request.setAttribute("niveauTermine", niveauTermine);

        // ===== ATTRIBUTS PREMIUM =====
        request.setAttribute("estPremium", estPremium);

        // 13. Affichage de la page
        request.getRequestDispatcher("/views/joueur/cours-niveau.jsp").forward(request, response);
    }

    /**
     * Retourne les informations formatées pour un niveau donné
     */
    private String[] getInfosNiveau(String niveau) {
        switch (niveau) {
            case NIVEAU_ELEMENTAIRE:
                return new String[]{"Elementaire", "fas fa-seedling", "#27ae60"};
            case NIVEAU_MOYEN:
                return new String[]{"Moyen", "fas fa-fire", "#f39c12"};
            case NIVEAU_AVANCE:
                return new String[]{"Avance", "fas fa-star", "#e74c3c"};
            default:
                return new String[]{"Niveau", "fas fa-circle", "#888888"};
        }
    }

    /**
     * Retourne le nom formaté d'un niveau
     */
    private String getNomFormateNiveau(String niveau) {
        switch (niveau) {
            case NIVEAU_ELEMENTAIRE: return "Elementaire";
            case NIVEAU_MOYEN:       return "Moyen";
            case NIVEAU_AVANCE:      return "Avance";
            default:                 return niveau;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}