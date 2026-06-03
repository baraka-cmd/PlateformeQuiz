<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Partie" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%-- ============================================================
     Fichier : historique.jsp
     Rôle    : Page d'historique des parties du joueur
              - Statistiques globales (total, réussies, échouées, taux)
              - Statistiques avancées (meilleur score, moyenne, temps)
              - Filtres par thème, niveau, résultat
              - Tableau paginé des parties avec actions
              - Pagination complète
              - Bouton "Rejouer" pour chaque partie
              - État vide avec message adapté
     ============================================================ --%>

<%
    // Récupération des attributs depuis le servlet
    List<Partie> parties = (List<Partie>) request.getAttribute("parties");
    Integer totalParties = (Integer) request.getAttribute("totalParties");
    Integer partiesReussies = (Integer) request.getAttribute("partiesReussies");
    Integer partiesEchouees = (Integer) request.getAttribute("partiesEchouees");
    Double tauxReussite = (Double) request.getAttribute("tauxReussite");
    Double moyenneGenerale = (Double) request.getAttribute("moyenneGenerale");
    Integer meilleurScoreGlobal = (Integer) request.getAttribute("meilleurScoreGlobal");
    String meilleurQuiz = (String) request.getAttribute("meilleurQuiz");
    Integer tempsTotalMinutes = (Integer) request.getAttribute("tempsTotalMinutes");
    List<String> themes = (List<String>) request.getAttribute("themes");
    Integer pageActuelle = (Integer) request.getAttribute("pageActuelle");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    Integer totalPartiesFiltrees = (Integer) request.getAttribute("totalPartiesFiltrees");
    String filtreTheme = (String) request.getAttribute("filtreTheme");
    String filtreNiveau = (String) request.getAttribute("filtreNiveau");
    String filtreReussite = (String) request.getAttribute("filtreReussite");
    
    // Valeurs par défaut
    if (filtreTheme == null) filtreTheme = "tous";
    if (filtreNiveau == null) filtreNiveau = "tous";
    if (filtreReussite == null) filtreReussite = "tous";
    if (totalParties == null) totalParties = 0;
    if (partiesReussies == null) partiesReussies = 0;
    if (partiesEchouees == null) partiesEchouees = 0;
    if (tauxReussite == null) tauxReussite = 0.0;
    if (moyenneGenerale == null) moyenneGenerale = 0.0;
    if (meilleurScoreGlobal == null) meilleurScoreGlobal = 0;
    if (tempsTotalMinutes == null) tempsTotalMinutes = 0;
    if (pageActuelle == null) pageActuelle = 1;
    if (totalPages == null) totalPages = 1;
    if (totalPartiesFiltrees == null) totalPartiesFiltrees = 0;
    
    // Formatages
    String tauxReussiteStr = String.format("%.0f%%", tauxReussite);
    String moyenneGeneraleStr = String.format("%.1f%%", moyenneGenerale);
    
    // URL de base pour la pagination
    String baseUrl = request.getContextPath() + "/joueur/historique?";
    String params = "";
    if (filtreTheme != null && !"tous".equals(filtreTheme)) params += "&theme=" + java.net.URLEncoder.encode(filtreTheme, "UTF-8");
    if (filtreNiveau != null && !"tous".equals(filtreNiveau)) params += "&niveau=" + filtreNiveau;
    if (filtreReussite != null && !"tous".equals(filtreReussite)) params += "&reussite=" + filtreReussite;
    
    // Titre de la page
    request.setAttribute("pageTitle", "Historique des parties - AmijayQuiz");
%>

<%-- ============================================================
     STYLES SPÉCIFIQUES
     ============================================================ --%>
<%
    String historiqueCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/historique.css\">";
    request.setAttribute("extraHead", historiqueCSS);
%>

<%@ include file="../header.jsp" %>



<div class="historique-container">

    <%-- ============================================================
         BREADCRUMB
         ============================================================ --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i>
            <span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-history"></i>
            <span>Historique</span>
        </span>
    </nav>

    <%-- ============================================================
         EN-TÊTE
         ============================================================ --%>
    <header class="historique-header">
        <div class="header-content">
            <div class="header-icon">
                <i class="fas fa-history"></i>
            </div>
            <div class="header-text">
                <h1>Historique des parties</h1>
                <p>Consultez toutes vos performances et suivez votre progression</p>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="header-back-btn">
            <i class="fas fa-arrow-left"></i>
            <span>Dashboard</span>
        </a>
    </header>

    <%-- ============================================================
         STATISTIQUES PRINCIPALES (4 CARTES)
         ============================================================ --%>
    <section class="stats-section">
        <div class="stats-grid-historique">
            <div class="stat-card-h">
                <div class="stat-icon-h stat-icon-total">
                    <i class="fas fa-list-ol"></i>
                </div>
                <div class="stat-content-h">
                    <span class="stat-number-h"><%= totalParties %></span>
                    <span class="stat-label-h">Total parties</span>
                </div>
                <div class="stat-trend-h stat-trend-neutral">
                    <i class="fas fa-gamepad"></i>
                </div>
            </div>

            <div class="stat-card-h">
                <div class="stat-icon-h stat-icon-reussies">
                    <i class="fas fa-check-circle"></i>
                </div>
                <div class="stat-content-h">
                    <span class="stat-number-h"><%= partiesReussies %></span>
                    <span class="stat-label-h">Parties réussies</span>
                </div>
                <div class="stat-trend-h <%= partiesReussies > partiesEchouees ? "stat-trend-up" : "stat-trend-neutral" %>">
                    <i class="fas <%= partiesReussies > partiesEchouees ? "fa-arrow-up" : "fa-minus" %>"></i>
                </div>
            </div>

            <div class="stat-card-h">
                <div class="stat-icon-h stat-icon-taux">
                    <i class="fas fa-chart-pie"></i>
                </div>
                <div class="stat-content-h">
                    <span class="stat-number-h"><%= tauxReussiteStr %></span>
                    <span class="stat-label-h">Taux de réussite</span>
                </div>
                <div class="stat-trend-h <%= tauxReussite >= 50 ? "stat-trend-up" : "stat-trend-down" %>">
                    <i class="fas <%= tauxReussite >= 50 ? "fa-arrow-up" : "fa-arrow-down" %>"></i>
                </div>
            </div>

            <div class="stat-card-h">
                <div class="stat-icon-h stat-icon-temps">
                    <i class="fas fa-clock"></i>
                </div>
                <div class="stat-content-h">
                    <span class="stat-number-h"><%= tempsTotalMinutes %> min</span>
                    <span class="stat-label-h">Temps total</span>
                </div>
                <div class="stat-trend-h stat-trend-neutral">
                    <i class="fas fa-hourglass-half"></i>
                </div>
            </div>
        </div>
    </section>

    <%-- ============================================================
         STATISTIQUES AVANCÉES (3 CARTES)
         ============================================================ --%>
    <section class="stats-advanced">
        <div class="stats-grid-advanced">
            <div class="stat-card-adv">
                <div class="stat-icon-adv" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                    <i class="fas fa-trophy"></i>
                </div>
                <div class="stat-content-adv">
                    <span class="stat-number-adv"><%= meilleurScoreGlobal %>%</span>
                    <span class="stat-label-adv">Meilleur score</span>
                </div>
                <% if (meilleurQuiz != null && !meilleurQuiz.isEmpty()) { %>
                    <span class="stat-detail-adv" title="<%= meilleurQuiz %>">
                        <i class="fas fa-question-circle"></i> <%= meilleurQuiz.length() > 25 ? meilleurQuiz.substring(0, 25) + "..." : meilleurQuiz %>
                    </span>
                <% } %>
            </div>

            <div class="stat-card-adv">
                <div class="stat-icon-adv" style="background: linear-gradient(135deg, #9b59b6, #8e44ad);">
                    <i class="fas fa-calculator"></i>
                </div>
                <div class="stat-content-adv">
                    <span class="stat-number-adv"><%= moyenneGeneraleStr %></span>
                    <span class="stat-label-adv">Moyenne générale</span>
                </div>
            </div>

            <div class="stat-card-adv">
                <div class="stat-icon-adv" style="background: linear-gradient(135deg, #e74c3c, #c0392b);">
                    <i class="fas fa-times-circle"></i>
                </div>
                <div class="stat-content-adv">
                    <span class="stat-number-adv"><%= partiesEchouees %></span>
                    <span class="stat-label-adv">Parties échouées</span>
                </div>
            </div>
        </div>
    </section>

    <%-- ============================================================
         FILTRES
         ============================================================ --%>
    <section class="filtres-section">
        <form action="${pageContext.request.contextPath}/joueur/historique" method="GET" class="filtres-bar">
            <div class="filtre-group">
                <label for="filtreTheme">
                    <i class="fas fa-book"></i> Thème
                </label>
                <select name="theme" id="filtreTheme" class="filtre-select">
                    <option value="tous" <%= "tous".equals(filtreTheme) ? "selected" : "" %>>Tous les thèmes</option>
                    <% if (themes != null) { 
                        for (String theme : themes) { %>
                            <option value="<%= theme %>" <%= theme.equals(filtreTheme) ? "selected" : "" %>><%= theme %></option>
                    <% } } %>
                </select>
            </div>

            <div class="filtre-group">
                <label for="filtreNiveau">
                    <i class="fas fa-signal"></i> Niveau
                </label>
                <select name="niveau" id="filtreNiveau" class="filtre-select">
                    <option value="tous" <%= "tous".equals(filtreNiveau) ? "selected" : "" %>>Tous les niveaux</option>
                    <option value="elementaire" <%= "elementaire".equals(filtreNiveau) ? "selected" : "" %>>Élémentaire</option>
                    <option value="moyen" <%= "moyen".equals(filtreNiveau) ? "selected" : "" %>>Moyen</option>
                    <option value="avance" <%= "avance".equals(filtreNiveau) ? "selected" : "" %>>Avancé</option>
                </select>
            </div>

            <div class="filtre-group">
                <label for="filtreReussite">
                    <i class="fas fa-filter"></i> Résultat
                </label>
                <select name="reussite" id="filtreReussite" class="filtre-select">
                    <option value="tous" <%= "tous".equals(filtreReussite) ? "selected" : "" %>>Tous</option>
                    <option value="reussies" <%= "reussies".equals(filtreReussite) ? "selected" : "" %>>Réussies</option>
                    <option value="echouees" <%= "echouees".equals(filtreReussite) ? "selected" : "" %>>Échouées</option>
                </select>
            </div>

            <div class="filtre-actions">
                <button type="submit" class="btn-filtrer">
                    <i class="fas fa-search"></i>
                    <span>Filtrer</span>
                </button>
                <a href="${pageContext.request.contextPath}/joueur/historique" class="btn-reset-filtres">
                    <i class="fas fa-undo"></i>
                    <span>Réinitialiser</span>
                </a>
            </div>
        </form>
    </section>

    <%-- ============================================================
         TABLEAU DES PARTIES
         ============================================================ --%>
    <section class="tableau-section">
        <div class="tableau-header">
            <span class="resultat-count">
                <i class="fas fa-list"></i>
                <strong><%= totalPartiesFiltrees %></strong> résultat<%= totalPartiesFiltrees > 1 ? "s" : "" %> trouvé<%= totalPartiesFiltrees > 1 ? "s" : "" %>
            </span>
        </div>

        <div class="tableau-historique">
            <% if (parties != null && !parties.isEmpty()) { %>
                <div class="table-responsive">
                    <table class="table-h">
                        <thead>
                            <tr>
                                <th><i class="fas fa-file-lines"></i> Quiz</th>
                                <th class="th-center"><i class="fas fa-star"></i> Score</th>
                                <th class="th-center"><i class="fas fa-flag-checkered"></i> Résultat</th>
                                <th class="th-center"><i class="fas fa-clock"></i> Temps</th>
                                <th class="th-center"><i class="fas fa-calendar"></i> Date</th>
                                <th class="th-center"><i class="fas fa-play"></i> Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% 
                                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", new java.util.Locale("fr", "FR"));
                                for (Partie partie : parties) { 
                                    double pourcentage = partie.getPourcentage();
                                    String badgeClass;
                                    String badgeIcon;
                                    if (pourcentage >= 80) {
                                        badgeClass = "badge-excellent";
                                        badgeIcon = "fa-check-circle";
                                    } else if (pourcentage >= 60) {
                                        badgeClass = "badge-bon";
                                        badgeIcon = "fa-check-circle";
                                    } else if (pourcentage >= 40) {
                                        badgeClass = "badge-moyen";
                                        badgeIcon = "fa-minus-circle";
                                    } else {
                                        badgeClass = "badge-faible";
                                        badgeIcon = "fa-times-circle";
                                    }
                                    boolean reussi = pourcentage >= 80;
                                    String dateFormatee = sdf.format(partie.getDatePartie());
                            %>
                                <tr>
                                    <td>
                                        <div class="quiz-cell">
                                            <i class="fas fa-question-circle quiz-cell-icon"></i>
                                            <span class="quiz-cell-title"><%= partie.getTitreQuiz() %></span>
                                        </div>
                                    </td>
                                    <td class="td-center">
                                        <span class="badge-score-h <%= badgeClass %>">
                                            <i class="fas <%= badgeIcon %>"></i>
                                            <%= partie.getScore() %>/<%= partie.getTotalQuestions() %>
                                            <small>(<%= String.format("%.0f%%", pourcentage) %>)</small>
                                        </span>
                                    </td>
                                    <td class="td-center">
                                        <span class="badge-reussite-h <%= reussi ? "badge-reussi" : "badge-echoue" %>">
                                            <i class="fas <%= reussi ? "fa-check-circle" : "fa-times-circle" %>"></i>
                                            <%= reussi ? "Réussi" : "Échoué" %>
                                        </span>
                                    </td>
                                    <td class="td-center">
                                        <span class="cell-icon-text">
                                            <i class="far fa-clock"></i>
                                            <%= partie.getTempsFormate() %>
                                        </span>
                                    </td>
                                    <td class="td-center">
                                        <span class="cell-icon-text">
                                            <i class="far fa-calendar-alt"></i>
                                            <%= dateFormatee %>
                                        </span>
                                    </td>
                                    <td class="td-center">
                                        <a href="${pageContext.request.contextPath}/joueur/demarrer-quiz?id=<%= partie.getIdQuiz() %>" 
                                           class="btn-rejouer" 
                                           title="Rejouer ce quiz"
                                           data-tooltip="Rejouer <%= partie.getTitreQuiz() %>">
                                            <i class="fas fa-redo"></i>
                                        </a>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>

                <%-- Pagination --%>
                <% if (totalPages > 1) { %>
                    <nav class="pagination-bar" aria-label="Pagination">
                        <a href="<%= baseUrl %>page=1<%= params %>" 
                           class="page-link <%= pageActuelle == 1 ? "disabled" : "" %>"
                           title="Première page">
                            <i class="fas fa-angle-double-left"></i>
                        </a>
                        <a href="<%= baseUrl %>page=<%= pageActuelle - 1 %><%= params %>" 
                           class="page-link <%= pageActuelle == 1 ? "disabled" : "" %>"
                           title="Page précédente">
                            <i class="fas fa-angle-left"></i>
                        </a>
                        
                        <% 
                            int debutPage = Math.max(1, pageActuelle - 2);
                            int finPage = Math.min(totalPages, pageActuelle + 2);
                            
                            if (debutPage > 1) { %>
                                <a href="<%= baseUrl %>page=1<%= params %>" class="page-link">1</a>
                                <% if (debutPage > 2) { %>
                                    <span class="page-ellipsis">...</span>
                                <% } %>
                            <% }
                            
                            for (int i = debutPage; i <= finPage; i++) { 
                        %>
                            <a href="<%= baseUrl %>page=<%= i %><%= params %>" 
                               class="page-link <%= i == pageActuelle ? "active" : "" %>"
                               <%= i == pageActuelle ? "aria-current=\"page\"" : "" %>>
                                <%= i %>
                            </a>
                        <% } 
                            if (finPage < totalPages) {
                                if (finPage < totalPages - 1) { %>
                                    <span class="page-ellipsis">...</span>
                                <% } %>
                                <a href="<%= baseUrl %>page=<%= totalPages %><%= params %>" class="page-link"><%= totalPages %></a>
                            <% } %>
                        
                        <a href="<%= baseUrl %>page=<%= pageActuelle + 1 %><%= params %>" 
                           class="page-link <%= pageActuelle == totalPages ? "disabled" : "" %>"
                           title="Page suivante">
                            <i class="fas fa-angle-right"></i>
                        </a>
                        <a href="<%= baseUrl %>page=<%= totalPages %><%= params %>" 
                           class="page-link <%= pageActuelle == totalPages ? "disabled" : "" %>"
                           title="Dernière page">
                            <i class="fas fa-angle-double-right"></i>
                        </a>
                    </nav>
                <% } %>

            <% } else { %>
                <div class="empty-state">
                    <div class="empty-icon">
                        <i class="fas fa-inbox"></i>
                    </div>
                    <h3>Aucune partie trouvée</h3>
                    <p>
                        <% if (!"tous".equals(filtreTheme) || !"tous".equals(filtreNiveau) || !"tous".equals(filtreReussite)) { %>
                            Aucun résultat avec ces filtres. Essayez d'élargir votre recherche.
                        <% } else { %>
                            Vous n'avez pas encore joué de quiz. Lancez votre première partie !
                        <% } %>
                    </p>
                    <a href="${pageContext.request.contextPath}/joueur/dashboard" class="btn-commencer">
                        <i class="fas fa-play"></i> Voir les parcours
                    </a>
                </div>
            <% } %>
        </div>
    </section>

    <%-- ============================================================
         BOUTON RETOUR
         ============================================================ --%>
    <div class="historique-back">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="btn-retour">
            <i class="fas fa-arrow-left"></i>
            <span>Retour au dashboard</span>
        </a>
    </div>

</div>

<%-- ============================================================
     SCRIPTS SPÉCIFIQUES
     ============================================================ --%>
<%
    String historiqueJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/historique.js\"></script>";
    request.setAttribute("extraScripts", historiqueJS);
%>

<%@ include file="../footer-connecte.jsp" %>