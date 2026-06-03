// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/ThemeCoursServlet.java
// RÔLE    : Contrôleur de la page d'un thème de cours
//           Affiche les 3 niveaux et les cours disponibles
//           MODIFIÉ : Vérification premium pour niveaux moyen/avancé
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
 * ThemeCoursServlet - Page d'un thème spécifique
 * 
 * Affiche les 3 niveaux (Élémentaire, Moyen, Avancé) d'un thème
 * avec leurs cours respectifs et la progression de l'utilisateur.
 * Charge également les chapitres de chaque cours pour les liens.
 * 
 * 🔒 MODIFIÉ : Les niveaux moyen et avancé nécessitent un compte premium.
 * 
 * URL : /joueur/cours/theme?theme=Développement+Web
 * 
 * @author AmijayQuiz
 * @version 1.2
 */
@WebServlet(name = "ThemeCoursServlet", urlPatterns = {"/joueur/cours/theme"})
public class ThemeCoursServlet extends HttpServlet {

    // Niveaux dans l'ordre
    private static final String[] NIVEAUX = {
        Cours.NIVEAU_ELEMENTAIRE, 
        Cours.NIVEAU_MOYEN, 
        Cours.NIVEAU_AVANCE
    };

    // Configuration des thèmes (icône, couleur)
    private static final Map<String, String[]> THEMES_CONFIG = new LinkedHashMap<>();

    static {
        THEMES_CONFIG.put("Développement Web",       new String[]{"fas fa-globe",         "#667eea"});
        THEMES_CONFIG.put("Langage Java",            new String[]{"fas fa-coffee",        "#f093fb"});
        THEMES_CONFIG.put("Langage Python",          new String[]{"fab fa-python",        "#4facfe"});
        THEMES_CONFIG.put("Langage PHP",             new String[]{"fab fa-php",           "#8892bf"});
        THEMES_CONFIG.put("Bases de Données & SQL",  new String[]{"fas fa-database",      "#43e97b"});
        THEMES_CONFIG.put("Cybersécurité",           new String[]{"fas fa-shield-alt",    "#f5576c"});
        THEMES_CONFIG.put("Développement Mobile",    new String[]{"fas fa-mobile-alt",    "#f39c12"});
        THEMES_CONFIG.put("Intelligence Artificielle",new String[]{"fas fa-robot",        "#9b59b6"});
        THEMES_CONFIG.put("Design UI/UX",            new String[]{"fas fa-palette",       "#e91e63"});
        THEMES_CONFIG.put("Réseaux Informatiques",   new String[]{"fas fa-network-wired", "#00bcd4"});
        THEMES_CONFIG.put("Systèmes d''Exploitation",new String[]{"fab fa-linux",         "#ff9800"});
        THEMES_CONFIG.put("Data Science",            new String[]{"fas fa-chart-pie",     "#795548"});
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

        int utilisateurId = utilisateur.getId();

        // ===== 🆕 VÉRIFICATION PREMIUM =====
        AccessPremiumDAO premiumDAO = new AccessPremiumDAO();
        boolean estPremium = premiumDAO.estPremium(utilisateurId);

        // 2. Récupération et validation du paramètre thème
        String theme = request.getParameter("theme");
        if (theme == null || theme.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }
        theme = theme.trim();

        // 3. Initialisation des DAO
        CoursDAO coursDAO = new CoursDAO();
        ChapitreDAO chapitreDAO = new ChapitreDAO();
        ProgressionCoursDAO progressionDAO = new ProgressionCoursDAO();

        // 4. Récupération des cours du thème
        List<Cours> coursDuTheme = coursDAO.listerParTheme(theme);

        if (coursDuTheme.isEmpty()) {
            session.setAttribute("messageErreur", "Aucun cours trouvé pour ce thème.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        // 5. Configuration du thème (icône, couleur)
        String[] config = THEMES_CONFIG.getOrDefault(theme,
                new String[]{"fas fa-book", "#667eea"});

        // 6. Organiser les cours par niveau
        List<Map<String, Object>> niveauxList = new ArrayList<>();
        int totalCoursTheme = 0;
        int totalCoursTerminesTheme = 0;
        int totalLeconsTheme = 0;
        int totalLeconsTermineesTheme = 0;

        for (String niveauStr : NIVEAUX) {
            List<Cours> coursDuNiveau = new ArrayList<>();
            
            // Filtrer les cours de ce niveau
            for (Cours cours : coursDuTheme) {
                if (niveauStr.equals(cours.getNiveau())) {
                    coursDuNiveau.add(cours);
                }
            }

            Map<String, Object> niveauData = new LinkedHashMap<>();
            niveauData.put("nom", niveauStr);
            niveauData.put("nomFormate", getNomFormateNiveau(niveauStr));
            niveauData.put("icone", getIconeNiveau(niveauStr));
            niveauData.put("couleur", getCouleurNiveau(niveauStr));

            int nbCours = coursDuNiveau.size();
            int nbCoursTermines = 0;
            int nbLeconsNiveau = 0;
            int nbLeconsTermineesNiveau = 0;

            // Calculer la progression pour chaque cours du niveau
            List<Map<String, Object>> coursAvecProgression = new ArrayList<>();
            for (Cours cours : coursDuNiveau) {
                List<Chapitre> chapitres = chapitreDAO.listerParCours(cours.getId());
                cours.setChapitres(chapitres);
                cours.setNombreChapitres(chapitres.size());
                cours.setNombreLecons(cours.getNombreTotalLecons());
                
                Map<String, Object> coursData = new LinkedHashMap<>();
                coursData.put("cours", cours);
                
                int totalLecons = cours.getNombreTotalLecons() > 0 ? 
                    cours.getNombreTotalLecons() : 
                    progressionDAO.getNombreTotalLecons(cours.getId());
                int leconsTerminees = progressionDAO.getNombreLeconsTerminees(utilisateurId, cours.getId());
                boolean estTermine = progressionDAO.estCoursTermine(utilisateurId, cours.getId());
                int pourcentage = (totalLecons > 0) ? (leconsTerminees * 100 / totalLecons) : 0;
                
                coursData.put("totalLecons", totalLecons);
                coursData.put("leconsTerminees", leconsTerminees);
                coursData.put("estTermine", estTermine);
                coursData.put("pourcentage", pourcentage);
                
                coursAvecProgression.add(coursData);
                
                nbLeconsNiveau += totalLecons;
                nbLeconsTermineesNiveau += leconsTerminees;
                if (estTermine) nbCoursTermines++;
            }

            int pourcentageNiveau = (nbLeconsNiveau > 0) ? 
                (nbLeconsTermineesNiveau * 100 / nbLeconsNiveau) : 0;

            niveauData.put("nbCours", nbCours);
            niveauData.put("nbCoursTermines", nbCoursTermines);
            niveauData.put("nbLecons", nbLeconsNiveau);
            niveauData.put("nbLeconsTerminees", nbLeconsTermineesNiveau);
            niveauData.put("pourcentage", pourcentageNiveau);
            niveauData.put("cours", coursAvecProgression);

            // ===== 🆕 DÉTERMINER SI LE NIVEAU EST DÉBLOQUÉ =====
           // ===== DÉTERMINER SI LE NIVEAU EST DÉBLOQUÉ =====
            boolean debloque;

            if (NIVEAUX[0].equals(niveauStr)) {
                // Niveau élémentaire : toujours débloqué (gratuit)
                debloque = true;
            } else {
                // Niveaux moyen et avancé : débloqué seulement si premium ET niveau précédent terminé
                if (!estPremium) {
                    debloque = false;
                } else {
                    // Vérifier que le niveau précédent est terminé
                    String niveauPrecedent = NIVEAUX[1].equals(niveauStr) ? NIVEAUX[0] : NIVEAUX[1];
                    List<Cours> coursNiveauPrecedent = coursDAO.listerParThemeEtNiveau(theme, niveauPrecedent);
                    boolean niveauPrecedentTermine = true;

                    if (coursNiveauPrecedent != null && !coursNiveauPrecedent.isEmpty()) {
                        for (Cours coursPrec : coursNiveauPrecedent) {
                            if (!progressionDAO.estCoursTermine(utilisateurId, coursPrec.getId())) {
                                niveauPrecedentTermine = false;
                                break;
                            }
                        }
                    }
                    debloque = niveauPrecedentTermine;
                }
            }

            boolean termine = (nbCours > 0 && nbCoursTermines >= nbCours) && debloque;
            niveauData.put("debloque", debloque);
            niveauData.put("termine", termine);

            niveauxList.add(niveauData);

            totalCoursTheme += nbCours;
            totalCoursTerminesTheme += nbCoursTermines;
            totalLeconsTheme += nbLeconsNiveau;
            totalLeconsTermineesTheme += nbLeconsTermineesNiveau;
        }

        // 7. Calcul du pourcentage global du thème
        int pourcentageGlobalTheme = (totalLeconsTheme > 0) ? 
            (totalLeconsTermineesTheme * 100 / totalLeconsTheme) : 0;

        // 8. Déterminer le niveau actif (premier non terminé et débloqué)
        String niveauActif = "Élémentaire";
        for (Map<String, Object> niveau : niveauxList) {
            boolean termine = (Boolean) niveau.get("termine");
            boolean debloque = (Boolean) niveau.get("debloque");
            if (!termine && debloque) {
                niveauActif = (String) niveau.get("nomFormate");
                break;
            }
        }
        if (totalCoursTerminesTheme >= totalCoursTheme && totalCoursTheme > 0) {
            niveauActif = "Terminé";
        } else if (!estPremium && totalCoursTerminesTheme >= totalCoursTheme) {
            niveauActif = "Premium requis";
        }

        // 9. Passage des attributs à la JSP
        request.setAttribute("pageTitle", theme + " - Cours - AmijayQuiz");
        request.setAttribute("nomTheme", theme);
        request.setAttribute("iconeTheme", config[0]);
        request.setAttribute("couleurTheme", config[1]);
        request.setAttribute("totalCours", totalCoursTheme);
        request.setAttribute("totalCoursTermines", totalCoursTerminesTheme);
        request.setAttribute("totalLecons", totalLeconsTheme);
        request.setAttribute("totalLeconsTerminees", totalLeconsTermineesTheme);
        request.setAttribute("pourcentageProgression", pourcentageGlobalTheme);
        request.setAttribute("niveaux", niveauxList);
        request.setAttribute("niveauActif", niveauActif);

        // ===== 🆕 ATTRIBUTS PREMIUM =====
        request.setAttribute("estPremium", estPremium);

        if (!estPremium) {
            request.setAttribute("messagePremium", 
                "🔒 Les niveaux Moyen et Avancé nécessitent un compte Premium. " +
                "<a href='" + request.getContextPath() + "/paiement' style='color: #f39c12; font-weight: 600;'>" +
                "Débloquez tout pour 4$ (10 000 FC) ✨</a>");
        }

        // 10. Affichage de la page
        request.getRequestDispatcher("/views/joueur/cours-theme.jsp").forward(request, response);
    }

    // ===== MÉTHODES UTILITAIRES (inchangées) =====

    private String getNomFormateNiveau(String niveau) {
        switch (niveau) {
            case Cours.NIVEAU_ELEMENTAIRE: return "Élémentaire";
            case Cours.NIVEAU_MOYEN:       return "Moyen";
            case Cours.NIVEAU_AVANCE:      return "Avancé";
            default:                       return niveau;
        }
    }

    private String getIconeNiveau(String niveau) {
        switch (niveau) {
            case Cours.NIVEAU_ELEMENTAIRE: return "fas fa-seedling";
            case Cours.NIVEAU_MOYEN:       return "fas fa-fire";
            case Cours.NIVEAU_AVANCE:      return "fas fa-star";
            default:                       return "fas fa-circle";
        }
    }

    private String getCouleurNiveau(String niveau) {
        switch (niveau) {
            case Cours.NIVEAU_ELEMENTAIRE: return "#27ae60";
            case Cours.NIVEAU_MOYEN:       return "#f39c12";
            case Cours.NIVEAU_AVANCE:      return "#e74c3c";
            default:                       return "#888888";
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}