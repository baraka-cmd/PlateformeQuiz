// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/PlanCoursServlet.java
// RÔLE    : Contrôleur du plan détaillé d'un cours
//           Affiche l'arborescence complète du cours :
//           tous les modules (1 à 15) avec chapitres, leçons,
//           examens et progression par module
//           Navigation vers les leçons via la page chapitre
//           + Gestion des cours payants et vérification d'accès
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
import com.amijayquiz.services.PaymentService;
import com.amijayquiz.services.CourseService;

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

@WebServlet(name = "PlanCoursServlet", urlPatterns = {"/joueur/cours/plan"})
public class PlanCoursServlet extends HttpServlet {

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
        // 4. RÉCUPÉRER LE COURS
        // ============================================================
        Cours cours = coursDAO.trouverParId(coursId);
        if (cours == null) {
            session.setAttribute("messageErreur", "Ce cours n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        List<Chapitre> chapitres = chapitreDAO.listerParCours(coursId);
        cours.setChapitres(chapitres);
        cours.setNombreChapitres(chapitres.size());
        cours.setNombreLecons(cours.getNombreTotalLecons());

        // ============================================================
        // 5. VÉRIFIER LE CERTIFICAT
        // ============================================================
        Certificat certificatExistant = certificatDAO.trouverParUtilisateurEtCours(utilisateurId, coursId);

        // ============================================================
        // 6. GESTION DES COURS PAYANTS (CORRIGÉE)
        // ============================================================
        double coursPrix = CourseService.getCoursePrice(coursId);
        boolean coursPayant = coursPrix > 0;
        boolean accesPayant = false;
        
        if (coursPayant) {
            accesPayant = PaymentService.canAccessCourse(utilisateurId, coursId);
            // ✅ CORRECTION : Seulement si cours PAYANT ET pas d'accès → rediriger
            if (!accesPayant) {
                session.setAttribute("messageErreur", "Ce cours est payant. Veuillez acheter l'accès pour continuer.");
                response.sendRedirect(request.getContextPath() + "/joueur/paiement/checkout?courseId=" + coursId);
                return;
            }
        }
        // ✅ Les cours GRATUITS continuent normalement (pas de redirection)

        // ============================================================
        // 7. ORGANISER LES CHAPITRES PAR MODULE
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
        // 8. RÉCUPÉRER LES EXAMENS
        // ============================================================
        List<ExamenModule> examens = examenModuleDAO.listerParCours(coursId);

        // ============================================================
        // 9. CONSTRUIRE LES DONNÉES DES MODULES
        // ============================================================
        List<Map<String, Object>> modulesData = new ArrayList<>();
        int totalLeconsCours = 0;
        int totalLeconsTermineesCours = 0;
        int totalExemplesCode = 0;
        int modulesReussis = 0;

        for (int moduleNumero = 1; moduleNumero <= NOMBRE_MODULES; moduleNumero++) {
            Map<String, Object> moduleData = new LinkedHashMap<>();
            moduleData.put("numero", moduleNumero);

            boolean debloque = progressionModuleDAO.estModuleDebloque(utilisateurId, coursId, moduleNumero);
            moduleData.put("debloque", debloque);

            ProgressionModule progression = progressionModuleDAO.getProgression(utilisateurId, coursId, moduleNumero);
            boolean moduleReussi = progression != null && progression.isReussi();
            moduleData.put("reussi", moduleReussi);
            moduleData.put("progression", progression);

            if (moduleReussi) {
                modulesReussis++;
            }

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

            List<Chapitre> chapitresModule = chapitresParModule.getOrDefault(moduleNumero, new ArrayList<>());
            moduleData.put("nbChapitres", chapitresModule.size());

            List<Map<String, Object>> chapitresData = new ArrayList<>();
            int leconsModule = 0;
            int leconsTermineesModule = 0;

            for (Chapitre chapitre : chapitresModule) {
                Map<String, Object> chapitreData = new LinkedHashMap<>();
                chapitreData.put("chapitre", chapitre);

                boolean chapitreTermine = progressionCoursDAO.estChapitreTermine(utilisateurId, chapitre.getId());
                chapitreData.put("termine", chapitreTermine);

                List<Lecon> lecons = leconDAO.listerParChapitre(chapitre.getId());
                if (lecons == null) lecons = new ArrayList<>();
                chapitre.setLecons(lecons);

                List<Map<String, Object>> leconsData = new ArrayList<>();

                for (int i = 0; i < lecons.size(); i++) {
                    Lecon lecon = lecons.get(i);
                    Map<String, Object> leconData = new LinkedHashMap<>();
                    leconData.put("lecon", lecon);
                    leconData.put("ordre", i + 1);

                    boolean leconTerminee = progressionCoursDAO.estLeconTerminee(utilisateurId, lecon.getId());
                    leconData.put("terminee", leconTerminee);
                    if (leconTerminee) leconsTermineesModule++;

                    boolean leconAccessible = debloque || moduleReussi;
                    leconData.put("accessible", leconAccessible);

                    if (lecon.hasCodeSource()) {
                        totalExemplesCode++;
                    }

                    leconsData.add(leconData);
                }

                chapitreData.put("lecons", leconsData);
                chapitreData.put("nbLecons", lecons.size());

                chapitresData.add(chapitreData);

                leconsModule += lecons.size();
                leconsTermineesModule += leconsTermineesModule;
            }

            moduleData.put("chapitres", chapitresData);
            moduleData.put("nbLecons", leconsModule);
            moduleData.put("leconsTerminees", leconsTermineesModule);

            modulesData.add(moduleData);

            totalLeconsCours += leconsModule;
            totalLeconsTermineesCours += leconsTermineesModule;
        }

        // ============================================================
        // 10. STATISTIQUES GLOBALES
        // ============================================================
        boolean coursTermine = progressionCoursDAO.estCoursTermine(utilisateurId, coursId);
        int pourcentageGlobal = (totalLeconsCours > 0) ? 
            (totalLeconsTermineesCours * 100 / totalLeconsCours) : 0;

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

        boolean examenAccessible = modulesReussis >= NOMBRE_MODULES && certificatExistant == null;
        boolean dejaCertifie = certificatExistant != null;

        // ============================================================
        // 11. MESSAGE DE MOTIVATION
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
        // 12. ATTRIBUTS JSP
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
        request.setAttribute("messageMotivation", messageMotivation);
        request.setAttribute("iconeMotivation", iconeMotivation);
        request.setAttribute("coursPrix", coursPrix);
        request.setAttribute("coursPayant", coursPayant);
        request.setAttribute("accesPayant", accesPayant);

        // ============================================================
        // 13. AFFICHAGE
        // ============================================================
        request.getRequestDispatcher("/views/joueur/cours-plan.jsp").forward(request, response);
    }

    private String formaterTempsLecture(int minutes) {
        if (minutes == 0) return "Moins d'1 min";
        if (minutes < 60) return minutes + " min";
        int heures = minutes / 60;
        int minsRestantes = minutes % 60;
        if (minsRestantes == 0) return heures + "h";
        return heures + "h" + String.format("%02d", minsRestantes);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}