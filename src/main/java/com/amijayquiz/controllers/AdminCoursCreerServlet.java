// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminCoursCreerServlet.java
// RÔLE    : Contrôleur pour créer un nouveau cours (admin)
//           GET  : Affiche le formulaire de création
//           POST : Valide les données et crée le cours
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
 * AdminCoursCreerServlet - Création d'un nouveau cours
 * 
 * GET  : Affiche le formulaire avec :
 *        - Sélection du thème (depuis la table themes)
 *        - Sélection du niveau
 *        - Titre, description, objectifs, prérequis
 *        - Image/icône (optionnel)
 * 
 * POST : Valide et crée le cours avec statut "brouillon"
 * 
 * URL : /admin/cours/creer
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminCoursCreerServlet", urlPatterns = {"/admin/cours/creer"})
public class AdminCoursCreerServlet extends HttpServlet {

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

        // 2. Récupération des thèmes pour le select
        ThemeDAO themeDAO = new ThemeDAO();
        List<Theme> themes = themeDAO.listerTous();

        // 3. Message d'erreur éventuel
        String messageErreur = (String) session.getAttribute("messageErreurCreationCours");
        if (messageErreur != null) session.removeAttribute("messageErreurCreationCours");

        // 4. Valeurs sauvegardées en cas d'erreur (brouillon session)
        String oldTitre = (String) session.getAttribute("oldCoursTitre");
        String oldTheme = (String) session.getAttribute("oldCoursTheme");
        String oldNiveau = (String) session.getAttribute("oldCoursNiveau");
        String oldDescription = (String) session.getAttribute("oldCoursDescription");
        String oldObjectifs = (String) session.getAttribute("oldCoursObjectifs");
        String oldPrerequis = (String) session.getAttribute("oldCoursPrerequis");
        String oldImage = (String) session.getAttribute("oldCoursImage");
        String oldDuree = (String) session.getAttribute("oldCoursDuree");

        // Nettoyer après récupération
        session.removeAttribute("oldCoursTitre");
        session.removeAttribute("oldCoursTheme");
        session.removeAttribute("oldCoursNiveau");
        session.removeAttribute("oldCoursDescription");
        session.removeAttribute("oldCoursObjectifs");
        session.removeAttribute("oldCoursPrerequis");
        session.removeAttribute("oldCoursImage");
        session.removeAttribute("oldCoursDuree");

        // 5. Passage des attributs
        request.setAttribute("pageTitle", "Créer un cours - Administration - AmijayQuiz");
        request.setAttribute("themes", themes);
        request.setAttribute("niveaux", NIVEAUX);
        request.setAttribute("messageErreur", messageErreur);
        request.setAttribute("oldTitre", oldTitre != null ? oldTitre : "");
        request.setAttribute("oldTheme", oldTheme != null ? oldTheme : "");
        request.setAttribute("oldNiveau", oldNiveau != null ? oldNiveau : "elementaire");
        request.setAttribute("oldDescription", oldDescription != null ? oldDescription : "");
        request.setAttribute("oldObjectifs", oldObjectifs != null ? oldObjectifs : "");
        request.setAttribute("oldPrerequis", oldPrerequis != null ? oldPrerequis : "");
        request.setAttribute("oldImage", oldImage != null ? oldImage : "");
        request.setAttribute("oldDuree", oldDuree != null ? oldDuree : "");

        // 6. Affichage
        request.getRequestDispatcher("/views/admin/admin-cours-creer.jsp").forward(request, response);
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
        String theme = request.getParameter("theme");
        String niveau = request.getParameter("niveau");
        String titre = request.getParameter("titre");
        String description = request.getParameter("description");
        String objectifs = request.getParameter("objectifs");
        String prerequis = request.getParameter("prerequis");
        String image = request.getParameter("image");
        String duree = request.getParameter("dureeEstimee");

        // 3. Sauvegarder les valeurs en session pour pré-remplissage en cas d'erreur
        session.setAttribute("oldCoursTitre", titre);
        session.setAttribute("oldCoursTheme", theme);
        session.setAttribute("oldCoursNiveau", niveau);
        session.setAttribute("oldCoursDescription", description);
        session.setAttribute("oldCoursObjectifs", objectifs);
        session.setAttribute("oldCoursPrerequis", prerequis);
        session.setAttribute("oldCoursImage", image);
        session.setAttribute("oldCoursDuree", duree);

        // 4. Validation du thème
        if (theme == null || theme.trim().isEmpty()) {
            session.setAttribute("messageErreurCreationCours", "Veuillez sélectionner un thème.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/creer");
            return;
        }
        theme = theme.trim();

        // Vérifier que le thème existe
        ThemeDAO themeDAO = new ThemeDAO();
        if (themeDAO.trouverParNom(theme) == null) {
            session.setAttribute("messageErreurCreationCours", "Le thème sélectionné n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/creer");
            return;
        }

        // 5. Validation du niveau
        if (niveau == null || niveau.trim().isEmpty()) {
            session.setAttribute("messageErreurCreationCours", "Veuillez sélectionner un niveau.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/creer");
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
            session.setAttribute("messageErreurCreationCours", "Niveau invalide. Choisissez : élémentaire, moyen ou avancé.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/creer");
            return;
        }

        // 6. Validation du titre
        if (titre == null || titre.trim().isEmpty()) {
            session.setAttribute("messageErreurCreationCours", "Le titre du cours est obligatoire.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/creer");
            return;
        }
        titre = titre.trim();

        if (titre.length() < 5) {
            session.setAttribute("messageErreurCreationCours", "Le titre doit contenir au moins 5 caractères.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/creer");
            return;
        }

        if (titre.length() > 200) {
            session.setAttribute("messageErreurCreationCours", "Le titre ne peut pas dépasser 200 caractères.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/creer");
            return;
        }

        // Vérifier qu'aucun cours avec le même thème + niveau + titre n'existe
        CoursDAO coursDAO = new CoursDAO();
        List<Cours> existants = coursDAO.listerParThemeEtNiveau(theme, niveau);
        for (Cours c : existants) {
            if (c.getTitre() != null && c.getTitre().equalsIgnoreCase(titre)) {
                session.setAttribute("messageErreurCreationCours", 
                    "Un cours avec ce titre existe déjà dans ce thème et ce niveau.");
                response.sendRedirect(request.getContextPath() + "/admin/cours/creer");
                return;
            }
        }

        // 7. Nettoyage des champs optionnels
        if (description != null) description = description.trim();
        if (objectifs != null) objectifs = objectifs.trim();
        if (prerequis != null) prerequis = prerequis.trim();
        if (image != null) image = image.trim();
        if (duree != null) duree = duree.trim();

        // 8. Création du cours
        Cours cours = new Cours();
        cours.setTheme(theme);
        cours.setNiveau(niveau);
        cours.setTitre(titre);
        cours.setDescription(description);
        cours.setObjectifs(objectifs);
        cours.setPrerequis(prerequis);
        cours.setImage(image);
        cours.setDureeEstimee(duree);
        cours.setStatut(Cours.STATUT_BROUILLON); // Toujours brouillon à la création
        cours.setOrdre(1);
        cours.setNombreChapitres(0);
        cours.setNombreLecons(0);

        boolean ajoute = coursDAO.ajouter(cours);

        // 9. Résultat
        if (ajoute) {
            // Nettoyer les valeurs sauvegardées
            nettoyerSessionCreation(session);

            session.setAttribute("messageSucces", 
                "Cours \"" + titre + "\" créé avec succès en brouillon ! " +
                "Vous pouvez maintenant configurer ses modules, chapitres et leçons.");

            System.out.println("✅ [AdminCoursCreer] Cours créé : " + titre + " (Thème: " + theme + ", Niveau: " + niveau + ")");

            response.sendRedirect(request.getContextPath() + "/admin/cours");
        } else {
            session.setAttribute("messageErreurCreationCours", 
                "Erreur lors de la création du cours. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/admin/cours/creer");
        }
    }

    /**
     * Nettoie les attributs de session après création réussie
     */
    private void nettoyerSessionCreation(HttpSession session) {
        session.removeAttribute("oldCoursTitre");
        session.removeAttribute("oldCoursTheme");
        session.removeAttribute("oldCoursNiveau");
        session.removeAttribute("oldCoursDescription");
        session.removeAttribute("oldCoursObjectifs");
        session.removeAttribute("oldCoursPrerequis");
        session.removeAttribute("oldCoursImage");
        session.removeAttribute("oldCoursDuree");
        session.removeAttribute("messageErreurCreationCours");
    }
}