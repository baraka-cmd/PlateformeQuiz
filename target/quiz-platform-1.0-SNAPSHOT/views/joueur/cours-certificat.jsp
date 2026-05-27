<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.Certificat" %>
<%@ page import="com.amijayquiz.models.Utilisateur" %>

<%-- ============================================================
     Fichier : cours-certificat.jsp
     Rôle    : Affichage du certificat de réussite
              Format diplôme officiel avec QR code
              Boutons : Imprimer, Télécharger, Partager
     ============================================================ --%>

<%
    Certificat certificat = (Certificat) request.getAttribute("certificat");
    Utilisateur utilisateur = (Utilisateur) request.getAttribute("utilisateur");
    Cours cours = (Cours) request.getAttribute("cours");
    Boolean estVerification = (Boolean) request.getAttribute("estVerification");

    if (certificat == null || utilisateur == null || cours == null) {
        response.sendRedirect(request.getContextPath() + "/joueur/cours");
        return;
    }

    if (estVerification == null) estVerification = false;

    String themeEncode = java.net.URLEncoder.encode(cours.getTheme(), "UTF-8");
    String niveauEncode = java.net.URLEncoder.encode(cours.getNiveau(), "UTF-8");
%>



<%-- ===== CHARGEMENT DES STYLES ===== --%>
<%
    String certificatCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/cours-certificat.css\">";
    request.setAttribute("extraHead", certificatCSS);
%>

<%@ include file="../header.jsp" %>

<div class="certificat-page-container">

    <%-- ============================================================
         BREADCRUMB
         ============================================================ --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours" class="breadcrumb-link">
            <i class="fas fa-graduation-cap"></i><span>Cours</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours/theme?theme=<%= themeEncode %>" class="breadcrumb-link">
            <span><%= cours.getTheme() %></span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours/niveau?theme=<%= themeEncode %>&niveau=<%= niveauEncode %>" class="breadcrumb-link">
            <span><%= cours.getNiveauFormate() %></span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" class="breadcrumb-link">
            <i class="fas fa-sitemap"></i><span>Plan</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-certificate"></i><span>Certificat</span>
        </span>
    </nav>

    <%-- ============================================================
         BARRE D'ACTIONS
         ============================================================ --%>
    <div class="certificat-actions-bar no-print">
        <button type="button" class="btn-action-cert" onclick="imprimerCertificat()" title="Imprimer le certificat">
            <i class="fas fa-print"></i>
            <span>Imprimer</span>
        </button>
        
        <a href="${pageContext.request.contextPath}/joueur/cours/certificat?coursId=<%= cours.getId() %>&download=pdf" 
           class="btn-action-cert" title="Télécharger en PDF">
            <i class="fas fa-download"></i>
            <span>Télécharger PDF</span>
        </a>
        
        <button type="button" class="btn-action-cert" onclick="partagerCertificat()" title="Partager le certificat">
            <i class="fas fa-share-alt"></i>
            <span>Partager</span>
        </button>

        <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" class="btn-action-cert btn-retour">
            <i class="fas fa-arrow-left"></i>
            <span>Plan du cours</span>
        </a>
    </div>

    <%-- ============================================================
         CERTIFICAT FORMAT DIPLÔME
         ============================================================ --%>
    <div class="certificat-diplome" id="certificatDiplome">
        
        <%-- Bordures décoratives --%>
        <div class="diplome-border"></div>
        
        <%-- Contenu du diplôme --%>
        <div class="diplome-content">
            
            <%-- En-tête --%>
            <div class="diplome-header">
                <div class="diplome-logo">
                    <i class="fas fa-brain"></i>
                    <span>AmijayQuiz</span>
                </div>
                <h2 class="diplome-titre-certificat">CERTIFICAT DE RÉUSSITE</h2>
                <p class="diplome-sous-titre">Ce certificat est décerné à</p>
            </div>

            <%-- Nom du récipiendaire --%>
            <h1 class="diplome-nom"><%= utilisateur.getPseudo() %></h1>

            <%-- Description --%>
            <div class="diplome-description">
                <p>pour avoir suivi avec succès le cours</p>
                <h3 class="diplome-cours"><%= cours.getTitre() %></h3>
                <p class="diplome-theme">
                    <i class="<%= cours.getIconeTheme() %>"></i>
                    <%= cours.getTheme() %> • Niveau <%= cours.getNiveauFormate() %>
                </p>
            </div>

            <%-- Score et Mention --%>
            <div class="diplome-score">
                <div class="diplome-score-item">
                    <span class="diplome-score-label">Score obtenu</span>
                    <span class="diplome-score-valeur"><%= certificat.getPourcentage() %></span>
                </div>
                <div class="diplome-score-separator"></div>
                <div class="diplome-score-item">
                    <span class="diplome-score-label">Mention</span>
                    <span class="diplome-score-valeur mention" style="color: <%= certificat.getCouleurMention() %>;">
                        <i class="<%= certificat.getIconeMention() %>"></i>
                        <%= certificat.getMention() %>
                    </span>
                </div>
            </div>

            <%-- Date et signature --%>
            <div class="diplome-footer">
                <div class="diplome-date">
                    <p>Délivré le <%= certificat.getDateFormatee() %></p>
                </div>
                <div class="diplome-signature">
                    <div class="signature-ligne"></div>
                    <p>L'équipe AmijayQuiz</p>
                </div>
            </div>

            <%-- Code de vérification --%>
            <div class="diplome-verification">
                <div class="verification-code">
                    <span class="verification-label">Code de vérification</span>
                    <span class="verification-valeur"><%= certificat.getCodeVerification() %></span>
                </div>
                <p class="verification-url">
                    Vérifiable sur : <%= request.getServerName() %>/verifier-certificat?code=<%= certificat.getCodeVerification() %>
                </p>
            </div>

        </div>

        <%-- Bordure décorative bas --%>
        <div class="diplome-border-bottom"></div>

    </div>

    <%-- ============================================================
         BADGE VÉRIFIÉ
         ============================================================ --%>
    <div class="certificat-verifie no-print">
        <div class="verifie-badge">
            <i class="fas fa-shield-check"></i>
            <span>Certificat officiel vérifié • Délivré par AmijayQuiz</span>
        </div>
    </div>

    <%-- ============================================================
         SECTION NAVIGATION
         ============================================================ --%>
    <div class="certificat-navigation no-print">
        <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" class="btn-nav-cert">
            <i class="fas fa-sitemap"></i>
            <span>Plan du cours</span>
        </a>
        <a href="${pageContext.request.contextPath}/joueur/cours/mes-certificats" class="btn-nav-cert">
            <i class="fas fa-award"></i>
            <span>Tous mes certificats</span>
        </a>
    </div>

</div>

<%-- ===== CHARGEMENT DES SCRIPTS ===== --%>
<%
    String certificatJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/cours-certificat.js\"></script>";
    request.setAttribute("extraScripts", certificatJS);
%>

<%@ include file="../footer.jsp" %>