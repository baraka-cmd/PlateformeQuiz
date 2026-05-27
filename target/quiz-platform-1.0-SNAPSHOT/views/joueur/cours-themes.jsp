<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%-- ============================================================
     Fichier : cours-themes.jsp
     Rôle    : Page d'accueil de la section Cours
              Affiche tous les thèmes disponibles avec progression
     ============================================================ --%>

<%
    List<Map<String, Object>> themes = (List<Map<String, Object>>) request.getAttribute("themes");
    Integer totalCours = (Integer) request.getAttribute("totalCours");
    Integer totalCoursTermines = (Integer) request.getAttribute("totalCoursTermines");
    Integer totalLecons = (Integer) request.getAttribute("totalLecons");
    Integer totalLeconsTerminees = (Integer) request.getAttribute("totalLeconsTerminees");
    Integer pourcentageGlobal = (Integer) request.getAttribute("pourcentageGlobal");

    if (totalCours == null) totalCours = 0;
    if (totalCoursTermines == null) totalCoursTermines = 0;
    if (totalLecons == null) totalLecons = 0;
    if (totalLeconsTerminees == null) totalLeconsTerminees = 0;
    if (pourcentageGlobal == null) pourcentageGlobal = 0;
%>



<%-- ===== CHARGEMENT DES STYLES ===== --%>
<%
    String coursThemesCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/cours-themes.css\">";
    request.setAttribute("extraHead", coursThemesCSS);
%>

<%@ include file="../header.jsp" %>

<div class="cours-themes-container">

    <%-- ===== BREADCRUMB ===== --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i>
            <span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-graduation-cap"></i>
            <span>Cours</span>
        </span>
    </nav>

    <%-- ===== BANNIÈRE STATISTIQUES ===== --%>
    <div class="stats-banner">
        <div class="stats-banner-icon">
            <i class="fas fa-graduation-cap"></i>
        </div>
        <div class="stats-banner-content">
            <h1>Plateforme de Cours</h1>
            <p>Apprenez à votre rythme avec des cours complets et des certifications</p>
        </div>
        <div class="stats-banner-numbers">
            <div class="stat-mini">
                <i class="fas fa-book"></i>
                <strong><%= totalCours %></strong>
                <span>Cours</span>
            </div>
            <div class="stat-mini">
                <i class="fas fa-check-circle"></i>
                <strong><%= totalCoursTermines %></strong>
                <span>Terminés</span>
            </div>
            <div class="stat-mini">
                <i class="fas fa-list-ol"></i>
                <strong><%= totalLecons %></strong>
                <span>Leçons</span>
            </div>
            <div class="stat-mini">
                <i class="fas fa-chart-line"></i>
                <strong><%= pourcentageGlobal %>%</strong>
                <span>Progression</span>
            </div>
        </div>
    </div>

    <%-- ===== BARRE DE PROGRESSION GLOBALE ===== --%>
    <div class="global-progress">
        <div class="global-progress-header">
            <span><i class="fas fa-chart-pie"></i> Progression globale</span>
            <span><strong><%= pourcentageGlobal %>%</strong></span>
        </div>
        <div class="global-progress-bar">
            <div class="global-progress-fill" style="width: <%= pourcentageGlobal %>%;"></div>
        </div>
        <div class="global-progress-detail">
            <span><i class="fas fa-check-circle"></i> <%= totalLeconsTerminees %> / <%= totalLecons %> leçons terminées</span>
            <span><i class="fas fa-trophy"></i> <%= totalCoursTermines %> / <%= totalCours %> cours validés</span>
        </div>
    </div>

    <%-- ===== TITRE SECTION ===== --%>
    <div class="section-header">
        <h2>
            <i class="fas fa-layer-group"></i>
            Choisissez un thème pour commencer
        </h2>
        <span class="badge-count">
            <i class="fas fa-puzzle-piece"></i>
            <%= themes != null ? themes.size() : 0 %> thèmes disponibles
        </span>
    </div>

    <%-- ===== GRILLE DES THÈMES ===== --%>
    <div class="themes-grid">
        <% if (themes != null && !themes.isEmpty()) {
            for (Map<String, Object> theme : themes) {
                String nomTheme = (String) theme.get("nom");
                String iconeTheme = (String) theme.get("icone");
                String couleurTheme = (String) theme.get("couleur");
                int totalCoursTheme = (Integer) theme.get("totalCours");
                int coursTerminesTheme = (Integer) theme.get("coursTermines");
                int totalLeconsTheme = (Integer) theme.get("totalLecons");
                int leconsTermineesTheme = (Integer) theme.get("leconsTerminees");
                int pourcentageTheme = (Integer) theme.get("pourcentageProgression");
        %>
            <a href="${pageContext.request.contextPath}/joueur/cours/theme?theme=<%= java.net.URLEncoder.encode(nomTheme, "UTF-8") %>" 
               class="theme-card-link">
                <div class="theme-card">
                    
                    <%-- Badge terminé si 100% --%>
                    <% if (pourcentageTheme >= 100 && totalCoursTheme > 0) { %>
                        <div class="theme-badge-completed">
                            <i class="fas fa-trophy"></i>
                        </div>
                    <% } %>
                    
                    <%-- Icône du thème --%>
                    <div class="theme-card-icon" style="background: linear-gradient(135deg, <%= couleurTheme %>, <%= couleurTheme %>dd);">
                        <i class="<%= iconeTheme %>"></i>
                    </div>
                    
                    <%-- Nom et stats --%>
                    <h3 class="theme-card-title"><%= nomTheme %></h3>
                    
                    <div class="theme-card-stats">
                        <span class="theme-stat">
                            <i class="fas fa-book"></i>
                            <%= totalCoursTheme %> cours
                        </span>
                        <span class="theme-stat-separator">•</span>
                        <span class="theme-stat">
                            <i class="fas fa-list-ol"></i>
                            <%= totalLeconsTheme %> leçons
                        </span>
                    </div>
                    
                    <%-- Barre de progression --%>
                    <div class="theme-card-progress">
                        <div class="theme-progress-bar">
                            <div class="theme-progress-fill" style="width: <%= pourcentageTheme %>%; background: <%= couleurTheme %>;"></div>
                        </div>
                        <span class="theme-progress-text"><%= pourcentageTheme %>%</span>
                    </div>
                    
                    <%-- Pied de carte --%>
                    <div class="theme-card-footer">
                        <span class="theme-footer-text">
                            <% if (pourcentageTheme >= 100 && totalCoursTheme > 0) { %>
                                <i class="fas fa-check-circle" style="color: #27ae60;"></i> Terminé
                            <% } else if (pourcentageTheme > 0) { %>
                                <i class="fas fa-spinner" style="color: #667eea;"></i> En cours
                            <% } else { %>
                                <i class="fas fa-play-circle" style="color: #f39c12;"></i> Commencer
                            <% } %>
                        </span>
                        <span class="theme-footer-arrow">
                            <i class="fas fa-arrow-right"></i>
                        </span>
                    </div>
                    
                </div>
            </a>
        <%  }
        } else { %>
            <div class="empty-state full-width">
                <div class="empty-icon">
                    <i class="fas fa-inbox"></i>
                </div>
                <h3>Aucun cours disponible</h3>
                <p>Les cours seront bientôt disponibles. Revenez prochainement !</p>
            </div>
        <% } %>
    </div>

    <%-- ===== SECTION INFORMATIONS ===== --%>
    <div class="info-section">
        <div class="info-card">
            <div class="info-card-icon">
                <i class="fas fa-lightbulb"></i>
            </div>
            <div class="info-card-content">
                <h4>Comment ça marche ?</h4>
                <p>Choisissez un thème → Sélectionnez un niveau → Suivez les cours → Passez l'examen → Obtenez votre certificat !</p>
            </div>
        </div>
        
        <div class="info-card">
            <div class="info-card-icon">
                <i class="fas fa-certificate"></i>
            </div>
            <div class="info-card-content">
                <h4>Certificats vérifiables</h4>
                <p>Chaque certificat possède un code unique vérifiable en ligne. Partagez-le sur LinkedIn ou votre CV !</p>
            </div>
        </div>
        
        <div class="info-card">
            <div class="info-card-icon">
                <i class="fas fa-clock"></i>
            </div>
            <div class="info-card-content">
                <h4>Apprenez à votre rythme</h4>
                <p>Reprenez exactement où vous en étiez. Votre progression est sauvegardée automatiquement.</p>
            </div>
        </div>
    </div>

</div>

<%-- ===== CHARGEMENT DES SCRIPTS ===== --%>
<%
    String coursThemesJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/cours-themes.js\"></script>";
    request.setAttribute("extraScripts", coursThemesJS);
%>

<%@ include file="../footer.jsp" %>