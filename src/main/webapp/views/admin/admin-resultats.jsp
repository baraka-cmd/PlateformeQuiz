<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%-- ============================================================
     FICHIER : views/admin/admin-resultats.jsp
     RÔLE    : Page d'administration des résultats des joueurs
              - Onglet Quiz : statistiques, filtres, tableau
              - Onglet Cours : progression, filtres, tableau
              - Liens vers détail joueur et détail quiz/cours
     ============================================================ --%>

<%
    // ===== PARAMÈTRES COMMUNS =====
    String tab = (String) request.getAttribute("tab");
    if (tab == null) tab = "quiz";

    // ===== RÉCUPÉRATION ATTRIBUTS QUIZ =====
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

    // ===== RÉCUPÉRATION ATTRIBUTS COURS =====
    List<Map<String, Object>> progressionCours = (List<Map<String, Object>>) request.getAttribute("progressionCours");
    Integer totalProgression = (Integer) request.getAttribute("totalProgression");
    Integer pageCours = (Integer) request.getAttribute("pageCours");
    Integer totalPagesCours = (Integer) request.getAttribute("totalPagesCours");
    String filtreThemeCours = (String) request.getAttribute("filtreThemeCours");
    String filtreNiveauCours = (String) request.getAttribute("filtreNiveauCours");
    String filtreStatutCours = (String) request.getAttribute("filtreStatutCours");
    String triCours = (String) request.getAttribute("triCours");
    String ordreCours = (String) request.getAttribute("ordreCours");
    List<String> themesCours = (List<String>) request.getAttribute("themesCours");
    Map<String, Object> statsCours = (Map<String, Object>) request.getAttribute("statsCours");
    String urlPaginationCours = (String) request.getAttribute("urlPaginationCours");

    // ===== VALEURS PAR DÉFAUT =====
    if (resultats == null) resultats = new java.util.ArrayList<>();
    if (totalResultats == null) totalResultats = 0;
    if (pageActuelle == null) pageActuelle = 1;
    if (totalPages == null) totalPages = 1;
    if (recherche == null) recherche = "";
    if (filtreTheme == null) filtreTheme = "tous";
    if (filtreNiveau == null) filtreNiveau = "tous";
    if (filtreResultat == null) filtreResultat = "tous";
    if (tri == null) tri = "date";
    if (themes == null) themes = new java.util.ArrayList<>();
    if (stats == null) stats = new java.util.LinkedHashMap<>();
    if (urlPagination == null) urlPagination = "/admin/resultats?";

    if (progressionCours == null) progressionCours = new java.util.ArrayList<>();
    if (totalProgression == null) totalProgression = 0;
    if (pageCours == null) pageCours = 1;
    if (totalPagesCours == null) totalPagesCours = 1;
    if (filtreThemeCours == null) filtreThemeCours = "tous";
    if (filtreNiveauCours == null) filtreNiveauCours = "tous";
    if (filtreStatutCours == null) filtreStatutCours = "tous";
    if (themesCours == null) themesCours = new java.util.ArrayList<>();
    if (statsCours == null) statsCours = new java.util.LinkedHashMap<>();
    if (urlPaginationCours == null) urlPaginationCours = "/admin/resultats?tab=cours&";

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new java.util.Locale("fr", "FR"));
    SimpleDateFormat sdfJour = new SimpleDateFormat("dd/MM/yyyy", new java.util.Locale("fr", "FR"));

    request.setAttribute("pageTitle", "Résultats - Administration - LearnPath");
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
            <i class="fas fa-chart-bar"></i><span>Résultats</span>
        </span>
    </nav>

    <%-- ============================================================
         SYSTÈME D'ONGLETS
         ============================================================ --%>
    <div class="admin-onglets-bar">
        <a href="${pageContext.request.contextPath}/admin/resultats?tab=quiz" 
           class="admin-onglet <%= !"cours".equals(tab) ? "active" : "" %>">
            <i class="fas fa-gamepad"></i>
            <span>Résultats Quiz</span>
        </a>
        <a href="${pageContext.request.contextPath}/admin/resultats?tab=cours" 
           class="admin-onglet <%= "cours".equals(tab) ? "active" : "" %>">
            <i class="fas fa-graduation-cap"></i>
            <span>Progression Cours</span>
        </a>
    </div>

    <%-- ============================================================
         ONGLET QUIZ
         ============================================================ --%>
    <% if (!"cours".equals(tab)) { %>

        <%-- ===== STATISTIQUES QUIZ ===== --%>
        <div class="admin-stats-grid">
            <div class="admin-stat-card">
                <div class="admin-stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                    <i class="fas fa-users"></i>
                </div>
                <div class="admin-stat-content">
                    <span class="admin-stat-nombre"><%= stats.getOrDefault("totalJoueurs", 0) %></span>
                    <span class="admin-stat-label">Joueurs actifs</span>
                </div>
            </div>
            <div class="admin-stat-card">
                <div class="admin-stat-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                    <i class="fas fa-gamepad"></i>
                </div>
                <div class="admin-stat-content">
                    <span class="admin-stat-nombre"><%= stats.getOrDefault("totalParties", 0) %></span>
                    <span class="admin-stat-label">Parties jouées</span>
                </div>
            </div>
            <div class="admin-stat-card">
                <div class="admin-stat-icon" style="background: linear-gradient(135deg, #27ae60, #2ecc71);">
                    <i class="fas fa-check-circle"></i>
                </div>
                <div class="admin-stat-content">
                    <span class="admin-stat-nombre"><%= String.format("%.0f", (Double) stats.getOrDefault("tauxReussite", 0.0)) %>%</span>
                    <span class="admin-stat-label">Taux de réussite</span>
                </div>
            </div>
            <div class="admin-stat-card">
                <div class="admin-stat-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe);">
                    <i class="fas fa-star"></i>
                </div>
                <div class="admin-stat-content">
                    <span class="admin-stat-nombre"><%= String.format("%.1f", (Double) stats.getOrDefault("scoreMoyen", 0.0)) %>%</span>
                    <span class="admin-stat-label">Score moyen</span>
                </div>
            </div>
            <div class="admin-stat-card">
                <div class="admin-stat-icon" style="background: linear-gradient(135deg, #e74c3c, #c0392b);">
                    <i class="fas fa-calendar-day"></i>
                </div>
                <div class="admin-stat-content">
                    <span class="admin-stat-nombre"><%= stats.getOrDefault("partiesAujourdhui", 0) %></span>
                    <span class="admin-stat-label">Parties aujourd'hui</span>
                </div>
            </div>
        </div>

        <%-- ===== BARRE DE FILTRES QUIZ ===== --%>
        <div class="admin-filtres-bar">
            <form action="${pageContext.request.contextPath}/admin/resultats" method="GET" class="form-filtres" id="formFiltres">
                <input type="hidden" name="tab" value="quiz">
                <div class="recherche-input-wrapper">
                    <i class="fas fa-search"></i>
                    <input type="text" name="recherche" value="<%= recherche %>" 
                           placeholder="Rechercher un joueur ou un quiz..." 
                           class="recherche-input" id="rechercheInput">
                </div>
                <select name="theme" id="filtreTheme" class="filtre-select">
                    <option value="tous" <%= "tous".equals(filtreTheme) ? "selected" : "" %>>Tous les thèmes</option>
                    <% for (String theme : themes) { %>
                        <option value="<%= theme %>" <%= theme.equals(filtreTheme) ? "selected" : "" %>><%= theme %></option>
                    <% } %>
                </select>
                <select name="niveau" id="filtreNiveau" class="filtre-select">
                    <option value="tous" <%= "tous".equals(filtreNiveau) ? "selected" : "" %>>Tous les niveaux</option>
                    <option value="elementaire" <%= "elementaire".equals(filtreNiveau) ? "selected" : "" %>>Élémentaire</option>
                    <option value="moyen" <%= "moyen".equals(filtreNiveau) ? "selected" : "" %>>Moyen</option>
                    <option value="avance" <%= "avance".equals(filtreNiveau) ? "selected" : "" %>>Avancé</option>
                </select>
                <select name="resultat" id="filtreResultat" class="filtre-select">
                    <option value="tous" <%= "tous".equals(filtreResultat) ? "selected" : "" %>>Tous les résultats</option>
                    <option value="reussies" <%= "reussies".equals(filtreResultat) ? "selected" : "" %>>Réussies</option>
                    <option value="echouees" <%= "echouees".equals(filtreResultat) ? "selected" : "" %>>Échouées</option>
                </select>
                <select name="tri" id="filtreTri" class="filtre-select">
                    <option value="date" <%= "date".equals(tri) ? "selected" : "" %>>Tri : Date</option>
                    <option value="score" <%= "score".equals(tri) ? "selected" : "" %>>Tri : Score</option>
                    <option value="joueur" <%= "joueur".equals(tri) ? "selected" : "" %>>Tri : Joueur</option>
                    <option value="quiz" <%= "quiz".equals(tri) ? "selected" : "" %>>Tri : Quiz</option>
                </select>
                <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreTheme) || !"tous".equals(filtreNiveau) || !"tous".equals(filtreResultat)) { %>
                    <a href="${pageContext.request.contextPath}/admin/resultats?tab=quiz" class="btn-reset-filtres">
                        <i class="fas fa-undo"></i><span>Réinitialiser</span>
                    </a>
                <% } %>
            </form>
        </div>

        <%-- ===== TABLEAU QUIZ ===== --%>
        <div class="admin-tableau-section">
            <div class="tableau-header">
                <span class="resultat-count">
                    <i class="fas fa-list"></i>
                    <strong><%= totalResultats %></strong> résultat<%= totalResultats > 1 ? "s" : "" %> trouvé<%= totalResultats > 1 ? "s" : "" %>
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
                                <th class="th-action">Détails</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% int index = (pageActuelle - 1) * 20 + 1;
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
                                String badgeClass = pourcentage >= 80 ? "badge-excellent" : (pourcentage >= 60 ? "badge-bon" : (pourcentage >= 40 ? "badge-moyen" : "badge-faible"));
                                String niveauFormate = "elementaire".equals(niveauQuiz) ? "Élémentaire" : ("moyen".equals(niveauQuiz) ? "Moyen" : ("avance".equals(niveauQuiz) ? "Avancé" : "N/A"));
                            %>
                                <tr>
                                    <td class="td-id"><%= index %></td>
                                    <td><a href="${pageContext.request.contextPath}/admin/resultats/joueur?id=<%= idJoueur %>" class="cell-joueur-link"><div class="cell-joueur"><div class="joueur-avatar"><i class="fas fa-user"></i></div><span><%= pseudo %></span></div></a></td>
                                    <td><a href="${pageContext.request.contextPath}/admin/resultats/quiz?id=<%= idQuiz %>" class="cell-quiz-link"><%= titreQuiz %></a></td>
                                    <td class="td-center"><span class="badge-niveau badge-niveau-<%= niveauQuiz %>"><%= niveauFormate %></span></td>
                                    <td class="td-center"><span class="badge-score <%= badgeClass %>"><%= score %>/<%= totalQuestions %> <small>(<%= String.format("%.0f", pourcentage) %>%)</small></span></td>
                                    <td class="td-center"><span class="cell-temps"><i class="far fa-clock"></i> <%= tempsFormate %></span></td>
                                    <td class="td-center"><span class="cell-date"><%= dateFormatee %></span></td>
                                    <td class="td-center"><span class="badge-statut <%= reussi ? "badge-reussi" : "badge-echoue" %>"><i class="fas <%= reussi ? "fa-check-circle" : "fa-times-circle" %>"></i> <%= reussi ? "Réussi" : "Échoué" %></span></td>
                                    <td class="td-actions">
                                        <a href="${pageContext.request.contextPath}/admin/resultats/joueur?id=<%= idJoueur %>" class="btn-action-icon btn-voir-joueur" title="Voir le joueur"><i class="fas fa-user"></i></a>
                                        <a href="${pageContext.request.contextPath}/admin/resultats/quiz?id=<%= idQuiz %>" class="btn-action-icon btn-voir-quiz" title="Voir le quiz"><i class="fas fa-eye"></i></a>
                                    </td>
                                </tr>
                            <% index++; } %>
                        </tbody>
                    </table>
                </div>
                <% if (totalPages > 1) { %>
                    <div class="pagination-bar">
                        <% if (pageActuelle > 1) { %><a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= pageActuelle - 1 %>" class="page-link"><i class="fas fa-chevron-left"></i></a><% } else { %><span class="page-link disabled"><i class="fas fa-chevron-left"></i></span><% } %>
                        <% int debutPage = Math.max(1, pageActuelle - 2); int finPage = Math.min(totalPages, pageActuelle + 2);
                           if (debutPage > 1) { %><a href="${pageContext.request.contextPath}<%= urlPagination %>page=1" class="page-link">1</a><% if (debutPage > 2) { %><span class="page-ellipsis">...</span><% } } %>
                        <% for (int i = debutPage; i <= finPage; i++) { %><a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= i %>" class="page-link <%= i == pageActuelle ? "active" : "" %>"><%= i %></a><% } %>
                        <% if (finPage < totalPages) { if (finPage < totalPages - 1) { %><span class="page-ellipsis">...</span><% } %><a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= totalPages %>" class="page-link"><%= totalPages %></a><% } %>
                        <% if (pageActuelle < totalPages) { %><a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= pageActuelle + 1 %>" class="page-link"><i class="fas fa-chevron-right"></i></a><% } else { %><span class="page-link disabled"><i class="fas fa-chevron-right"></i></span><% } %>
                        <span class="page-info"><%= totalResultats %> résultats - Page <%= pageActuelle %>/<%= totalPages %></span>
                    </div>
                <% } %>
            <% } else { %>
                <div class="empty-state"><div class="empty-icon"><i class="fas fa-inbox"></i></div><h3>Aucun résultat trouvé</h3><p>Aucune partie n'a encore été jouée.</p></div>
            <% } %>
        </div>

    <% } %>

    <%-- ============================================================
         ONGLET COURS
         ============================================================ --%>
    <% if ("cours".equals(tab)) { %>

        <%-- ===== STATISTIQUES COURS ===== --%>
        <div class="admin-stats-grid">
            <div class="admin-stat-card">
                <div class="admin-stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                    <i class="fas fa-users"></i>
                </div>
                <div class="admin-stat-content">
                    <span class="admin-stat-nombre"><%= statsCours.getOrDefault("apprenantsActifs", 0) %></span>
                    <span class="admin-stat-label">Apprenants actifs</span>
                </div>
            </div>
            <div class="admin-stat-card">
                <div class="admin-stat-icon" style="background: linear-gradient(135deg, #27ae60, #2ecc71);">
                    <i class="fas fa-book"></i>
                </div>
                <div class="admin-stat-content">
                    <span class="admin-stat-nombre"><%= statsCours.getOrDefault("totalInscriptions", 0) %></span>
                    <span class="admin-stat-label">Inscriptions aux cours</span>
                </div>
            </div>
            <div class="admin-stat-card">
                <div class="admin-stat-icon" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                    <i class="fas fa-flag-checkered"></i>
                </div>
                <div class="admin-stat-content">
                    <span class="admin-stat-nombre"><%= statsCours.getOrDefault("coursTermines", 0) %></span>
                    <span class="admin-stat-label">Cours terminés</span>
                </div>
            </div>
            <div class="admin-stat-card">
                <div class="admin-stat-icon" style="background: linear-gradient(135deg, #ffd700, #f39c12);">
                    <i class="fas fa-certificate"></i>
                </div>
                <div class="admin-stat-content">
                    <span class="admin-stat-nombre"><%= statsCours.getOrDefault("totalCertificats", 0) %></span>
                    <span class="admin-stat-label">Certificats délivrés</span>
                </div>
            </div>
            <div class="admin-stat-card">
                <div class="admin-stat-icon" style="background: linear-gradient(135deg, #9b59b6, #8e44ad);">
                    <i class="fas fa-chart-line"></i>
                </div>
                <div class="admin-stat-content">
                    <span class="admin-stat-nombre"><%= String.format("%.1f", (Double) statsCours.getOrDefault("tauxCompletionMoyen", 0.0)) %>%</span>
                    <span class="admin-stat-label">Taux complétion moyen</span>
                </div>
            </div>
        </div>

        <%-- ===== BARRE DE FILTRES COURS ===== --%>
        <div class="admin-filtres-bar">
            <form action="${pageContext.request.contextPath}/admin/resultats" method="GET" class="form-filtres" id="formFiltresCours">
                <input type="hidden" name="tab" value="cours">
                <div class="recherche-input-wrapper">
                    <i class="fas fa-search"></i>
                    <input type="text" name="recherche" value="<%= recherche %>" 
                           placeholder="Rechercher un apprenant ou un cours..." 
                           class="recherche-input" id="rechercheInputCours">
                </div>
                <select name="themeCours" id="filtreThemeCours" class="filtre-select">
                    <option value="tous" <%= "tous".equals(filtreThemeCours) ? "selected" : "" %>>Tous les thèmes</option>
                    <% for (String theme : themesCours) { %><option value="<%= theme %>" <%= theme.equals(filtreThemeCours) ? "selected" : "" %>><%= theme %></option><% } %>
                </select>
                <select name="niveauCours" id="filtreNiveauCours" class="filtre-select">
                    <option value="tous" <%= "tous".equals(filtreNiveauCours) ? "selected" : "" %>>Tous les niveaux</option>
                    <option value="elementaire" <%= "elementaire".equals(filtreNiveauCours) ? "selected" : "" %>>Élémentaire</option>
                    <option value="moyen" <%= "moyen".equals(filtreNiveauCours) ? "selected" : "" %>>Moyen</option>
                    <option value="avance" <%= "avance".equals(filtreNiveauCours) ? "selected" : "" %>>Avancé</option>
                </select>
                <select name="statutCours" id="filtreStatutCours" class="filtre-select">
                    <option value="tous" <%= "tous".equals(filtreStatutCours) ? "selected" : "" %>>Tous les statuts</option>
                    <option value="en_cours" <%= "en_cours".equals(filtreStatutCours) ? "selected" : "" %>>En cours</option>
                    <option value="termine" <%= "termine".equals(filtreStatutCours) ? "selected" : "" %>>Terminé</option>
                    <option value="certifie" <%= "certifie".equals(filtreStatutCours) ? "selected" : "" %>>Certifié</option>
                </select>
                <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreThemeCours) || !"tous".equals(filtreNiveauCours) || !"tous".equals(filtreStatutCours)) { %>
                    <a href="${pageContext.request.contextPath}/admin/resultats?tab=cours" class="btn-reset-filtres"><i class="fas fa-undo"></i><span>Réinitialiser</span></a>
                <% } %>
            </form>
        </div>

        <%-- ===== TABLEAU PROGRESSION COURS ===== --%>
        <div class="admin-tableau-section">
            <div class="tableau-header">
                <span class="resultat-count"><i class="fas fa-list"></i> <strong><%= totalProgression %></strong> résultat<%= totalProgression > 1 ? "s" : "" %> trouvé<%= totalProgression > 1 ? "s" : "" %></span>
            </div>
            <% if (progressionCours != null && !progressionCours.isEmpty()) { %>
                <div class="table-responsive">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th class="th-id">#</th>
                                <th>Apprenant</th>
                                <th>Cours</th>
                                <th class="th-center">Thème</th>
                                <th class="th-center">Niveau</th>
                                <th class="th-center">Progression</th>
                                <th class="th-center">Modules</th>
                                <th class="th-center">Statut</th>
                                <th class="th-center">Certificat</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% int indexCours = (pageCours - 1) * 20 + 1;
                               for (Map<String, Object> row : progressionCours) {
                                int utilisateurId = (Integer) row.get("utilisateur_id");
                                int coursId = (Integer) row.get("cours_id");
                                String pseudo = (String) row.get("pseudo");
                                String titreCours = (String) row.get("titre_cours");
                                String theme = (String) row.get("theme");
                                String niveau = (String) row.get("niveau");
                                int pourcentage = (Integer) row.get("pourcentage");
                                int modulesReussis = (Integer) row.get("modules_reussis");
                                int leconsTerminees = (Integer) row.get("lecons_terminees");
                                int totalLecons = (Integer) row.get("total_lecons");
                                String statut = (String) row.get("statut");
                                boolean aCertificat = (Boolean) row.get("a_certificat");
                                String mention = (String) row.get("mention");
                                if (pseudo == null) pseudo = "N/A";
                                if (titreCours == null) titreCours = "N/A";
                                if (titreCours.length() > 35) titreCours = titreCours.substring(0, 35) + "...";
                                String couleurProgression = pourcentage >= 80 ? "#27ae60" : (pourcentage >= 50 ? "#f39c12" : "#667eea");
                                String statutBadgeClass = "certifie".equals(statut) ? "badge-excellent" : ("termine".equals(statut) ? "badge-bon" : "badge-moyen");
                                String statutLibelle = "certifie".equals(statut) ? "Certifié" : ("termine".equals(statut) ? "Terminé" : "En cours");
                            %>
                                <tr>
                                    <td class="td-id"><%= indexCours %></td>
                                    <td><a href="${pageContext.request.contextPath}/admin/utilisateurs/detail?id=<%= utilisateurId %>" class="cell-joueur-link"><div class="cell-joueur"><div class="joueur-avatar"><i class="fas fa-user"></i></div><span><%= pseudo %></span></div></a></td>
                                    <td><a href="${pageContext.request.contextPath}/admin/cours/statistiques?id=<%= coursId %>" class="cell-quiz-link"><%= titreCours %></a></td>
                                    <td class="td-center"><span class="badge-niveau"><%= theme %></span></td>
                                    <td class="td-center"><span class="badge-niveau badge-niveau-<%= niveau %>"><%= niveau %></span></td>
                                    <td class="td-center">
                                        <div style="display: flex; align-items: center; gap: 8px;">
                                            <div style="flex: 1; height: 8px; background: #e8eaef; border-radius: 4px; overflow: hidden;"><div style="width: <%= pourcentage %>%; height: 100%; background: <%= couleurProgression %>; border-radius: 4px;"></div></div>
                                            <span style="font-size: 12px; font-weight: 600;"><%= pourcentage %>%</span>
                                        </div>
                                        <small style="color: #8b8fa3;"><%= leconsTerminees %>/<%= totalLecons %> leçons</small>
                                    </td>
                                    <td class="td-center"><%= modulesReussis %>/15</td>
                                    <td class="td-center"><span class="badge-score <%= statutBadgeClass %>"><%= statutLibelle %></span></td>
                                    <td class="td-center"><% if (aCertificat && mention != null) { %><span class="badge-score badge-excellent"><i class="fas fa-certificate"></i> <%= mention %></span><% } else { %><span style="color: #8b8fa3;">—</span><% } %></td>
                                </tr>
                            <% indexCours++; } %>
                        </tbody>
                    </table>
                </div>
                <% if (totalPagesCours > 1) { %>
                    <div class="pagination-bar">
                        <% if (pageCours > 1) { %><a href="${pageContext.request.contextPath}<%= urlPaginationCours %>pageCours=<%= pageCours - 1 %>" class="page-link"><i class="fas fa-chevron-left"></i></a><% } else { %><span class="page-link disabled"><i class="fas fa-chevron-left"></i></span><% } %>
                        <% int debutCours = Math.max(1, pageCours - 2); int finCours = Math.min(totalPagesCours, pageCours + 2);
                           if (debutCours > 1) { %><a href="${pageContext.request.contextPath}<%= urlPaginationCours %>pageCours=1" class="page-link">1</a><% if (debutCours > 2) { %><span class="page-ellipsis">...</span><% } } %>
                        <% for (int i = debutCours; i <= finCours; i++) { %><a href="${pageContext.request.contextPath}<%= urlPaginationCours %>pageCours=<%= i %>" class="page-link <%= i == pageCours ? "active" : "" %>"><%= i %></a><% } %>
                        <% if (finCours < totalPagesCours) { if (finCours < totalPagesCours - 1) { %><span class="page-ellipsis">...</span><% } %><a href="${pageContext.request.contextPath}<%= urlPaginationCours %>pageCours=<%= totalPagesCours %>" class="page-link"><%= totalPagesCours %></a><% } %>
                        <% if (pageCours < totalPagesCours) { %><a href="${pageContext.request.contextPath}<%= urlPaginationCours %>pageCours=<%= pageCours + 1 %>" class="page-link"><i class="fas fa-chevron-right"></i></a><% } else { %><span class="page-link disabled"><i class="fas fa-chevron-right"></i></span><% } %>
                        <span class="page-info"><%= totalProgression %> résultats - Page <%= pageCours %>/<%= totalPagesCours %></span>
                    </div>
                <% } %>
            <% } else { %>
                <div class="empty-state"><div class="empty-icon"><i class="fas fa-graduation-cap"></i></div><h3>Aucune progression trouvée</h3><p>Aucun apprenant n'a encore commencé de cours.</p></div>
            <% } %>
        </div>

    <% } %>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="admin-back">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn-retour">
            <i class="fas fa-arrow-left"></i> Retour au dashboard admin
        </a>
    </div>

</div>

<%-- ===== SCRIPTS ===== --%>
<script>
    // Filtres pour l'onglet quiz
    ['filtreTheme', 'filtreNiveau', 'filtreResultat', 'filtreTri'].forEach(function(id) {
        var el = document.getElementById(id);
        if (el) el.addEventListener('change', function() { document.getElementById('formFiltres').submit(); });
    });
    var rechercheInput = document.getElementById('rechercheInput');
    if (rechercheInput) {
        var timeout;
        rechercheInput.dataset.previousValue = rechercheInput.value;
        rechercheInput.addEventListener('input', function() {
            clearTimeout(timeout);
            var val = this.value;
            timeout = setTimeout(function() {
                if (val !== rechercheInput.dataset.previousValue) {
                    rechercheInput.dataset.previousValue = val;
                    document.getElementById('formFiltres').submit();
                }
            }, 600);
        });
    }

    // Filtres pour l'onglet cours
    ['filtreThemeCours', 'filtreNiveauCours', 'filtreStatutCours'].forEach(function(id) {
        var el = document.getElementById(id);
        if (el) el.addEventListener('change', function() { document.getElementById('formFiltresCours').submit(); });
    });
    var rechercheInputCours = document.getElementById('rechercheInputCours');
    if (rechercheInputCours) {
        var timeoutCours;
        rechercheInputCours.dataset.previousValue = rechercheInputCours.value;
        rechercheInputCours.addEventListener('input', function() {
            clearTimeout(timeoutCours);
            var val = this.value;
            timeoutCours = setTimeout(function() {
                if (val !== rechercheInputCours.dataset.previousValue) {
                    rechercheInputCours.dataset.previousValue = val;
                    document.getElementById('formFiltresCours').submit();
                }
            }, 600);
        });
    }
</script>

<%
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-resultats.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../footer-connecte.jsp" %>