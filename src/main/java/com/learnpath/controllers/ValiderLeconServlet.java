// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/ValiderLeconServlet.java
// RÔLE    : Contrôleur de validation d'une leçon
//           Marque la leçon comme terminée et redirige vers la suite
//           Navigation intelligente dans le module :
//           - Leçon suivante dans le module (peut être dans un autre chapitre)
//           - Examen du module si dernière leçon du module
//           - Module suivant si module déjà réussi
//           - Examen final si tous les modules sont réussis
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.ChapitreDAO;
import com.learnpath.dao.CoursDAO;
import com.learnpath.dao.ExamenModuleDAO;
import com.learnpath.dao.LeconDAO;
import com.learnpath.dao.ProgressionCoursDAO;
import com.learnpath.dao.ProgressionModuleDAO;
import com.learnpath.models.Chapitre;
import com.learnpath.models.Cours;
import com.learnpath.models.Lecon;
import com.learnpath.models.Utilisateur;

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
 * ValiderLeconServlet - Validation d'une leçon terminée
 * 
 * Marque la leçon comme terminée et redirige vers :
 * - La leçon suivante dans le même module (même si dans un autre chapitre)
 * - L'examen du module si c'était la dernière leçon du module
 * - Le module suivant si le module actuel est déjà réussi
 * - L'examen final si tous les modules sont réussis
 * 
 * URL : /joueur/cours/valider (POST)
 * 
 * @author LearnPath
 * @version 2.0
 */
@WebServlet(name = "ValiderLeconServlet", urlPatterns = {"/joueur/cours/valider"})
public class ValiderLeconServlet extends HttpServlet {

    private static final int NOMBRE_MODULES = 15;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // GET non autorisé, rediriger vers les cours
        response.sendRedirect(request.getContextPath() + "/joueur/cours");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

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
        // 2. RÉCUPÉRATION DES PARAMÈTRES
        // ============================================================
        String coursIdParam = request.getParameter("coursId");
        String chapitreIdParam = request.getParameter("chapitreId");
        String leconIdParam = request.getParameter("leconId");
        String tempsPasseParam = request.getParameter("tempsPasse");

        if (coursIdParam == null || chapitreIdParam == null || leconIdParam == null) {
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        int coursId;
        int chapitreId;
        int leconId;
        int tempsPasse = 0;

        try {
            coursId = Integer.parseInt(coursIdParam);
            chapitreId = Integer.parseInt(chapitreIdParam);
            leconId = Integer.parseInt(leconIdParam);
            if (tempsPasseParam != null && !tempsPasseParam.isEmpty()) {
                tempsPasse = Integer.parseInt(tempsPasseParam);
            }
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

        // ============================================================
        // 4. VÉRIFIER QUE LE COURS, LE CHAPITRE ET LA LEÇON EXISTENT
        // ============================================================
        Cours cours = coursDAO.trouverParId(coursId);
        if (cours == null) {
            session.setAttribute("messageErreur", "Ce cours n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours");
            return;
        }

        Chapitre chapitre = chapitreDAO.trouverParId(chapitreId);
        if (chapitre == null || chapitre.getCoursId() != coursId) {
            session.setAttribute("messageErreur", "Ce chapitre n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + coursId);
            return;
        }

        Lecon lecon = leconDAO.trouverParId(leconId);
        if (lecon == null || lecon.getChapitreId() != chapitreId) {
            session.setAttribute("messageErreur", "Cette leçon n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours/chapitre?coursId=" + 
                coursId + "&chapitreId=" + chapitreId);
            return;
        }

        // ============================================================
        // 5. DÉTERMINER LE MODULE ACTIF
        // ============================================================
        int moduleActifNumero = chapitre.getModuleNumero();
        if (moduleActifNumero <= 0) {
            moduleActifNumero = 1;
        }

        // ============================================================
        // 6. MARQUER LA LEÇON COMME TERMINÉE
        // ============================================================
        progressionCoursDAO.marquerTermine(utilisateurId, leconId);

        // Ajouter le temps passé si fourni
        if (tempsPasse > 0) {
            progressionCoursDAO.ajouterTemps(utilisateurId, leconId, tempsPasse);
        }

        // ============================================================
        // 7. CONSTRUIRE LA LISTE DE TOUTES LES LEÇONS DU MODULE
        //    (tous les chapitres appartenant à ce module)
        // ============================================================
        List<Chapitre> tousLesChapitres = chapitreDAO.listerParCours(coursId);

        // Filtrer les chapitres du module actif
        List<Chapitre> chapitresDuModule = new ArrayList<>();
        for (Chapitre chap : tousLesChapitres) {
            int chapModule = chap.getModuleNumero() > 0 ? chap.getModuleNumero() : 1;
            if (chapModule == moduleActifNumero) {
                chapitresDuModule.add(chap);
            }
        }

        // Construire la liste ordonnée de toutes les leçons du module
        List<Lecon> toutesLesLeconsDuModule = new ArrayList<>();
        for (Chapitre chap : chapitresDuModule) {
            List<Lecon> leconsChap = leconDAO.listerParChapitre(chap.getId());
            if (leconsChap != null) {
                toutesLesLeconsDuModule.addAll(leconsChap);
            }
        }

        // ============================================================
        // 8. TROUVER LA LEÇON SUIVANTE DANS LE MODULE
        // ============================================================
        Lecon leconSuivante = null;
        int prochainChapitreId = 0;

        for (int i = 0; i < toutesLesLeconsDuModule.size(); i++) {
            Lecon l = toutesLesLeconsDuModule.get(i);
            if (l.getId() == leconId) {
                if (i < toutesLesLeconsDuModule.size() - 1) {
                    leconSuivante = toutesLesLeconsDuModule.get(i + 1);
                    prochainChapitreId = leconSuivante.getChapitreId();
                }
                break;
            }
        }

        // ============================================================
        // 9. SI UNE LEÇON SUIVANTE EXISTE DANS LE MODULE → REDIRIGER
        // ============================================================
        if (leconSuivante != null) {
            session.setAttribute("messageSucces", 
                "✅ Leçon \"" + lecon.getTitre() + "\" terminée ! Passage à la leçon suivante.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours/lecon?coursId=" + 
                coursId + "&chapitreId=" + prochainChapitreId + "&leconId=" + leconSuivante.getId());
            return;
        }

        // ============================================================
        // 10. C'ÉTAIT LA DERNIÈRE LEÇON DU MODULE
        //     → Vérifier si un examen de module existe
        // ============================================================
        boolean examenModuleExiste = examenModuleDAO.trouverParCoursEtModule(coursId, moduleActifNumero) != null;
        boolean moduleDejaReussi = progressionModuleDAO.estModuleReussi(utilisateurId, coursId, moduleActifNumero);

        if (examenModuleExiste && !moduleDejaReussi) {
            // Rediriger vers l'examen du module
            session.setAttribute("messageSucces", 
                "📚 Toutes les leçons du Module " + moduleActifNumero + " sont terminées ! " +
                "Passez l'examen pour valider ce module.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours/examen-module?coursId=" + 
                coursId + "&module=" + moduleActifNumero);
            return;
        }

        // ============================================================
        // 11. LE MODULE EST RÉUSSI OU PAS D'EXAMEN
        //     → Chercher le prochain module
        // ============================================================
        int prochainModule = moduleActifNumero + 1;
        boolean prochainModuleDebloque = false;

        if (prochainModule <= NOMBRE_MODULES) {
            prochainModuleDebloque = progressionModuleDAO.estModuleDebloque(utilisateurId, coursId, prochainModule);
        }

        if (prochainModuleDebloque && prochainModule <= NOMBRE_MODULES) {
            // Trouver la première leçon du module suivant
            Lecon premiereLeconProchainModule = trouverPremiereLeconModule(
                tousLesChapitres, prochainModule, leconDAO);

            if (premiereLeconProchainModule != null) {
                session.setAttribute("messageSucces", 
                    "✅ Module " + moduleActifNumero + " terminé ! Passage au Module " + prochainModule + ".");
                response.sendRedirect(request.getContextPath() + "/joueur/cours/lecon?coursId=" + 
                    coursId + "&chapitreId=" + premiereLeconProchainModule.getChapitreId() + 
                    "&leconId=" + premiereLeconProchainModule.getId());
                return;
            }
        }

        // ============================================================
        // 12. VÉRIFIER SI TOUS LES MODULES SONT RÉUSSIS → EXAMEN FINAL
        // ============================================================
        boolean tousModulesReussis = true;
        for (int m = 1; m <= NOMBRE_MODULES; m++) {
            if (!progressionModuleDAO.estModuleReussi(utilisateurId, coursId, m)) {
                tousModulesReussis = false;
                break;
            }
        }

        if (tousModulesReussis) {
            session.setAttribute("messageSucces", 
                "🎉 Tous les modules sont réussis ! Vous pouvez passer l'examen final.");
            response.sendRedirect(request.getContextPath() + "/joueur/cours/examen?coursId=" + coursId);
            return;
        }

        // ============================================================
        // 13. FALLBACK : RETOUR AU PLAN DU COURS
        // ============================================================
        session.setAttribute("messageSucces", 
            "✅ Leçon \"" + lecon.getTitre() + "\" terminée !");
        response.sendRedirect(request.getContextPath() + "/joueur/cours/plan?coursId=" + coursId);
    }

    // ============================================================
    // MÉTHODE UTILITAIRE
    // ============================================================

    /**
     * Trouve la première leçon d'un module donné
     * 
     * @param tousLesChapitres Liste de tous les chapitres du cours
     * @param moduleNumero    Numéro du module recherché
     * @param leconDAO        DAO des leçons
     * @return La première leçon du module, ou null si aucune
     */
    private Lecon trouverPremiereLeconModule(List<Chapitre> tousLesChapitres, 
                                              int moduleNumero, 
                                              LeconDAO leconDAO) {
        for (Chapitre chap : tousLesChapitres) {
            int chapModule = chap.getModuleNumero() > 0 ? chap.getModuleNumero() : 1;
            if (chapModule == moduleNumero) {
                List<Lecon> lecons = leconDAO.listerParChapitre(chap.getId());
                if (lecons != null && !lecons.isEmpty()) {
                    return lecons.get(0);
                }
            }
        }
        return null;
    }
}