// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/LeconCoursServlet.java
// RÔLE    : Contrôleur de la page d'une leçon de cours
//           Charge le contenu de la leçon, la progression,
//           et les données des modules pour la barre latérale
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.ChapitreDAO;
import com.amijayquiz.dao.CoursDAO;
import com.amijayquiz.dao.ExamenModuleDAO;
import com.amijayquiz.dao.LeconDAO;
import com.amijayquiz.dao.ProgressionCoursDAO;
import com.amijayquiz.dao.ProgressionModuleDAO;
import com.amijayquiz.models.Chapitre;
import com.amijayquiz.models.Cours;
import com.amijayquiz.models.ExamenModule;
import com.amijayquiz.models.Lecon;
import com.amijayquiz.models.ProgressionCours;
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

@WebServlet(name = "LeconCoursServlet", urlPatterns = {"/joueur/cours/lecon"})
public class LeconCoursServlet extends HttpServlet {

    private static final int NOMBRE_MODULES = 15;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. VÉRIFICATION DE LA SESSION
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

        // 2. RÉCUPÉRATION DES PARAMÈTRES
        String coursIdParam = request.getParameter("coursId");
        String chapitreIdParam = request.getParameter("chapitreId");
        String leconIdParam = request.getParameter("leconId");

        if (coursIdParam == null || coursIdParam.trim().isEmpty() ||
            chapitreIdParam == null || chapitreIdParam.trim().isEmpty() ||
            leconIdParam == null || leconIdParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        int coursId, chapitreId, leconId;
        try {
            coursId = Integer.parseInt(coursIdParam.trim());
            chapitreId = Integer.parseInt(chapitreIdParam.trim());
            leconId = Integer.parseInt(leconIdParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        // 3. INITIALISATION DES DAO
        CoursDAO coursDAO = new CoursDAO();
        ChapitreDAO chapitreDAO = new ChapitreDAO();
        LeconDAO leconDAO = new LeconDAO();
        ProgressionCoursDAO progressionCoursDAO = new ProgressionCoursDAO();
        ProgressionModuleDAO progressionModuleDAO = new ProgressionModuleDAO();
        ExamenModuleDAO examenModuleDAO = new ExamenModuleDAO();

        // 4. RÉCUPÉRATION DU COURS
        Cours cours = coursDAO.trouverParId(coursId);
        if (cours == null) {
            session.setAttribute("messageErreur", "Ce cours n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        // 5. RÉCUPÉRATION DU CHAPITRE
        Chapitre chapitre = chapitreDAO.trouverParId(chapitreId);
        if (chapitre == null || chapitre.getCoursId() != coursId) {
            session.setAttribute("messageErreur", "Ce chapitre n'existe pas dans ce cours.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + coursId);
            return;
        }

        // 6. RÉCUPÉRATION DE LA LEÇON
        Lecon lecon = leconDAO.trouverParId(leconId);
        if (lecon == null || lecon.getChapitreId() != chapitreId) {
            session.setAttribute("messageErreur", "Cette leçon n'existe pas dans ce chapitre.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours/chapitre?coursId=" +
                    coursId + "&chapitreId=" + chapitreId);
            return;
        }

        // 7. DÉTERMINER LE MODULE ACTIF
        int moduleActifNumero = chapitre.getModuleNumero();
        if (moduleActifNumero <= 0) moduleActifNumero = 1;

        // 8. VÉRIFIER SI LE MODULE EST DÉBLOQUÉ
        boolean moduleDebloque = progressionModuleDAO.estModuleDebloque(utilisateurId, coursId, moduleActifNumero);
        boolean moduleReussi = progressionModuleDAO.estModuleReussi(utilisateurId, coursId, moduleActifNumero);

        if (!moduleDebloque && !moduleReussi) {
            session.setAttribute("messageErreur",
                    "Ce module n'est pas encore débloqué. Réussissez d'abord le module " +
                    (moduleActifNumero - 1) + ".");
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + coursId);
            return;
        }

        // 9. ENREGISTRER LE DÉBUT DE LA LEÇON
        progressionCoursDAO.enregistrerDebut(utilisateurId, coursId, chapitreId, leconId);

        // 10. RÉCUPÉRER LA PROGRESSION
        ProgressionCours progression = progressionCoursDAO.getProgression(utilisateurId, leconId);

        // 11. CHARGER TOUS LES CHAPITRES
        List<Chapitre> tousLesChapitres = chapitreDAO.listerParCours(coursId);
        cours.setChapitres(tousLesChapitres);

        // 12. FILTRER CHAPITRES DU MODULE ACTIF
        List<Chapitre> chapitresDuModule = new ArrayList<>();
        for (Chapitre chap : tousLesChapitres) {
            int chapModule = chap.getModuleNumero();
            if (chapModule <= 0) chapModule = 1;
            if (chapModule == moduleActifNumero) {
                chapitresDuModule.add(chap);
            }
        }

        // 13. LISTE DES LEÇONS DU MODULE
        List<Lecon> toutesLesLeconsDuModule = new ArrayList<>();
        for (Chapitre chap : chapitresDuModule) {
            List<Lecon> leconsChap = leconDAO.listerParChapitre(chap.getId());
            if (leconsChap != null) {
                toutesLesLeconsDuModule.addAll(leconsChap);
            }
        }

        // 14. LEÇON PRÉCÉDENTE ET SUIVANTE
        Lecon leconPrecedente = null;
        Lecon leconSuivante = null;
        for (int i = 0; i < toutesLesLeconsDuModule.size(); i++) {
            Lecon leconCourante = toutesLesLeconsDuModule.get(i);
            if (leconCourante.getId() == leconId) {
                if (i > 0) leconPrecedente = toutesLesLeconsDuModule.get(i - 1);
                if (i < toutesLesLeconsDuModule.size() - 1) leconSuivante = toutesLesLeconsDuModule.get(i + 1);
                break;
            }
        }

        boolean estDerniereLeconDuModule = (leconSuivante == null);

        // 15. VÉRIFIER TOUS LES MODULES RÉUSSIS
        boolean tousModulesReussis = true;
        for (int m = 1; m <= NOMBRE_MODULES; m++) {
            if (!progressionModuleDAO.estModuleReussi(utilisateurId, coursId, m)) {
                tousModulesReussis = false;
                break;
            }
        }

        // 16. RÉCUPÉRER EXAMENS
        List<ExamenModule> examens = examenModuleDAO.listerParCours(coursId);

        // 17. CONSTRUIRE DONNÉES MODULES
        List<Map<String, Object>> modulesData = new ArrayList<>();
        for (int moduleNumero = 1; moduleNumero <= NOMBRE_MODULES; moduleNumero++) {
            Map<String, Object> moduleData = new LinkedHashMap<>();
            moduleData.put("numero", moduleNumero);

            boolean debloque = progressionModuleDAO.estModuleDebloque(utilisateurId, coursId, moduleNumero);
            boolean reussi = progressionModuleDAO.estModuleReussi(utilisateurId, coursId, moduleNumero);
            moduleData.put("debloque", debloque);
            moduleData.put("reussi", reussi);

            ProgressionModule progressionModule = progressionModuleDAO.getProgression(utilisateurId, coursId, moduleNumero);
            moduleData.put("progression", progressionModule);

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

            List<Chapitre> chapitresModule = new ArrayList<>();
            for (Chapitre chap : tousLesChapitres) {
                int chapModule = chap.getModuleNumero();
                if (chapModule <= 0) chapModule = 1;
                if (chapModule == moduleNumero) {
                    chapitresModule.add(chap);
                }
            }

            List<Map<String, Object>> chapitresData = new ArrayList<>();
            int nbLeconsModule = 0;

            for (Chapitre chap : chapitresModule) {
                Map<String, Object> chapitreData = new LinkedHashMap<>();
                chapitreData.put("chapitre", chap);

                boolean chapTermine = progressionCoursDAO.estChapitreTermine(utilisateurId, chap.getId());
                chapitreData.put("termine", chapTermine);

                List<Lecon> leconsChap = leconDAO.listerParChapitre(chap.getId());
                if (leconsChap == null) leconsChap = new ArrayList<>();

                List<Map<String, Object>> leconsData = new ArrayList<>();
                for (int i = 0; i < leconsChap.size(); i++) {
                    Lecon leconDuChap = leconsChap.get(i);
                    Map<String, Object> leconData = new LinkedHashMap<>();
                    leconData.put("lecon", leconDuChap);
                    leconData.put("ordre", i + 1);

                    boolean leconTerminee = progressionCoursDAO.estLeconTerminee(utilisateurId, leconDuChap.getId());
                    leconData.put("terminee", leconTerminee);

                    boolean leconAccessible = (debloque || reussi);
                    leconData.put("accessible", leconAccessible);

                    leconsData.add(leconData);
                }

                chapitreData.put("lecons", leconsData);
                chapitreData.put("nbLecons", leconsChap.size());
                chapitresData.add(chapitreData);
                nbLeconsModule += leconsChap.size();
            }

            moduleData.put("chapitres", chapitresData);
            moduleData.put("nbChapitres", chapitresModule.size());
            moduleData.put("nbLecons", nbLeconsModule);
            modulesData.add(moduleData);
        }

        // 18. ATTRIBUTS JSP
        request.setAttribute("pageTitle", lecon.getTitre() + " - " + cours.getTitre() + " - AmijayQuiz");
        request.setAttribute("cours", cours);
        request.setAttribute("chapitre", chapitre);
        request.setAttribute("lecon", lecon);
        request.setAttribute("progression", progression);
        request.setAttribute("leconPrecedente", leconPrecedente);
        request.setAttribute("leconSuivante", leconSuivante);
        request.setAttribute("estDerniereLecon", estDerniereLeconDuModule);
        request.setAttribute("moduleDebloque", moduleDebloque);
        request.setAttribute("moduleReussi", moduleReussi);
        request.setAttribute("moduleSuivant", moduleActifNumero < NOMBRE_MODULES ? moduleActifNumero + 1 : 0);
        request.setAttribute("tousChapitresTermines", tousModulesReussis);
        request.setAttribute("totalLeconsChapitre", leconDAO.compterParChapitre(chapitreId));
        request.setAttribute("modules", modulesData);
        request.setAttribute("moduleActiveNumero", moduleActifNumero);
        request.setAttribute("leconActiveId", lecon.getId());
        request.setAttribute("chapitreActiveId", chapitre.getId());

        // 19. AFFICHAGE
        request.getRequestDispatcher("/views/joueur/cours-lecon.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}