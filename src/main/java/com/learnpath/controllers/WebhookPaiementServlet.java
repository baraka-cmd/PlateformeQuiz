// ============================================================
// FICHIER : src/main/java/com/LearnPath/controllers/WebhookPaiementServlet.java
// RÔLE    : Endpoint de réception du callback PawaPay (webhook)
//           Reçoit la confirmation de paiement, valide,
//           active le premium et journalise l'événement
// ============================================================

package com.learnpath.controllers;

import com.learnpath.dao.AccessPremiumDAO;
import com.learnpath.dao.PaiementDAO;
import com.learnpath.dao.UtilisateurDAO;
import com.learnpath.models.Paiement;
import com.learnpath.models.Utilisateur;
import com.learnpath.utils.PawaPayUtil;
import com.learnpath.utils.SecuriteUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * WebhookPaiementServlet - Réception des callbacks PawaPay
 * 
 * Endpoint appelé par PawaPay après un paiement Mobile Money.
 * 
 * TRAITEMENT :
 * 1. Lit le corps JSON de la requête
 * 2. Vérifie la signature du webhook (si configurée)
 * 3. Vérifie l'IP appelante (sécurité)
 * 4. Extrait la référence, le statut et le transaction_id
 * 5. Met à jour le paiement dans la base
 * 6. Si SUCCESS → active le premium + synchro utilisateur
 * 7. Journalise l'événement
 * 
 * URL : /api/paiements/webhook
 * Méthode : POST (appelée par PawaPay uniquement)
 * 
 * @author LearnPath
 * @version 1.0
 */
@WebServlet(name = "WebhookPaiementServlet", urlPatterns = {"/api/paiements/webhook"})
public class WebhookPaiementServlet extends HttpServlet {

    // ===== CONSTANTES =====
    private static final String HEADER_SIGNATURE = "X-PawaPay-Signature";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType(CONTENT_TYPE_JSON);

        // ============================================================
        // 1. VÉRIFICATION DE L'IP APPELANTE
        // ============================================================
        String ipCliente = recupererIpCliente(request);

        if (!PawaPayUtil.estIpAutorisee(ipCliente)) {
            SecuriteUtil.logErreur("Webhook rejeté - IP non autorisée : " + ipCliente);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"IP non autorisée\"}");
            return;
        }

        // ============================================================
        // 2. LECTURE DU CORPS DE LA REQUÊTE
        // ============================================================
        String corpsRequete = lireCorpsRequete(request);

        if (corpsRequete == null || corpsRequete.isEmpty()) {
            SecuriteUtil.logErreur("Webhook reçu sans corps de requête");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Corps de requête vide\"}");
            return;
        }

        SecuriteUtil.logInfo("Webhook reçu - IP: " + ipCliente);
        System.out.println("   Payload: " + corpsRequete);

        // ============================================================
        // 3. VÉRIFICATION DE LA SIGNATURE (SI CONFIGURÉE)
        // ============================================================
        String signature = request.getHeader(HEADER_SIGNATURE);

        if (!PawaPayUtil.verifierSignatureWebhook(corpsRequete, signature)) {
            SecuriteUtil.logErreur("Webhook rejeté - Signature invalide");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Signature invalide\"}");
            return;
        }

        // ============================================================
        // 4. PARSING DU JSON
        // ============================================================
        String reference;
        String transactionId;
        String statut;
        double montant;

        try {
            JsonObject json = JsonParser.parseString(corpsRequete).getAsJsonObject();

            // Récupérer la référence (le champ peut varier selon l'API PawaPay)
            reference = extraireChampJson(json, "reference", "merchantReference", "ref");

            // Récupérer le transaction ID
            transactionId = extraireChampJson(json, "depositId", "transactionId", "id", "transaction_id");

            // Récupérer le statut
            statut = extraireChampJson(json, "status", "state");
            if (statut != null) {
                statut = statut.toUpperCase();
            }

            // Récupérer le montant (optionnel, pour vérification)
            montant = 0.0;
            if (json.has("amount")) {
                try {
                    montant = Double.parseDouble(json.get("amount").getAsString());
                } catch (NumberFormatException e) {
                    montant = json.get("amount").getAsDouble();
                }
            }

        } catch (Exception e) {
            SecuriteUtil.logErreur("Erreur parsing JSON webhook : " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"JSON invalide\"}");
            return;
        }

        // ============================================================
        // 5. VALIDATION DES DONNÉES
        // ============================================================
        if (reference == null || reference.isEmpty()) {
            SecuriteUtil.logErreur("Webhook sans référence");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Référence manquante\"}");
            return;
        }

        if (statut == null || statut.isEmpty()) {
            SecuriteUtil.logErreur("Webhook sans statut - Réf: " + reference);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Statut manquant\"}");
            return;
        }

        // Vérifier que la référence existe dans notre base
        PaiementDAO paiementDAO = new PaiementDAO();
        Paiement paiementExistant = paiementDAO.trouverParReference(reference);

        if (paiementExistant == null) {
            SecuriteUtil.logErreur("Webhook - Référence inconnue : " + reference);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\":\"Référence inconnue\"}");
            return;
        }

        int utilisateurId = paiementExistant.getUtilisateurId();

        // ============================================================
        // 6. MISE À JOUR DU PAIEMENT
        // ============================================================
        String nouveauStatut;

        switch (statut) {
            case "SUCCESS":
            case "COMPLETED":
            case "SUCCEEDED":
                nouveauStatut = Paiement.STATUT_SUCCESS;
                break;
            case "FAILED":
            case "FAILURE":
            case "REJECTED":
                nouveauStatut = Paiement.STATUT_FAILED;
                break;
            case "CANCELLED":
            case "CANCELED":
                nouveauStatut = Paiement.STATUT_CANCELLED;
                break;
            case "EXPIRED":
                nouveauStatut = Paiement.STATUT_EXPIRED;
                break;
            case "PENDING":
            case "PROCESSING":
                nouveauStatut = Paiement.STATUT_PENDING;
                break;
            default:
                nouveauStatut = Paiement.STATUT_PENDING;
        }

        boolean misAJour = paiementDAO.mettreAJourStatut(reference, nouveauStatut, transactionId);

        if (!misAJour) {
            SecuriteUtil.logErreur("Échec mise à jour paiement - Réf: " + reference);
        }

        // ============================================================
        // 7. TRAITEMENT SI PAIEMENT RÉUSSI
        // ============================================================
        if (Paiement.STATUT_SUCCESS.equals(nouveauStatut)) {

            // Vérifier le montant
            if (montant > 0 && !SecuriteUtil.estMontantCorrect(montant)) {
                SecuriteUtil.logWarn("Montant incorrect - Réf: " + reference
                    + " - Attendu: " + com.learnpath.config.PawaPayConfig.MONTANT_USD
                    + " - Reçu: " + montant);
            }

            // Activer le premium
            AccessPremiumDAO premiumDAO = new AccessPremiumDAO();
            boolean premiumActive = premiumDAO.activerPremium(
                utilisateurId,
                reference,
                paiementExistant.getMontant(),
                paiementExistant.getDevise()
            );

            if (premiumActive) {
                SecuriteUtil.logSucces("🎉 PREMIUM ACTIVÉ - UserID: " + utilisateurId
                    + " - Réf: " + reference
                    + " - TransactionID: " + transactionId);

                // Récupérer l'email pour notification (optionnel)
                UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
                Utilisateur utilisateur = utilisateurDAO.trouverParId(utilisateurId);

                if (utilisateur != null && utilisateur.getEmail() != null) {
                    // TODO: Envoyer email de confirmation premium
                    // EmailUtil.envoyerConfirmationPremium(utilisateur.getEmail(), utilisateur.getPseudo());
                    System.out.println("📧 Email de confirmation à envoyer à : " + utilisateur.getEmail());
                }
            } else {
                SecuriteUtil.logErreur("Échec activation premium - UserID: " + utilisateurId);
            }

        } else if (Paiement.STATUT_FAILED.equals(nouveauStatut)) {
            SecuriteUtil.logErreur("Paiement échoué - UserID: " + utilisateurId
                + " - Réf: " + reference);

        } else if (Paiement.STATUT_CANCELLED.equals(nouveauStatut)) {
            SecuriteUtil.logWarn("Paiement annulé - UserID: " + utilisateurId
                + " - Réf: " + reference);

        } else if (Paiement.STATUT_EXPIRED.equals(nouveauStatut)) {
            SecuriteUtil.logWarn("Paiement expiré - UserID: " + utilisateurId
                + " - Réf: " + reference);
        }

        // ============================================================
        // 8. RÉPONSE À PAWAPAY
        // ============================================================
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("{\"status\":\"received\"}");

        SecuriteUtil.logInfo("Webhook traité avec succès - Réf: " + reference
            + " - Statut: " + nouveauStatut);
    }

    // ============================================================
    // MÉTHODES PRIVÉES
    // ============================================================

    /**
     * Lit le corps complet de la requête HTTP
     * 
     * @param request La requête HTTP
     * @return Le corps en String, ou null si erreur
     */
    private String lireCorpsRequete(HttpServletRequest request) {
        StringBuilder corps = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                corps.append(ligne);
            }
        } catch (IOException e) {
            SecuriteUtil.logErreur("Erreur lecture corps requête webhook : " + e.getMessage());
            return null;
        }

        return corps.toString();
    }

    /**
     * Récupère l'adresse IP réelle du client (même derrière proxy)
     * 
     * @param request La requête HTTP
     * @return L'adresse IP du client
     */
    private String recupererIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Si plusieurs IPs (proxy chain), prendre la première
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * Extrait un champ JSON en testant plusieurs noms possibles.
     * Utile car les APIs peuvent utiliser des noms de champs différents.
     * 
     * @param json   L'objet JSON
     * @param champs Liste de noms de champs possibles
     * @return La valeur trouvée, ou null
     */
    private String extraireChampJson(JsonObject json, String... champs) {
        for (String champ : champs) {
            if (json.has(champ) && !json.get(champ).isJsonNull()) {
                try {
                    return json.get(champ).getAsString();
                } catch (Exception e) {
                    // Essayer comme nombre
                    try {
                        return String.valueOf(json.get(champ).getAsDouble());
                    } catch (Exception e2) {
                        // Ignorer ce champ
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // GET non autorisé sur le webhook
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.getWriter().write("{\"error\":\"Méthode non autorisée. Utilisez POST.\"}");
    }
}