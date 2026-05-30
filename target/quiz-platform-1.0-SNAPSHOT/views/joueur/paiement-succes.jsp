<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Utilisateur" %>

<%-- ============================================================
     Fichier : paiement-succes.jsp
     Rôle    : Page de confirmation après paiement réussi
              - Affiche une animation de succès
              - Confirme l'activation du premium
              - Rappelle les avantages débloqués
              - Boutons vers les cours et quiz
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
    Double montantPaiement = (Double) session.getAttribute("montantPaiement");

    // Nettoyer les attributs de session liés au paiement
    session.removeAttribute("referencePaiement");
    session.removeAttribute("telephonePaiement");
    session.removeAttribute("montantPaiement");
    session.removeAttribute("csrf_token_paiement");
    session.removeAttribute("urlRedirectionApresPaiement");

    // Rafraîchir le cache premium
    session.removeAttribute("_cachePremium");
    session.removeAttribute("_cachePremiumTimestamp");

    if (montantPaiement == null) montantPaiement = 4.00;

    request.setAttribute("pageTitle", "Paiement réussi - AmijayQuiz");
%>

<%
    String succesCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/paiement.css\">";
    request.setAttribute("extraHead", succesCSS);
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
            <i class="fas fa-check-circle"></i><span>Succès</span>
        </span>
    </nav>

    <%-- ===== CARTE PRINCIPALE ===== --%>
    <div class="succes-card">

        <%-- ===== ANIMATION DE SUCCÈS ===== --%>
        <div class="succes-animation">
            <div class="succes-cercle">
                <i class="fas fa-check"></i>
            </div>
            <div class="succes-confettis">
                <span class="confetti c1">🎉</span>
                <span class="confetti c2">✨</span>
                <span class="confetti c3">🎊</span>
                <span class="confetti c4">⭐</span>
                <span class="confetti c5">🌟</span>
                <span class="confetti c6">💫</span>
            </div>
        </div>

        <%-- ===== MESSAGE DE SUCCÈS ===== --%>
        <h1>Paiement réussi !</h1>
        <p class="succes-sous-titre">
            Félicitations <strong><%= utilisateur.getPseudo() %></strong> ! Votre compte est maintenant <strong>Premium</strong>.
        </p>

        <%-- ===== BADGE PREMIUM ACTIVÉ ===== --%>
        <div class="succes-badge-premium">
            <i class="fas fa-crown"></i>
            <span>Premium Activé</span>
        </div>

        <%-- ===== DÉTAILS DE LA TRANSACTION ===== --%>
        <div class="succes-details">
            <div class="detail-ligne">
                <span class="detail-label"><i class="fas fa-hashtag"></i> Référence</span>
                <span class="detail-valeur"><%= referencePaiement != null ? referencePaiement : "N/A" %></span>
            </div>
            <div class="detail-ligne">
                <span class="detail-label"><i class="fas fa-money-bill"></i> Montant payé</span>
                <span class="detail-valeur"><%= String.format("%.2f", montantPaiement) %> USD</span>
            </div>
            <div class="detail-ligne">
                <span class="detail-label"><i class="fas fa-infinity"></i> Durée</span>
                <span class="detail-valeur">Accès à vie</span>
            </div>
        </div>

        <%-- ===== CE QUI EST DÉBLOQUÉ ===== --%>
        <div class="succes-deblocages">
            <h3><i class="fas fa-unlock-alt"></i> Ce qui est maintenant débloqué</h3>

            <div class="deblocage-grid">
                <div class="deblocage-item">
                    <div class="deblocage-icon" style="background: linear-gradient(135deg, #27ae60, #2ecc71);">
                        <i class="fas fa-book-open"></i>
                    </div>
                    <div class="deblocage-texte">
                        <strong>Cours Moyen & Avancé</strong>
                        <span>Tous les cours de tous les thèmes</span>
                    </div>
                    <i class="fas fa-check-circle deblocage-check"></i>
                </div>

                <div class="deblocage-item">
                    <div class="deblocage-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                        <i class="fas fa-layer-group"></i>
                    </div>
                    <div class="deblocage-texte">
                        <strong>Quiz débloqués</strong>
                        <span>Toutes les parties de tous les niveaux</span>
                    </div>
                    <i class="fas fa-check-circle deblocage-check"></i>
                </div>

                <div class="deblocage-item">
                    <div class="deblocage-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                        <i class="fas fa-certificate"></i>
                    </div>
                    <div class="deblocage-texte">
                        <strong>Certificats</strong>
                        <span>Examens finaux et certifications</span>
                    </div>
                    <i class="fas fa-check-circle deblocage-check"></i>
                </div>

                <div class="deblocage-item">
                    <div class="deblocage-icon" style="background: linear-gradient(135deg, #e74c3c, #c0392b);">
                        <i class="fas fa-trophy"></i>
                    </div>
                    <div class="deblocage-texte">
                        <strong>Tous les niveaux</strong>
                        <span>Élémentaire + Moyen + Avancé</span>
                    </div>
                    <i class="fas fa-check-circle deblocage-check"></i>
                </div>
            </div>
        </div>

        <%-- ===== BOUTONS D'ACTION ===== --%>
        <div class="succes-actions">
            <a href="${pageContext.request.contextPath}/joueur/cours" class="btn-succes btn-succes-primary">
                <i class="fas fa-graduation-cap"></i>
                <span>Explorer les cours</span>
            </a>

            <a href="${pageContext.request.contextPath}/joueur/dashboard" class="btn-succes btn-succes-secondary">
                <i class="fas fa-layer-group"></i>
                <span>Voir les quiz</span>
            </a>
        </div>

        <%-- ===== REDIRECTION AUTO ===== --%>
        <div class="succes-redirection">
            <i class="fas fa-clock"></i>
            <span>Redirection automatique vers le dashboard dans <strong id="compteRebours">10</strong> secondes...</span>
        </div>

    </div>

    <%-- ===== MESSAGE DE BIENVENUE PREMIUM ===== --%>
    <div class="succes-message">
        <div class="message-icon">
            <i class="fas fa-heart"></i>
        </div>
        <div class="message-content">
            <h3>Merci pour votre confiance !</h3>
            <p>
                Grâce à vous, AmijayQuiz continue de s'améliorer.
                N'hésitez pas à nous contacter si vous avez des suggestions.
            </p>
        </div>
    </div>

</div>

<%-- ===== SCRIPT REDIRECTION AUTO ===== --%>
<script>
    var secondes = 10;
    var compteReboursElem = document.getElementById('compteRebours');

    var interval = setInterval(function() {
        secondes--;
        if (compteReboursElem) {
            compteReboursElem.innerText = secondes;
        }
        if (secondes <= 0) {
            clearInterval(interval);
            window.location.href = '${pageContext.request.contextPath}/joueur/dashboard';
        }
    }, 1000);
</script>

<%
    String succesJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/paiement-succes.js\"></script>";
    request.setAttribute("extraScripts", succesJS);
%>

<%@ include file="../footer.jsp" %>