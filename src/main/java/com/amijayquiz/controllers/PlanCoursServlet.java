// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/PlanCoursServlet.java
// RÔLE    : Contrôleur du plan détaillé d'un cours
//           Affiche l'arborescence complète du cours :
//           tous les modules (1 à 15) avec chapitres, leçons,
//           examens et progression par module
//           Navigation vers les leçons via la page chapitre
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.CertificatDAO;
import com.amijayquiz.dao.ChapitreDAO;
import com.amijayquiz.dao.CoursDAO;
import com.amijayquiz.dao.ExamenModuleDAO;
import com.amijayquiz.dao.LeconDAO;
import com.amijayquiz.dao.ProgressionCoursDAO;
import com.amijayquiz.dao.ProgressionModuleDAO;
import com.amijayquiz.models.Certificat;
import com.amijayquiz.models.Chapitre;
import com.amijayquiz.models.Cours;
import com.amijayquiz.models.ExamenModule;
import com.amijayquiz.models.Lecon;
import com.amijayquiz.models.ProgressionModule;
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
 * PlanCoursServlet - Plan détaillé d'un cours organisé par modules
 * 
 * Affiche l'arborescence complète du cours regroupée par modules (1 à 15).
 * Chaque module contient :
 * - Ses chapitres avec leurs leçons
 * - Le statut de progression (verrouillé, débloqué, réussi)
 * - Un bouton pour passer l'examen du module
 * - Des liens pour accéder aux chapitres et leçons
 * 
 * URL : /joueur/cours/plan?coursId=1
 * 
 * @author AmijayQuiz
 * @version 2.1
 */
@WebServlet(name = "PlanCoursServlet", urlPatterns = {"/joueur/cours/plan"})
public class PlanCoursServlet extends HttpServlet {

    // Constantes des modules
    private static final int NOMBRE_MODULES = 15;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ============================================================
        // 1. VÉRIFICATION DE LA SESSION ET DU RÔLE
        // ============================================================
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

        // ============================================================
        // 2. RÉCUPÉRATION ET VALIDATION DU PARAMÈTRE COURSID
        // ============================================================
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

        // ============================================================
        // 3. INITIALISATION DES DAO
        // ============================================================
        CoursDAO coursDAO = new CoursDAO();
        ChapitreDAO chapitreDAO = new ChapitreDAO();
        LeconDAO leconDAO = new LeconDAO();
        ProgressionCoursDAO progressionCoursDAO = new ProgressionCoursDAO();
        ProgressionModuleDAO progressionModuleDAO = new ProgressionModuleDAO();
        ExamenModuleDAO examenModuleDAO = new ExamenModuleDAO();
        CertificatDAO certificatDAO = new CertificatDAO();

        // ============================================================
        // 4. RÉCUPÉRER LE COURS COMPLET AVEC SES CHAPITRES ET LEÇONS
        // ============================================================
        Cours cours = coursDAO.trouverParId(coursId);
        if (cours == null) {
            session.setAttribute("messageErreur", "Ce cours n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        // Récupérer tous les chapitres avec leurs leçons
        List<Chapitre> chapitres = chapitreDAO.listerParCours(coursId);
        cours.setChapitres(chapitres);
        cours.setNombreChapitres(chapitres.size());
        cours.setNombreLecons(cours.getNombreTotalLecons());

        // ============================================================
        // 5. VÉRIFIER SI L'UTILISATEUR A DÉJÀ UN CERTIFICAT
        // ============================================================
        Certificat certificatExistant = certificatDAO.trouverParUtilisateurEtCours(utilisateurId, coursId);

        // ============================================================
        // 6. ORGANISER LES CHAPITRES PAR MODULE
        // ============================================================
        Map<Integer, List<Chapitre>> chapitresParModule = new LinkedHashMap<>();
        for (Chapitre chapitre : chapitres) {
            int moduleNumero = chapitre.getModuleNumero() > 0 ? chapitre.getModuleNumero() : 1;
            if (!chapitresParModule.containsKey(moduleNumero)) {
                chapitresParModule.put(moduleNumero, new ArrayList<>());
            }
            chapitresParModule.get(moduleNumero).add(chapitre);
        }

        // ============================================================
        // 7. RÉCUPÉRER TOUS LES EXAMENS DU COURS
        // ============================================================
        List<ExamenModule> examens = examenModuleDAO.listerParCours(coursId);

        // ============================================================
        // 8. CONSTRUIRE LES DONNÉES DES MODULES AVEC PROGRESSION
        // ============================================================
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

            // Trouver la première leçon du module (pour le bouton "Commencer")
            int premiereLeconId = 0;
            int premierChapitreId = 0;

            for (Chapitre chapitre : chapitresModule) {
                Map<String, Object> chapitreData = new LinkedHashMap<>();
                chapitreData.put("chapitre", chapitre);

                boolean chapitreTermine = progressionCoursDAO.estChapitreTermine(utilisateurId, chapitre.getId());
                chapitreData.put("termine", chapitreTermine);
                if (chapitreTermine) chapitresTerminesModule++;

                // Leçons du chapitre avec progression
                List<Lecon> lecons = leconDAO.listerParChapitre(chapitre.getId());
                if (lecons == null) lecons = new ArrayList<>();
                chapitre.setLecons(lecons);

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

                    // Si le module est débloqué ou réussi, toutes les leçons sont accessibles
                    boolean leconAccessible = debloque || moduleReussi;
                    leconData.put("accessible", leconAccessible);

                    // Compter les exemples de code
                    if (lecon.hasCodeSource()) {
                        totalExemplesCode++;
                    }

                    // Mémoriser la première leçon accessible du module
                    if (leconAccessible && premiereLeconId == 0) {
                        premiereLeconId = lecon.getId();
                        premierChapitreId = chapitre.getId();
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
            moduleData.put("premiereLeconId", premiereLeconId);
            moduleData.put("premierChapitreId", premierChapitreId);

            modulesData.add(moduleData);

            totalLeconsCours += leconsModule;
            totalLeconsTermineesCours += leconsTermineesModule;
        }

        // ============================================================
        // 9. CALCULER LES STATISTIQUES GLOBALES DU COURS
        // ============================================================
        boolean coursTermine = progressionCoursDAO.estCoursTermine(utilisateurId, coursId);
        int pourcentageGlobal = (totalLeconsCours > 0) ? 
            (totalLeconsTermineesCours * 100 / totalLeconsCours) : 0;

        // Calculer le temps de lecture total estimé
        int tempsLectureTotalMinutes = 0;
        for (Chapitre chapitre : chapitres) {
            List<Lecon> leconsChap = leconDAO.listerParChapitre(chapitre.getId());
            if (leconsChap != null) {
                for (Lecon lecon : leconsChap) {
                    tempsLectureTotalMinutes += lecon.getTempsLectureEstime();
                }
            }
        }
        String tempsLectureTotalFormate = formaterTempsLecture(tempsLectureTotalMinutes);

        // ============================================================
        // 10. DÉTERMINER SI L'EXAMEN FINAL EST ACCESSIBLE
        // ============================================================
        boolean examenAccessible = modulesReussis >= NOMBRE_MODULES && certificatExistant == null;
        boolean dejaCertifie = certificatExistant != null;

        // ============================================================
        // 11. DÉTERMINER LE PROCHAIN MODULE À FAIRE
        // ============================================================
        Integer prochainModule = null;
        for (int m = 1; m <= NOMBRE_MODULES; m++) {
            if (progressionModuleDAO.estModuleDebloque(utilisateurId, coursId, m) && 
                !progressionModuleDAO.estModuleReussi(utilisateurId, coursId, m)) {
                prochainModule = m;
                break;
            }
        }

        // ============================================================
        // 12. MESSAGE DE MOTIVATION
        // ============================================================
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

        // ============================================================
        // 13. PASSAGE DES ATTRIBUTS À LA JSP
        // ============================================================
        request.setAttribute("pageTitle", "Plan - " + cours.getTitre() + " - AmijayQuiz");
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

        // ============================================================
        // 14. AFFICHAGE DE LA PAGE
        // ============================================================
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


