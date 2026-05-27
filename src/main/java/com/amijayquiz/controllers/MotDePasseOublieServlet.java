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
 * MotDePasseOublieServlet - Gère la réinitialisation de mot de passe
 * 
 * Reçoit l'email de l'utilisateur, génère un nouveau mot de passe aléatoire,
 * l'envoie par email et met à jour la base de données.
 * 
 * URL : /mot-de-passe-oublie
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "MotDePasseOublieServlet", urlPatterns = {"/mot-de-passe-oublie"})
public class MotDePasseOublieServlet extends HttpServlet {

    // Caractères utilisés pour générer le mot de passe aléatoire
    private static final String MAJUSCULES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String MINUSCULES = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHIFFRES = "0123456789";
    private static final String SPECIAUX = "!@#$%&*";
    private static final String TOUS = MAJUSCULES + MINUSCULES + CHIFFRES + SPECIAUX;
    private static final int LONGUEUR_MDP = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirection vers la page de mot de passe oublié
        request.getRequestDispatcher("/views/mot-de-passe-oublie.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();

        // 1. Récupération de l'email
        String email = request.getParameter("email");

        // 2. Validation de l'email
        if (email == null || email.trim().isEmpty()) {
            session.setAttribute("messageErreur", "Veuillez entrer votre adresse email.");
            response.sendRedirect(request.getContextPath() + "/mot-de-passe-oublie");
            return;
        }

        email = email.trim().toLowerCase();

        // 3. Validation du format email
        String emailRegex = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
        if (!email.matches(emailRegex)) {
            session.setAttribute("messageErreur", "Veuillez entrer une adresse email valide.");
            session.setAttribute("oldEmail", email);
            response.sendRedirect(request.getContextPath() + "/mot-de-passe-oublie");
            return;
        }

        // 4. Vérifier si l'email existe dans la base de données
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        Utilisateur utilisateur = utilisateurDAO.trouverParEmail(email);

        if (utilisateur == null) {
            // Pour des raisons de sécurité, on ne révèle pas si l'email existe ou non
            // On affiche un message générique
            session.setAttribute("messageSucces", 
                "Si un compte est associé à cette adresse email, vous recevrez un nouveau mot de passe dans quelques instants. " +
                "Vérifiez votre boîte de réception et vos spams.");
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }

        // 5. Générer un nouveau mot de passe aléatoire
        String nouveauMotDePasse = genererMotDePasseAleatoire();

        // 6. Hasher le nouveau mot de passe
        String motDePasseHashe = HashUtil.hasher(nouveauMotDePasse);

        if (motDePasseHashe == null) {
            session.setAttribute("messageErreur", "Une erreur est survenue. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/mot-de-passe-oublie");
            return;
        }

        // 7. Mettre à jour le mot de passe dans la base de données
        boolean modifie = utilisateurDAO.modifierMotDePasse(utilisateur.getId(), motDePasseHashe);

        if (!modifie) {
            session.setAttribute("messageErreur", "Une erreur est survenue lors de la réinitialisation. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/mot-de-passe-oublie");
            return;
        }

        // 8. Envoyer l'email avec le nouveau mot de passe
        boolean emailEnvoye = EmailUtil.envoyerMotDePasseOublie(
            email,
            utilisateur.getPseudo(),
            nouveauMotDePasse
        );

        // 9. Message de confirmation (même si l'email échoue, le mot de passe a été changé)
        if (emailEnvoye) {
            session.setAttribute("messageSucces", 
                "Un email contenant votre nouveau mot de passe vous a été envoyé à <strong>" + email + "</strong>. " +
                "Vérifiez votre boîte de réception et vos spams.");
        } else {
            session.setAttribute("messageSucces", 
                "Votre mot de passe a été réinitialisé avec succès. " +
                "Cependant, l'email n'a pas pu être envoyé. Veuillez contacter l'administrateur.");
            System.err.println("⚠️ Échec de l'envoi d'email à : " + email);
        }

        // 10. Redirection vers la page de connexion
        response.sendRedirect(request.getContextPath() + "/connexion");
    }

    /**
     * Génère un mot de passe aléatoire sécurisé de 10 caractères
     * Format : au moins 1 majuscule, 1 minuscule, 1 chiffre, 1 caractère spécial
     * 
     * @return Le mot de passe généré
     */
    private String genererMotDePasseAleatoire() {
        SecureRandom random = new SecureRandom();
        StringBuilder motDePasse = new StringBuilder(LONGUEUR_MDP);

        // Garantir au moins un caractère de chaque type
        motDePasse.append(MAJUSCULES.charAt(random.nextInt(MAJUSCULES.length())));
        motDePasse.append(MINUSCULES.charAt(random.nextInt(MINUSCULES.length())));
        motDePasse.append(CHIFFRES.charAt(random.nextInt(CHIFFRES.length())));
        motDePasse.append(SPECIAUX.charAt(random.nextInt(SPECIAUX.length())));

        // Compléter avec des caractères aléatoires
        for (int i = 4; i < LONGUEUR_MDP; i++) {
            motDePasse.append(TOUS.charAt(random.nextInt(TOUS.length())));
        }

        // Mélanger les caractères pour plus de sécurité
        char[] chars = motDePasse.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }
}