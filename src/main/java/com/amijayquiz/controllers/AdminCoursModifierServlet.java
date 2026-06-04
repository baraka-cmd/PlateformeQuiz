// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminCoursModifierServlet.java
// RÔLE    : Contrôleur pour modifier un cours existant (admin)
//           GET  : Affiche le formulaire pré-rempli
//           POST : Valide les données et met à jour le cours
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.CoursDAO;
import com.amijayquiz.dao.ThemeDAO;
import com.amijayquiz.models.Cours;
import com.amijayquiz.models.Theme;
import com.amijayquiz.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * AdminCoursModifierServlet - Modification d'un cours existant
 * 
 * GET  : Affiche le formulaire pré-rempli avec :
 *        - Les données actuelles du cours
 *        - Liste des thèmes disponibles
 *        - Avertissement si des apprenants sont inscrits
 * 
 * POST : Valide et met à jour le cours
 *        - Le thème et le niveau sont verrouillés si des apprenants existent
 *        - Le statut peut être changé (brouillon → publié)
 * 
 * URL : /admin/cours/modifier
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminCoursModifierServlet", urlPatterns = {"/admin/cours/modifier"})
public class AdminCoursModifierServlet extends HttpServlet {

    // Niveaux disponibles
    private static final String[] NIVEAUX = {"elementaire", "moyen", "avance"};

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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

        // 3. Récupération du cours
        CoursDAO coursDAO = new CoursDAO();
        Cours cours = coursDAO.trouverParId(id);

        if (cours == null) {
            session.setAttribute("messageErreur", "Ce cours n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 4. Récupération des thèmes
        ThemeDAO themeDAO = new ThemeDAO();
        List<Theme> themes = themeDAO.listerTous();

        // 5. Statistiques du cours
        int nbApprenants = coursDAO.compterApprenants(id);
        int nbCertificats = coursDAO.compterCertificats(id);
        boolean aDesApprenants = nbApprenants > 0;

        // 6. Message d'erreur éventuel
        String messageErreur = (String) session.getAttribute("messageErreurModificationCours");
        if (messageErreur != null) session.removeAttribute("messageErreurModificationCours");

        // 7. Passage des attributs
        request.setAttribute("pageTitle", "Modifier le cours - " + cours.getTitre() + " - Administration");
        request.setAttribute("cours", cours);
        request.setAttribute("themes", themes);
        request.setAttribute("niveaux", NIVEAUX);
        request.setAttribute("nbApprenants", nbApprenants);
        request.setAttribute("nbCertificats", nbCertificats);
        request.setAttribute("aDesApprenants", aDesApprenants);
        request.setAttribute("messageErreur", messageErreur);

        // 8. Affichage
        request.getRequestDispatcher("/views/admin/admin-cours-modifier.jsp").forward(request, response);
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

        // 2. Récupération des paramètres
        String idStr = request.getParameter("id");
        String theme = request.getParameter("theme");
        String niveau = request.getParameter("niveau");
        String titre = request.getParameter("titre");
        String description = request.getParameter("description");
        String objectifs = request.getParameter("objectifs");
        String prerequis = request.getParameter("prerequis");
        String image = request.getParameter("image");
        String duree = request.getParameter("dureeEstimee");
        String statut = request.getParameter("statut");

        // 3. Validation ID
        if (idStr == null || idStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID du cours manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID de cours invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 4. Vérifier que le cours existe
        CoursDAO coursDAO = new CoursDAO();
        Cours cours = coursDAO.trouverParId(id);

        if (cours == null) {
            session.setAttribute("messageErreur", "Ce cours n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours");
            return;
        }

        // 5. Vérifier si des apprenants sont inscrits
        boolean aDesApprenants = coursDAO.compterApprenants(id) > 0;

        // 6. Validation du titre
        if (titre == null || titre.trim().isEmpty()) {
            session.setAttribute("messageErreurModificationCours", "Le titre du cours est obligatoire.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/modifier?id=" + id);
            return;
        }
        titre = titre.trim();

        if (titre.length() < 5) {
            session.setAttribute("messageErreurModificationCours", "Le titre doit contenir au moins 5 caractères.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/modifier?id=" + id);
            return;
        }

        if (titre.length() > 200) {
            session.setAttribute("messageErreurModificationCours", "Le titre ne peut pas dépasser 200 caractères.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/modifier?id=" + id);
            return;
        }

        // 7. Validation du thème (verrouillé si apprenants)
        if (aDesApprenants) {
            // Garder le thème actuel
            theme = cours.getTheme();
        } else {
            if (theme == null || theme.trim().isEmpty()) {
                session.setAttribute("messageErreurModificationCours", "Veuillez sélectionner un thème.");
                response.sendRedirect(request.getContextPath() + "/admin/cours/modifier?id=" + id);
                return;
            }
            theme = theme.trim();

            // Vérifier que le thème existe
            ThemeDAO themeDAO = new ThemeDAO();
            if (themeDAO.trouverParNom(theme) == null) {
                session.setAttribute("messageErreurModificationCours", "Le thème sélectionné n'existe pas.");
                response.sendRedirect(request.getContextPath() + "/admin/cours/modifier?id=" + id);
                return;
            }
        }

        // 8. Validation du niveau (verrouillé si apprenants)
        if (aDesApprenants) {
            niveau = cours.getNiveau();
        } else {
            if (niveau == null || niveau.trim().isEmpty()) {
                session.setAttribute("messageErreurModificationCours", "Veuillez sélectionner un niveau.");
                response.sendRedirect(request.getContextPath() + "/admin/cours/modifier?id=" + id);
                return;
            }
            niveau = niveau.trim().toLowerCase();

            boolean niveauValide = false;
            for (String n : NIVEAUX) {
                if (n.equals(niveau)) {
                    niveauValide = true;
                    break;
                }
            }
            if (!niveauValide) {
                session.setAttribute("messageErreurModificationCours", "Niveau invalide.");
                response.sendRedirect(request.getContextPath() + "/admin/cours/modifier?id=" + id);
                return;
            }
        }

        // 9. Nettoyage des champs optionnels
        if (description != null) description = description.trim();
        if (objectifs != null) objectifs = objectifs.trim();
        if (prerequis != null) prerequis = prerequis.trim();
        if (image != null) image = image.trim();
        if (duree != null) duree = duree.trim();

        // 10. Validation du statut
        if (statut != null && !statut.trim().isEmpty()) {
            statut = statut.trim().toLowerCase();
            if (!statut.equals("brouillon") && !statut.equals("publié") && !statut.equals("archivé")) {
                statut = cours.getStatut(); // Garder l'ancien statut
            }
        } else {
            statut = cours.getStatut();
        }

        // 11. Mise à jour du cours
        cours.setTheme(theme);
        cours.setNiveau(niveau);
        cours.setTitre(titre);
        cours.setDescription(description);
        cours.setObjectifs(objectifs);
        cours.setPrerequis(prerequis);
        cours.setImage(image);
        cours.setDureeEstimee(duree);
        cours.setStatut(statut);

        boolean modifie = coursDAO.modifier(cours);

        // 12. Résultat
        if (modifie) {
            session.setAttribute("messageSucces", 
                "Cours \"" + titre + "\" modifié avec succès !");

            System.out.println("✅ [AdminCoursModifier] Cours modifié : ID=" + id + 
                " - Titre=" + titre + " - Statut=" + statut);

            response.sendRedirect(request.getContextPath() + "/admin/cours");
        } else {
            session.setAttribute("messageErreurModificationCours", 
                "Erreur lors de la modification du cours. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/modifier?id=" + id);
        }
    }
}