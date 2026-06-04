<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%-- ============================================================
     FICHIER : views/admin/admin-resultats.jsp
     RÔLE    : Page d'administration des résultats des joueurs
              - Statistiques globales
              - Filtres avancés
              - Tableau paginé des résultats
              - Liens vers détail joueur et détail quiz
     ============================================================ --%>

<%
    List<Map<String, Object>> resultats = (List<Map<String, Object>>) request.getAttribute("resultats");
    Integer totalResultats = (Integer) request.getAttribute("totalResultats");
    Integer pageActuelle = (Integer) request.getAttribute("page");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    String recherche = (String) request.getAttribute("recherche");
    String filtreTheme = (String) request.getAttribute("filtreTheme");
    String filtreNiveau = (String) request.getAttribute("filtreNiveau");
    String filtreResultat = (String) request.getAttribute("filtreResultat");
    String tri = (String) request.getAttribute("tri");
    List<String> themes = (List<String>) request.getAttribute("themes");
    Map<String, Object> stats = (Map<String, Object>) request.getAttribute("stats");
    String urlPagination = (String) request.getAttribute("urlPagination");

    if (resultats == null) resultats = new java.util.ArrayList<Map<String, Object>>();
    if (totalResultats == null) totalResultats = 0;
    if (pageActuelle == null) pageActuelle = 1;
    if (totalPages == null) totalPages = 1;
    if (recherche == null) recherche = "";
    if (filtreTheme == null) filtreTheme = "tous";
    if (filtreNiveau == null) filtreNiveau = "tous";
    if (filtreResultat == null) filtreResultat = "tous";
    if (tri == null) tri = "date";
    if (themes == null) themes = new java.util.ArrayList<String>();
    if (stats == null) stats = new java.util.LinkedHashMap<String, Object>();
    if (urlPagination == null) urlPagination = "/admin/resultats?";

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new java.util.Locale("fr", "FR"));
    SimpleDateFormat sdfJour = new SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("fr", "FR"));

    request.setAttribute("pageTitle", "Résultats des joueurs - Administration - AmijayQuiz");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-resultats.css\">";
    request.setAttribute("extraHead", adminCSS);
%>

<%@ include file="../header.jsp" %>

<div class="admin-resultats-container">

    <%-- ===== BREADCRUMB ===== --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-chart-bar"></i><span>Resultats</span>
        </span>
    </nav>

    <%-- ===== EN-TETE ===== --%>
    <div class="admin-resultats-header">
        <div class="admin-header-left">
            <div class="admin-header-icon">
                <i class="fas fa-chart-bar"></i>
            </div>
            <div class="admin-header-text">
                <h1>Resultats des joueurs</h1>
                <p>Consultez les performances de tous les joueurs</p>
            </div>
        </div>
    </div>

    <%-- ===== STATISTIQUES GLOBALES ===== --%>
    <div class="admin-stats-grid">
        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                <i class="fas fa-users"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre">
                    <%= stats.getOrDefault("totalJoueurs", 0) %>
                </span>
                <span class="admin-stat-label">Joueurs actifs</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                <i class="fas fa-gamepad"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre">
                    <%= stats.getOrDefault("totalParties", 0) %>
                </span>
                <span class="admin-stat-label">Parties jouees</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #27ae60, #2ecc71);">
                <i class="fas fa-check-circle"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre">
                    <%= String.format("%.0f", (Double) stats.getOrDefault("tauxReussite", 0.0)) %>%
                </span>
                <span class="admin-stat-label">Taux de reussite</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe);">
                <i class="fas fa-star"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre">
                    <%= String.format("%.1f", (Double) stats.getOrDefault("scoreMoyen", 0.0)) %>%
                </span>
                <span class="admin-stat-label">Score moyen</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #e74c3c, #c0392b);">
                <i class="fas fa-calendar-day"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre">
                    <%= stats.getOrDefault("partiesAujourdhui", 0) %>
                </span>
                <span class="admin-stat-label">Parties aujourd'hui</span>
            </div>
        </div>
    </div>

    <%-- ===== BARRE DE FILTRES ===== --%>
    <div class="admin-filtres-bar">
        <form action="${pageContext.request.contextPath}/admin/resultats" method="GET" class="form-filtres" id="formFiltres">
            <div class="recherche-input-wrapper">
                <i class="fas fa-search"></i>
                <input type="text" name="recherche" value="<%= recherche %>" 
                       placeholder="Rechercher un joueur ou un quiz..." 
                       class="recherche-input" id="rechercheInput">
            </div>

            <select name="theme" class="filtre-select" onchange="document.getElementById('formFiltres').submit()">
                <option value="tous" <%= "tous".equals(filtreTheme) ? "selected" : "" %>>Tous les themes</option>
                <% for (String theme : themes) { %>
                    <option value="<%= theme %>" <%= theme.equals(filtreTheme) ? "selected" : "" %>><%= theme %></option>
                <% } %>
            </select>

            <select name="niveau" class="filtre-select" onchange="document.getElementById('formFiltres').submit()">
                <option value="tous" <%= "tous".equals(filtreNiveau) ? "selected" : "" %>>Tous les niveaux</option>
                <option value="elementaire" <%= "elementaire".equals(filtreNiveau) ? "selected" : "" %>>Elementaire</option>
                <option value="moyen" <%= "moyen".equals(filtreNiveau) ? "selected" : "" %>>Moyen</option>
                <option value="avance" <%= "avance".equals(filtreNiveau) ? "selected" : "" %>>Avance</option>
            </select>

            <select name="resultat" class="filtre-select" onchange="document.getElementById('formFiltres').submit()">
                <option value="tous" <%= "tous".equals(filtreResultat) ? "selected" : "" %>>Tous les resultats</option>
                <option value="reussies" <%= "reussies".equals(filtreResultat) ? "selected" : "" %>>Reussies</option>
                <option value="echouees" <%= "echouees".equals(filtreResultat) ? "selected" : "" %>>Echouees</option>
            </select>

            <select name="tri" class="filtre-select" onchange="document.getElementById('formFiltres').submit()">
                <option value="date" <%= "date".equals(tri) ? "selected" : "" %>>Tri : Date</option>
                <option value="score" <%= "score".equals(tri) ? "selected" : "" %>>Tri : Score</option>
                <option value="joueur" <%= "joueur".equals(tri) ? "selected" : "" %>>Tri : Joueur</option>
                <option value="quiz" <%= "quiz".equals(tri) ? "selected" : "" %>>Tri : Quiz</option>
            </select>

            <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreTheme) || !"tous".equals(filtreNiveau) || !"tous".equals(filtreResultat)) { %>
                <a href="${pageContext.request.contextPath}/admin/resultats" class="btn-reset-filtres">
                    <i class="fas fa-times"></i> Reinitialiser
                </a>
            <% } %>
        </form>
    </div>

    <%-- ===== TABLEAU DES RÉSULTATS ===== --%>
    <div class="admin-tableau-section">
        <div class="tableau-header">
            <span class="resultat-count">
                <i class="fas fa-list"></i>
                <strong><%= totalResultats %></strong> resultat<%= totalResultats > 1 ? "s" : "" %> trouve<%= totalResultats > 1 ? "s" : "" %>
            </span>
        </div>

        <% if (resultats != null && !resultats.isEmpty()) { %>
            <div class="table-responsive">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th class="th-id">#</th>
                            <th>Joueur</th>
                            <th>Quiz</th>
                            <th class="th-center">Niveau</th>
                            <th class="th-center">Score</th>
                            <th class="th-center">Temps</th>
                            <th class="th-center">Date</th>
                            <th class="th-center">Statut</th>
                            <th class="th-action">Details</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            int index = (pageActuelle - 1) * 20 + 1;
                            for (Map<String, Object> row : resultats) {
                                int idJoueur = (int) row.get("id_joueur");
                                int idQuiz = (int) row.get("id_quiz");
                                String pseudo = (String) row.get("pseudo_joueur");
                                String titreQuiz = (String) row.get("titre_quiz");
                                String niveauQuiz = (String) row.get("niveau_quiz");
                                int score = (int) row.get("score");
                                int totalQuestions = (int) row.get("total_questions");
                                double pourcentage = (double) row.get("pourcentage");
                                String tempsFormate = (String) row.get("temps_formate");
                                Timestamp datePartie = (Timestamp) row.get("date_partie");
                                boolean reussi = (boolean) row.get("reussi");
                                String dateFormatee = datePartie != null ? sdf.format(datePartie) : "N/A";

                                if (pseudo == null) pseudo = "N/A";
                                if (titreQuiz == null) titreQuiz = "N/A";
                                if (titreQuiz.length() > 40) titreQuiz = titreQuiz.substring(0, 40) + "...";

                                String badgeClass = "";
                                if (pourcentage >= 80) badgeClass = "badge-excellent";
                                else if (pourcentage >= 60) badgeClass = "badge-bon";
                                else if (pourcentage >= 40) badgeClass = "badge-moyen";
                                else badgeClass = "badge-faible";

                                String niveauFormate = "";
                                if ("elementaire".equals(niveauQuiz)) niveauFormate = "Elementaire";
                                else if ("moyen".equals(niveauQuiz)) niveauFormate = "Moyen";
                                else if ("avance".equals(niveauQuiz)) niveauFormate = "Avance";
                                else niveauFormate = "N/A";
                        %>
                            <tr>
                                <td class="td-id"><%= index %></td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/admin/resultats/joueur?id=<%= idJoueur %>" 
                                       class="cell-joueur-link" title="Voir le detail de <%= pseudo %>">
                                        <div class="cell-joueur">
                                            <div class="joueur-avatar">
                                                <i class="fas fa-user"></i>
                                            </div>
                                            <span><%= pseudo %></span>
                                        </div>
                                    </a>
                                </td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/admin/resultats/quiz?id=<%= idQuiz %>" 
                                       class="cell-quiz-link" title="Voir le detail du quiz">
                                        <%= titreQuiz %>
                                    </a>
                                </td>
                                <td class="td-center">
                                    <span class="badge-niveau badge-niveau-<%= niveauQuiz %>">
                                        <%= niveauFormate %>
                                    </span>
                                </td>
                                <td class="td-center">
                                    <span class="badge-score <%= badgeClass %>">
                                        <%= score %>/<%= totalQuestions %>
                                        <small>(<%= String.format("%.0f", pourcentage) %>%)</small>
                                    </span>
                                </td>
                                <td class="td-center">
                                    <span class="cell-temps">
                                        <i class="far fa-clock"></i> <%= tempsFormate %>
                                    </span>
                                </td>
                                <td class="td-center">
                                    <span class="cell-date"><%= dateFormatee %></span>
                                </td>
                                <td class="td-center">
                                    <span class="badge-statut <%= reussi ? "badge-reussi" : "badge-echoue" %>">
                                        <i class="fas <%= reussi ? "fa-check-circle" : "fa-times-circle" %>"></i>
                                        <%= reussi ? "Reussi" : "Echoue" %>
                                    </span>
                                </td>
                                <td class="td-actions">
                                    <a href="${pageContext.request.contextPath}/admin/resultats/joueur?id=<%= idJoueur %>" 
                                       class="btn-action-icon btn-voir-joueur" title="Voir le joueur">
                                        <i class="fas fa-user"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/resultats/quiz?id=<%= idQuiz %>" 
                                       class="btn-action-icon btn-voir-quiz" title="Voir le quiz">
                                        <i class="fas fa-eye"></i>
                                    </a>
                                </td>
                            </tr>
                        <%
                                index++;
                            }
                        %>
                    </tbody>
                </table>
            </div>

            <%-- ===== PAGINATION ===== --%>
            <% if (totalPages > 1) { %>
                <div class="pagination-bar">
                    <% if (pageActuelle > 1) { %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= pageActuelle - 1 %>" 
                           class="page-link" title="Page precedente">
                            <i class="fas fa-chevron-left"></i>
                        </a>
                    <% } else { %>
                        <span class="page-link disabled">
                            <i class="fas fa-chevron-left"></i>
                        </span>
                    <% } %>

                    <%
                        int debutPage = Math.max(1, pageActuelle - 2);
                        int finPage = Math.min(totalPages, pageActuelle + 2);

                        if (debutPage > 1) {
                    %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=1" class="page-link">1</a>
                        <% if (debutPage > 2) { %>
                            <span class="page-ellipsis">...</span>
                        <% } %>
                    <% } %>

                    <% for (int i = debutPage; i <= finPage; i++) { %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= i %>" 
                           class="page-link <%= i == pageActuelle ? "active" : "" %>">
                            <%= i %>
                        </a>
                    <% } %>

                    <% if (finPage < totalPages) { %>
                        <% if (finPage < totalPages - 1) { %>
                            <span class="page-ellipsis">...</span>
                        <% } %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= totalPages %>" 
                           class="page-link"><%= totalPages %></a>
                    <% } %>

                    <% if (pageActuelle < totalPages) { %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= pageActuelle + 1 %>" 
                           class="page-link" title="Page suivante">
                            <i class="fas fa-chevron-right"></i>
                        </a>
                    <% } else { %>
                        <span class="page-link disabled">
                            <i class="fas fa-chevron-right"></i>
                        </span>
                    <% } %>

                    <span class="page-info">
                        <%= totalResultats %> resultats - Page <%= pageActuelle %>/<%= totalPages %>
                    </span>
                </div>
            <% } %>

        <% } else { %>
            <div class="empty-state">
                <div class="empty-icon">
                    <i class="fas fa-inbox"></i>
                </div>
                <h3>Aucun resultat trouve</h3>
                <p>
                    <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreTheme) || !"tous".equals(filtreNiveau) || !"tous".equals(filtreResultat)) { %>
                        Aucun resultat ne correspond a ces filtres. Essayez de les modifier.
                    <% } else { %>
                        Aucune partie n'a encore ete jouee sur la plateforme.
                    <% } %>
                </p>
            </div>
        <% } %>
    </div>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="admin-back">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn-retour">
            <i class="fas fa-arrow-left"></i> Retour au dashboard admin
        </a>
    </div>

</div>

<%
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-resultats.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../footer-connecte.jsp" %>