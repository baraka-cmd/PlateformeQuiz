package com.amijayquiz.controllers;

import com.amijayquiz.dao.UtilisateurDAO;
import com.amijayquiz.models.Utilisateur;
import com.amijayquiz.utils.HashUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "InscriptionServlet", urlPatterns = {"/inscription"})
public class InscriptionServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("views/inscription.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // Récupération des paramètres du formulaire
        String pseudo = request.getParameter("pseudo");
        String email = request.getParameter("email");
        String motDePasse = request.getParameter("motDePasse");
        String confirmation = request.getParameter("confirmation");
        
        HttpSession session = request.getSession();
        
        // Conserver les valeurs saisies en cas d'erreur
        session.setAttribute("oldPseudo", pseudo);
        session.setAttribute("oldEmail", email);
        
        // Validation 1 : Vérifier que les champs obligatoires ne sont pas vides
        if (pseudo == null || pseudo.trim().isEmpty() ||
            motDePasse == null || motDePasse.trim().isEmpty() ||
            confirmation == null || confirmation.trim().isEmpty()) {
            
            session.setAttribute("messageErreur", "Veuillez remplir tous les champs obligatoires !");
            response.sendRedirect("inscription");
            return;
        }
        
        // Nettoyage des espaces
        pseudo = pseudo.trim();
        if (email != null) email = email.trim();
        motDePasse = motDePasse.trim();
        confirmation = confirmation.trim();
        
        // Validation 2 : Longueur du pseudo
        if (pseudo.length() < 3) {
            session.setAttribute("messageErreur", "Le pseudo doit contenir au moins 3 caractères !");
            response.sendRedirect("inscription");
            return;
        }
        
        // Validation 3 : Format de l'email (si renseigné)
        if (email != null && !email.isEmpty()) {
            String emailRegex = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
            if (!email.matches(emailRegex)) {
                session.setAttribute("messageErreur", "Veuillez entrer une adresse email valide !");
                response.sendRedirect("inscription");
                return;
            }
        }
        
        // Validation 4 : Longueur du mot de passe
        if (motDePasse.length() < 6) {
            session.setAttribute("messageErreur", "Le mot de passe doit contenir au moins 6 caractères !");
            response.sendRedirect("inscription");
            return;
        }
        
        // Validation 5 : Correspondance des mots de passe
        if (!motDePasse.equals(confirmation)) {
            session.setAttribute("messageErreur", "Les mots de passe ne correspondent pas !");
            response.sendRedirect("inscription");
            return;
        }
        
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        
        // Validation 6 : Pseudo déjà utilisé ?
        if (utilisateurDAO.existePseudo(pseudo)) {
            session.setAttribute("messageErreur", "Ce pseudo est déjà utilisé ! Veuillez en choisir un autre.");
            response.sendRedirect("inscription");
            return;
        }
        
        // Validation 7 : Email déjà utilisé ? (si renseigné)
        if (email != null && !email.isEmpty() && utilisateurDAO.existeEmail(email)) {
            session.setAttribute("messageErreur", "Cet email est déjà utilisé ! Veuillez en choisir un autre.");
            response.sendRedirect("inscription");
            return;
        }
        
        // Hashage du mot de passe
        String motDePasseHashe = HashUtil.hasher(motDePasse);
        
        if (motDePasseHashe == null) {
            session.setAttribute("messageErreur", "Une erreur est survenue lors du traitement. Veuillez réessayer.");
            response.sendRedirect("inscription");
            return;
        }
        
        // Création de l'objet utilisateur
        Utilisateur utilisateur = new Utilisateur(pseudo, motDePasseHashe);
        utilisateur.setEmail(email != null && !email.isEmpty() ? email : null);
        utilisateur.setRole("joueur");
        utilisateur.setActif(true);
        
        // Inscription dans la base de données
        boolean inscriptionReussie = utilisateurDAO.inscrire(utilisateur);
        
        // Nettoyer les anciennes valeurs
        session.removeAttribute("oldPseudo");
        session.removeAttribute("oldEmail");
        
        if (inscriptionReussie) {
            session.setAttribute("messageSucces", "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            response.sendRedirect("connexion");
        } else {
            session.setAttribute("messageErreur", "Erreur lors de l'inscription. Veuillez réessayer.");
            response.sendRedirect("inscription");
        }
    }
}