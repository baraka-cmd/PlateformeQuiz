<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%-- ============================================================
     FICHIER : views/admin/admin-resultats-joueur.jsp
     RÔLE    : Page de détail d'un joueur
              - Informations personnelles
              - Statistiques individuelles
              - Progression par thème
              - Historique complet des parties
     ============================================================ --%>

<%
    Map<String, Object> infosJoueur = (Map<String, Object>) request.getAttribute("infosJoueur");
    Map<String, Object> statsJoueur = (Map<String, Object>) request.getAttribute("statsJoueur");
    List<Map<String, Object>> progressionThemes = (List<Map<String, Object>>) request.getAttribute("progressionThemes");
    List<Map<String, Object>> historique = (List<Map<String, Object>>) request.getAttribute("historique");

    if (infosJoueur == null || infosJoueur.isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/admin/resultats");
        return;
    }

    String pseudo = (String) infosJoueur.get("pseudo");
    String email = (String) infosJoueur.get("email");
    Timestamp dateInscription = (Timestamp) infosJoueur.get("date_inscription");
    Boolean premium = (Boolean) infosJoueur.get("premium");
    int joueurId = (int) infosJoueur.get("id");

    if (pseudo == null) pseudo = "N/A";
    if (email == null) email = "Non renseigne";
    if (premium == null) premium = false;

    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new java.util.Locale("fr", "FR"));
    SimpleDateFormat sdfCourt = new SimpleDateFormat("dd/MM/yyyy HH:mm", new java.util.Locale("fr", "FR"));
    String dateInscriptionStr = dateInscription != null ? sdf.format(dateInscription) : "N/A";

    request.setAttribute("pageTitle", "Detail joueur - " + pseudo + " - Administration");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-resultats-joueur.css\">"
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
            <i class="fas fa-user"></i><span><%= pseudo %></span>
        </span>
    </nav>

    <%-- ===== EN-TETE JOUEUR ===== --%>
    <div class="joueur-detail-header">
        <div class="joueur-detail-top">
            <a href="${pageContext.request.contextPath}/admin/resultats" class="btn-retour-quiz">
                <i class="fas fa-arrow-left"></i> Retour aux resultats
            </a>
            <div class="joueur-profil">
                <div class="joueur-profil-avatar">
                    <i class="fas fa-user"></i>
                </div>
                <div class="joueur-profil-info">
                    <h1><%= pseudo %></h1>
                    <div class="joueur-profil-meta">
                        <span class="profil-meta-item">
                            <i class="fas fa-envelope"></i> <%= email %>
                        </span>
                        <span class="profil-meta-item">
                            <i class="fas fa-calendar-alt"></i> Inscrit le <%= dateInscriptionStr %>
                        </span>
                        <span class="profil-meta-item">
                            <i class="fas fa-id-badge"></i> ID : #<%= joueurId %>
                        </span>
                        <% if (premium) { %>
                            <span class="badge-premium-actif">
                                <i class="fas fa-crown"></i> Premium
                            </span>
                        <% } else { %>
                            <span class="badge-premium-inactif">
                                <i class="fas fa-user"></i> Gratuit
                            </span>
                        <% } %>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <%-- ===== STATISTIQUES PERSONNELLES ===== --%>
    <div class="admin-stats-grid">
        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                <i class="fas fa-gamepad"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= statsJoueur.getOrDefault("totalParties", 0) %></span>
                <span class="admin-stat-label">Parties jouees</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #27ae60, #2ecc71);">
                <i class="fas fa-check-circle"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= String.format("%.0f", (Double) statsJoueur.getOrDefault("tauxReussite", 0.0)) %>%</span>
                <span class="admin-stat-label">Taux de reussite</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                <i class="fas fa-trophy"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= String.format("%.0f", (Double) statsJoueur.getOrDefault("meilleurScore", 0.0)) %>%</span>
                <span class="admin-stat-label">Meilleur score</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe);">
                <i class="far fa-clock"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= statsJoueur.getOrDefault("tempsMoyen", "N/A") %></span>
                <span class="admin-stat-label">Temps moyen</span>
            </div>
        </div>

        <div class="admin-stat-card">
            <div class="admin-stat-icon" style="background: linear-gradient(135deg, #e74c3c, #c0392b);">
                <i class="fas fa-layer-group"></i>
            </div>
            <div class="admin-stat-content">
                <span class="admin-stat-nombre"><%= statsJoueur.getOrDefault("nbThemes", 0) %></span>
                <span class="admin-stat-label">Themes joues</span>
            </div>
        </div>
    </div>

    <%-- ===== DISPOSITION 2 COLONNES ===== --%>
    <div class="joueur-detail-layout">

        <%-- ============================================================
             COLONNE GAUCHE : PROGRESSION PAR THÈME
             ============================================================ --%>
        <div class="detail-col detail-col-gauche">
            <div class="detail-card">
                <div class="card-header">
                    <div class="card-header-icon">
                        <i class="fas fa-chart-bar"></i>
                    </div>
                    <h2>Progression par theme</h2>
                </div>
                <div class="card-body">
                    <% if (progressionThemes != null && !progressionThemes.isEmpty()) { %>
                        <div class="progression-liste">
                            <%
                                for (Map<String, Object> theme : progressionThemes) {
                                    String nomTheme = (String) theme.get("theme");
                                    int totalParties = (int) theme.get("totalParties");
                                    int partiesReussies = (int) theme.get("partiesReussies");
                                    int pourcentage = (int) theme.get("pourcentage");
                                    double moyenne = (double) theme.get("moyenne");

                                    String couleur = "#667eea";
                                    if (pourcentage >= 80) couleur = "#27ae60";
                                    else if (pourcentage >= 50) couleur = "#f39c12";
                                    else couleur = "#e74c3c";
                            %>
                                <div class="progression-item">
                                    <div class="progression-item-header">
                                        <strong><%= nomTheme %></strong>
                                        <span><%= partiesReussies %>/<%= totalParties %> reussies</span>
                                    </div>
                                    <div class="progression-bar-outer">
                                        <div class="progression-bar-inner" style="width: <%= pourcentage %>%; background: <%= couleur %>;"></div>
                                    </div>
                                    <div class="progression-item-footer">
                                        <span><%= pourcentage %>%</span>
                                        <span>Moyenne : <%= String.format("%.1f", moyenne) %>%</span>
                                    </div>
                                </div>
                            <%
                                }
                            %>
                        </div>
                    <% } else { %>
                        <div class="empty-mini">
                            <i class="fas fa-inbox"></i>
                            <p>Aucune donnee de progression</p>
                        </div>
                    <% } %>
                </div>
            </div>
        </div>

        <%-- ============================================================
             COLONNE DROITE : HISTORIQUE DES PARTIES
             ============================================================ --%>
        <div class="detail-col detail-col-droite">
            <div class="detail-card">
                <div class="card-header">
                    <div class="card-header-icon">
                        <i class="fas fa-history"></i>
                    </div>
                    <h2>Historique des parties</h2>
                    <span class="badge-count"><%= historique != null ? historique.size() : 0 %> parties</span>
                </div>
                <div class="card-body">
                    <% if (historique != null && !historique.isEmpty()) { %>
                        <div class="table-responsive">
                            <table class="admin-table">
                                <thead>
                                    <tr>
                                        <th>Quiz</th>
                                        <th class="th-center">Score</th>
                                        <th class="th-center">Temps</th>
                                        <th class="th-center">Date</th>
                                        <th class="th-center">Statut</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <%
                                        for (Map<String, Object> row : historique) {
                                            String titreQuiz = (String) row.get("titre_quiz");
                                            int score = (int) row.get("score");
                                            int totalQuestions = (int) row.get("total_questions");
                                            double pourcentage = (double) row.get("pourcentage");
                                            String tempsFormate = (String) row.get("temps_formate");
                                            Timestamp datePartie = (Timestamp) row.get("date_partie");
                                            boolean reussi = (boolean) row.get("reussi");
                                            String dateFormatee = datePartie != null ? sdfCourt.format(datePartie) : "N/A";

                                            if (titreQuiz == null) titreQuiz = "N/A";
                                            if (titreQuiz.length() > 35) titreQuiz = titreQuiz.substring(0, 35) + "...";

                                            String badgeClass = "";
                                            if (pourcentage >= 80) badgeClass = "badge-excellent";
                                            else if (pourcentage >= 60) badgeClass = "badge-bon";
                                            else if (pourcentage >= 40) badgeClass = "badge-moyen";
                                            else badgeClass = "badge-faible";
                                    %>
                                        <tr>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/admin/resultats/quiz?id=<%= row.get("id_quiz") %>" 
                                                   class="cell-quiz-link" title="Voir le detail du quiz">
                                                    <%= titreQuiz %>
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
                            <p>Aucune partie jouee</p>
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
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-resultats-joueur.js\"></script>"
                   + "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-resultats.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../footer-connecte.jsp" %>