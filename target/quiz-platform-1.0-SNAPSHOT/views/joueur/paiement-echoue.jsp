<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Utilisateur" %>

<%-- ============================================================
     Fichier : paiement-echoue.jsp
     Rôle    : Page affichée en cas d'échec de paiement
              - Explique la raison de l'échec
              - Propose de réessayer
              - Affiche la référence pour le support
              - Alternatives de paiement
     ============================================================ --%>

<%
    // Vérification authentification
    Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
    if (utilisateur == null) {
        response.sendRedirect(request.getContextPath() + "/connexion");
        return;
    }

    // Récupération de la référence (passée en paramètre ou en session)
    String referencePaiement = request.getParameter("reference");
    if (referencePaiement == null || referencePaiement.isEmpty()) {
        referencePaiement = (String) session.getAttribute("referencePaiement");
    }

    // Récupération du message d'erreur de la session
    String messageErreur = (String) session.getAttribute("messageErreurPaiement");
    if (messageErreur == null) {
        messageErreur = "Le paiement n'a pas pu aboutir. Cela peut être dû à un solde insuffisant, une annulation ou un problème réseau.";
    }

    // Nettoyer les attributs de session liés au paiement
    session.removeAttribute("referencePaiement");
    session.removeAttribute("telephonePaiement");
    session.removeAttribute("montantPaiement");
    session.removeAttribute("csrf_token_paiement");
    session.removeAttribute("messageErreurPaiement");

    request.setAttribute("pageTitle", "Paiement échoué - AmijayQuiz");
%>

<%
    String echecCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/paiement.css\">";
    request.setAttribute("extraHead", echecCSS);
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
            <i class="fas fa-times-circle"></i><span>Échec</span>
        </span>
    </nav>

    <%-- ===== CARTE PRINCIPALE ===== --%>
    <div class="echec-card">

        <%-- ===== ANIMATION D'ÉCHEC ===== --%>
        <div class="echec-animation">
            <div class="echec-cercle">
                <i class="fas fa-times"></i>
            </div>
        </div>

        <%-- ===== MESSAGE D'ÉCHEC ===== --%>
        <h1>Paiement échoué</h1>
        <p class="echec-sous-titre">
            Malheureusement, la transaction n'a pas pu aboutir.
        </p>

        <%-- ===== RAISON DE L'ÉCHEC ===== --%>
        <div class="echec-raison">
            <div class="raison-icon">
                <i class="fas fa-exclamation-triangle"></i>
            </div>
            <div class="raison-texte">
                <strong>Que s'est-il passé ?</strong>
                <p><%= messageErreur %></p>
            </div>
        </div>

        <%-- ===== DÉTAILS DE LA TRANSACTION ===== --%>
        <div class="echec-details">
            <div class="detail-ligne">
                <span class="detail-label"><i class="fas fa-hashtag"></i> Référence</span>
                <span class="detail-valeur"><%= referencePaiement != null ? referencePaiement : "N/A" %></span>
            </div>
            <div class="detail-ligne">
                <span class="detail-label"><i class="fas fa-clock"></i> Statut</span>
                <span class="detail-valeur statut-failed">
                    <i class="fas fa-times-circle"></i> Échoué
                </span>
            </div>
        </div>

        <%-- ===== CAUSES POSSIBLES ===== --%>
        <div class="echec-causes">
            <h3><i class="fas fa-search"></i> Causes possibles</h3>
            <ul>
                <li>
                    <i class="fas fa-wallet"></i>
                    <span><strong>Solde insuffisant</strong> — Vérifiez que votre compte Mobile Money a assez de fonds.</span>
                </li>
                <li>
                    <i class="fas fa-ban"></i>
                    <span><strong>Paiement annulé</strong> — Vous avez peut-être annulé la transaction sur votre téléphone.</span>
                </li>
                <li>
                    <i class="fas fa-clock"></i>
                    <span><strong>Délai expiré</strong> — La transaction a pris trop de temps et a expiré.</span>
                </li>
                <li>
                    <i class="fas fa-wifi"></i>
                    <span><strong>Problème réseau</strong> — La connexion a peut-être été interrompue.</span>
                </li>
            </ul>
        </div>

        <%-- ===== BOUTONS D'ACTION ===== --%>
        <div class="echec-actions">
            <a href="${pageContext.request.contextPath}/paiement" class="btn-echec btn-echec-primary">
                <i class="fas fa-redo"></i>
                <span>Réessayer le paiement</span>
            </a>

            <a href="${pageContext.request.contextPath}/joueur/dashboard" class="btn-echec btn-echec-secondary">
                <i class="fas fa-arrow-left"></i>
                <span>Retour au dashboard</span>
            </a>
        </div>

        <%-- ===== SECTION SUPPORT ===== --%>
        <div class="echec-support">
            <div class="support-icon">
                <i class="fas fa-headset"></i>
            </div>
            <div class="support-content">
                <h4>Besoin d'aide ?</h4>
                <p>
                    Si le problème persiste, contactez-nous avec la référence ci-dessus.
                    Nous vous répondrons dans les plus brefs délais.
                </p>
                <div class="support-contacts">
                    <a href="mailto:barakantwali06@gmail.com" class="support-contact-item">
                        <i class="fas fa-envelope"></i> barakantwali06@gmail.com
                    </a>
                    <a href="tel:+243984270438" class="support-contact-item">
                        <i class="fas fa-phone"></i> +243 984 270 438
                    </a>
                </div>
            </div>
        </div>

        <%-- ===== ALTERNATIVE ===== --%>
        <div class="echec-alternative">
            <i class="fas fa-lightbulb"></i>
            <span>
                <strong>Astuce :</strong> Essayez avec un autre opérateur (Airtel Money, Orange Money ou M-Pesa)
                ou vérifiez que votre compte n'a pas atteint sa limite quotidienne.
            </span>
        </div>

    </div>

    <%-- ===== RAPPEL DES AVANTAGES ===== --%>
    <div class="echec-rappel">
        <h3><i class="fas fa-gem"></i> Pourquoi passer Premium ?</h3>
        <div class="rappel-grid">
            <div class="rappel-item">
                <i class="fas fa-book-open"></i>
                <span>Tous les cours Moyen & Avancé</span>
            </div>
            <div class="rappel-item">
                <i class="fas fa-layer-group"></i>
                <span>360 parties de quiz</span>
            </div>
            <div class="rappel-item">
                <i class="fas fa-certificate"></i>
                <span>Certificats vérifiables</span>
            </div>
            <div class="rappel-item">
                <i class="fas fa-infinity"></i>
                <span>Accès à vie — 4$ seulement</span>
            </div>
        </div>
    </div>

</div>

<%
    String echecJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/paiement-echoue.js\"></script>";
    request.setAttribute("extraScripts", echecJS);
%>

<%@ include file="../footer-connecte.jsp" %>