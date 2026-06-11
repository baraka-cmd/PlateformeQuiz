<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.learnpath.models.Certificat" %>
<%@ page import="com.learnpath.models.Cours" %>
<%@ page import="com.learnpath.models.Utilisateur" %>

<%-- ============================================================
     Fichier : cours-mes-certificats.jsp
     Rôle    : Liste de tous les certificats de l'utilisateur
              Affiche les certificats obtenus avec stats
     ============================================================ --%>

<%
    List<Map<String, Object>> certificatsAvecCours = (List<Map<String, Object>>) request.getAttribute("certificats");
    Integer totalCertificats = (Integer) request.getAttribute("totalCertificats");
    Integer totalCoursDisponibles = (Integer) request.getAttribute("totalCoursDisponibles");
    Utilisateur utilisateur = (Utilisateur) request.getAttribute("utilisateur");

    if (totalCertificats == null) totalCertificats = 0;
    if (totalCoursDisponibles == null) totalCoursDisponibles = 0;
%>



<%-- ===== CHARGEMENT DES STYLES ===== --%>
<%
    String mesCertificatsCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/cours-mes-certificats.css\">";
    request.setAttribute("extraHead", mesCertificatsCSS);
%>

<%@ include file="../header.jsp" %>

<div class="mes-certificats-container">

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
        <span class="breadcrumb-current">
            <i class="fas fa-certificate"></i><span>Mes Certificats</span>
        </span>
    </nav>

    <%-- ============================================================
         BANNIÈRE STATISTIQUES
         ============================================================ --%>
    <div class="stats-banner">
        <div class="stats-banner-icon">
            <i class="fas fa-trophy"></i>
        </div>
        <div class="stats-banner-content">
            <h1>Mes Certificats</h1>
            <p>Collection de vos réalisations et compétences validées</p>
        </div>
        <div class="stats-banner-numbers">
            <div class="stat-mini">
                <i class="fas fa-certificate"></i>
                <strong><%= totalCertificats %></strong>
                <span>Obtenus</span>
            </div>
            <div class="stat-mini">
                <i class="fas fa-book"></i>
                <strong><%= totalCoursDisponibles %></strong>
                <span>Disponibles</span>
            </div>
            <div class="stat-mini">
                <i class="fas fa-chart-pie"></i>
                <strong><%= totalCoursDisponibles > 0 ? (totalCertificats * 100 / totalCoursDisponibles) : 0 %>%</strong>
                <span>Complétion</span>
            </div>
        </div>
    </div>

    <%-- ============================================================
         SECTION TITRE
         ============================================================ --%>
    <div class="section-header">
        <h2>
            <i class="fas fa-award"></i>
            <% if (certificatsAvecCours != null && !certificatsAvecCours.isEmpty()) { %>
                Vos <%= totalCertificats %> certificat<%= totalCertificats > 1 ? "s" : "" %>
            <% } else { %>
                Aucun certificat pour le moment
            <% } %>
        </h2>
        <a href="${pageContext.request.contextPath}/joueur/cours" class="btn-voir-cours">
            <i class="fas fa-graduation-cap"></i> Voir les cours
        </a>
    </div>

    <%-- ============================================================
         GRILLE DES CERTIFICATS
         ============================================================ --%>
    <% if (certificatsAvecCours != null && !certificatsAvecCours.isEmpty()) { %>
        <div class="certificats-grid">
            <% for (Map<String, Object> data : certificatsAvecCours) {
                Certificat certificat = (Certificat) data.get("certificat");
                Cours cours = (Cours) data.get("cours");
                if (certificat == null) continue;
            %>
                <a href="${pageContext.request.contextPath}/joueur/cours/certificat?coursId=<%= certificat.getCoursId() %>" 
                   class="certificat-card-link">
                    <div class="certificat-card">
                        
                        <%-- Ruban de mention --%>
                        <div class="certificat-ribbon" style="background: <%= certificat.getCouleurMention() %>;">
                            <i class="<%= certificat.getIconeMention() %>"></i>
                            <%= certificat.getMention() %>
                        </div>

                        <%-- Icône du cours --%>
                        <div class="certificat-card-icon" style="background: linear-gradient(135deg, <%= cours != null ? cours.getCouleurTheme() : "#667eea" %>, <%= cours != null ? cours.getCouleurTheme() : "#667eea" %>dd);">
                            <i class="<%= cours != null ? cours.getIconeTheme() : "fas fa-book" %>"></i>
                        </div>

                        <%-- Infos --%>
                        <h3 class="certificat-card-titre">
                            <%= cours != null ? cours.getTitre() : "Cours" %>
                        </h3>
                        
                        <% if (cours != null) { %>
                            <span class="certificat-card-theme">
                                <i class="<%= cours.getIconeTheme() %>"></i>
                                <%= cours.getTheme() %> • <%= cours.getNiveauFormate() %>
                            </span>
                        <% } %>

                        <%-- Score --%>
                        <div class="certificat-card-score">
                            <div class="mini-score-circle" style="border-color: <%= certificat.getCouleurMention() %>;">
                                <span style="color: <%= certificat.getCouleurMention() %>;"><%= certificat.getPourcentage() %></span>
                            </div>
                        </div>

                        <%-- Date --%>
                        <span class="certificat-card-date">
                            <i class="far fa-calendar-alt"></i>
                            <%= certificat.getDateFormatee() %>
                        </span>

                        <%-- Flèche --%>
                        <div class="certificat-card-arrow">
                            <i class="fas fa-arrow-right"></i>
                        </div>

                    </div>
                </a>
            <% } %>
        </div>
    <% } else { %>
        <%-- ============================================================
             ÉTAT VIDE
             ============================================================ --%>
        <div class="empty-state">
            <div class="empty-icon">
                <i class="fas fa-certificate"></i>
            </div>
            <h3>Aucun certificat obtenu</h3>
            <p>Terminez tous les chapitres d'un cours et réussissez l'examen final pour obtenir votre premier certificat !</p>
            <a href="${pageContext.request.contextPath}/joueur/cours" class="btn-commencer">
                <i class="fas fa-play"></i> Commencer un cours
            </a>
        </div>
    <% } %>

    <%-- ============================================================
         SECTION MOTIVATION
         ============================================================ --%>
    <div class="motivation-section">
        <div class="motivation-card">
            <div class="motivation-icon">
                <i class="fas fa-lightbulb"></i>
            </div>
            <div class="motivation-content">
                <h4>Partagez vos réussites</h4>
                <p>Les certificats LearnPath sont vérifiables en ligne. Ajoutez-les à votre CV ou partagez-les sur LinkedIn pour valoriser vos compétences.</p>
            </div>
        </div>
    </div>

    <%-- ============================================================
         BOUTON RETOUR
         ============================================================ --%>
    <div class="back-section">
        <a href="${pageContext.request.contextPath}/joueur/cours" class="btn-back">
            <i class="fas fa-arrow-left"></i>
            <span>Retour aux cours</span>
        </a>
    </div>

</div>

<%-- ===== CHARGEMENT DES SCRIPTS ===== --%>
<%
    String mesCertificatsJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/cours-mes-certificats.js\"></script>";
    request.setAttribute("extraScripts", mesCertificatsJS);
%>

<%@ include file="../footer-connecte.jsp" %>