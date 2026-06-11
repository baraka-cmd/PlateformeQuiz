// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/AdminUtilisateurActionServlet.java
// RÔLE    : Contrôleur pour les actions sur un utilisateur
//           POST : Activer, Désactiver, Changer rôle, Réinitialiser MDP
//           SÉCURISÉ : Seul le Super Admin peut agir
//           Promotion → "gestionnaire" (pas "admin")
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.UtilisateurDAO;
import com.learnpath.models.Utilisateur;
import com.learnpath.utils.EmailUtil;
import com.learnpath.utils.HashUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;

@WebServlet(name = "AdminUtilisateurActionServlet", urlPatterns = {"/admin/utilisateurs/action"})
public class AdminUtilisateurActionServlet extends HttpServlet {

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

        // 1. Vérification authentification
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        Utilisateur adminConnecte = (Utilisateur) session.getAttribute("utilisateur");

        // 2. 🔒 VÉRIFICATION SUPER ADMIN UNIQUEMENT
        if (!"admin".equals(adminConnecte.getRole())) {
            session.setAttribute("messageErreur", 
                "Accès refusé. Seul le Super Administrateur peut gérer les utilisateurs.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        int adminId = adminConnecte.getId();

        // 3. Récupération des paramètres
        String action = request.getParameter("action");
        String idStr = request.getParameter("id");

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

        // 5. 🔒 Sécurité : empêcher d'agir sur son propre compte (sauf reset-mdp)
        if (utilisateurId == adminId && !"reset-mdp".equals(action)) {
            session.setAttribute("messageErreur", 
                "Vous ne pouvez pas effectuer cette action sur votre propre compte.");
            response.sendRedirect(request.getContextPath() + "/admin/utilisateurs/detail?id=" + utilisateurId);
            return;
        }

        // 6. 🔒 Sécurité : empêcher de modifier un autre Super Admin
        if ("admin".equals(utilisateur.getRole()) && !"reset-mdp".equals(action)) {
            session.setAttribute("messageErreur", 
                "Vous ne pouvez pas modifier un autre Super Administrateur.");
            response.sendRedirect(request.getContextPath() + "/admin/utilisateurs/detail?id=" + utilisateurId);
            return;
        }

        // 7. Traitement selon l'action
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
                    "\"" + utilisateur.getPseudo() + "\" est maintenant Gestionnaire." :
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

        if (success) {
            session.setAttribute("messageSucces", message);
        } else {
            session.setAttribute("messageErreur", message);
        }

        response.sendRedirect(redirectUrl);
    }

    private boolean traiterActivation(UtilisateurDAO dao, Utilisateur utilisateur) {
        boolean result = dao.activer(utilisateur.getId());
        if (result) {
            System.out.println("✅ [AdminAction] Activation compte : ID=" + utilisateur.getId());
        }
        return result;
    }

    private boolean traiterDesactivation(UtilisateurDAO dao, Utilisateur utilisateur) {
        boolean result = dao.desactiver(utilisateur.getId());
        if (result) {
            System.out.println("⚠️ [AdminAction] Désactivation compte : ID=" + utilisateur.getId());
        }
        return result;
    }

    private boolean traiterPromotion(UtilisateurDAO dao, Utilisateur utilisateur) {
        if (!"joueur".equals(utilisateur.getRole())) {
            return false;
        }
        // 🔒 PROMOTION → GESTIONNAIRE (pas admin)
        boolean result = dao.changerRole(utilisateur.getId(), "gestionnaire");
        if (result) {
            System.out.println("👑 [AdminAction] Promotion gestionnaire : ID=" + utilisateur.getId());
        }
        return result;
    }

    private boolean traiterRetrogradation(UtilisateurDAO dao, Utilisateur utilisateur) {
        if (!"gestionnaire".equals(utilisateur.getRole())) {
            return false;
        }
        boolean result = dao.changerRole(utilisateur.getId(), "joueur");
        if (result) {
            System.out.println("⬇️ [AdminAction] Rétrogradation joueur : ID=" + utilisateur.getId());
        }
        return result;
    }

    private boolean traiterReinitialisationMDP(UtilisateurDAO dao, Utilisateur utilisateur, 
                                                HttpSession session) {
        String email = utilisateur.getEmail();
        if (email == null || email.trim().isEmpty()) {
            session.setAttribute("messageErreur", 
                "Cet utilisateur n'a pas d'email renseigné.");
            return false;
        }

        String nouveauMDP = genererMotDePasseAleatoire();
        String mdpHashe = HashUtil.hasher(nouveauMDP);

        if (mdpHashe == null) {
            session.setAttribute("messageErreur", "Erreur lors du hachage du mot de passe.");
            return false;
        }

        boolean mdpModifie = dao.modifierMotDePasse(utilisateur.getId(), mdpHashe);
        if (!mdpModifie) {
            session.setAttribute("messageErreur", "Erreur lors de la mise à jour du mot de passe.");
            return false;
        }

        boolean emailEnvoye = EmailUtil.envoyerMotDePasseOublie(email, utilisateur.getPseudo(), nouveauMDP);

        if (emailEnvoye) {
            System.out.println("🔑 [AdminAction] Reset MDP : ID=" + utilisateur.getId());
            return true;
        } else {
            System.err.println("⚠️ [AdminAction] Reset MDP réussi mais échec envoi email : " + email);
            session.setAttribute("messageErreur", 
                "Mot de passe réinitialisé mais l'email n'a pas pu être envoyé.");
            return false;
        }
    }

    private String genererMotDePasseAleatoire() {
        SecureRandom random = new SecureRandom();
        StringBuilder mdp = new StringBuilder(LONGUEUR_MDP);
        mdp.append(MAJUSCULES.charAt(random.nextInt(MAJUSCULES.length())));
        mdp.append(MINUSCULES.charAt(random.nextInt(MINUSCULES.length())));
        mdp.append(CHIFFRES.charAt(random.nextInt(CHIFFRES.length())));
        mdp.append(SPECIAUX.charAt(random.nextInt(SPECIAUX.length())));
        for (int i = 4; i < LONGUEUR_MDP; i++) {
            mdp.append(TOUS.charAt(random.nextInt(TOUS.length())));
        }
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