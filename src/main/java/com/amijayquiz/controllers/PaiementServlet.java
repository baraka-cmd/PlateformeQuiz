// ============================================================
// FICHIER : src/main/java/com/amijayquiz/controllers/PaiementServlet.java
// RÔLE    : Contrôleur de paiement
//           GET  : Affiche la page de paiement
//           POST : Crée un paiement via PawaPay et redirige
// ============================================================

package com.amijayquiz.controllers;

import com.amijayquiz.config.PawaPayConfig;
import com.amijayquiz.dao.AccessPremiumDAO;
import com.amijayquiz.dao.PaiementDAO;
import com.amijayquiz.models.Paiement;
import com.amijayquiz.models.Utilisateur;
import com.amijayquiz.utils.PawaPayUtil;
import com.amijayquiz.utils.SecuriteUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * PaiementServlet - Gestion du processus de paiement
 * 
 * GET  : Affiche la page de paiement avec les informations
 *        (montant, avantages premium, formulaire de telephone)
 * 
 * POST : 1. Valide le numéro de telephone et l'operateur
 *        2. Genere une reference unique (UUID)
 *        3. Envoie la demande de paiement à PawaPay
 *        4. Enregistre le paiement en statut PENDING
 *        5. Redirige vers la page d'attente
 * 
 * URL : /paiement
 * 
 * @author AmijayQuiz
 * @version 1.0
 */
@WebServlet(name = "PaiementServlet", urlPatterns = {"/paiement"})
public class PaiementServlet extends HttpServlet {

    // ===== CONSTANTES =====
    private static final String PAGE_PAIEMENT = "/views/joueur/paiement.jsp";
    private static final String PAGE_ATTENTE = "/views/joueur/paiement-attente.jsp";
    private static final String PAGE_ERREUR = "/views/joueur/paiement-echoue.jsp";
    private static final String PAGE_SUCCES = "/views/joueur/paiement-succes.jsp";

    private static final String URL_CONNEXION = "/connexion";
    private static final String URL_DASHBOARD = "/joueur/dashboard";

    // ============================================================
    // DOGET : AFFICHAGE PAGE PAIEMENT
    // ============================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Verification authentification
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + URL_CONNEXION);
            return;
        }

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");

        // 2. Verifier si deja premium
        AccessPremiumDAO premiumDAO = new AccessPremiumDAO();
        if (premiumDAO.estPremium(utilisateur.getId())) {
            session.setAttribute("messageSucces", "Vous etes deja Premium ! Tous les niveaux sont debloques.");
            response.sendRedirect(request.getContextPath() + URL_DASHBOARD);
            return;
        }

        // 3. Verifier si un paiement est deja en attente
        PaiementDAO paiementDAO = new PaiementDAO();
        if (paiementDAO.aPaiementEnAttente(utilisateur.getId())) {
            session.setAttribute("messageErreur",
                "Vous avez deja un paiement en attente. Veuillez patienter ou reessayer.");
        }

        // 4. Generer un token CSRF pour le formulaire
        String tokenCSRF = SecuriteUtil.genererTokenCSRF();
        session.setAttribute("csrf_token_paiement", tokenCSRF);

        // 5. Passer les attributs a la JSP
        request.setAttribute("pageTitle", "Debloquer tous les niveaux - AmijayQuiz");
        request.setAttribute("montantUSD", PawaPayConfig.MONTANT_USD);
        request.setAttribute("montantFC", PawaPayConfig.MONTANT_FC);
        request.setAttribute("tokenCSRF", tokenCSRF);

        // Message d'erreur depuis la session (redirection apres echec)
        String messageErreur = (String) session.getAttribute("messageErreur");
        if (messageErreur != null) {
            request.setAttribute("messageErreur", messageErreur);
            session.removeAttribute("messageErreur");
        }

        // 6. Afficher la page de paiement
        request.getRequestDispatcher(PAGE_PAIEMENT).forward(request, response);
    }

    // ============================================================
    // DOPOST : TRAITEMENT DU PAIEMENT
    // ============================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 1. Verification authentification
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + URL_CONNEXION);
            return;
        }

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        int utilisateurId = utilisateur.getId();

        // 2. Verifier si deja premium (double-clic ou malveillance)
        AccessPremiumDAO premiumDAO = new AccessPremiumDAO();
        if (premiumDAO.estPremium(utilisateurId)) {
            session.setAttribute("messageSucces", "Vous etes deja Premium !");
            response.sendRedirect(request.getContextPath() + URL_DASHBOARD);
            return;
        }

        // 3. Verifier token CSRF
        String tokenCSRFSession = (String) session.getAttribute("csrf_token_paiement");
        String tokenCSRFRequete = request.getParameter("csrf_token");
        if (!SecuriteUtil.verifierTokenCSRF(tokenCSRFSession, tokenCSRFRequete)) {
            SecuriteUtil.logErreur("Tentative CSRF detectee - UserID: " + utilisateurId);
            session.setAttribute("messageErreur", "Erreur de securite. Veuillez reessayer.");
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }
        session.removeAttribute("csrf_token_paiement");

        // 4. Recuperation et validation du telephone
        String telephoneBrut = request.getParameter("telephone");
        String telephone = SecuriteUtil.normaliserTelephone(telephoneBrut);
        


        if (telephone == null) {
            SecuriteUtil.logWarn("Telephone invalide - UserID: " + utilisateurId + " - Saisi: " + telephoneBrut);
            session.setAttribute("messageErreur",
                "Numero de telephone invalide. Format attendu : +243XXXXXXXXX ou 0XXXXXXXXX");
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }
        
        // Enlever le prefixe + pour PawaPay
        if (telephone != null && telephone.startsWith("+")) {
            telephone = telephone.substring(1);
        }

        // 5. Recuperation et validation de l'operateur
        String operateur = request.getParameter("operateur");
        if (operateur == null || operateur.trim().isEmpty()) {
            SecuriteUtil.logWarn("Operateur non selectionne - UserID: " + utilisateurId);
            session.setAttribute("messageErreur",
                "Veuillez selectionner votre operateur Mobile Money.");
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }
        operateur = operateur.trim();

        // 6. Verifier si un paiement est deja en attente
        PaiementDAO paiementDAO = new PaiementDAO();
        if (paiementDAO.aPaiementEnAttente(utilisateurId)) {
            SecuriteUtil.logWarn("Paiement deja en attente - UserID: " + utilisateurId);
            session.setAttribute("messageErreur",
                "Vous avez deja un paiement en attente de confirmation. Veuillez patienter.");
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }

        // 7. Generation de la reference unique (UUID)
        String reference = SecuriteUtil.genererReference(utilisateurId);
        double montant = PawaPayConfig.MONTANT_USD;

        SecuriteUtil.logInfo("Debut creation paiement - UserID: " + utilisateurId
            + " - Ref: " + reference + " - Tel: " + SecuriteUtil.masquerTelephone(telephone)
            + " - Operateur: " + operateur);

        // 8. Enregistrement du paiement en statut PENDING dans la base
        Paiement paiement = new Paiement(utilisateurId, reference, montant, telephone);
        paiement.setStatut(Paiement.STATUT_PENDING);
        paiement.setDevise(PawaPayConfig.DEVISE);

        boolean enregistre = paiementDAO.inserer(paiement);

        if (!enregistre) {
            SecuriteUtil.logErreur("Echec enregistrement paiement - UserID: " + utilisateurId);
            session.setAttribute("messageErreur",
                "Erreur lors de l'initialisation du paiement. Veuillez reessayer.");
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }

        // 9. Envoi de la demande de paiement a PawaPay
        Map<String, Object> resultatPawaPay = PawaPayUtil.creerPaiement(reference, montant, telephone, operateur);

        boolean success = (boolean) resultatPawaPay.get("success");

        if (success) {
            // Paiement cree avec succes chez PawaPay
            String transactionId = (String) resultatPawaPay.get("transactionId");

            // Mettre a jour le transaction_id dans la base
            if (transactionId != null && !transactionId.isEmpty()) {
                paiementDAO.mettreAJourStatut(reference, Paiement.STATUT_PENDING, transactionId);
            }

            SecuriteUtil.logSucces("Paiement cree chez PawaPay - UserID: " + utilisateurId
                + " - Ref: " + reference + " - TransactionID: " + transactionId);

            // Stocker les infos en session pour la page d'attente
            session.setAttribute("referencePaiement", reference);
            session.setAttribute("telephonePaiement", SecuriteUtil.masquerTelephone(telephone));
            session.setAttribute("montantPaiement", montant);

            // Rediriger vers la page d'attente
            response.sendRedirect(request.getContextPath() + "/paiement-attente");

        } else {
            // Echec de la creation chez PawaPay
            String erreur = (String) resultatPawaPay.get("erreur");

            // Mettre a jour le statut en FAILED
            paiementDAO.mettreAJourStatut(reference, Paiement.STATUT_FAILED, null);

            SecuriteUtil.logErreur("Echec creation PawaPay - UserID: " + utilisateurId
                + " - Ref: " + reference + " - Erreur: " + erreur);

            session.setAttribute("messageErreur",
                "Le service de paiement est momentanement indisponible. Veuillez reessayer.");
            response.sendRedirect(request.getContextPath() + "/paiement");
        }
    }
}