// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminCoursSupprimerServlet.java
// RÔLE    : Contrôleur pour supprimer ou archiver un cours (admin)
//           POST : Supprime si aucun apprenant/certificat
//                  Archive si des apprenants sont inscrits
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.CoursDAO;
import com.amijayquiz.models.Cours;
import com.amijayquiz.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AdminCoursSupprimerServlet - Suppression ou archivage d'un cours
 * 
 * POST : Deux modes possibles :
 *        - action=supprimer : Supprime définitivement (si aucun apprenant/certificat)
 *        - action=archiver  : Archive le cours (le rend invisible mais conserve les données)
 * 
 * RÈGLES MÉTIER :
 * - Si le cours a des apprenants → seul l'archivage est possible
 * - Si le cours a des certificats → seul l'archivage est possible
 * - Si le cours n'a ni apprenants ni certificats → suppression possible
 * 
 * URL : /admin/cours/supprimer
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminCoursSupprimerServlet", urlPatterns = {"/admin/cours/supprimer"})
public class AdminCoursSupprimerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // GET non autorisé
        response.sendRedirect(request.getContextPath() + "/admin/cours");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // 1. Vérification authentification + rôle admin
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (!"admin".equals(utilisateur.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        // 2. Récupération et validation de l'ID
        String idStr = request.getParameter("id");
        String action = request.getParameter("action");

        if (idStr == null || idStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID du cours manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr.trim());
            if (id <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de cours invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 3. Vérifier que le cours existe
        CoursDAO coursDAO = new CoursDAO();
        Cours cours = coursDAO.trouverParId(id);

        if (cours == null) {
            session.setAttribute("messageErreur", "Ce cours n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        String titreCours = cours.getTitre();
        String themeCours = cours.getTheme();
        String niveauCours = cours.getNiveauFormate();

        // 4. Vérifier les apprenants et certificats
        int nbApprenants = coursDAO.compterApprenants(id);
        int nbCertificats = coursDAO.compterCertificats(id);

        boolean peutSupprimer = (nbApprenants == 0 && nbCertificats == 0);

        // 5. Traitement selon l'action
        if ("supprimer".equals(action) && peutSupprimer) {
            // ===== SUPPRESSION DÉFINITIVE =====
            boolean supprime = coursDAO.supprimer(id);

            if (supprime) {
                session.setAttribute("messageSucces", 
                    "Cours \"" + titreCours + "\" supprimé définitivement avec succès !");

                System.out.println("✅ [AdminCoursSupprimer] Cours supprimé : ID=" + id + 
                    " - Titre=" + titreCours + " - Thème=" + themeCours + " - Niveau=" + niveauCours);

            } else {
                session.setAttribute("messageErreur", 
                    "Erreur lors de la suppression du cours. Veuillez réessayer.");

                System.err.println("❌ [AdminCoursSupprimer] Échec suppression : ID=" + id);
            }

        } else if ("archiver".equals(action)) {
            // ===== ARCHIVAGE =====
            cours.setStatut(Cours.STATUT_ARCHIVE);
            boolean archive = coursDAO.modifier(cours);

            if (archive) {
                session.setAttribute("messageSucces", 
                    "Cours \"" + titreCours + "\" archivé avec succès ! " +
                    "Les apprenants qui l'ont déjà commencé peuvent toujours y accéder.");

                System.out.println("✅ [AdminCoursSupprimer] Cours archivé : ID=" + id + 
                    " - Titre=" + titreCours);

            } else {
                session.setAttribute("messageErreur", 
                    "Erreur lors de l'archivage du cours. Veuillez réessayer.");

                System.err.println("❌ [AdminCoursSupprimer] Échec archivage : ID=" + id);
            }

        } else if ("supprimer".equals(action) && !peutSupprimer) {
            // ===== TENTATIVE DE SUPPRESSION IMPOSSIBLE =====
            StringBuilder raison = new StringBuilder();
            if (nbApprenants > 0) {
                raison.append("• ").append(nbApprenants).append(" apprenant(s) inscrit(s)\n");
            }
            if (nbCertificats > 0) {
                raison.append("• ").append(nbCertificats).append(" certificat(s) délivré(s)\n");
            }

            session.setAttribute("messageErreur", 
                "Impossible de supprimer le cours \"" + titreCours + "\".\n" +
                raison.toString() +
                "Utilisez l'archivage à la place.");

            System.out.println("⚠️ [AdminCoursSupprimer] Suppression bloquée : ID=" + id + 
                " - Apprenants=" + nbApprenants + " - Certificats=" + nbCertificats);

        } else {
            // Action invalide
            session.setAttribute("messageErreur", "Action invalide. Utilisez 'supprimer' ou 'archiver'.");
        }

        // 6. Redirection
        response.sendRedirect(request.getContextPath() + "/admin/cours");
    }
}