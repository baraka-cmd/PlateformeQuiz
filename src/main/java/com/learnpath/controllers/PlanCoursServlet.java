// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/PlanCoursServlet.java
// RÔLE    : Contrôleur du plan détaillé d'un cours
//           Affiche l'arborescence complète du cours :
//           tous les chapitres avec leurs leçons visibles,
//           regroupés par MODULES avec examens et progression
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.CertificatDAO;
import com.learnpath.dao.ChapitreDAO;
import com.learnpath.dao.CoursDAO;
import com.learnpath.dao.ExamenModuleDAO;
import com.learnpath.dao.ProgressionCoursDAO;
import com.learnpath.dao.ProgressionModuleDAO;
import com.learnpath.models.Certificat;
import com.learnpath.models.Chapitre;
import com.learnpath.models.Cours;
import com.learnpath.models.ExamenModule;
import com.learnpath.models.Lecon;
import com.learnpath.models.ProgressionModule;
import com.learnpath.models.Utilisateur;

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
 * PlanCoursServlet - Plan détaillé d'un cours organisé par modules
 * 
 * Affiche l'arborescence complète du cours regroupée par modules (1 à 15).
 * Chaque module contient :
 * - Ses chapitres avec leurs leçons
 * - Le statut de progression (verrouillé, débloqué, réussi)
 * - Un bouton pour passer l'examen du module
 * 
 * URL : /joueur/cours/plan?coursId=1
 * 
 * @author LearnPath
 * @version 2.0
 */
@WebServlet(name = "PlanCoursServlet", urlPatterns = {"/joueur/cours/plan"})
public class PlanCoursServlet extends HttpServlet {

    // Constantes des modules
    private static final int NOMBRE_MODULES = 15;

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
        if (!"joueur".equals(utilisateur.getRole()) && !"admin".equals(utilisateur.getRole()) && !"gestionnaire".equals(utilisateur.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        int utilisateurId = utilisateur.getId();

        // 2. Récupération et validation du paramètre coursId
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
        ProgressionCoursDAO progressionCoursDAO = new ProgressionCoursDAO();
        ProgressionModuleDAO progressionModuleDAO = new ProgressionModuleDAO();
        ExamenModuleDAO examenModuleDAO = new ExamenModuleDAO();
        CertificatDAO certificatDAO = new CertificatDAO();

        // 4. Récupérer le cours complet avec ses chapitres et leçons
        Cours cours = coursDAO.trouverParId(coursId);
        if (cours == null) {
            session.setAttribute("messageErreur", "Ce cours n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        // 5. Récupérer tous les chapitres avec leurs leçons
        List<Chapitre> chapitres = chapitreDAO.listerParCours(coursId);
        cours.setChapitres(chapitres);
        cours.setNombreChapitres(chapitres.size());
        cours.setNombreLecons(cours.getNombreTotalLecons());

        // 6. Vérifier si l'utilisateur a déjà un certificat pour ce cours
        Certificat certificatExistant = certificatDAO.trouverParUtilisateurEtCours(utilisateurId, coursId);

        // 7. Organiser les chapitres par module
        Map<Integer, List<Chapitre>> chapitresParModule = new LinkedHashMap<>();
        for (Chapitre chapitre : chapitres) {
            int moduleNumero = chapitre.getModuleNumero() > 0 ? chapitre.getModuleNumero() : 1;
            if (!chapitresParModule.containsKey(moduleNumero)) {
                chapitresParModule.put(moduleNumero, new ArrayList<>());
            }
            chapitresParModule.get(moduleNumero).add(chapitre);
        }

        // 8. Récupérer tous les examens du cours
        List<ExamenModule> examens = examenModuleDAO.listerParCours(coursId);

        // 9. Construire les données des modules avec progression
        List<Map<String, Object>> modulesData = new ArrayList<>();
        int totalLeconsCours = 0;
        int totalLeconsTermineesCours = 0;
        int totalExemplesCode = 0;
        int modulesReussis = 0;

        for (int moduleNumero = 1; moduleNumero <= NOMBRE_MODULES; moduleNumero++) {
            Map<String, Object> moduleData = new LinkedHashMap<>();
            moduleData.put("numero", moduleNumero);

            // Vérifier si le module est débloqué
            boolean debloque = progressionModuleDAO.estModuleDebloque(utilisateurId, coursId, moduleNumero);
            moduleData.put("debloque", debloque);

            // Récupérer la progression du module
            ProgressionModule progression = progressionModuleDAO.getProgression(utilisateurId, coursId, moduleNumero);
            boolean moduleReussi = progression != null && progression.isReussi();
            moduleData.put("reussi", moduleReussi);
            moduleData.put("progression", progression);

            if (moduleReussi) {
                modulesReussis++;
            }

            // Récupérer l'examen du module
            ExamenModule examen = null;
            if (examens != null) {
                for (ExamenModule e : examens) {
                    if (e.getModuleNumero() == moduleNumero) {
                        examen = e;
                        break;
                    }
                }
            }
            moduleData.put("examen", examen);

            // Récupérer les chapitres de ce module
            List<Chapitre> chapitresModule = chapitresParModule.getOrDefault(moduleNumero, new ArrayList<>());
            moduleData.put("nbChapitres", chapitresModule.size());

            // Construire les données des chapitres avec progression
            List<Map<String, Object>> chapitresData = new ArrayList<>();
            int leconsModule = 0;
            int leconsTermineesModule = 0;
            int chapitresTerminesModule = 0;

            for (Chapitre chapitre : chapitresModule) {
                Map<String, Object> chapitreData = new LinkedHashMap<>();
                chapitreData.put("chapitre", chapitre);

                boolean chapitreTermine = progressionCoursDAO.estChapitreTermine(utilisateurId, chapitre.getId());
                chapitreData.put("termine", chapitreTermine);
                if (chapitreTermine) chapitresTerminesModule++;

                // Leçons du chapitre avec progression
                List<Lecon> lecons = chapitre.getLecons();
                if (lecons == null) lecons = new ArrayList<>();

                List<Map<String, Object>> leconsData = new ArrayList<>();
                int leconsTermineesChapitre = 0;

                for (int i = 0; i < lecons.size(); i++) {
                    Lecon lecon = lecons.get(i);
                    Map<String, Object> leconData = new LinkedHashMap<>();
                    leconData.put("lecon", lecon);
                    leconData.put("ordre", i + 1);

                    boolean leconTerminee = progressionCoursDAO.estLeconTerminee(utilisateurId, lecon.getId());
                    leconData.put("terminee", leconTerminee);
                    if (leconTerminee) leconsTermineesChapitre++;

                    // Accessibilité de la leçon
                    boolean leconAccessible;
                    if (i == 0) {
                        leconAccessible = debloque || moduleReussi;
                    } else {
                        leconAccessible = (debloque || moduleReussi) && 
                            progressionCoursDAO.estLeconTerminee(utilisateurId, lecons.get(i - 1).getId());
                    }
                    leconData.put("accessible", leconAccessible);

                    // Compter les exemples de code
                    if (lecon.hasCodeSource()) {
                        totalExemplesCode++;
                    }

                    leconsData.add(leconData);
                }

                chapitreData.put("lecons", leconsData);
                chapitreData.put("nbLecons", lecons.size());
                chapitreData.put("leconsTerminees", leconsTermineesChapitre);
                chapitreData.put("pourcentage", lecons.size() > 0 ? 
                    (leconsTermineesChapitre * 100 / lecons.size()) : 0);

                chapitresData.add(chapitreData);

                leconsModule += lecons.size();
                leconsTermineesModule += leconsTermineesChapitre;
            }

            moduleData.put("chapitres", chapitresData);
            moduleData.put("nbChapitres", chapitresModule.size());
            moduleData.put("chapitresTermines", chapitresTerminesModule);
            moduleData.put("nbLecons", leconsModule);
            moduleData.put("leconsTerminees", leconsTermineesModule);
            moduleData.put("pourcentage", leconsModule > 0 ? 
                (leconsTermineesModule * 100 / leconsModule) : 0);

            modulesData.add(moduleData);

            totalLeconsCours += leconsModule;
            totalLeconsTermineesCours += leconsTermineesModule;
        }

        // 10. Calculer les statistiques globales du cours
        boolean coursTermine = progressionCoursDAO.estCoursTermine(utilisateurId, coursId);
        int pourcentageGlobal = (totalLeconsCours > 0) ? 
            (totalLeconsTermineesCours * 100 / totalLeconsCours) : 0;

        // 11. Calculer le temps de lecture total estimé
        int tempsLectureTotalMinutes = 0;
        for (Chapitre chapitre : chapitres) {
            if (chapitre.getLecons() != null) {
                for (Lecon lecon : chapitre.getLecons()) {
                    tempsLectureTotalMinutes += lecon.getTempsLectureEstime();
                }
            }
        }
        String tempsLectureTotalFormate = formaterTempsLecture(tempsLectureTotalMinutes);

        // 12. Déterminer si l'examen final est accessible
        boolean examenAccessible = coursTermine && certificatExistant == null;
        boolean dejaCertifie = certificatExistant != null;

        // 13. Déterminer le prochain module à faire
        Integer prochainModule = null;
        for (int m = 1; m <= NOMBRE_MODULES; m++) {
            if (progressionModuleDAO.estModuleDebloque(utilisateurId, coursId, m) && 
                !progressionModuleDAO.estModuleReussi(utilisateurId, coursId, m)) {
                prochainModule = m;
                break;
            }
        }

        // 14. Message de motivation
        String messageMotivation;
        String iconeMotivation;
        if (dejaCertifie) {
            messageMotivation = "Félicitations ! Vous avez déjà obtenu le certificat pour ce cours.";
            iconeMotivation = "fas fa-crown";
        } else if (modulesReussis >= NOMBRE_MODULES) {
            messageMotivation = "Tous les modules sont terminés ! Passez l'examen final pour obtenir votre certificat.";
            iconeMotivation = "fas fa-trophy";
        } else if (modulesReussis >= NOMBRE_MODULES / 2) {
            messageMotivation = "Vous êtes à mi-chemin ! Continuez vos efforts pour terminer ce cours.";
            iconeMotivation = "fas fa-fire";
        } else if (modulesReussis > 0) {
            messageMotivation = "Bon début ! Chaque module réussi vous rapproche de la maîtrise de ce cours.";
            iconeMotivation = "fas fa-thumbs-up";
        } else {
            messageMotivation = "Commencez dès maintenant votre apprentissage par la lecture et la pratique !";
            iconeMotivation = "fas fa-rocket";
        }

        // 15. Passage des attributs à la JSP
        request.setAttribute("pageTitle", "Plan - " + cours.getTitre() + " - LearnPath");
        request.setAttribute("cours", cours);
        request.setAttribute("modules", modulesData);
        request.setAttribute("nombreModules", NOMBRE_MODULES);
        request.setAttribute("modulesReussis", modulesReussis);
        request.setAttribute("totalChapitres", chapitres.size());
        request.setAttribute("totalLecons", totalLeconsCours);
        request.setAttribute("totalLeconsTerminees", totalLeconsTermineesCours);
        request.setAttribute("totalExemplesCode", totalExemplesCode);
        request.setAttribute("tempsLectureTotal", tempsLectureTotalFormate);
        request.setAttribute("pourcentageGlobal", pourcentageGlobal);
        request.setAttribute("coursTermine", coursTermine);
        request.setAttribute("examenAccessible", examenAccessible);
        request.setAttribute("dejaCertifie", dejaCertifie);
        request.setAttribute("certificatExistant", certificatExistant);
        request.setAttribute("prochainModule", prochainModule);
        request.setAttribute("messageMotivation", messageMotivation);
        request.setAttribute("iconeMotivation", iconeMotivation);

        // 16. Affichage de la page
        request.getRequestDispatcher("/views/joueur/cours-plan.jsp").forward(request, response);
    }

    /**
     * Formate un temps en minutes en chaîne lisible
     * Exemples : 45 → "45 min", 120 → "2h", 150 → "2h30"
     * 
     * @param minutes Temps en minutes
     * @return Chaîne formatée
     */
    private String formaterTempsLecture(int minutes) {
        if (minutes == 0) {
            return "Moins d'1 min";
        }
        if (minutes < 60) {
            return minutes + " min";
        }
        int heures = minutes / 60;
        int minsRestantes = minutes % 60;
        if (minsRestantes == 0) {
            return heures + "h";
        }
        return heures + "h" + String.format("%02d", minsRestantes);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}