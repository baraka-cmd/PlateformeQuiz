<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Utilisateur" %>

<%-- ============================================================
     Fichier : paiement-attente.jsp
     Rôle    : Page d'attente après initiation du paiement
              - Affiche un spinner animé
              - Rappelle la référence et le montant
              - Propose de vérifier le statut manuellement
              - Redirection automatique après paiement
     ============================================================ --%>

<%
    // Vérification authentification
    Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
    if (utilisateur == null) {
        response.sendRedirect(request.getContextPath() + "/connexion");
        return;
    }

    // Récupération des infos de la session
    String referencePaiement = (String) session.getAttribute("referencePaiement");
    String telephonePaiement = (String) session.getAttribute("telephonePaiement");
    Double montantPaiement = (Double) session.getAttribute("montantPaiement");

    // Si pas de référence, rediriger vers la page de paiement
    if (referencePaiement == null || referencePaiement.isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/paiement");
        return;
    }

    if (montantPaiement == null) montantPaiement = 4.00;

    request.setAttribute("pageTitle", "Paiement en cours - AmijayQuiz");
%>

<%
    String attenteCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/paiement.css\">";
    request.setAttribute("extraHead", attenteCSS);
%>

<%@ include file="../header.jsp" %>

<div class="paiement-container">

    <%-- ===== BREADCRUMB ===== --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/paiement" class="breadcrumb-link">
            <i class="fas fa-crown"></i><span>Premium</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-clock"></i><span>En attente</span>
        </span>
    </nav>

    <%-- ===== CARTE PRINCIPALE ===== --%>
    <div class="attente-card">

        <%-- ===== SPINNER ANIMÉ ===== --%>
        <div class="attente-spinner">
            <div class="spinner-ring"></div>
            <div class="spinner-icon">
                <i class="fas fa-mobile-alt"></i>
            </div>
        </div>

        <%-- ===== TITRE ===== --%>
        <h1>Paiement en cours</h1>
        <p class="attente-sous-titre">
            Veuillez vérifier votre téléphone pour <strong>confirmer le paiement</strong>.
        </p>

        <%-- ===== ALERTE INFO ===== --%>
        <div class="attente-alerte">
            <div class="alerte-icon">
                <i class="fas fa-info-circle"></i>
            </div>
            <div class="alerte-content">
                <p>
                    Une demande de paiement a été envoyée au numéro
                    <strong><%= telephonePaiement != null ? telephonePaiement : "Non renseigné" %></strong>.
                </p>
                <p>
                    <i class="fas fa-mobile-alt"></i>
                    Composez votre code secret Mobile Money pour valider la transaction.
                </p>
            </div>
        </div>

        <%-- ===== DÉTAILS DE LA TRANSACTION ===== --%>
        <div class="attente-details">
            <div class="detail-ligne">
                <span class="detail-label">Référence</span>
                <span class="detail-valeur"><%= referencePaiement %></span>
            </div>
            <div class="detail-ligne">
                <span class="detail-label">Montant</span>
                <span class="detail-valeur"><%= String.format("%.2f", montantPaiement) %> USD</span>
            </div>
            <div class="detail-ligne">
                <span class="detail-label">Statut</span>
                <span class="detail-valeur statut-pending">
                    <i class="fas fa-spinner fa-spin"></i> En attente
                </span>
            </div>
        </div>

        <%-- ===== ÉTAPES ===== --%>
        <div class="attente-etapes">
            <div class="etape-item etape-active">
                <div class="etape-numero">1</div>
                <div class="etape-texte">
                    <strong>Demande envoyée</strong>
                    <span>Paiement initié avec succès</span>
                </div>
            </div>
            <div class="etape-item">
                <div class="etape-numero">2</div>
                <div class="etape-texte">
                    <strong>Confirmation Mobile</strong>
                    <span>Validez sur votre téléphone</span>
                </div>
            </div>
            <div class="etape-item">
                <div class="etape-numero">3</div>
                <div class="etape-texte">
                    <strong>Premium activé</strong>
                    <span>Accès immédiat à tous les niveaux</span>
                </div>
            </div>
        </div>

        <%-- ===== BOUTONS D'ACTION ===== --%>
        <div class="attente-actions">
            <button type="button" class="btn-verifier" id="btnVerifier" onclick="verifierStatutPaiement()">
                <i class="fas fa-sync-alt"></i>
                <span>Vérifier le statut</span>
            </button>

            <a href="${pageContext.request.contextPath}/paiement" class="btn-annuler-paiement">
                <i class="fas fa-times"></i>
                <span>Annuler et réessayer</span>
            </a>
        </div>

        <%-- ===== MESSAGE D'ATTENTE ===== --%>
        <div class="attente-patience">
            <i class="fas fa-clock"></i>
            <span>Cette page se mettra à jour automatiquement une fois le paiement confirmé.</span>
        </div>

    </div>

    <%-- ===== CARTE AIDE ===== --%>
    <div class="attente-aide">
        <h3><i class="fas fa-question-circle"></i> Besoin d'aide ?</h3>
        <div class="aide-items">
            <div class="aide-item">
                <i class="fas fa-clock"></i>
                <span>Le paiement prend généralement <strong>30 secondes à 2 minutes</strong>.</span>
            </div>
            <div class="aide-item">
                <i class="fas fa-mobile-alt"></i>
                <span>Assurez-vous que votre téléphone est allumé et a du réseau.</span>
            </div>
            <div class="aide-item">
                <i class="fas fa-undo"></i>
                <span>Si le paiement échoue, vous pouvez réessayer gratuitement.</span>
            </div>
            <div class="aide-item">
                <i class="fas fa-envelope"></i>
                <span>Contactez <strong>barakantwali06@gmail.com</strong> en cas de problème.</span>
            </div>
        </div>
    </div>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="paiement-back">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="btn-retour">
            <i class="fas fa-arrow-left"></i> Retour au dashboard
        </a>
    </div>

</div>

<%-- ===== SCRIPT DE VÉRIFICATION AUTOMATIQUE ===== --%>
<script>
    var REFERENCE = '<%= referencePaiement %>';
    var CONTEXT_PATH = '${pageContext.request.contextPath}';
    var VERIFICATION_INTERVAL = 5000; // 5 secondes
    var MAX_VERIFICATIONS = 24; // 2 minutes max
    var compteurVerifications = 0;

    // Vérifier périodiquement le statut du paiement
    var intervalId = setInterval(function() {
        compteurVerifications++;

        if (compteurVerifications > MAX_VERIFICATIONS) {
            clearInterval(intervalId);
            document.querySelector('.attente-patience span').innerText =
                'La vérification automatique est terminée. Cliquez sur "Vérifier le statut" ou contactez le support.';
            return;
        }

        fetch(CONTEXT_PATH + '/api/paiements/statut?reference=' + REFERENCE)
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.statut === 'SUCCESS') {
                    clearInterval(intervalId);
                    window.location.href = CONTEXT_PATH + '/paiement-succes';
                } else if (data.statut === 'FAILED' || data.statut === 'CANCELLED' || data.statut === 'EXPIRED') {
                    clearInterval(intervalId);
                    window.location.href = CONTEXT_PATH + '/paiement-echoue?reference=' + REFERENCE;
                }
            })
            .catch(function(error) {
                console.log('Vérification en cours...');
            });
    }, VERIFICATION_INTERVAL);

    // Vérification manuelle
    function verifierStatutPaiement() {
        var btn = document.getElementById('btnVerifier');
        btn.disabled = true;
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i><span>Vérification...</span>';

        fetch(CONTEXT_PATH + '/api/paiements/statut?reference=' + REFERENCE)
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.statut === 'SUCCESS') {
                    window.location.href = CONTEXT_PATH + '/paiement-succes';
                } else if (data.statut === 'FAILED' || data.statut === 'CANCELLED' || data.statut === 'EXPIRED') {
                    window.location.href = CONTEXT_PATH + '/paiement-echoue?reference=' + REFERENCE;
                } else {
                    alert('Le paiement est toujours en attente. Vérifiez votre téléphone et réessayez dans quelques instants.');
                    btn.disabled = false;
                    btn.innerHTML = '<i class="fas fa-sync-alt"></i><span>Vérifier le statut</span>';
                }
            })
            .catch(function(error) {
                alert('Erreur de connexion. Veuillez réessayer.');
                btn.disabled = false;
                btn.innerHTML = '<i class="fas fa-sync-alt"></i><span>Vérifier le statut</span>';
            });
    }
</script>

<%
    String attenteJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/paiement-attente.js\"></script>";
    request.setAttribute("extraScripts", attenteJS);
%>

<%@ include file="../footer-connecte.jsp" %>