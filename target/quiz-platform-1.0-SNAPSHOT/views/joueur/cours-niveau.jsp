<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.Chapitre" %>
<%@ page import="com.amijayquiz.models.Lecon" %>

<%-- ============================================================
     Fichier : cours-niveau.jsp
     Rôle    : Page niveau d'un thème de cours
              Affiche la liste de tous les cours disponibles
              pour un thème et un niveau donnés
              Chaque cours est une carte avec progression
              et bouton pour voir le plan détaillé du cours
              Stats : leçons, exemples de code, temps de lecture
     ============================================================ --%>

<%
    // Récupération des attributs depuis le servlet
    String nomTheme = (String) request.getAttribute("nomTheme");
    String nomNiveau = (String) request.getAttribute("nomNiveau");
    String nomNiveauFormate = (String) request.getAttribute("nomNiveauFormate");
    String iconeNiveau = (String) request.getAttribute("iconeNiveau");
    String couleurNiveau = (String) request.getAttribute("couleurNiveau");
    List<Map<String, Object>> coursAvecProgression = (List<Map<String, Object>>) request.getAttribute("coursAvecProgression");
    Integer totalCours = (Integer) request.getAttribute("totalCours");
    Integer totalCoursTermines = (Integer) request.getAttribute("totalCoursTermines");
    Integer totalChapitres = (Integer) request.getAttribute("totalChapitres");
    Integer totalLecons = (Integer) request.getAttribute("totalLecons");
    Integer pourcentageNiveau = (Integer) request.getAttribute("pourcentageNiveau");
    Boolean niveauTermine = (Boolean) request.getAttribute("niveauTermine");

    // Valeurs par défaut
    if (nomTheme == null) nomTheme = "Thème";
    if (nomNiveau == null) nomNiveau = "elementaire";
    if (nomNiveauFormate == null) nomNiveauFormate = "Niveau";
    if (iconeNiveau == null) iconeNiveau = "fas fa-circle";
    if (couleurNiveau == null) couleurNiveau = "#888888";
    if (totalCours == null) totalCours = 0;
    if (totalCoursTermines == null) totalCoursTermines = 0;
    if (totalChapitres == null) totalChapitres = 0;
    if (totalLecons == null) totalLecons = 0;
    if (pourcentageNiveau == null) pourcentageNiveau = 0;
    if (niveauTermine == null) niveauTermine = false;

    // URL encodée pour les liens
    String themeEncode = java.net.URLEncoder.encode(nomTheme, "UTF-8");
    
    // Message de statut
    String statutMessage;
    String statutIcone;
    String statutClass;
    if (niveauTermine) {
        statutMessage = "Niveau terminé - Félicitations !";
        statutIcone = "fas fa-trophy";
        statutClass = "termine";
    } else if (pourcentageNiveau > 0) {
        statutMessage = "Continuez vos efforts !";
        statutIcone = "fas fa-forward";
        statutClass = "en-cours";
    } else {
        statutMessage = "Commencez votre premier cours !";
        statutIcone = "fas fa-play-circle";
        statutClass = "a-debuter";
    }
%>



<%-- ===== CHARGEMENT DES STYLES ===== --%>
<%
    String coursNiveauCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/cours-niveau.css\">";
    request.setAttribute("extraHead", coursNiveauCSS);
%>

<%@ include file="../header.jsp" %>

<div class="cours-niveau-container">

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
            <span><%= nomTheme %></span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="<%= iconeNiveau %>"></i>
            <span><%= nomNiveauFormate %></span>
        </span>
    </nav>

    <%-- ============================================================
         BANNIÈRE DU NIVEAU
         ============================================================ --%>
    <header class="niveau-banner" style="background: linear-gradient(135deg, <%= couleurNiveau %>, <%= couleurNiveau %>dd);">
        <div class="niveau-banner-bg"></div>
        
        <div class="niveau-banner-icon">
            <i class="<%= iconeNiveau %>"></i>
        </div>
        
        <div class="niveau-banner-content">
            <span class="niveau-banner-theme">
                <i class="fas fa-tag"></i> <%= nomTheme %>
            </span>
            <h1><%= nomNiveauFormate %></h1>
            <div class="niveau-banner-stats">
                <span class="banner-stat-item">
                    <i class="fas fa-book"></i>
                    <strong><%= totalCours %></strong> cours
                </span>
                <span class="banner-stat-separator">•</span>
                <span class="banner-stat-item">
                    <i class="fas fa-list-ul"></i>
                    <strong><%= totalChapitres %></strong> chapitres
                </span>
                <span class="banner-stat-separator">•</span>
                <span class="banner-stat-item">
                    <i class="fas fa-file-alt"></i>
                    <strong><%= totalLecons %></strong> leçons
                </span>
            </div>
            
            <%-- Barre de progression --%>
            <div class="banner-progress">
                <div class="banner-progress-header">
                    <span>Progression du niveau</span>
                    <span><strong><%= pourcentageNiveau %>%</strong></span>
                </div>
                <div class="banner-progress-bar">
                    <div class="banner-progress-fill" style="width: <%= pourcentageNiveau %>%; background: rgba(255,255,255,0.5);"></div>
                </div>
                <div class="banner-progress-detail">
                    <span><i class="fas fa-check-circle"></i> <%= totalCoursTermines %>/<%= totalCours %> cours terminés</span>
                </div>
            </div>
        </div>
        
        <%-- Badge statut --%>
        <div class="niveau-banner-status">
            <div class="status-badge <%= statutClass %>">
                <i class="<%= statutIcone %>"></i>
                <span><%= statutMessage %></span>
            </div>
        </div>
    </header>

    <%-- ============================================================
         MESSAGE DE MOTIVATION
         ============================================================ --%>
    <div class="motivation-banner" style="border-left-color: <%= couleurNiveau %>;">
        <i class="fas fa-lightbulb" style="color: <%= couleurNiveau %>;"></i>
        <p>
            <% if (niveauTermine) { %>
                🎉 Vous avez terminé tous les cours de ce niveau ! 
                <% if (!"avance".equals(nomNiveau)) { %>
                    Le niveau suivant est débloqué.
                <% } else { %>
                    Vous maîtrisez complètement ce thème !
                <% } %>
            <% } else if (pourcentageNiveau >= 50) { %>
                💪 Vous êtes à mi-chemin ! Continuez vos efforts pour terminer ce niveau.
            <% } else if (pourcentageNiveau > 0) { %>
                🚀 Bon début ! Choisissez un cours ci-dessous et commencez votre apprentissage.
            <% } else { %>
                🎯 Choisissez un cours parmi ceux disponibles ci-dessous pour débuter votre formation.
            <% } %>
        </p>
    </div>

    <%-- ============================================================
         SECTION : CHOISISSEZ UN COURS
         ============================================================ --%>
    <section class="cours-section">
        <div class="section-header">
            <div class="section-header-left">
                <h2>
                    <i class="fas fa-book-open"></i>
                    Choisissez un cours pour voir son plan détaillé
                </h2>
                <span class="badge-count" style="background: <%= couleurNiveau %>;">
                    <i class="fas fa-book"></i>
                    <%= totalCours %> cours disponible<%= totalCours > 1 ? "s" : "" %>
                </span>
            </div>
            <div class="section-header-right">
                <span class="section-hint">
                    <i class="fas fa-info-circle"></i>
                    Cliquez sur "Voir le plan" pour accéder au plan complet du cours
                </span>
            </div>
        </div>

        <%-- ===== GRILLE DES COURS ===== --%>
        <div class="cours-grid">
            <% if (coursAvecProgression != null && !coursAvecProgression.isEmpty()) {
                for (Map<String, Object> coursData : coursAvecProgression) {
                    Cours cours = (Cours) coursData.get("cours");
                    int nbChapitres = (Integer) coursData.get("nbChapitres");
                    int nbLecons = (Integer) coursData.get("nbLecons");
                    int leconsTerminees = (Integer) coursData.get("leconsTerminees");
                    int chapitresTermines = (Integer) coursData.get("chapitresTermines");
                    boolean estTermine = (Boolean) coursData.get("estTermine");
                    int pourcentage = (Integer) coursData.get("pourcentage");
                    int premierChapitreId = (Integer) coursData.get("premierChapitreId");
                    String statutCours = (String) coursData.get("statutCours");
                    String iconeStatutCours = (String) coursData.get("iconeStatutCours");
                    String couleurStatutCours = (String) coursData.get("couleurStatutCours");
                    
                    // Icône du cours basée sur son thème
                    String iconeCours = cours.getIconeTheme();
                    String couleurCours = cours.getCouleurTheme();
                    
                    // Calculer le nombre d'exemples de code et temps de lecture
                    int exemplesCode = 0;
                    int tempsLectureTotal = 0;
                    if (cours.getChapitres() != null) {
                        for (Chapitre chap : cours.getChapitres()) {
                            if (chap.getLecons() != null) {
                                for (Lecon l : chap.getLecons()) {
                                    if (l.hasCodeSource()) exemplesCode++;
                                    tempsLectureTotal += l.getTempsLectureEstime();
                                }
                            }
                        }
                    }
                    String tempsLectureFormate = formaterTempsLecture(tempsLectureTotal);
            %>
                <div class="cours-card <%= estTermine ? "termine" : "" %>">
                    
                    <%-- Badge "Terminé" --%>
                    <% if (estTermine) { %>
                        <div class="cours-card-badge-termine">
                            <i class="fas fa-trophy"></i>
                        </div>
                    <% } %>
                    
                    <%-- En-tête du cours --%>
                    <div class="cours-card-header">
                        <div class="cours-card-icon" style="background: linear-gradient(135deg, <%= couleurCours %>, <%= couleurCours %>dd);">
                            <i class="<%= iconeCours %>"></i>
                        </div>
                        <div class="cours-card-title-section">
                            <h3 class="cours-card-titre"><%= cours.getTitre() %></h3>
                            <span class="cours-card-theme">
                                <i class="<%= iconeCours %>"></i> <%= cours.getTheme() %>
                            </span>
                        </div>
                        <div class="cours-card-status-icon" style="color: <%= couleurStatutCours %>;">
                            <i class="<%= iconeStatutCours %>"></i>
                        </div>
                    </div>
                    
                    <%-- Description du cours --%>
                    <% if (cours.getDescription() != null && !cours.getDescription().isEmpty()) { %>
                        <p class="cours-card-description">
                            <%= cours.getDescription().length() > 150 ? 
                                cours.getDescription().substring(0, 150) + "..." : 
                                cours.getDescription() %>
                        </p>
                    <% } %>
                    
                    <%-- Statistiques du cours --%>
                    <div class="cours-card-stats">
                        <div class="cours-stat">
                            <i class="fas fa-list-ul"></i>
                            <span><strong><%= nbChapitres %></strong> chapitres</span>
                        </div>
                        <div class="cours-stat">
                            <i class="fas fa-file-alt"></i>
                            <span><strong><%= nbLecons %></strong> leçons</span>
                        </div>
                        <div class="cours-stat">
                            <i class="fas fa-code"></i>
                            <span><strong><%= exemplesCode %></strong> exemples</span>
                        </div>
                        <div class="cours-stat">
                            <i class="fas fa-book-reader"></i>
                            <span><%= tempsLectureFormate %></span>
                        </div>
                    </div>
                    
                    <%-- Barre de progression --%>
                    <div class="cours-card-progress">
                        <div class="cours-progress-header">
                            <span>Progression du cours</span>
                            <span><strong><%= pourcentage %>%</strong></span>
                        </div>
                        <div class="cours-progress-bar">
                            <div class="cours-progress-fill" style="width: <%= pourcentage %>%; background: <%= couleurCours %>;"></div>
                        </div>
                        <div class="cours-progress-detail">
                            <span>
                                <i class="fas fa-check-circle"></i> 
                                <%= leconsTerminees %>/<%= nbLecons %> leçons terminées
                            </span>
                        </div>
                    </div>
                    
                    <%-- Statut du cours --%>
                    <div class="cours-card-statut">
                        <span class="statut-badge" style="color: <%= couleurStatutCours %>; background: <%= couleurStatutCours %>15;">
                            <i class="<%= iconeStatutCours %>"></i>
                            <%= statutCours %>
                        </span>
                    </div>
                    
                    <%-- Boutons d'action --%>
                    <div class="cours-card-actions">
                        <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" 
                           class="btn-cours-action btn-voir-plan">
                            <i class="fas fa-sitemap"></i>
                            <span>Voir le plan du cours</span>
                        </a>
                        
                        <% if (!estTermine && premierChapitreId > 0) { %>
                            <a href="${pageContext.request.contextPath}/joueur/cours/chapitre?coursId=<%= cours.getId() %>&chapitreId=<%= premierChapitreId %>" 
                               class="btn-cours-action btn-commencer-cours">
                                <i class="fas fa-play"></i>
                                <span>Commencer</span>
                            </a>
                        <% } else if (estTermine && premierChapitreId > 0) { %>
                            <a href="${pageContext.request.contextPath}/joueur/cours/chapitre?coursId=<%= cours.getId() %>&chapitreId=<%= premierChapitreId %>" 
                               class="btn-cours-action btn-revoir-cours">
                                <i class="fas fa-redo"></i>
                                <span>Revoir</span>
                            </a>
                        <% } %>
                    </div>
                    
                </div>
            <%  }
            } else { %>
                <%-- État vide --%>
                <div class="empty-state full-width">
                    <div class="empty-icon">
                        <i class="fas fa-inbox"></i>
                    </div>
                    <h3>Aucun cours disponible</h3>
                    <p>Ce niveau ne contient pas encore de cours. Revenez bientôt !</p>
                    <a href="${pageContext.request.contextPath}/joueur/cours/theme?theme=<%= themeEncode %>" class="btn-retour-empty">
                        <i class="fas fa-arrow-left"></i> Retour au thème
                    </a>
                </div>
            <% } %>
        </div>
    </section>

    <%-- ============================================================
         SECTION INFORMATION
         ============================================================ --%>
    <div class="info-section">
        <div class="info-card">
            <div class="info-card-icon">
                <i class="fas fa-book-reader"></i>
            </div>
            <div class="info-card-content">
                <h4>Apprentissage par la lecture</h4>
                <p>Chaque leçon contient des explications détaillées et des exemples de code prêts à copier-coller.</p>
            </div>
        </div>
        
        <div class="info-card">
            <div class="info-card-icon">
                <i class="fas fa-code"></i>
            </div>
            <div class="info-card-content">
                <h4>Exemples pratiques</h4>
                <p>Des exemples de code complets et commentés pour comprendre et pratiquer immédiatement.</p>
            </div>
        </div>
    </div>

    <%-- ============================================================
         BOUTON RETOUR
         ============================================================ --%>
    <div class="back-section">
        <a href="${pageContext.request.contextPath}/joueur/cours/theme?theme=<%= themeEncode %>" class="btn-back">
            <i class="fas fa-arrow-left"></i>
            <span>Retour au thème <%= nomTheme %></span>
        </a>
    </div>

</div>

<%-- ===== MÉTHODE UTILITAIRE JSP ===== --%>
<%!
    /**
     * Formate un temps en minutes en chaîne lisible
     */
    private String formaterTempsLecture(int minutes) {
        if (minutes == 0) return "N/A";
        if (minutes < 60) return minutes + " min";
        int heures = minutes / 60;
        int minsRestantes = minutes % 60;
        if (minsRestantes == 0) return heures + "h";
        return heures + "h" + String.format("%02d", minsRestantes);
    }
%>

<%-- ===== CHARGEMENT DES SCRIPTS ===== --%>
<%
    String coursNiveauJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/cours-niveau.js\"></script>";
    request.setAttribute("extraScripts", coursNiveauJS);
%>

<%@ include file="../footer.jsp" %>