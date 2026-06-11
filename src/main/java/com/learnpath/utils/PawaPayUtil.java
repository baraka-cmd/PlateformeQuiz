// ============================================================
// FICHIER : src/main/java/com/LearnPath/utils/PawaPayUtil.java
// RÔLE    : Utilitaire de communication avec l'API PawaPay
//           - Création de dépôt (paiement)
//           - Vérification statut d'un paiement
//           - Vérification signature webhook
//           - Gestion des erreurs et timeouts
// ============================================================

package com.learnpath.utils;

import com.learnpath.config.PawaPayConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * PawaPayUtil - Utilitaire de communication avec l'API PawaPay
 * 
 * Fournit toutes les méthodes pour interagir avec PawaPay :
 * création de paiement, vérification de statut,
 * validation des webhooks entrants.
 * 
 * Utilise HttpURLConnection (sans dépendance externe supplémentaire).
 * 
 * @author LearnPath
 * @version 1.2
 */
public final class PawaPayUtil {

    // ===== CONSTRUCTEUR PRIVÉ =====
    private PawaPayUtil() {
        throw new UnsupportedOperationException("Classe utilitaire - ne pas instancier");
    }

    // ===== CONSTANTES =====
    private static final Gson GSON = new Gson();
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_GET = "GET";
    private static final String HMAC_SHA256 = "HmacSHA256";

    // ============================================================
    // CRÉATION DE PAIEMENT (DÉPÔT)
    // ============================================================

    /**
     * Crée un dépôt de paiement via l'API PawaPay
     * 
     * @param reference  Référence unique de la transaction (UUID v4)
     * @param montant    Montant en USD (ex: 4.00)
     * @param telephone  Numéro de téléphone au format international (ex: +243XXXXXXXXX)
     * @param provider   Code du fournisseur Mobile Money (ex: AIRTEL_CDI, ORANGE_CDI)
     * @return Map contenant :
     *         - "success" (Boolean) : true si la requête a abouti
     *         - "transactionId" (String) : ID PawaPay si succès, null sinon
     *         - "message" (String) : message descriptif
     *         - "erreur" (String) : message d'erreur si échec
     */
    public static Map<String, Object> creerPaiement(String reference, double montant, String telephone, String provider) {
        Map<String, Object> resultat = new HashMap<>();

        // 1. Validation des paramètres
        if (reference == null || reference.trim().isEmpty()) {
            resultat.put("success", false);
            resultat.put("erreur", "La référence de paiement est obligatoire.");
            return resultat;
        }

        if (montant <= 0) {
            resultat.put("success", false);
            resultat.put("erreur", "Le montant doit être supérieur à 0.");
            return resultat;
        }

        if (telephone == null || !telephone.matches("^\\+?[0-9]{10,15}$")) {
            resultat.put("success", false);
            resultat.put("erreur", "Numéro de téléphone invalide. Format attendu : +243XXXXXXXXX");
            return resultat;
        }

        if (provider == null || provider.trim().isEmpty()) {
            resultat.put("success", false);
            resultat.put("erreur", "L'opérateur Mobile Money est obligatoire.");
            return resultat;
        }

        HttpURLConnection connexion = null;

        try {
            // 2. Construction du corps de la requête selon la documentation PawaPay v2
            JsonObject body = new JsonObject();
            body.addProperty("depositId", reference);
            body.addProperty("amount", String.valueOf((int) montant));
            body.addProperty("currency", PawaPayConfig.DEVISE);

            // Payer (obligatoire - format v2)
            JsonObject payer = new JsonObject();
            payer.addProperty("type", "MMO");

            JsonObject accountDetails = new JsonObject();
            accountDetails.addProperty("phoneNumber", telephone);
            accountDetails.addProperty("provider", provider);
            payer.add("accountDetails", accountDetails);

            body.add("payer", payer);

            // 3. Envoi de la requête HTTP POST
            URL url = new URL(PawaPayConfig.DEPOSIT_URL);
            connexion = (HttpURLConnection) url.openConnection();
            connexion.setRequestMethod(METHOD_POST);
            connexion.setRequestProperty(HEADER_AUTHORIZATION, "Bearer " + PawaPayConfig.API_TOKEN);
            connexion.setRequestProperty(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
            connexion.setDoOutput(true);
            connexion.setConnectTimeout(PawaPayConfig.CONNECT_TIMEOUT_MS);
            connexion.setReadTimeout(PawaPayConfig.READ_TIMEOUT_MS);

            // Écriture du corps JSON
            String jsonBody = GSON.toJson(body);

            // Log du JSON envoyé pour debug
            System.out.println("📤 [PawaPay] JSON envoyé à PawaPay :");
            System.out.println("   " + jsonBody);

            try (OutputStream os = connexion.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 4. Lecture de la réponse
            int codeReponse = connexion.getResponseCode();
            String reponseJson = lireReponse(connexion);

            System.out.println("📤 [PawaPay] Création paiement - Référence: " + reference);
            System.out.println("   Code HTTP: " + codeReponse);
            System.out.println("   Réponse: " + reponseJson);

            // 5. Analyse de la réponse
            if (codeReponse == 200 || codeReponse == 201) {
                JsonObject reponse = JsonParser.parseString(reponseJson).getAsJsonObject();

                String transactionId = null;
                if (reponse.has("depositId")) {
                    transactionId = reponse.get("depositId").getAsString();
                } else if (reponse.has("transactionId")) {
                    transactionId = reponse.get("transactionId").getAsString();
                } else if (reponse.has("id")) {
                    transactionId = reponse.get("id").getAsString();
                }

                resultat.put("success", true);
                resultat.put("transactionId", transactionId);
                resultat.put("message", "Paiement créé avec succès. En attente de confirmation Mobile Money.");

                System.out.println("✅ [PawaPay] Paiement créé - Transaction ID: " + transactionId);

            } else if (codeReponse == 400) {
                // Essayer d'extraire le message d'erreur détaillé de PawaPay
                String messageErreur = extraireMessageErreurPawaPay(reponseJson);
                resultat.put("success", false);
                resultat.put("erreur", messageErreur);

                System.err.println("❌ [PawaPay] Erreur création paiement: " + messageErreur);
            } else {
                // Autre erreur API
                String messageErreur = extraireMessageErreur(reponseJson, codeReponse);
                resultat.put("success", false);
                resultat.put("erreur", messageErreur);

                System.err.println("❌ [PawaPay] Erreur création paiement: " + messageErreur);
            }

        } catch (IOException e) {
            resultat.put("success", false);
            resultat.put("erreur", "Erreur réseau : " + e.getMessage());
            System.err.println("❌ [PawaPay] Erreur réseau: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (connexion != null) {
                connexion.disconnect();
            }
        }

        return resultat;
    }

    // ============================================================
    // VÉRIFICATION STATUT PAIEMENT
    // ============================================================

    /**
     * Vérifie le statut d'un paiement auprès de PawaPay
     * 
     * @param transactionId L'ID de transaction PawaPay (depositId)
     * @return Map contenant :
     *         - "success" (Boolean)
     *         - "statut" (String) : ACCEPTED, COMPLETED, REJECTED, FAILED...
     *         - "message" (String)
     */
    public static Map<String, Object> verifierStatutPaiement(String transactionId) {
        Map<String, Object> resultat = new HashMap<>();

        if (transactionId == null || transactionId.trim().isEmpty()) {
            resultat.put("success", false);
            resultat.put("erreur", "Transaction ID manquant.");
            return resultat;
        }

        HttpURLConnection connexion = null;

        try {
            URL url = new URL(PawaPayConfig.DEPOSIT_STATUS_URL + transactionId);
            connexion = (HttpURLConnection) url.openConnection();
            connexion.setRequestMethod(METHOD_GET);
            connexion.setRequestProperty(HEADER_AUTHORIZATION, "Bearer " + PawaPayConfig.API_TOKEN);
            connexion.setConnectTimeout(PawaPayConfig.CONNECT_TIMEOUT_MS);
            connexion.setReadTimeout(PawaPayConfig.READ_TIMEOUT_MS);

            int codeReponse = connexion.getResponseCode();
            String reponseJson = lireReponse(connexion);

            System.out.println("🔍 [PawaPay] Vérification statut - Transaction: " + transactionId);
            System.out.println("   Code HTTP: " + codeReponse);

            if (codeReponse == 200) {
                JsonObject reponse = JsonParser.parseString(reponseJson).getAsJsonObject();

                String statut = "UNKNOWN";
                if (reponse.has("status")) {
                    statut = reponse.get("status").getAsString();
                }

                resultat.put("success", true);
                resultat.put("statut", statut);
                resultat.put("reponse", reponseJson);

                System.out.println("   Statut: " + statut);
            } else {
                resultat.put("success", false);
                resultat.put("erreur", "Transaction introuvable ou erreur API.");
            }

        } catch (IOException e) {
            resultat.put("success", false);
            resultat.put("erreur", "Erreur réseau : " + e.getMessage());
            System.err.println("❌ [PawaPay] Erreur vérification: " + e.getMessage());
        } finally {
            if (connexion != null) {
                connexion.disconnect();
            }
        }

        return resultat;
    }

    // ============================================================
    // VÉRIFICATION SIGNATURE WEBHOOK
    // ============================================================

    /**
     * Vérifie la signature HMAC d'un webhook entrant de PawaPay.
     * En sandbox, le secret peut être vide : la vérification est alors ignorée.
     * 
     * @param payload   Le corps JSON brut reçu
     * @param signature L'en-tête X-PawaPay-Signature (ou équivalent)
     * @return true si la signature est valide ou si le secret est vide (sandbox)
     */
    public static boolean verifierSignatureWebhook(String payload, String signature) {
        if (PawaPayConfig.WEBHOOK_SECRET == null || PawaPayConfig.WEBHOOK_SECRET.isEmpty()) {
            System.out.println("⚠️ [PawaPay] Vérification signature désactivée (sandbox)");
            return true;
        }

        if (signature == null || signature.isEmpty()) {
            System.err.println("❌ [PawaPay] Signature webhook manquante");
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(
                PawaPayConfig.WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String signatureCalculee = Base64.getEncoder().encodeToString(hash);

            boolean estValide = signatureCalculee.equals(signature);

            if (estValide) {
                System.out.println("✅ [PawaPay] Signature webhook valide");
            } else {
                System.err.println("❌ [PawaPay] Signature webhook invalide");
                System.err.println("   Attendue: " + signature);
                System.err.println("   Calculée: " + signatureCalculee);
            }

            return estValide;

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            System.err.println("❌ [PawaPay] Erreur vérification signature: " + e.getMessage());
            return false;
        }
    }

    // ============================================================
    // VALIDATION IP WEBHOOK
    // ============================================================

    /**
     * Vérifie si l'IP appelante est autorisée (sécurité webhook)
     * 
     * @param ipAdresse L'adresse IP du client appelant
     * @return true si l'IP est dans la liste autorisée
     */
    public static boolean estIpAutorisee(String ipAdresse) {
        if (ipAdresse == null) return false;

        for (String ipAutorisee : PawaPayConfig.IPS_AUTORISEES) {
            if (ipAutorisee.equals(ipAdresse)) {
                return true;
            }
        }

        System.err.println("❌ [PawaPay] IP non autorisée pour webhook: " + ipAdresse);
        return false;
    }

    // ============================================================
    // MÉTHODES PRIVÉES
    // ============================================================

    /**
     * Lit la réponse HTTP (succès ou erreur)
     * 
     * @param connexion La connexion HTTP
     * @return Le corps de la réponse en String
     * @throws IOException En cas d'erreur de lecture
     */
    private static String lireReponse(HttpURLConnection connexion) throws IOException {
        StringBuilder reponse = new StringBuilder();

        try {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connexion.getInputStream(), StandardCharsets.UTF_8))) {
                String ligne;
                while ((ligne = reader.readLine()) != null) {
                    reponse.append(ligne);
                }
            }
        } catch (IOException e) {
            if (connexion.getErrorStream() != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connexion.getErrorStream(), StandardCharsets.UTF_8))) {
                    String ligne;
                    while ((ligne = reader.readLine()) != null) {
                        reponse.append(ligne);
                    }
                }
            }
        }

        return reponse.toString();
    }

    /**
     * Extrait un message d'erreur lisible depuis la réponse PawaPay (format v2)
     * 
     * @param reponseJson Corps JSON de la réponse
     * @return Message d'erreur formaté
     */
    private static String extraireMessageErreurPawaPay(String reponseJson) {
        if (reponseJson == null || reponseJson.isEmpty()) {
            return "Erreur API PawaPay - Réponse vide";
        }

        try {
            JsonObject reponse = JsonParser.parseString(reponseJson).getAsJsonObject();

            // Format v2 : "failureReason": {"failureCode": "...", "failureMessage": "..."}
            if (reponse.has("failureReason")) {
                JsonObject failureReason = reponse.getAsJsonObject("failureReason");
                if (failureReason.has("failureMessage")) {
                    String failureCode = failureReason.has("failureCode") ? 
                        failureReason.get("failureCode").getAsString() + " - " : "";
                    return failureCode + failureReason.get("failureMessage").getAsString();
                }
            }

            // Format v1 : "errorMessage"
            if (reponse.has("errorMessage")) {
                return reponse.get("errorMessage").getAsString();
            }

            // Format générique : "message"
            if (reponse.has("message")) {
                return reponse.get("message").getAsString();
            }

        } catch (Exception e) {
            // JSON invalide, retourner le brut
        }

        return "Erreur API PawaPay : " + reponseJson;
    }

    /**
     * Extrait un message d'erreur lisible depuis la réponse PawaPay
     * 
     * @param reponseJson Corps JSON de la réponse
     * @param codeHttp    Code HTTP reçu
     * @return Message d'erreur formaté
     */
    private static String extraireMessageErreur(String reponseJson, int codeHttp) {
        if (reponseJson == null || reponseJson.isEmpty()) {
            return "Erreur API PawaPay (HTTP " + codeHttp + ") - Réponse vide";
        }

        // Essayer d'abord le format v2
        String messageV2 = extraireMessageErreurPawaPay(reponseJson);
        if (!messageV2.startsWith("Erreur API PawaPay :")) {
            return messageV2;
        }

        try {
            JsonObject reponse = JsonParser.parseString(reponseJson).getAsJsonObject();

            if (reponse.has("error")) {
                JsonObject error = reponse.getAsJsonObject("error");
                if (error.has("message")) {
                    return error.get("message").getAsString();
                }
            }
            if (reponse.has("message")) {
                return reponse.get("message").getAsString();
            }
            if (reponse.has("errorMessage")) {
                return reponse.get("errorMessage").getAsString();
            }

        } catch (Exception e) {
            // JSON invalide, retourner le brut
        }

        return "Erreur API PawaPay (HTTP " + codeHttp + ") : " + reponseJson;
    }
}