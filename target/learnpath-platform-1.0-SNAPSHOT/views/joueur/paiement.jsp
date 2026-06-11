<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.learnpath.models.Utilisateur" %>

<%-- ============================================================
     FICHIER : views/joueur/paiement.jsp
     RÔLE    : Page de paiement LearnPath
              - Système PawaPay (désactivé temporairement)
              - Système manuel (actif)
              - Instructions claires pour le client
     ============================================================ --%>

<%
    // Vérification authentification
    Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
    if (utilisateur == null) {
        response.sendRedirect(request.getContextPath() + "/connexion");
        return;
    }

    // Vérifier si déjà premium
    com.learnpath.dao.AccessPremiumDAO premiumDAOCheck = new com.learnpath.dao.AccessPremiumDAO();
    if (premiumDAOCheck.estPremium(utilisateur.getId())) {
        session.setAttribute("messageSucces", "Vous etes deja Premium ! Tous les niveaux sont debloques.");
        response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
        return;
    }

    // Récupération des attributs
    Double montantUSD = (Double) request.getAttribute("montantUSD");
    Integer montantFC = (Integer) request.getAttribute("montantFC");
    String tokenCSRF = (String) request.getAttribute("tokenCSRF");
    String messageErreur = (String) request.getAttribute("messageErreur");

    if (montantUSD == null) montantUSD = 4.00;
    if (montantFC == null) montantFC = 10000;
    if (tokenCSRF == null) tokenCSRF = "";

    // ===== CONFIGURATION : Mettre à false pour désactiver PawaPay =====
    boolean pawapayActif = false;
    // =================================================================

    request.setAttribute("pageTitle", "Débloquer tous les niveaux - LearnPath");
%>

<%
    String paiementCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/paiement.css\">";
    request.setAttribute("extraHead", paiementCSS);
%>

<%@ include file="../header.jsp" %>

<div class="paiement-container">

    <%-- ===== BREADCRUMB ===== --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-crown"></i><span>Premium</span>
        </span>
    </nav>

    <%-- ===== MESSAGE D'ERREUR ===== --%>
    <% if (messageErreur != null) { %>
        <div class="message-alerte message-erreur">
            <i class="fas fa-exclamation-circle"></i>
            <span><%= messageErreur %></span>
        </div>
    <% } %>

    <%-- ===== MESSAGE DE SUCCÈS (après soumission manuelle) ===== --%>
    <%
        String messageSucces = (String) session.getAttribute("messageSucces");
        if (messageSucces != null) {
            session.removeAttribute("messageSucces");
    %>
        <div class="message-alerte message-succes">
            <i class="fas fa-check-circle"></i>
            <span><%= messageSucces %></span>
        </div>
    <% } %>

    <%-- ============================================================
         CARTE PRINCIPALE
         ============================================================ --%>
    <div class="paiement-card">

        <%-- ===== EN-TÊTE ===== --%>
        <div class="paiement-header">
            <div class="paiement-icon">
                <i class="fas fa-crown"></i>
            </div>
            <h1>Débloquez tous les niveaux</h1>
            <p class="paiement-sous-titre">
                Un seul paiement — Accès à vie à tous les niveaux Moyen et Avancé
            </p>
        </div>

        <%-- ===== GRILLE DE PRIX ===== --%>
        <div class="paiement-prix-grid">
            <div class="prix-card prix-usd">
                <span class="prix-montant"><%= String.format("%.0f", montantUSD) %> $</span>
                <span class="prix-devise">Dollars USD</span>
            </div>
            <div class="prix-ou">
                <span>OU</span>
            </div>
            <div class="prix-card prix-fc">
                <span class="prix-montant"><%= montantFC %> FC</span>
                <span class="prix-devise">Francs Congolais</span>
            </div>
        </div>

        <%-- ===== AVANTAGES ===== --%>
        <div class="paiement-avantages">
            <h3><i class="fas fa-gem"></i> Ce que vous obtenez</h3>

            <div class="avantage-item">
                <div class="avantage-icon" style="background: linear-gradient(135deg, #27ae60, #2ecc71);">
                    <i class="fas fa-book-open"></i>
                </div>
                <div class="avantage-texte">
                    <strong>Tous les cours Moyen et Avancé</strong>
                    <span>12 thèmes × 3 niveaux = des dizaines de cours complets</span>
                </div>
            </div>

            <div class="avantage-item">
                <div class="avantage-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                    <i class="fas fa-layer-group"></i>
                </div>
                <div class="avantage-texte">
                    <strong>Tous les quiz débloqués</strong>
                    <span>6 thèmes × 20 parties par niveau = 360 parties de quiz</span>
                </div>
            </div>

            <div class="avantage-item">
                <div class="avantage-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                    <i class="fas fa-infinity"></i>
                </div>
                <div class="avantage-texte">
                    <strong>Accès à vie</strong>
                    <span>Payez une seule fois, accédez pour toujours</span>
                </div>
            </div>

            <div class="avantage-item">
                <div class="avantage-icon" style="background: linear-gradient(135deg, #e74c3c, #c0392b);">
                    <i class="fas fa-certificate"></i>
                </div>
                <div class="avantage-texte">
                    <strong>Certificats débloqués</strong>
                    <span>Passez tous les examens et obtenez vos certificats</span>
                </div>
            </div>
        </div>

        <%-- ============================================================
             SECTION PAWAPAY (DÉSACTIVÉE TEMPORAIREMENT)
             ============================================================ --%>
        <% if (pawapayActif) { %>
            <div class="paiement-form-section">
                <h3><i class="fas fa-mobile-alt"></i> Paiement automatique par Mobile Money</h3>
                <p class="form-description">
                    Entrez votre numéro de téléphone Mobile Money pour procéder au paiement.
                    Vous recevrez une notification sur votre téléphone pour confirmer.
                </p>

                <form action="${pageContext.request.contextPath}/paiement" method="POST" id="formPaiement" novalidate>
                    <input type="hidden" name="csrf_token" value="<%= tokenCSRF %>">

                    <div class="form-group">
                        <label for="telephone">
                            <i class="fas fa-phone"></i>
                            <span>Numéro Mobile Money</span>
                        </label>
                        <div class="input-wrapper">
                            <span class="input-prefix">CD</span>
                            <input type="tel" id="telephone" name="telephone"
                                   placeholder="+243XXXXXXXXX ou 0XXXXXXXXX"
                                   pattern="^(\+243|0)[0-9]{9}$" maxlength="14"
                                   required autocomplete="tel" autofocus>
                        </div>
                        <span class="form-hint">
                            <i class="fas fa-info-circle"></i>
                            Formats acceptés : +243XXXXXXXXX ou 0XXXXXXXXX (Airtel, Orange, Vodacom)
                        </span>
                        <span class="form-error" id="telephoneError"></span>
                    </div>

                    <div class="form-group">
                        <label for="operateur">
                            <i class="fas fa-wallet"></i>
                            <span>Opérateur Mobile Money</span>
                        </label>
                        <select id="operateur" name="operateur" class="filtre-select" required>
                            <option value="">-- Choisissez votre opérateur --</option>
                            <option value="AIRTEL_COD">Airtel Money</option>
                            <option value="ORANGE_COD">Orange Money</option>
                            <option value="VODACOM_MPESA_COD">M-Pesa (Vodacom)</option>
                        </select>
                    </div>

                    <button type="submit" class="btn-paiement" id="btnPayer">
                        <i class="fas fa-lock"></i>
                        <span>Payer <%= String.format("%.0f", montantUSD) %> $ maintenant</span>
                        <span class="btn-loader" id="btnLoader" style="display:none;">
                            <i class="fas fa-spinner fa-spin"></i>
                        </span>
                    </button>
                </form>

                <div class="paiement-securite">
                    <i class="fas fa-shield-alt"></i>
                    <span>Paiement sécurisé par <strong>PawaPay</strong></span>
                </div>
            </div>
        <% } else { %>
            <%-- Message PawaPay indisponible --%>
            <div class="paiement-form-section" style="opacity: 0.5; pointer-events: none; filter: grayscale(0.8);">
                <div class="paiement-indisponible-banner" style="background: #f5f5fa; border: 2px dashed #ccc; border-radius: 12px; padding: 16px 20px; margin-bottom: 16px; text-align: center;">
                    <i class="fas fa-tools" style="font-size: 24px; color: #f39c12;"></i>
                    <p style="margin: 8px 0 0; font-size: 13px; color: #8b8fa3;">
                        Paiement automatique momentanément indisponible.<br>
                        Veuillez utiliser le <strong>paiement manuel</strong> ci-dessous.
                    </p>
                </div>
            </div>
        <% } %>

        <%-- ============================================================
             SECTION PAIEMENT MANUEL (TOUJOURS ACTIVE)
             ============================================================ --%>
        <div class="paiement-manuel-section" style="background: #fafbfe; border: 2px solid #667eea; border-radius: 16px; padding: 28px 24px; margin-top: 20px;">
            <h3 style="font-size: 17px; font-weight: 700; color: #1a1a2e; margin: 0 0 6px 0; display: flex; align-items: center; gap: 8px;">
                <i class="fas fa-hand-holding-usd" style="color: #667eea;"></i> Paiement Manuel
            </h3>
            <p style="font-size: 13px; color: #555; margin: 0 0 20px 0; line-height: 1.6;">
                Envoyez <strong style="color: #1a1a2e;">4$ (10 000 FC)</strong> sur le numéro ci-dessous, 
                puis remplissez le formulaire avec l'ID de transaction reçu par SMS.
                Votre compte sera activé <strong>sous 24 heures</strong> après vérification.
            </p>

            <%-- Numéro de paiement --%>
            <div style="background: linear-gradient(135deg, #667eea, #764ba2); color: white; border-radius: 14px; padding: 20px 24px; text-align: center; margin-bottom: 22px;">
                <i class="fas fa-mobile-alt" style="font-size: 28px; margin-bottom: 8px; display: block;"></i>
                <span style="font-size: 12px; opacity: 0.9;">Envoyez à ce numéro :</span>
                <strong style="display: block; font-size: 22px; margin: 4px 0;">+243 984 270 438</strong>
                <span style="font-size: 11px; opacity: 0.8;">Airtel Money / Orange Money</span>
            </div>

            <%-- Étapes à suivre --%>
            <div style="background: #fff; border-radius: 12px; padding: 16px 18px; margin-bottom: 22px; border: 1px solid #e8eaef;">
                <h4 style="font-size: 14px; font-weight: 600; color: #1a1a2e; margin: 0 0 12px 0;">
                    <i class="fas fa-list-ol" style="color: #667eea;"></i> Étapes à suivre :
                </h4>
                <ol style="padding-left: 20px; margin: 0; display: flex; flex-direction: column; gap: 8px;">
                    <li style="font-size: 13px; color: #555;">Envoyez <strong>10 000 FC</strong> au numéro ci-dessus via Airtel Money ou Orange Money</li>
                    <li style="font-size: 13px; color: #555;">Notez l'<strong>ID de transaction</strong> reçu par SMS (ex: TID: MP240603.1234.A12345)</li>
                    <li style="font-size: 13px; color: #555;">Remplissez le formulaire ci-dessous avec cet ID</li>
                    <li style="font-size: 13px; color: #555;">Votre compte sera activé après vérification (max 24h)</li>
                </ol>
            </div>

            <%-- Formulaire manuel --%>
            <form action="${pageContext.request.contextPath}/paiement-manuel" method="POST" id="formPaiementManuel">
                <div class="form-group">
                    <label for="idTransaction" style="display: flex; align-items: center; gap: 6px; font-size: 13px; font-weight: 600; color: #1a1a2e; margin-bottom: 6px;">
                        <i class="fas fa-receipt" style="color: #667eea;"></i>
                        ID de la transaction
                    </label>
                    <input type="text" id="idTransaction" name="idTransaction"
                           placeholder="Ex: MP240603.1234.A12345 (le numéro après TID: dans le SMS)"
                           style="width: 100%; padding: 14px 16px; border: 2px solid #e8eaef; border-radius: 10px; font-size: 14px; font-family: 'Poppins', sans-serif; box-sizing: border-box;"
                           required>
                </div>

                <div class="form-group">
                    <label for="operateurManuel" style="display: flex; align-items: center; gap: 6px; font-size: 13px; font-weight: 600; color: #1a1a2e; margin-bottom: 6px;">
                        <i class="fas fa-wallet" style="color: #667eea;"></i>
                        Opérateur utilisé
                    </label>
                    <select id="operateurManuel" name="operateur"
                            style="width: 100%; padding: 14px 16px; border: 2px solid #e8eaef; border-radius: 10px; font-size: 14px; font-family: 'Poppins', sans-serif; box-sizing: border-box; background: white;"
                            required>
                        <option value="">-- Choisissez votre opérateur --</option>
                        <option value="AIRTEL">Airtel Money</option>
                        <option value="ORANGE">Orange Money</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="telephoneManuel" style="display: flex; align-items: center; gap: 6px; font-size: 13px; font-weight: 600; color: #1a1a2e; margin-bottom: 6px;">
                        <i class="fas fa-phone" style="color: #667eea;"></i>
                        Votre numéro de téléphone
                    </label>
                    <input type="tel" id="telephoneManuel" name="telephone"
                           placeholder="Ex: 243984270438"
                           style="width: 100%; padding: 14px 16px; border: 2px solid #e8eaef; border-radius: 10px; font-size: 14px; font-family: 'Poppins', sans-serif; box-sizing: border-box;"
                           required>
                    <span style="display: flex; align-items: center; gap: 5px; font-size: 11px; color: #8b8fa3; margin-top: 6px;">
                        <i class="fas fa-info-circle"></i>
                        Le numéro qui a envoyé l'argent
                    </span>
                </div>

                <button type="submit" style="width: 100%; padding: 16px 24px; background: linear-gradient(135deg, #667eea, #764ba2); color: white; border: none; border-radius: 12px; font-size: 15px; font-weight: 700; font-family: 'Poppins', sans-serif; cursor: pointer; transition: all 0.3s ease; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.25); display: flex; align-items: center; justify-content: center; gap: 10px;">
                    <i class="fas fa-check-circle"></i>
                    <span>J'ai payé - Activer mon compte</span>
                </button>
            </form>

            <p style="text-align: center; font-size: 12px; color: #8b8fa3; margin-top: 14px;">
                <i class="fas fa-clock"></i> Délai d'activation : 1 à 24 heures après vérification.
            </p>
        </div>

        <%-- ===== GARANTIES ===== --%>
        <div class="paiement-garanties" style="margin-top: 24px;">
            <div class="garantie-item">
                <i class="fas fa-shield-alt"></i>
                <span>Paiement 100% sécurisé</span>
            </div>
            <div class="garantie-item">
                <i class="fas fa-redo"></i>
                <span>Satisfait ou remboursé</span>
            </div>
            <div class="garantie-item">
                <i class="fas fa-infinity"></i>
                <span>Accès à vie</span>
            </div>
        </div>

    </div>

    <%-- ===== SECTION FAQ ===== --%>
    <div class="paiement-faq">
        <h3><i class="fas fa-question-circle"></i> Questions fréquentes</h3>

        <div class="faq-item">
            <h4>Combien de temps dure l'accès premium ?</h4>
            <p>L'accès est <strong>à vie</strong>. Un seul paiement, aucun abonnement mensuel.</p>
        </div>
        <div class="faq-item">
            <h4>Comment trouver l'ID de transaction ?</h4>
            <p>Après avoir envoyé l'argent, vous recevez un SMS contenant l'ID (ex: TID: MP240603.1234.A12345). Copiez ce numéro dans le formulaire.</p>
        </div>
        <div class="faq-item">
            <h4>Combien de temps pour l'activation ?</h4>
            <p>Votre compte est activé <strong>sous 1 à 24 heures</strong> après vérification du paiement.</p>
        </div>
        <div class="faq-item">
            <h4>Que faire si mon compte n'est pas activé après 24h ?</h4>
            <p>Contactez-nous à <strong>barakantwali06@gmail.com</strong> ou au <strong>+243 984 270 438</strong> avec votre ID de transaction.</p>
        </div>
    </div>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="paiement-back">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="btn-retour">
            <i class="fas fa-arrow-left"></i> Retour au dashboard
        </a>
    </div>

</div>

<%
    String paiementJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/paiement.js\"></script>";
    request.setAttribute("extraScripts", paiementJS);
%>

<%@ include file="../footer-connecte.jsp" %>