<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.learnpath.models.Partie" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%-- ============================================================
     FICHIER : views/joueur/historique.jsp
     RÔLE    : Page d'historique complet du joueur
              - SECTION QUIZ : statistiques, filtres, tableau
              - SECTION COURS : progression, certificats, leçons
              - Statistiques globales (quiz + cours)
              - Filtres par thème, niveau, résultat
              - Pagination complète
     ============================================================ --%>

<%
    // ===== RÉCUPÉRATION DES ATTRIBUTS QUIZ =====
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
    
    // ===== RÉCUPÉRATION DES ATTRIBUTS COURS =====
    List<Map<String, Object>> progressionCours = (List<Map<String, Object>>) request.getAttribute("progressionCours");
    Integer totalCoursCommences = (Integer) request.getAttribute("totalCoursCommences");
    Integer totalCoursTermines = (Integer) request.getAttribute("totalCoursTermines");
    Integer totalLeconsTerminees = (Integer) request.getAttribute("totalLeconsTerminees");
    Integer totalCertificatsObtenus = (Integer) request.getAttribute("totalCertificatsObtenus");
    List<Map<String, Object>> certificatsRecents = (List<Map<String, Object>>) request.getAttribute("certificatsRecents");
    
    // ===== VALEURS PAR DÉFAUT =====
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
    if (totalCoursCommences == null) totalCoursCommences = 0;
    if (totalCoursTermines == null) totalCoursTermines = 0;
    if (totalLeconsTerminees == null) totalLeconsTerminees = 0;
    if (totalCertificatsObtenus == null) totalCertificatsObtenus = 0;
    if (progressionCours == null) progressionCours = new java.util.ArrayList<>();
    if (certificatsRecents == null) certificatsRecents = new java.util.ArrayList<>();
    
    String tauxReussiteStr = String.format("%.0f%%", tauxReussite);
    String moyenneGeneraleStr = String.format("%.1f%%", moyenneGenerale);
    
    String baseUrl = request.getContextPath() + "/joueur/historique?";
    String params = "";
    if (filtreTheme != null && !"tous".equals(filtreTheme)) params += "&theme=" + java.net.URLEncoder.encode(filtreTheme, "UTF-8");
    if (filtreNiveau != null && !"tous".equals(filtreNiveau)) params += "&niveau=" + filtreNiveau;
    if (filtreReussite != null && !"tous".equals(filtreReussite)) params += "&reussite=" + filtreReussite;
    
    request.setAttribute("pageTitle", "Historique complet - LearnPath");
%>

<%
    String historiqueCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/historique.css\">";
    request.setAttribute("extraHead", historiqueCSS);
%>

<%@ include file="../header.jsp" %>

<div class="historique-container">

    <%-- ===== BREADCRUMB ===== --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-history"></i><span>Historique</span>
        </span>
    </nav>

    <%-- ===== EN-TÊTE ===== --%>
    <header class="historique-header">
        <div class="header-content">
            <div class="header-icon">
                <i class="fas fa-history"></i>
            </div>
            <div class="header-text">
                <h1>Historique complet</h1>
                <p>Consultez toutes vos performances : Quiz et Cours</p>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="header-back-btn">
            <i class="fas fa-arrow-left"></i><span>Dashboard</span>
        </a>
    </header>

    <%-- ============================================================
         STATISTIQUES GLOBALES (7 CARTES : Quiz + Cours)
         ============================================================ --%>
    <section class="stats-section">
        <div class="stats-grid-historique">
            <div class="stat-card-h">
                <div class="stat-icon-h stat-icon-total">
                    <i class="fas fa-gamepad"></i>
                </div>
                <div class="stat-content-h">
                    <span class="stat-number-h"><%= totalParties %></span>
                    <span class="stat-label-h">Parties jouées</span>
                </div>
            </div>
            <div class="stat-card-h">
                <div class="stat-icon-h stat-icon-reussies">
                    <i class="fas fa-check-circle"></i>
                </div>
                <div class="stat-content-h">
                    <span class="stat-number-h"><%= partiesReussies %></span>
                    <span class="stat-label-h">Quiz réussis</span>
                </div>
            </div>
            <div class="stat-card-h">
                <div class="stat-icon-h stat-icon-taux">
                    <i class="fas fa-chart-pie"></i>
                </div>
                <div class="stat-content-h">
                    <span class="stat-number-h"><%= tauxReussiteStr %></span>
                    <span class="stat-label-h">Taux réussite Quiz</span>
                </div>
            </div>
            <div class="stat-card-h">
                <div class="stat-icon-h" style="background: linear-gradient(135deg, #27ae60, #2ecc71);">
                    <i class="fas fa-graduation-cap"></i>
                </div>
                <div class="stat-content-h">
                    <span class="stat-number-h"><%= totalCoursCommences %></span>
                    <span class="stat-label-h">Cours commencés</span>
                </div>
            </div>
            <div class="stat-card-h">
                <div class="stat-icon-h" style="background: linear-gradient(135deg, #2980b9, #3498db);">
                    <i class="fas fa-book"></i>
                </div>
                <div class="stat-content-h">
                    <span class="stat-number-h"><%= totalCoursTermines %></span>
                    <span class="stat-label-h">Cours terminés</span>
                </div>
            </div>
            <div class="stat-card-h">
                <div class="stat-icon-h" style="background: linear-gradient(135deg, #8e44ad, #9b59b6);">
                    <i class="fas fa-certificate"></i>
                </div>
                <div class="stat-content-h">
                    <span class="stat-number-h"><%= totalCertificatsObtenus %></span>
                    <span class="stat-label-h">Certificats</span>
                </div>
            </div>
            <div class="stat-card-h">
                <div class="stat-icon-h" style="background: linear-gradient(135deg, #f39c12, #e67e22);">
                    <i class="fas fa-file-alt"></i>
                </div>
                <div class="stat-content-h">
                    <span class="stat-number-h"><%= totalLeconsTerminees %></span>
                    <span class="stat-label-h">Leçons terminées</span>
                </div>
            </div>
        </div>
    </section>

    <%-- ============================================================
         SECTION : HISTORIQUE QUIZ
         ============================================================ --%>
    <div class="section-separator">
        <h2><i class="fas fa-layer-group"></i> Historique des Quiz</h2>
    </div>

    <%-- Statistiques avancées Quiz --%>
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
                    <span class="stat-label-adv">Quiz échoués</span>
                </div>
            </div>
        </div>
    </section>

    <%-- Filtres Quiz --%>
    <section class="filtres-section">
        <form action="${pageContext.request.contextPath}/joueur/historique" method="GET" class="filtres-bar">
            <input type="hidden" name="tab" value="quiz">
            <div class="filtre-group">
                <label for="filtreTheme"><i class="fas fa-book"></i> Thème</label>
                <select name="theme" id="filtreTheme" class="filtre-select">
                    <option value="tous" <%= "tous".equals(filtreTheme) ? "selected" : "" %>>Tous les thèmes</option>
                    <% if (themes != null) { 
                        for (String theme : themes) { %>
                            <option value="<%= theme %>" <%= theme.equals(filtreTheme) ? "selected" : "" %>><%= theme %></option>
                    <% } } %>
                </select>
            </div>
            <div class="filtre-group">
                <label for="filtreNiveau"><i class="fas fa-signal"></i> Niveau</label>
                <select name="niveau" id="filtreNiveau" class="filtre-select">
                    <option value="tous" <%= "tous".equals(filtreNiveau) ? "selected" : "" %>>Tous les niveaux</option>
                    <option value="elementaire" <%= "elementaire".equals(filtreNiveau) ? "selected" : "" %>>Élémentaire</option>
                    <option value="moyen" <%= "moyen".equals(filtreNiveau) ? "selected" : "" %>>Moyen</option>
                    <option value="avance" <%= "avance".equals(filtreNiveau) ? "selected" : "" %>>Avancé</option>
                </select>
            </div>
            <div class="filtre-group">
                <label for="filtreReussite"><i class="fas fa-filter"></i> Résultat</label>
                <select name="reussite" id="filtreReussite" class="filtre-select">
                    <option value="tous" <%= "tous".equals(filtreReussite) ? "selected" : "" %>>Tous</option>
                    <option value="reussies" <%= "reussies".equals(filtreReussite) ? "selected" : "" %>>Réussies</option>
                    <option value="echouees" <%= "echouees".equals(filtreReussite) ? "selected" : "" %>>Échouées</option>
                </select>
            </div>
            <div class="filtre-actions">
                <button type="submit" class="btn-filtrer"><i class="fas fa-search"></i><span>Filtrer</span></button>
                <a href="${pageContext.request.contextPath}/joueur/historique?tab=quiz" class="btn-reset-filtres"><i class="fas fa-undo"></i><span>Réinitialiser</span></a>
            </div>
        </form>
    </section>

    <%-- Tableau Quiz --%>
    <section class="tableau-section">
        <div class="tableau-header">
            <span class="resultat-count"><i class="fas fa-list"></i> <strong><%= totalPartiesFiltrees %></strong> résultat<%= totalPartiesFiltrees > 1 ? "s" : "" %></span>
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
                                    String badgeClass = pourcentage >= 80 ? "badge-excellent" : (pourcentage >= 60 ? "badge-bon" : (pourcentage >= 40 ? "badge-moyen" : "badge-faible"));
                                    String badgeIcon = pourcentage >= 40 ? "fa-check-circle" : "fa-times-circle";
                                    boolean reussi = pourcentage >= 80;
                                    String dateFormatee = sdf.format(partie.getDatePartie());
                            %>
                                <tr>
                                    <td><div class="quiz-cell"><i class="fas fa-question-circle quiz-cell-icon"></i><span class="quiz-cell-title"><%= partie.getTitreQuiz() %></span></div></td>
                                    <td class="td-center"><span class="badge-score-h <%= badgeClass %>"><i class="fas <%= badgeIcon %>"></i> <%= partie.getScore() %>/<%= partie.getTotalQuestions() %> <small>(<%= String.format("%.0f%%", pourcentage) %>)</small></span></td>
                                    <td class="td-center"><span class="badge-reussite-h <%= reussi ? "badge-reussi" : "badge-echoue" %>"><i class="fas <%= reussi ? "fa-check-circle" : "fa-times-circle" %>"></i> <%= reussi ? "Réussi" : "Échoué" %></span></td>
                                    <td class="td-center"><span class="cell-icon-text"><i class="far fa-clock"></i> <%= partie.getTempsFormate() %></span></td>
                                    <td class="td-center"><span class="cell-icon-text"><i class="far fa-calendar-alt"></i> <%= dateFormatee %></span></td>
                                    <td class="td-center"><a href="${pageContext.request.contextPath}/joueur/demarrer-quiz?id=<%= partie.getIdQuiz() %>" class="btn-rejouer" title="Rejouer"><i class="fas fa-redo"></i></a></td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
                <% if (totalPages > 1) { %>
                    <nav class="pagination-bar">
                        <a href="<%= baseUrl %>page=1<%= params %>" class="page-link <%= pageActuelle == 1 ? "disabled" : "" %>"><i class="fas fa-angle-double-left"></i></a>
                        <a href="<%= baseUrl %>page=<%= pageActuelle - 1 %><%= params %>" class="page-link <%= pageActuelle == 1 ? "disabled" : "" %>"><i class="fas fa-angle-left"></i></a>
                        <% int debutPage = Math.max(1, pageActuelle - 2); int finPage = Math.min(totalPages, pageActuelle + 2);
                           if (debutPage > 1) { %><a href="<%= baseUrl %>page=1<%= params %>" class="page-link">1</a><% if (debutPage > 2) { %><span class="page-ellipsis">...</span><% } } %>
                        <% for (int i = debutPage; i <= finPage; i++) { %><a href="<%= baseUrl %>page=<%= i %><%= params %>" class="page-link <%= i == pageActuelle ? "active" : "" %>"><%= i %></a><% } %>
                        <% if (finPage < totalPages) { if (finPage < totalPages - 1) { %><span class="page-ellipsis">...</span><% } %><a href="<%= baseUrl %>page=<%= totalPages %><%= params %>" class="page-link"><%= totalPages %></a><% } %>
                        <a href="<%= baseUrl %>page=<%= pageActuelle + 1 %><%= params %>" class="page-link <%= pageActuelle == totalPages ? "disabled" : "" %>"><i class="fas fa-angle-right"></i></a>
                        <a href="<%= baseUrl %>page=<%= totalPages %><%= params %>" class="page-link <%= pageActuelle == totalPages ? "disabled" : "" %>"><i class="fas fa-angle-double-right"></i></a>
                    </nav>
                <% } %>
            <% } else { %>
                <div class="empty-state">
                    <div class="empty-icon"><i class="fas fa-inbox"></i></div>
                    <h3>Aucune partie trouvée</h3>
                    <p><% if (!"tous".equals(filtreTheme) || !"tous".equals(filtreNiveau) || !"tous".equals(filtreReussite)) { %>Aucun résultat avec ces filtres.<% } else { %>Vous n'avez pas encore joué de quiz.<% } %></p>
                </div>
            <% } %>
        </div>
    </section>

    <%-- ============================================================
         SECTION : PROGRESSION COURS
         ============================================================ --%>
    <div class="section-separator" style="margin-top: 30px;">
        <h2><i class="fas fa-graduation-cap"></i> Progression des Cours</h2>
    </div>

    <section class="tableau-section">
        <div class="tableau-historique">
            <% if (progressionCours != null && !progressionCours.isEmpty()) { %>
                <div class="table-responsive">
                    <table class="table-h">
                        <thead>
                            <tr>
                                <th><i class="fas fa-book"></i> Cours</th>
                                <th class="th-center"><i class="fas fa-signal"></i> Niveau</th>
                                <th class="th-center"><i class="fas fa-chart-line"></i> Progression</th>
                                <th class="th-center"><i class="fas fa-check-circle"></i> Statut</th>
                                <th class="th-center"><i class="fas fa-certificate"></i> Certificat</th>
                                <th class="th-center"><i class="fas fa-eye"></i> Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Map<String, Object> coursData : progressionCours) {
                                String titreCours = (String) coursData.get("titre");
                                String themeCours = (String) coursData.get("theme");
                                String niveauCours = (String) coursData.get("niveau");
                                int pourcentage = coursData.get("pourcentage") != null ? (Integer) coursData.get("pourcentage") : 0;
                                boolean estTermine = coursData.get("termine") != null && (Boolean) coursData.get("termine");
                                boolean aCertificat = coursData.get("aCertificat") != null && (Boolean) coursData.get("aCertificat");
                                int coursId = coursData.get("coursId") != null ? (Integer) coursData.get("coursId") : 0;
                                String couleurProgression = pourcentage >= 80 ? "#27ae60" : (pourcentage >= 50 ? "#f39c12" : (pourcentage > 0 ? "#667eea" : "#e8eaef"));
                            %>
                                <tr>
                                    <td>
                                        <div class="quiz-cell">
                                            <i class="fas fa-book quiz-cell-icon" style="color: #667eea;"></i>
                                            <div>
                                                <span class="quiz-cell-title"><%= titreCours %></span>
                                                <small style="color: #8b8fa3; font-size: 11px;"><%= themeCours %></small>
                                            </div>
                                        </div>
                                    </td>
                                    <td class="td-center"><span class="badge-score-h badge-bon"><%= niveauCours %></span></td>
                                    <td class="td-center">
                                        <div style="display: flex; align-items: center; gap: 8px;">
                                            <div style="flex: 1; height: 8px; background: #e8eaef; border-radius: 4px; overflow: hidden;">
                                                <div style="width: <%= pourcentage %>%; height: 100%; background: <%= couleurProgression %>; border-radius: 4px;"></div>
                                            </div>
                                            <span style="font-size: 12px; font-weight: 600;"><%= pourcentage %>%</span>
                                        </div>
                                    </td>
                                    <td class="td-center">
                                        <span class="badge-reussite-h <%= estTermine ? "badge-reussi" : (pourcentage > 0 ? "badge-echoue" : "badge-echoue") %>" style="<%= pourcentage > 0 && !estTermine ? "background: #eef0ff; color: #667eea; border-color: #d4d8ff;" : "" %>">
                                            <i class="fas <%= estTermine ? "fa-check-circle" : (pourcentage > 0 ? "fa-spinner" : "fa-circle") %>"></i>
                                            <%= estTermine ? "Terminé" : (pourcentage > 0 ? "En cours" : "Non commencé") %>
                                        </span>
                                    </td>
                                    <td class="td-center">
                                        <% if (aCertificat) { %>
                                            <span class="badge-score-h badge-excellent"><i class="fas fa-certificate"></i> Obtenu</span>
                                        <% } else if (estTermine) { %>
                                            <span style="color: #f39c12; font-size: 12px;"><i class="fas fa-exclamation-circle"></i> Examen requis</span>
                                        <% } else { %>
                                            <span style="color: #8b8fa3;">—</span>
                                        <% } %>
                                    </td>
                                    <td class="td-center">
                                        <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= coursId %>" class="btn-rejouer" title="Voir le plan" style="background: #eef0ff; color: #667eea; border-color: #d4d8ff;">
                                            <i class="fas fa-eye"></i>
                                        </a>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <div class="empty-state">
                    <div class="empty-icon"><i class="fas fa-graduation-cap"></i></div>
                    <h3>Aucun cours commencé</h3>
                    <p>Commencez un cours pour voir votre progression ici.</p>
                    <a href="${pageContext.request.contextPath}/joueur/cours" class="btn-commencer"><i class="fas fa-play"></i> Voir les cours</a>
                </div>
            <% } %>
        </div>
    </section>

    <%-- ============================================================
         SECTION : CERTIFICATS RÉCENTS
         ============================================================ --%>
    <% if (certificatsRecents != null && !certificatsRecents.isEmpty()) { %>
        <div class="section-separator" style="margin-top: 30px;">
            <h2><i class="fas fa-certificate"></i> Certificats obtenus</h2>
        </div>
        <section class="tableau-section">
            <div class="tableau-historique">
                <div class="table-responsive">
                    <table class="table-h">
                        <thead>
                            <tr>
                                <th><i class="fas fa-certificate"></i> Cours</th>
                                <th class="th-center"><i class="fas fa-star"></i> Mention</th>
                                <th class="th-center"><i class="fas fa-chart-line"></i> Score</th>
                                <th class="th-center"><i class="fas fa-calendar"></i> Date</th>
                                <th class="th-center"><i class="fas fa-eye"></i> Voir</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Map<String, Object> certData : certificatsRecents) {
                                com.learnpath.models.Certificat cert = (com.learnpath.models.Certificat) certData.get("certificat");
                                String titreCoursCert = (String) certData.get("titreCours");
                            %>
                                <tr>
                                    <td><div class="quiz-cell"><i class="fas fa-certificate quiz-cell-icon" style="color: #f39c12;"></i><span class="quiz-cell-title"><%= titreCoursCert %></span></div></td>
                                    <td class="td-center"><span style="color: <%= cert.getCouleurMention() %>; font-weight: 600;"><i class="<%= cert.getIconeMention() %>"></i> <%= cert.getMention() %></span></td>
                                    <td class="td-center"><span class="badge-score-h badge-excellent"><%= cert.getPourcentage() %></span></td>
                                    <td class="td-center"><span class="cell-icon-text"><i class="far fa-calendar-alt"></i> <%= cert.getDateFormatee() %></span></td>
                                    <td class="td-center"><a href="${pageContext.request.contextPath}/joueur/cours/certificat?coursId=<%= cert.getCoursId() %>" class="btn-rejouer" style="background: #fef3e2; color: #e67e22; border-color: #fad7a0;" title="Voir le certificat"><i class="fas fa-eye"></i></a></td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>
    <% } %>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="historique-back">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="btn-retour">
            <i class="fas fa-arrow-left"></i><span>Retour au dashboard</span>
        </a>
    </div>

</div>

<%
    String historiqueJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/historique.js\"></script>";
    request.setAttribute("extraScripts", historiqueJS);
%>

<%@ include file="../footer-connecte.jsp" %>