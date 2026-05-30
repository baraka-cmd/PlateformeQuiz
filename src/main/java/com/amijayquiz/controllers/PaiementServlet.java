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
 *        (montant, avantages premium, formulaire de téléphone)
 * 
 * POST : 1. Valide le numéro de téléphone
 *        2. Génère une référence unique
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

        // 1. Vérification authentification
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + URL_CONNEXION);
            return;
        }

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");

        // 2. Vérifier si déjà premium
        AccessPremiumDAO premiumDAO = new AccessPremiumDAO();
        if (premiumDAO.estPremium(utilisateur.getId())) {
            session.setAttribute("messageSucces", "✅ Vous êtes déjà Premium ! Tous les niveaux sont débloqués.");
            response.sendRedirect(request.getContextPath() + URL_DASHBOARD);
            return;
        }

        // 3. Vérifier si un paiement est déjà en attente
        PaiementDAO paiementDAO = new PaiementDAO();
        if (paiementDAO.aPaiementEnAttente(utilisateur.getId())) {
            session.setAttribute("messageErreur",
                "⚠️ Vous avez déjà un paiement en attente. Veuillez patienter ou réessayer.");
        }

        // 4. Générer un token CSRF pour le formulaire
        String tokenCSRF = SecuriteUtil.genererTokenCSRF();
        session.setAttribute("csrf_token_paiement", tokenCSRF);

        // 5. Passer les attributs à la JSP
        request.setAttribute("pageTitle", "Débloquer tous les niveaux - AmijayQuiz");
        request.setAttribute("montantUSD", PawaPayConfig.MONTANT_USD);
        request.setAttribute("montantFC", PawaPayConfig.MONTANT_FC);
        request.setAttribute("tokenCSRF", tokenCSRF);

        // Message d'erreur depuis la session (redirection après échec)
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

        // 1. Vérification authentification
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("utilisateur") == null) {
            response.sendRedirect(request.getContextPath() + URL_CONNEXION);
            return;
        }

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        int utilisateurId = utilisateur.getId();

        // 2. Vérifier si déjà premium (double-clic ou malveillance)
        AccessPremiumDAO premiumDAO = new AccessPremiumDAO();
        if (premiumDAO.estPremium(utilisateurId)) {
            session.setAttribute("messageSucces", "✅ Vous êtes déjà Premium !");
            response.sendRedirect(request.getContextPath() + URL_DASHBOARD);
            return;
        }

        // 3. Vérifier token CSRF
        String tokenCSRFSession = (String) session.getAttribute("csrf_token_paiement");
        String tokenCSRFRequete = request.getParameter("csrf_token");
        if (!SecuriteUtil.verifierTokenCSRF(tokenCSRFSession, tokenCSRFRequete)) {
            SecuriteUtil.logErreur("Tentative CSRF détectée - UserID: " + utilisateurId);
            session.setAttribute("messageErreur", "⛔ Erreur de sécurité. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }
        session.removeAttribute("csrf_token_paiement");

        // 4. Récupération et validation du téléphone
        String telephoneBrut = request.getParameter("telephone");
        String telephone = SecuriteUtil.normaliserTelephone(telephoneBrut);

        if (telephone == null) {
            SecuriteUtil.logWarn("Téléphone invalide - UserID: " + utilisateurId + " - Saisi: " + telephoneBrut);
            session.setAttribute("messageErreur",
                "📱 Numéro de téléphone invalide. Format attendu : +243XXXXXXXXX ou 0XXXXXXXXX");
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }

        // 5. Vérifier si un paiement est déjà en attente
        PaiementDAO paiementDAO = new PaiementDAO();
        if (paiementDAO.aPaiementEnAttente(utilisateurId)) {
            SecuriteUtil.logWarn("Paiement déjà en attente - UserID: " + utilisateurId);
            session.setAttribute("messageErreur",
                "⚠️ Vous avez déjà un paiement en attente de confirmation. Veuillez patienter.");
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }

        // 6. Génération de la référence unique
        String reference = SecuriteUtil.genererReference(utilisateurId);
        double montant = PawaPayConfig.MONTANT_USD;

        SecuriteUtil.logInfo("Début création paiement - UserID: " + utilisateurId
            + " - Réf: " + reference + " - Tél: " + SecuriteUtil.masquerTelephone(telephone));

        // 7. Enregistrement du paiement en statut PENDING dans la base
        Paiement paiement = new Paiement(utilisateurId, reference, montant, telephone);
        paiement.setStatut(Paiement.STATUT_PENDING);
        paiement.setDevise(PawaPayConfig.DEVISE);

        boolean enregistre = paiementDAO.inserer(paiement);

        if (!enregistre) {
            SecuriteUtil.logErreur("Échec enregistrement paiement - UserID: " + utilisateurId);
            session.setAttribute("messageErreur",
                "❌ Erreur lors de l'initialisation du paiement. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/paiement");
            return;
        }

        // 8. Envoi de la demande de paiement à PawaPay
        Map<String, Object> resultatPawaPay = PawaPayUtil.creerPaiement(reference, montant, telephone);

        boolean success = (boolean) resultatPawaPay.get("success");

        if (success) {
            // Paiement créé avec succès chez PawaPay
            String transactionId = (String) resultatPawaPay.get("transactionId");

            // Mettre à jour le transaction_id dans la base
            if (transactionId != null && !transactionId.isEmpty()) {
                paiementDAO.mettreAJourStatut(reference, Paiement.STATUT_PENDING, transactionId);
            }

            SecuriteUtil.logSucces("Paiement créé chez PawaPay - UserID: " + utilisateurId
                + " - Réf: " + reference + " - TransactionID: " + transactionId);

            // Stocker les infos en session pour la page d'attente
            session.setAttribute("referencePaiement", reference);
            session.setAttribute("telephonePaiement", SecuriteUtil.masquerTelephone(telephone));
            session.setAttribute("montantPaiement", montant);

            // Rediriger vers la page d'attente
            response.sendRedirect(request.getContextPath() + "/paiement-attente");

        } else {
            // Échec de la création chez PawaPay
            String erreur = (String) resultatPawaPay.get("erreur");

            // Mettre à jour le statut en FAILED
            paiementDAO.mettreAJourStatut(reference, Paiement.STATUT_FAILED, null);

            SecuriteUtil.logErreur("Échec création PawaPay - UserID: " + utilisateurId
                + " - Réf: " + reference + " - Erreur: " + erreur);

            session.setAttribute("messageErreur",
                "❌ Le service de paiement est momentanément indisponible. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/paiement");
        }
    }
}