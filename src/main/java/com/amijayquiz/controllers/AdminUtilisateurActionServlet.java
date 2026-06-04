// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/AdminUtilisateurActionServlet.java
// RÔLE    : Contrôleur pour les actions sur un utilisateur
//           POST : Activer, Désactiver, Changer rôle, Réinitialiser MDP
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.UtilisateurDAO;
import com.amijayquiz.models.Utilisateur;
import com.amijayquiz.utils.EmailUtil;
import com.amijayquiz.utils.HashUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * AdminUtilisateurActionServlet - Actions admin sur un utilisateur
 * 
 * POST : action=activer → Active le compte
 *        action=desactiver → Désactive le compte
 *        action=promouvoir → Passe en admin
 *        action=retrograder → Passe en joueur
 *        action=reset-mdp → Réinitialise le mot de passe et envoie par email
 * 
 * RÈGLES DE SÉCURITÉ :
 * - Un admin ne peut pas agir sur son propre compte
 * - Toute action est journalisée
 * 
 * URL : /admin/utilisateurs/action
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "AdminUtilisateurActionServlet", urlPatterns = {"/admin/utilisateurs/action"})
public class AdminUtilisateurActionServlet extends HttpServlet {

    // Caractères pour le mot de passe temporaire
    private static final String MAJUSCULES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String MINUSCULES = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHIFFRES = "0123456789";
    private static final String SPECIAUX = "!@#$%&*";
    private static final String TOUS = MAJUSCULES + MINUSCULES + CHIFFRES + SPECIAUX;
    private static final int LONGUEUR_MDP = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/admin/utilisateurs");
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

        Utilisateur adminConnecte = (Utilisateur) session.getAttribute("utilisateur");
        if (!"admin".equals(adminConnecte.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        int adminId = adminConnecte.getId();

        // 2. Récupération des paramètres
        String action = request.getParameter("action");
        String idStr = request.getParameter("id");

        // 3. Validation de l'ID
        if (idStr == null || idStr.trim().isEmpty()) {
            session.setAttribute("messageErreur", "ID de l'utilisateur manquant.");
            response.sendRedirect(request.getContextPath() + "/admin/utilisateurs");
            return;
        }

        int utilisateurId;
        try {
            utilisateurId = Integer.parseInt(idStr.trim());
            if (utilisateurId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            session.setAttribute("messageErreur", "ID d'utilisateur invalide.");
            response.sendRedirect(request.getContextPath() + "/admin/utilisateurs");
            return;
        }

        // 4. Vérifier que l'utilisateur existe
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        Utilisateur utilisateur = utilisateurDAO.trouverParId(utilisateurId);

        if (utilisateur == null) {
            session.setAttribute("messageErreur", "Cet utilisateur n'existe pas.");
            response.sendRedirect(request.getContextPath() + "/admin/utilisateurs");
            return;
        }

        // 5. Sécurité : empêcher l'admin d'agir sur son propre compte
        if (utilisateurId == adminId && !"reset-mdp".equals(action)) {
            session.setAttribute("messageErreur", 
                "Vous ne pouvez pas effectuer cette action sur votre propre compte.");
            response.sendRedirect(request.getContextPath() + "/admin/utilisateurs/detail?id=" + utilisateurId);
            return;
        }

        // 6. Traitement selon l'action
        String redirectUrl = request.getContextPath() + "/admin/utilisateurs/detail?id=" + utilisateurId;
        boolean success = false;
        String message = "";

        switch (action) {
            case "activer":
                success = traiterActivation(utilisateurDAO, utilisateur);
                message = success ? 
                    "Compte de \"" + utilisateur.getPseudo() + "\" activé avec succès." :
                    "Erreur lors de l'activation du compte.";
                break;

            case "desactiver":
                success = traiterDesactivation(utilisateurDAO, utilisateur);
                message = success ? 
                    "Compte de \"" + utilisateur.getPseudo() + "\" désactivé avec succès." :
                    "Erreur lors de la désactivation du compte.";
                break;

            case "promouvoir":
                success = traiterPromotion(utilisateurDAO, utilisateur);
                message = success ? 
                    "\"" + utilisateur.getPseudo() + "\" est maintenant Administrateur." :
                    "Erreur lors de la promotion.";
                break;

            case "retrograder":
                success = traiterRetrogradation(utilisateurDAO, utilisateur);
                message = success ? 
                    "\"" + utilisateur.getPseudo() + "\" est maintenant Joueur." :
                    "Erreur lors de la rétrogradation.";
                break;

            case "reset-mdp":
                success = traiterReinitialisationMDP(utilisateurDAO, utilisateur, session);
                message = success ? 
                    "Mot de passe de \"" + utilisateur.getPseudo() + "\" réinitialisé et envoyé par email." :
                    "Erreur lors de la réinitialisation du mot de passe.";
                break;

            default:
                session.setAttribute("messageErreur", "Action invalide.");
                response.sendRedirect(redirectUrl);
                return;
        }

        // 7. Message de résultat
        if (success) {
            session.setAttribute("messageSucces", message);
        } else {
            session.setAttribute("messageErreur", message);
        }

        // 8. Redirection vers le détail
        response.sendRedirect(redirectUrl);
    }

    /**
     * Active le compte d'un utilisateur
     */
    private boolean traiterActivation(UtilisateurDAO dao, Utilisateur utilisateur) {
        boolean result = dao.activer(utilisateur.getId());
        if (result) {
            System.out.println("✅ [AdminAction] Activation compte : ID=" + utilisateur.getId() + 
                " - Pseudo=" + utilisateur.getPseudo());
        }
        return result;
    }

    /**
     * Désactive le compte d'un utilisateur
     */
    private boolean traiterDesactivation(UtilisateurDAO dao, Utilisateur utilisateur) {
        boolean result = dao.desactiver(utilisateur.getId());
        if (result) {
            System.out.println("⚠️ [AdminAction] Désactivation compte : ID=" + utilisateur.getId() + 
                " - Pseudo=" + utilisateur.getPseudo());
        }
        return result;
    }

    /**
     * Promeut un joueur en administrateur
     */
    private boolean traiterPromotion(UtilisateurDAO dao, Utilisateur utilisateur) {
        if (!"joueur".equals(utilisateur.getRole())) {
            return false;
        }
        boolean result = dao.changerRole(utilisateur.getId(), "admin");
        if (result) {
            System.out.println("👑 [AdminAction] Promotion admin : ID=" + utilisateur.getId() + 
                " - Pseudo=" + utilisateur.getPseudo());
        }
        return result;
    }

    /**
     * Rétrograde un administrateur en joueur
     */
    private boolean traiterRetrogradation(UtilisateurDAO dao, Utilisateur utilisateur) {
        if (!"admin".equals(utilisateur.getRole())) {
            return false;
        }
        boolean result = dao.changerRole(utilisateur.getId(), "joueur");
        if (result) {
            System.out.println("⬇️ [AdminAction] Rétrogradation joueur : ID=" + utilisateur.getId() + 
                " - Pseudo=" + utilisateur.getPseudo());
        }
        return result;
    }

    /**
     * Réinitialise le mot de passe et l'envoie par email
     */
    private boolean traiterReinitialisationMDP(UtilisateurDAO dao, Utilisateur utilisateur, 
                                                HttpSession session) {
        // Vérifier que l'utilisateur a un email
        String email = utilisateur.getEmail();
        if (email == null || email.trim().isEmpty()) {
            session.setAttribute("messageErreur", 
                "Cet utilisateur n'a pas d'email renseigné. Impossible d'envoyer le nouveau mot de passe.");
            return false;
        }

        // Générer un nouveau mot de passe aléatoire
        String nouveauMDP = genererMotDePasseAleatoire();
        String mdpHashe = HashUtil.hasher(nouveauMDP);

        if (mdpHashe == null) {
            session.setAttribute("messageErreur", "Erreur lors du hachage du mot de passe.");
            return false;
        }

        // Mettre à jour le mot de passe en base
        boolean mdpModifie = dao.modifierMotDePasse(utilisateur.getId(), mdpHashe);
        if (!mdpModifie) {
            session.setAttribute("messageErreur", "Erreur lors de la mise à jour du mot de passe.");
            return false;
        }

        // Envoyer l'email
        boolean emailEnvoye = EmailUtil.envoyerMotDePasseOublie(
            email, 
            utilisateur.getPseudo(), 
            nouveauMDP
        );

        if (emailEnvoye) {
            System.out.println("🔑 [AdminAction] Reset MDP : ID=" + utilisateur.getId() + 
                " - Pseudo=" + utilisateur.getPseudo() + " - Email=" + email);
            return true;
        } else {
            // Le MDP a été changé mais l'email n'a pas pu être envoyé
            System.err.println("⚠️ [AdminAction] Reset MDP réussi mais échec envoi email : " + email);
            session.setAttribute("messageErreur", 
                "Mot de passe réinitialisé mais l'email n'a pas pu être envoyé. " +
                "Contactez le support technique.");
            return false;
        }
    }

    /**
     * Génère un mot de passe aléatoire sécurisé
     */
    private String genererMotDePasseAleatoire() {
        SecureRandom random = new SecureRandom();
        StringBuilder mdp = new StringBuilder(LONGUEUR_MDP);

        // Garantir au moins un caractère de chaque type
        mdp.append(MAJUSCULES.charAt(random.nextInt(MAJUSCULES.length())));
        mdp.append(MINUSCULES.charAt(random.nextInt(MINUSCULES.length())));
        mdp.append(CHIFFRES.charAt(random.nextInt(CHIFFRES.length())));
        mdp.append(SPECIAUX.charAt(random.nextInt(SPECIAUX.length())));

        // Compléter avec des caractères aléatoires
        for (int i = 4; i < LONGUEUR_MDP; i++) {
            mdp.append(TOUS.charAt(random.nextInt(TOUS.length())));
        }

        // Mélanger les caractères
        char[] chars = mdp.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }
}