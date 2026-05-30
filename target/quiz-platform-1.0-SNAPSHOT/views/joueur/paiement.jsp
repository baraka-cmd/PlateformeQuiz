<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Utilisateur" %>

<%-- ============================================================
     Fichier : paiement.jsp
     Rôle    : Page de paiement pour débloquer les niveaux premium
              - Affiche le montant (4$ / 10 000 FC)
              - Liste les avantages premium
              - Formulaire de numéro de téléphone Mobile Money
              - Protection CSRF
     ============================================================ --%>

<%
    // Vérification authentification
    Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
    if (utilisateur == null) {
        response.sendRedirect(request.getContextPath() + "/connexion");
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

    request.setAttribute("pageTitle", "Débloquer tous les niveaux - AmijayQuiz");
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

    <%-- ===== CARTE PRINCIPALE ===== --%>
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
                    <strong>Tous les cours Moyen & Avancé</strong>
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

        <%-- ===== FORMULAIRE DE PAIEMENT ===== --%>
        <div class="paiement-form-section">
            <h3><i class="fas fa-mobile-alt"></i> Paiement par Mobile Money</h3>
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
                        <span class="input-prefix">🇨🇩</span>
                        <input
                            type="tel"
                            id="telephone"
                            name="telephone"
                            placeholder="+243XXXXXXXXX ou 0XXXXXXXXX"
                            pattern="^(\+243|0)[0-9]{9}$"
                            maxlength="14"
                            required
                            autocomplete="tel"
                            autofocus
                        >
                    </div>
                    <span class="form-hint">
                        <i class="fas fa-info-circle"></i>
                        Formats acceptés : +243XXXXXXXXX ou 0XXXXXXXXX (Airtel, Orange, Vodacom)
                    </span>
                    <span class="form-error" id="telephoneError"></span>
                </div>

                <div class="paiement-operateurs">
                    <span class="operateur-badge">
                        <i class="fas fa-wifi"></i> Airtel Money
                    </span>
                    <span class="operateur-badge">
                        <i class="fas fa-wifi"></i> Orange Money
                    </span>
                    <span class="operateur-badge">
                        <i class="fas fa-wifi"></i> M-Pesa
                    </span>
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

        <%-- ===== GARANTIES ===== --%>
        <div class="paiement-garanties">
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
            <h4>Quels modes de paiement sont acceptés ?</h4>
            <p>Airtel Money, Orange Money et M-Pesa via PawaPay.</p>
        </div>
        <div class="faq-item">
            <h4>Le paiement est-il sécurisé ?</h4>
            <p>Oui, le paiement est traité par <strong>PawaPay</strong>, une plateforme certifiée PCI-DSS.</p>
        </div>
    </div>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="paiement-back">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="btn-retour">
            <i class="fas fa-arrow-left"></i> Retour au dashboard
        </a>
    </div>

</div>

<script>
document.getElementById('formPaiement').addEventListener('submit', function(e) {
    var telephone = document.getElementById('telephone').value.trim();
    var regex = /^(\+243|0)[0-9]{9}$/;

    if (!regex.test(telephone)) {
        e.preventDefault();
        document.getElementById('telephoneError').innerText = 'Format invalide. Exemple : +243984270438';
        document.getElementById('telephoneError').style.display = 'block';
        return false;
    }

    document.getElementById('btnPayer').querySelector('span').innerText = 'Paiement en cours...';
    document.getElementById('btnLoader').style.display = 'inline-block';
    document.getElementById('btnPayer').disabled = true;
    return true;
});
</script>

<%
    String paiementJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/paiement.js\"></script>";
    request.setAttribute("extraScripts", paiementJS);
%>

<%@ include file="../footer.jsp" %>