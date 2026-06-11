<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.learnpath.models.Cours" %>
<%@ page import="com.learnpath.models.ExamenModule" %>

<%-- ============================================================
     FICHIER : views/admin/admin-cours-modules.jsp
     RÔLE    : Page de gestion des 15 modules d'un cours
              - Grille 3×5 des modules avec statuts
              - Configuration des examens de module
              - Navigation vers les questions d'examen
              - Statistiques globales
     ============================================================ --%>

<%
    // ===== RÉCUPÉRATION DES ATTRIBUTS =====
    Cours cours = (Cours) request.getAttribute("cours");
    List<Map<String, Object>> modules = (List<Map<String, Object>>) request.getAttribute("modules");
    Integer nombreModules = (Integer) request.getAttribute("nombreModules");
    Integer examensConfigured = (Integer) request.getAttribute("examensConfigured");
    Integer totalQuestions = (Integer) request.getAttribute("totalQuestions");
    String messageSucces = (String) request.getAttribute("messageSucces");
    String messageErreur = (String) request.getAttribute("messageErreur");

    if (cours == null) {
        response.sendRedirect(request.getContextPath() + "/admin/cours");
        return;
    }

    if (modules == null) modules = new java.util.ArrayList<>();
    if (nombreModules == null) nombreModules = 15;
    if (examensConfigured == null) examensConfigured = 0;
    if (totalQuestions == null) totalQuestions = 0;

    int coursId = cours.getId();
    String themeEncode = java.net.URLEncoder.encode(cours.getTheme(), "UTF-8");
    String niveauEncode = java.net.URLEncoder.encode(cours.getNiveau(), "UTF-8");

    request.setAttribute("pageTitle", "Modules - " + cours.getTitre() + " - Administration");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-cours-modules.css\">";
    request.setAttribute("extraHead", adminCSS);
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-cours-modules.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../header.jsp" %>

<div class="acm-container">

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
            <i class="fas fa-cubes"></i><span>Modules : <%= cours.getTitre() %></span>
        </span>
    </nav>

    <%-- ===== MESSAGES FLASH ===== --%>
    <% if (messageSucces != null) { %>
        <div class="message-alerte message-succes" data-timeout="6000">
            <i class="fas fa-check-circle"></i><span><%= messageSucces %></span>
        </div>
    <% } %>
    <% if (messageErreur != null) { %>
        <div class="message-alerte message-erreur" data-timeout="8000">
            <i class="fas fa-exclamation-circle"></i><span><%= messageErreur %></span>
        </div>
    <% } %>

    <%-- ===== EN-TÊTE DU COURS ===== --%>
    <div class="acm-header">
        <div class="acm-header-top">
            <div class="acm-header-icon" style="background: <%= cours.getCouleurTheme() %>;">
                <i class="<%= cours.getIconeTheme() %>"></i>
            </div>
            <div class="acm-header-info">
                <span class="acm-header-theme"><%= cours.getTheme() %> • <%= cours.getNiveauFormate() %></span>
                <h1><%= cours.getTitre() %></h1>
                <p>Gestion des <%= nombreModules %> modules et de leurs examens</p>
            </div>
        </div>
        <div class="acm-header-actions">
            <a href="${pageContext.request.contextPath}/admin/cours/chapitres?coursId=<%= coursId %>" class="acm-btn-outline">
                <i class="fas fa-bookmark"></i><span>Chapitres</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/cours/modifier?id=<%= coursId %>" class="acm-btn-outline">
                <i class="fas fa-edit"></i><span>Modifier le cours</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/cours/statistiques?id=<%= coursId %>" class="acm-btn-outline">
                <i class="fas fa-chart-bar"></i><span>Statistiques</span>
            </a>
        </div>
    </div>

    <%-- ===== BARRE DE STATISTIQUES ===== --%>
    <div class="acm-stats-bar">
        <div class="acm-stat-item">
            <i class="fas fa-cubes"></i>
            <strong><%= nombreModules %></strong> modules
        </div>
        <div class="acm-stat-separator"></div>
        <div class="acm-stat-item <%= examensConfigured > 0 ? "acm-stat-success" : "" %>">
            <i class="fas fa-check-circle"></i>
            <strong><%= examensConfigured %></strong> examens configurés
        </div>
        <div class="acm-stat-separator"></div>
        <div class="acm-stat-item">
            <i class="fas fa-question-circle"></i>
            <strong><%= totalQuestions %></strong> questions au total
        </div>
        <div class="acm-stat-separator"></div>
        <div class="acm-stat-item">
            <i class="fas fa-clock"></i>
            <strong><%= nombreModules - examensConfigured %></strong> restants
        </div>
    </div>

    <%-- ===== GUIDE ===== --%>
    <div class="acm-guide">
        <div class="acm-guide-icon"><i class="fas fa-info-circle"></i></div>
        <div class="acm-guide-content">
            <strong>Gestion des modules</strong>
            <p>Chaque cours contient exactement <%= nombreModules %> modules. Configurez l'examen de chaque module (20 QCM, seuil 75%). Les modules sans examen configuré n'afficheront pas d'examen côté apprenant.</p>
        </div>
    </div>

    <%-- ===== GRILLE DES 15 MODULES (3 colonnes × 5 lignes) ===== --%>
    <div class="acm-grid">
        <% if (modules != null && !modules.isEmpty()) {
            for (Map<String, Object> moduleData : modules) {
                int numero = (Integer) moduleData.get("numero");
                boolean examenConfigure = (Boolean) moduleData.get("examenConfigure");
                ExamenModule examen = (ExamenModule) moduleData.get("examen");
                int nbQuestions = (Integer) moduleData.get("nbQuestions");

                String cardClass = examenConfigure ? "acm-card-configure" : "acm-card-vide";
                String statusIcon = examenConfigure ? "fa-check-circle" : "fa-circle";
                String statusColor = examenConfigure ? "#27ae60" : "#ccc";
                String statusText = examenConfigure ? "Examen configuré" : "Aucun examen";
                String questionsText = examenConfigure ? nbQuestions + " question" + (nbQuestions > 1 ? "s" : "") : "—";
        %>
            <div class="acm-card <%= cardClass %>" data-module="<%= numero %>">
                
                <%-- En-tête de la carte --%>
                <div class="acm-card-header">
                    <span class="acm-card-numero" style="background: <%= statusColor %>;"><%= numero %></span>
                    <span class="acm-card-statut" style="color: <%= statusColor %>;">
                        <i class="fas <%= statusIcon %>"></i> <%= statusText %>
                    </span>
                </div>

                <%-- Corps de la carte --%>
                <div class="acm-card-body">
                    <div class="acm-card-info">
                        <div class="acm-card-info-item">
                            <i class="fas fa-question-circle"></i>
                            <span>Questions : <strong><%= questionsText %></strong></span>
                        </div>
                        <% if (examenConfigure && examen != null) { %>
                            <div class="acm-card-info-item">
                                <i class="fas fa-flag-checkered"></i>
                                <span>Seuil : <strong><%= examen.getSeuilReussite() %>%</strong></span>
                            </div>
                            <div class="acm-card-info-item">
                                <i class="fas fa-tag"></i>
                                <span><%= examen.getTitre() != null ? examen.getTitre() : "Examen " + examen.getModuleFormate() %></span>
                            </div>
                        <% } else { %>
                            <div class="acm-card-info-item acm-card-info-empty">
                                <i class="fas fa-info-circle"></i>
                                <span>Pas encore d'examen configuré</span>
                            </div>
                        <% } %>
                    </div>
                </div>

                <%-- Pied de la carte (actions) --%>
                <div class="acm-card-footer">
                    <% if (examenConfigure && examen != null) { %>
                        <a href="${pageContext.request.contextPath}/admin/cours/modules/modifier?id=<%= examen.getId() %>&coursId=<%= coursId %>" 
                           class="acm-btn-card acm-btn-modifier">
                            <i class="fas fa-edit"></i><span>Modifier</span>
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/cours/modules/questions?examenId=<%= examen.getId() %>&coursId=<%= coursId %>" 
                           class="acm-btn-card acm-btn-questions">
                            <i class="fas fa-list-ul"></i><span>Questions</span>
                        </a>
                    <% } else { %>
                        <a href="${pageContext.request.contextPath}/admin/cours/modules/creer?coursId=<%= coursId %>&module=<%= numero %>" 
                           class="acm-btn-card acm-btn-creer">
                            <i class="fas fa-plus-circle"></i><span>Configurer</span>
                        </a>
                    <% } %>
                </div>

            </div>
        <%  }
        } else { %>
            <div class="acm-empty full-width">
                <div class="acm-empty-icon"><i class="fas fa-cubes"></i></div>
                <h3>Aucun module trouvé</h3>
                <p>Une erreur est survenue lors du chargement des modules.</p>
            </div>
        <% } %>
    </div>

    <%-- ===== BOUTONS RETOUR ===== --%>
    <div class="acm-back">
        <a href="${pageContext.request.contextPath}/admin/cours" class="acm-btn-retour">
            <i class="fas fa-arrow-left"></i> Retour à la liste des cours
        </a>
        <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= coursId %>" class="acm-btn-retour" target="_blank">
            <i class="fas fa-eye"></i> Voir côté apprenant
        </a>
    </div>

</div>

<%@ include file="../footer-connecte.jsp" %>