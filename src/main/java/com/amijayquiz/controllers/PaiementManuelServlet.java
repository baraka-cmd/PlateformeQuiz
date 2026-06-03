// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/PaiementManuelServlet.java
// RÔLE    : Contrôleur de paiement manuel
//           Reçoit le formulaire de paiement manuel
//           Enregistre la demande dans la base de données
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.dao.PaiementManuelDAO;
import com.amijayquiz.models.Utilisateur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * PaiementManuelServlet - Gestion du paiement manuel
 * 
 * POST : Reçoit le formulaire (ID transaction, opérateur, téléphone)
 *        Enregistre la demande en statut EN_ATTENTE
 *        L'administrateur valide manuellement après vérification
 * 
 * URL : /paiement-manuel
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "PaiementManuelServlet", urlPatterns = {"/paiement-manuel"})
public class PaiementManuelServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // GET non autorisé, rediriger vers la page de paiement
        response.sendRedirect(request.getContextPath() + "/paiement");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 1. Vérification de la session et du rôle
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

        // 2. Vérifier si l'utilisateur est déjà premium
        com.amijayquiz.dao.AccessPremiumDAO premiumDAOVerif = new com.amijayquiz.dao.AccessPremiumDAO();
        if (premiumDAOVerif.estPremium(utilisateurId)) {
            session.setAttribute("messageSucces", "Vous etes deja Premium ! Tous les niveaux sont debloques.");
            response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
            return;
        }

        // 3. Vérifier si une demande est déjà en attente
        PaiementManuelDAO paiementManuelDAOVerif = new PaiementManuelDAO();
        if (paiementManuelDAOVerif.aDemandeEnAttente(utilisateurId)) {
            session.setAttribute("messageErreur",
                "Vous avez deja une demande de paiement en attente de validation. Veuillez patienter.");
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }

        // 4. Récupération des paramètres du formulaire
        String idTransaction = request.getParameter("idTransaction");
        String operateur = request.getParameter("operateur");
        String telephone = request.getParameter("telephone");

        // 5. Validation des champs
        boolean hasError = false;
        StringBuilder messageErreur = new StringBuilder();

        if (idTransaction == null || idTransaction.trim().isEmpty()) {
            messageErreur.append("L'ID de transaction est obligatoire. ");
            hasError = true;
        }

        if (operateur == null || operateur.trim().isEmpty()) {
            messageErreur.append("L'operateur est obligatoire. ");
            hasError = true;
        }

        if (telephone == null || telephone.trim().isEmpty()) {
            messageErreur.append("Le numero de telephone est obligatoire. ");
            hasError = true;
        }

        if (hasError) {
            session.setAttribute("messageErreur", messageErreur.toString().trim());
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }

        // Nettoyage des espaces
        idTransaction = idTransaction.trim();
        operateur = operateur.trim();
        telephone = telephone.trim();

        // 6. Validation du format de l'ID de transaction (minimum 5 caractères)
        if (idTransaction.length() < 5) {
            session.setAttribute("messageErreur",
                "L'ID de transaction semble trop court. Verifiez le SMS recu et reessayez.");
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }

        // 7. Validation du numéro de téléphone (minimum 9 chiffres)
        String telephoneChiffres = telephone.replaceAll("[^0-9]", "");
        if (telephoneChiffres.length() < 9) {
            session.setAttribute("messageErreur",
                "Le numero de telephone est invalide. Il doit contenir au moins 9 chiffres.");
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }

        // 8. Enregistrement de la demande dans la base de données
        PaiementManuelDAO dao = new PaiementManuelDAO();
        boolean enregistre = dao.enregistrerDemande(utilisateurId, idTransaction, operateur, telephoneChiffres);

        if (enregistre) {
            // Succès
            session.setAttribute("messageSucces",
                "Votre paiement a ete enregistre avec succes ! " +
                "Votre compte sera active apres verification du paiement (delai : 1 a 24 heures). " +
                "En cas de probleme, contactez-nous au +243 984 270 438.");

            System.out.println("✅ [Paiement Manuel] Demande enregistrée - UserID: " + utilisateurId +
                " - ID Transaction: " + idTransaction + " - Operateur: " + operateur);

        } else {
            // Erreur base de données
            session.setAttribute("messageErreur",
                "Une erreur est survenue lors de l'enregistrement de votre demande. Veuillez reessayer.");

            System.err.println("❌ [Paiement Manuel] Erreur enregistrement - UserID: " + utilisateurId);
        }

        // 9. Redirection vers le dashboard avec message
        response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
    }
}