<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.learnpath.models.Quiz" %>

<%-- ============================================================
     FICHIER : views/admin/admin-resultats-quiz.jsp
     RÔLE    : Page de détail d'un quiz
              - Informations du quiz
              - Statistiques du quiz
              - Distribution des scores
              - Classement des joueurs
     ============================================================ --%>

<%
    Quiz quiz = (Quiz) request.getAttribute("quiz");
    Map<String, Object> statsQuiz = (Map<String, Object>) request.getAttribute("statsQuiz");
    List<Map<String, Object>> distributionScores = (List<Map<String, Object>>) request.getAttribute("distributionScores");
    List<Map<String, Object>> classement = (List<Map<String, Object>>) request.getAttribute("classement");

    if (quiz == null) {
        response.sendRedirect(request.getContextPath() + "/admin/resultats");
        return;
    }

    if (statsQuiz == null) statsQuiz = new java.util.LinkedHashMap<String, Object>();
    if (distributionScores == null) distributionScores = new java.util.ArrayList<Map<String, Object>>();
    if (classement == null) classement = new java.util.ArrayList<Map<String, Object>>();

    SimpleDateFormat sdfCourt = new SimpleDateFormat("dd/MM/yyyy HH:mm", new java.util.Locale("fr", "FR"));

    // Calculer le max pour la distribution
    int maxJoueurs = 0;
    for (Map<String, Object> row : distributionScores) {
        int nb = (int) row.get("nbJoueurs");
        if (nb > maxJoueurs) maxJoueurs = nb;
    }

    request.setAttribute("pageTitle", "Detail quiz - " + quiz.getTitre() + " - Administration");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-resultats-quiz.css\">"
                    + "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-resultats.css\">";
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
        <a href="${pageContext.request.contextPath}/admin/resultats" class="breadcrumb-link">
            <i class="fas fa-chart-bar"></i><span>Resultats</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-eye"></i><span><%= quiz.getTitre() %></span>
        </span>
    </nav>

    <%-- ===== EN-TETE QUIZ ===== --%>
    <div class="quiz-detail-header">
        <div class="quiz-detail-top">
            <a href="${pageContext.request.contextPath}/admin/resultats" class="btn-retour-quiz">
                <i class="fas fa-arrow-left"></i> Retour aux resultats
            </a>
            <div class="quiz-detail-info">
                <div class="quiz-detail-icon">
                    <i class="<%= quiz.getIconeNiveau() %>"></i>
                </div>
                <div class="quiz-detail-text">
                    <h1><%= quiz.getTitre() %></h1>
                    <div class="quiz-detail-meta">
                        <span class="badge-niveau badge-niveau-<%= quiz.getNiveau() %>">
                            <i class="<%= quiz.getIconeNiveau() %>"></i>
                            <%= quiz.getNiveauFormate() %>
                        </span>
                        <span class="quiz-meta-item">
                            <i class="fas fa-hashtag"></i> Partie <%= quiz.getOrdrePartie() %>
                        </span>
                        <span class="quiz-meta-item">
                            <i class="far fa-clock"></i> <%= quiz.getDureeMaxMinutes() %> min
                        </span>
                        <span class="quiz-meta-item">
                            <i class="fas fa-question-circle"></i> <%= quiz.getNombreQuestions() %> questions
                        </span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <%-- ===== STATISTIQUES DU QUIZ ===== --%>
    <div class="admin-stats-grid">
        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                <i class="fas fa-users"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= statsQuiz.getOrDefault("nbJoueurs", 0) %></span>
                <span class="admin-stat-label">Joueurs</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #27ae60, #2ecc71);">
                <i class="fas fa-check-circle"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= String.format("%.0f", (Double) statsQuiz.getOrDefault("tauxReussite", 0.0)) %>%</span>
                <span class="admin-stat-label">Taux de reussite</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                <i class="fas fa-star"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= String.format("%.1f", (Double) statsQuiz.getOrDefault("scoreMoyen", 0.0)) %>%</span>
                <span class="admin-stat-label">Score moyen</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #e74c3c, #c0392b);">
                <i class="fas fa-trophy"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= statsQuiz.getOrDefault("meilleurScore", 0) %>/10</span>
                <span class="admin-stat-label">Meilleur score</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #e74c3c, #c0392b);">
                <i class="fas fa-times-circle"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= String.format("%.0f", (Double) statsQuiz.getOrDefault("tauxEchec", 0.0)) %>%</span>
                <span class="admin-stat-label">Taux d'echec</span>
            </div>
        </div>
    </div>

    <%-- ===== DISPOSITION 2 COLONNES ===== --%>
    <div class="quiz-detail-layout">

        <%-- ============================================================
             COLONNE GAUCHE : DISTRIBUTION DES SCORES
             ============================================================ --%>
        <div class="detail-col detail-col-gauche">
            <div class="detail-card">
                <div class="card-header">
                    <div class="card-header-icon">
                        <i class="fas fa-chart-simple"></i>
                    </div>
                    <h2>Distribution des scores</h2>
                </div>
                <div class="card-body">
                    <% if (distributionScores != null && !distributionScores.isEmpty()) { %>
                        <div class="distribution-liste">
                            <%
                                for (Map<String, Object> row : distributionScores) {
                                    int score = (int) row.get("score");
                                    int nbJoueurs = (int) row.get("nbJoueurs");
                                    int barWidth = maxJoueurs > 0 ? (nbJoueurs * 100 / maxJoueurs) : 0;
                                    String barColor = score >= 8 ? "#27ae60" : "#e74c3c";
                            %>
                                <div class="distribution-item">
                                    <div class="distribution-label">
                                        <span class="distribution-score"><%= score %>/10</span>
                                        <span class="distribution-joueurs"><%= nbJoueurs %> joueur<%= nbJoueurs > 1 ? "s" : "" %></span>
                                    </div>
                                    <div class="distribution-bar-outer">
                                        <div class="distribution-bar-inner" style="width: <%= barWidth %>%; background: <%= barColor %>;"></div>
                                    </div>
                                </div>
                            <%
                                }
                            %>
                        </div>
                    <% } else { %>
                        <div class="empty-mini">
                            <i class="fas fa-inbox"></i>
                            <p>Aucune donnee de distribution</p>
                        </div>
                    <% } %>
                </div>
            </div>
        </div>

        <%-- ============================================================
             COLONNE DROITE : CLASSEMENT DES JOUEURS
             ============================================================ --%>
        <div class="detail-col detail-col-droite">
            <div class="detail-card">
                <div class="card-header">
                    <div class="card-header-icon">
                        <i class="fas fa-trophy"></i>
                    </div>
                    <h2>Classement</h2>
                    <span class="badge-count"><%= classement != null ? classement.size() : 0 %> joueurs</span>
                </div>
                <div class="card-body">
                    <% if (classement != null && !classement.isEmpty()) { %>
                        <div class="table-responsive">
                            <table class="admin-table">
                                <thead>
                                    <tr>
                                        <th class="th-id">#</th>
                                        <th>Joueur</th>
                                        <th class="th-center">Score</th>
                                        <th class="th-center">Temps</th>
                                        <th class="th-center">Date</th>
                                        <th class="th-center">Statut</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <%
                                        for (Map<String, Object> row : classement) {
                                            int position = (int) row.get("position");
                                            int idJoueur = (int) row.get("id_joueur");
                                            String pseudo = (String) row.get("pseudo");
                                            int score = (int) row.get("score");
                                            int totalQuestions = (int) row.get("total_questions");
                                            double pourcentage = (double) row.get("pourcentage");
                                            String tempsFormate = (String) row.get("temps_formate");
                                            Timestamp datePartie = (Timestamp) row.get("date_partie");
                                            boolean reussi = (boolean) row.get("reussi");
                                            String dateFormatee = datePartie != null ? sdfCourt.format(datePartie) : "N/A";

                                            if (pseudo == null) pseudo = "N/A";

                                            String medaille = "";
                                            if (position == 1) medaille = "🥇";
                                            else if (position == 2) medaille = "🥈";
                                            else if (position == 3) medaille = "🥉";

                                            String badgeClass = "";
                                            if (pourcentage >= 80) badgeClass = "badge-excellent";
                                            else if (pourcentage >= 60) badgeClass = "badge-bon";
                                            else if (pourcentage >= 40) badgeClass = "badge-moyen";
                                            else badgeClass = "badge-faible";
                                    %>
                                        <tr class="<%= position <= 3 ? "ligne-podium" : "" %>">
                                            <td class="td-id">
                                                <% if (!medaille.isEmpty()) { %>
                                                    <span class="medaille"><%= medaille %></span>
                                                <% } else { %>
                                                    <%= position %>
                                                <% } %>
                                            </td>
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
                                            <td class="td-center">
                                                <span class="badge-score <%= badgeClass %>">
                                                    <%= score %>/<%= totalQuestions %>
                                                    <small>(<%= String.format("%.0f", pourcentage) %>%)</small>
                                                </span>
                                            </td>
                                            <td class="td-center">
                                                <span class="cell-temps"><i class="far fa-clock"></i> <%= tempsFormate %></span>
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
                                        </tr>
                                    <%
                                        }
                                    %>
                                </tbody>
                            </table>
                        </div>
                    <% } else { %>
                        <div class="empty-mini">
                            <i class="fas fa-inbox"></i>
                            <p>Aucun joueur n'a encore joue ce quiz</p>
                        </div>
                    <% } %>
                </div>
            </div>
        </div>

    </div>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="admin-back">
        <a href="${pageContext.request.contextPath}/admin/resultats" class="btn-retour">
            <i class="fas fa-arrow-left"></i> Retour aux resultats
        </a>
    </div>

</div>

<%
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-resultats.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../footer-connecte.jsp" %>