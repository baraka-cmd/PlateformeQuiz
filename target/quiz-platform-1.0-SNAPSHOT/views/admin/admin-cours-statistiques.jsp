<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.Certificat" %>

<%-- ============================================================
     FICHIER : views/admin/admin-cours-statistiques.jsp
     RÔLE    : Page de statistiques détaillées d'un cours
              - Résumé (apprenants, certificats, leçons)
              - Progression par module (barres)
              - Derniers certificats
              - Derniers apprenants actifs
     ============================================================ --%>

<%
    Cours cours = (Cours) request.getAttribute("cours");
    Integer nbApprenants = (Integer) request.getAttribute("nbApprenants");
    Integer nbCertificats = (Integer) request.getAttribute("nbCertificats");
    Integer totalLecons = (Integer) request.getAttribute("totalLecons");
    List<Map<String, Object>> progressionModules = (List<Map<String, Object>>) request.getAttribute("progressionModules");
    List<Map<String, Object>> certificats = (List<Map<String, Object>>) request.getAttribute("certificats");
    List<Map<String, Object>> apprenants = (List<Map<String, Object>>) request.getAttribute("apprenants");

    if (cours == null) {
        response.sendRedirect(request.getContextPath() + "/admin/cours");
        return;
    }

    if (nbApprenants == null) nbApprenants = 0;
    if (nbCertificats == null) nbCertificats = 0;
    if (totalLecons == null) totalLecons = 0;
    if (progressionModules == null) progressionModules = new java.util.ArrayList<>();
    if (certificats == null) certificats = new java.util.ArrayList<>();
    if (apprenants == null) apprenants = new java.util.ArrayList<>();

    int coursId = cours.getId();
    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new java.util.Locale("fr", "FR"));
    SimpleDateFormat sdfComplet = new SimpleDateFormat("dd MMM yyyy HH:mm", new java.util.Locale("fr", "FR"));

    request.setAttribute("pageTitle", "Statistiques - " + cours.getTitre() + " - Administration");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-cours-statistiques.css\">";
    request.setAttribute("extraHead", adminCSS);
%>

<%@ include file="../header.jsp" %>

<div class="acst-container">

    <%-- ===== BREADCRUMB ===== --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/admin/cours" class="breadcrumb-link">
            <i class="fas fa-graduation-cap"></i><span>Cours</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-chart-bar"></i><span>Statistiques : <%= cours.getTitre() %></span>
        </span>
    </nav>

    <%-- ===== EN-TÊTE ===== --%>
    <div class="acst-header">
        <div class="acst-header-top">
            <div class="acst-header-icon" style="background: <%= cours.getCouleurTheme() %>;">
                <i class="<%= cours.getIconeTheme() %>"></i>
            </div>
            <div class="acst-header-info">
                <span class="acst-header-theme"><%= cours.getTheme() %> • <%= cours.getNiveauFormate() %></span>
                <h1><%= cours.getTitre() %></h1>
                <p>Statistiques détaillées du cours</p>
            </div>
        </div>
        <div class="acst-header-actions">
            <a href="${pageContext.request.contextPath}/admin/cours/modules?coursId=<%= coursId %>" class="acst-btn-outline">
                <i class="fas fa-cubes"></i><span>Modules</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/cours/chapitres?coursId=<%= coursId %>" class="acst-btn-outline">
                <i class="fas fa-bookmark"></i><span>Chapitres</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/cours" class="acst-btn-outline">
                <i class="fas fa-arrow-left"></i><span>Retour</span>
            </a>
        </div>
    </div>

    <%-- ===== CARTES RÉSUMÉ ===== --%>
    <div class="acst-resume-grid">
        <div class="acst-resume-card">
            <div class="acst-resume-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                <i class="fas fa-users"></i>
            </div>
            <div class="acst-resume-content">
                <span class="acst-resume-nombre"><%= nbApprenants %></span>
                <span class="acst-resume-label">Apprenants</span>
            </div>
        </div>
        <div class="acst-resume-card">
            <div class="acst-resume-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                <i class="fas fa-certificate"></i>
            </div>
            <div class="acst-resume-content">
                <span class="acst-resume-nombre"><%= nbCertificats %></span>
                <span class="acst-resume-label">Certificats</span>
            </div>
        </div>
        <div class="acst-resume-card">
            <div class="acst-resume-icon" style="background: linear-gradient(135deg, #27ae60, #2ecc71);">
                <i class="fas fa-file-alt"></i>
            </div>
            <div class="acst-resume-content">
                <span class="acst-resume-nombre"><%= totalLecons %></span>
                <span class="acst-resume-label">Leçons</span>
            </div>
        </div>
        <div class="acst-resume-card">
            <div class="acst-resume-icon" style="background: linear-gradient(135deg, #e74c3c, #c0392b);">
                <i class="fas fa-chart-pie"></i>
            </div>
            <div class="acst-resume-content">
                <span class="acst-resume-nombre"><%= nbApprenants > 0 ? (nbCertificats * 100 / nbApprenants) : 0 %>%</span>
                <span class="acst-resume-label">Taux réussite</span>
            </div>
        </div>
    </div>

    <%-- ===== LAYOUT 2 COLONNES ===== --%>
    <div class="acst-layout">

        <%-- ===== COLONNE GAUCHE : PROGRESSION PAR MODULE ===== --%>
        <div class="acst-col-gauche">
            <div class="acst-card">
                <div class="acst-card-header">
                    <h2><i class="fas fa-chart-simple"></i> Progression par module</h2>
                    <span class="acst-card-badge"><%= nbApprenants %> apprenants</span>
                </div>
                <div class="acst-card-body">
                    <% if (progressionModules != null && !progressionModules.isEmpty()) { %>
                        <div class="acst-modules-progression">
                            <% for (Map<String, Object> mod : progressionModules) {
                                int module = (Integer) mod.get("module");
                                int reussis = (Integer) mod.get("reussis");
                                int pourcentage = (Integer) mod.get("pourcentage");
                                String barColor = pourcentage >= 75 ? "#27ae60" : (pourcentage >= 50 ? "#f39c12" : (pourcentage > 0 ? "#e74c3c" : "#e8eaef"));
                            %>
                                <div class="acst-module-bar-item">
                                    <div class="acst-module-bar-label">
                                        <span>Module <%= module %></span>
                                        <span><%= reussis %> / <%= nbApprenants %></span>
                                    </div>
                                    <div class="acst-module-bar-track">
                                        <div class="acst-module-bar-fill" style="width: <%= pourcentage %>%; background: <%= barColor %>;"></div>
                                    </div>
                                </div>
                            <% } %>
                        </div>
                    <% } else { %>
                        <div class="acst-empty-mini">
                            <i class="fas fa-inbox"></i><p>Aucune donnée de progression.</p>
                        </div>
                    <% } %>
                </div>
            </div>
        </div>

        <%-- ===== COLONNE DROITE : CERTIFICATS + APPRENANTS ===== --%>
        <div class="acst-col-droite">

            <%-- ===== DERNIERS CERTIFICATS ===== --%>
            <div class="acst-card">
                <div class="acst-card-header">
                    <h2><i class="fas fa-certificate"></i> Derniers certificats</h2>
                    <span class="acst-card-badge"><%= certificats.size() %></span>
                </div>
                <div class="acst-card-body">
                    <% if (certificats != null && !certificats.isEmpty()) { %>
                        <div class="acst-liste-mini">
                            <% for (Map<String, Object> certData : certificats) {
                                Certificat cert = (Certificat) certData.get("certificat");
                                String pseudo = (String) certData.get("pseudo");
                            %>
                                <div class="acst-liste-item">
                                    <div class="acst-liste-avatar">
                                        <i class="fas fa-user"></i>
                                    </div>
                                    <div class="acst-liste-info">
                                        <strong><%= pseudo %></strong>
                                        <span>
                                            <span style="color: <%= cert.getCouleurMention() %>; font-weight: 600;">
                                                <i class="<%= cert.getIconeMention() %>"></i> <%= cert.getMention() %>
                                            </span>
                                            • <%= cert.getPourcentage() %>
                                        </span>
                                    </div>
                                    <span class="acst-liste-date"><%= cert.getDateFormatee() %></span>
                                </div>
                            <% } %>
                        </div>
                    <% } else { %>
                        <div class="acst-empty-mini">
                            <i class="fas fa-inbox"></i><p>Aucun certificat délivré.</p>
                        </div>
                    <% } %>
                </div>
            </div>

            <%-- ===== DERNIERS APPRENANTS ===== --%>
            <div class="acst-card">
                <div class="acst-card-header">
                    <h2><i class="fas fa-user-graduate"></i> Apprenants récents</h2>
                    <span class="acst-card-badge"><%= apprenants.size() %></span>
                </div>
                <div class="acst-card-body">
                    <% if (apprenants != null && !apprenants.isEmpty()) { %>
                        <div class="acst-liste-mini">
                            <% for (Map<String, Object> app : apprenants) {
                                String pseudo = (String) app.get("pseudo");
                                java.sql.Timestamp derniereActivite = (java.sql.Timestamp) app.get("derniereActivite");
                                int leconsVues = (Integer) app.get("leconsVues");
                                String dateStr = derniereActivite != null ? sdfComplet.format(derniereActivite) : "N/A";
                            %>
                                <div class="acst-liste-item">
                                    <div class="acst-liste-avatar">
                                        <i class="fas fa-user"></i>
                                    </div>
                                    <div class="acst-liste-info">
                                        <strong><%= pseudo %></strong>
                                        <span><%= leconsVues %> leçon<%= leconsVues > 1 ? "s" : "" %> vues</span>
                                    </div>
                                    <span class="acst-liste-date"><%= dateStr %></span>
                                </div>
                            <% } %>
                        </div>
                    <% } else { %>
                        <div class="acst-empty-mini">
                            <i class="fas fa-inbox"></i><p>Aucun apprenant pour le moment.</p>
                        </div>
                    <% } %>
                </div>
            </div>

        </div>
    </div>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="acst-back">
        <a href="${pageContext.request.contextPath}/admin/cours" class="acst-btn-retour">
            <i class="fas fa-arrow-left"></i> Retour à la liste des cours
        </a>
    </div>

</div>

<%@ include file="../footer-connecte.jsp" %>