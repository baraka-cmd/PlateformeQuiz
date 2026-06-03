<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.Chapitre" %>
<%@ page import="com.amijayquiz.models.Lecon" %>

<%-- ============================================================
     FICHIER : views/joueur/cours-chapitre.jsp
     RÔLE    : Page d'un chapitre de cours AmijayQuiz
              Affiche la liste des lecons du chapitre
              avec navigation vers le plan du cours
     ============================================================ --%>

<%
    // ===== RECUPERATION DES ATTRIBUTS =====
    Cours cours = (Cours) request.getAttribute("cours");
    Chapitre chapitre = (Chapitre) request.getAttribute("chapitre");
    List<Map<String, Object>> lecons = (List<Map<String, Object>>) request.getAttribute("lecons");
    Integer totalLecons = (Integer) request.getAttribute("totalLecons");
    Integer moduleActifNumero = (Integer) request.getAttribute("moduleActifNumero");

    // ===== VERIFICATION SECURITE =====
    if (cours == null || chapitre == null) {
        response.sendRedirect(request.getContextPath() + "/joueur/cours");
        return;
    }

    // ===== VALEURS PAR DEFAUT =====
    if (totalLecons == null) totalLecons = 0;
    if (moduleActifNumero == null) moduleActifNumero = 1;
    if (lecons == null) lecons = new java.util.ArrayList<Map<String, Object>>();

    String themeEncode = java.net.URLEncoder.encode(cours.getTheme(), "UTF-8");
    String niveauEncode = java.net.URLEncoder.encode(cours.getNiveau(), "UTF-8");

    request.setAttribute("pageTitle", chapitre.getTitre() + " - " + cours.getTitre() + " - AmijayQuiz");
%>

<%
    String coursChapitreCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/cours-chapitre.css\">";
    request.setAttribute("extraHead", coursChapitreCSS);
%>

<%@ include file="../header.jsp" %>

<div class="chapitre-container">

    <%-- ============================================================
         BREADCRUMB
         ============================================================ --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i>
            <span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours" class="breadcrumb-link">
            <i class="fas fa-graduation-cap"></i>
            <span>Cours</span>
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
            <i class="fas fa-sitemap"></i>
            <span>Plan</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-bookmark"></i>
            <span><%= chapitre.getTitre() %></span>
        </span>
    </nav>

    <%-- ============================================================
         EN-TETE DU CHAPITRE
         ============================================================ --%>
    <div class="chapitre-header">
        <div class="chapitre-header-top">
            <span class="chapitre-module-badge">
                <i class="fas fa-cube"></i> Module <%= moduleActifNumero %>
            </span>
            <span class="chapitre-lecon-count">
                <i class="fas fa-file-alt"></i> <%= totalLecons %> lecon<%= totalLecons > 1 ? "s" : "" %>
            </span>
        </div>
        <h1><%= chapitre.getTitre() %></h1>
        <% if (chapitre.getDescription() != null && !chapitre.getDescription().isEmpty()) { %>
            <p class="chapitre-description"><%= chapitre.getDescription() %></p>
        <% } %>
        <% if (chapitre.getObjectifs() != null && !chapitre.getObjectifs().isEmpty()) { %>
            <div class="chapitre-objectifs">
                <h3><i class="fas fa-bullseye"></i> Objectifs du chapitre</h3>
                <p><%= chapitre.getObjectifs() %></p>
            </div>
        <% } %>
    </div>

    <%-- ============================================================
         LISTE DES LECONS
         ============================================================ --%>
    <div class="lecons-liste">
        <% if (lecons != null && !lecons.isEmpty()) {
            int index = 1;
            for (Map<String, Object> leconData : lecons) {
                Lecon lecon = (Lecon) leconData.get("lecon");
                if (lecon == null) continue;

                boolean estTerminee = leconData.get("estTerminee") != null ? (Boolean) leconData.get("estTerminee") : false;
                boolean estAccessible = leconData.get("estAccessible") != null ? (Boolean) leconData.get("estAccessible") : true;
                boolean estActive = leconData.get("estActive") != null ? (Boolean) leconData.get("estActive") : false;

                String tempsLecture = lecon.getTempsLectureFormate();
        %>
            <div class="lecon-item <%= estTerminee ? "terminee" : "" %> <%= estActive ? "active" : "" %>">
                <div class="lecon-numero">
                    <% if (estTerminee) { %>
                        <i class="fas fa-check-circle"></i>
                    <% } else if (estActive) { %>
                        <i class="fas fa-play-circle"></i>
                    <% } else { %>
                        <span><%= index %></span>
                    <% } %>
                </div>
                <div class="lecon-info">
                    <h3><%= lecon.getTitre() %></h3>
                    <div class="lecon-meta">
                        <% if (lecon.hasContenuTexte()) { %>
                            <span class="lecon-meta-item">
                                <i class="fas fa-book-reader"></i> <%= tempsLecture %>
                            </span>
                        <% } %>
                        <% if (lecon.hasCodeSource()) { %>
                            <span class="lecon-meta-item">
                                <i class="fas fa-code"></i> Exemple de code
                            </span>
                        <% } %>
                        <% if (lecon.hasRessources()) { %>
                            <span class="lecon-meta-item">
                                <i class="fas fa-paperclip"></i> <%= lecon.getNombreRessources() %> ressource<%= lecon.getNombreRessources() > 1 ? "s" : "" %>
                            </span>
                        <% } %>
                        <% if (!lecon.isGratuit()) { %>
                            <span class="lecon-meta-item lecon-premium-badge">
                                <i class="fas fa-crown"></i> Premium
                            </span>
                        <% } %>
                    </div>
                </div>
                <div class="lecon-action">
                    <% if (estAccessible) { %>
                        <a href="${pageContext.request.contextPath}/joueur/cours/lecon?coursId=<%= cours.getId() %>&chapitreId=<%= chapitre.getId() %>&leconId=<%= lecon.getId() %>"
                           class="lecon-acces-btn">
                            <i class="fas fa-arrow-right"></i>
                            <span>Acceder</span>
                        </a>
                    <% } else { %>
                        <span class="lecon-acces-btn disabled" title="Lecon non accessible pour le moment">
                            <i class="fas fa-lock"></i>
                        </span>
                    <% } %>
                </div>
            </div>
        <%      index++;
            }
        } else { %>
            <div class="empty-state">
                <div class="empty-icon">
                    <i class="fas fa-inbox"></i>
                </div>
                <h3>Aucune lecon dans ce chapitre</h3>
                <p>Les lecons de ce chapitre seront bientot disponibles.</p>
            </div>
        <% } %>
    </div>

    <%-- ============================================================
         BOUTON RETOUR
         ============================================================ --%>
    <div class="chapitre-footer">
        <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" class="btn-retour">
            <i class="fas fa-arrow-left"></i> Retour au plan du cours
        </a>
    </div>

</div>

<%
    String coursChapitreJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/cours-chapitre.js\"></script>";
    request.setAttribute("extraScripts", coursChapitreJS);
%>

<%@ include file="../footer-connecte.jsp" %>