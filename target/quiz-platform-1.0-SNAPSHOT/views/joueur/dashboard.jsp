<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Quiz" %>
<%@ page import="com.amijayquiz.models.Partie" %>
<%@ page import="com.amijayquiz.models.Utilisateur" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.Certificat" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>

<%-- ============================================================
     Fichier : dashboard.jsp
     Rôle    : Tableau de bord unifié du joueur AmijayQuiz
              - Message de bienvenue personnalisé avec avatar
              - Section "Reprendre" (Cours + Quiz)
              - 6 cartes statistiques (Cours + Quiz)
              - SECTION COURS EN PREMIER (12 thèmes avec "Voir plus")
              - SECTION QUIZ EN SECOND (6 thèmes)
              - Dernières activités (Quiz)
              - Conseils et astuces
              - Animations fluides et responsive
     ============================================================ --%>

<%
    // ===== RÉCUPÉRATION DES ATTRIBUTS DEPUIS LE SERVLET =====
    // Quiz
    List<Map<String, Object>> themesQuiz = (List<Map<String, Object>>) request.getAttribute("themesQuiz");
    Integer nombreParties = (Integer) request.getAttribute("nombreParties");
    Double meilleurScore = (Double) request.getAttribute("meilleurScore");
    Double moyenneScore = (Double) request.getAttribute("moyenneScore");
    List<Partie> dernieresParties = (List<Partie>) request.getAttribute("dernieresParties");
    Integer niveauxTermines = (Integer) request.getAttribute("niveauxTermines");

    // Cours
    List<Map<String, Object>> themesCours = (List<Map<String, Object>>) request.getAttribute("themesCours");
    Integer totalCours = (Integer) request.getAttribute("totalCours");
    Integer totalCoursTermines = (Integer) request.getAttribute("totalCoursTermines");
    Integer totalLecons = (Integer) request.getAttribute("totalLecons");
    Integer totalLeconsTerminees = (Integer) request.getAttribute("totalLeconsTerminees");
    Integer pourcentageCoursGlobal = (Integer) request.getAttribute("pourcentageCoursGlobal");
    Integer totalCertificats = (Integer) request.getAttribute("totalCertificats");

    // Message
    String messageSucces = (String) request.getAttribute("messageSucces");

    // Récupération de l'utilisateur connecté
    Utilisateur joueur = (Utilisateur) session.getAttribute("utilisateur");
    String pseudo = (joueur != null) ? joueur.getPseudo() : "Joueur";
    int joueurId = (joueur != null) ? joueur.getId() : 0;
    
    // Vérification premium
    com.amijayquiz.dao.AccessPremiumDAO premiumDAO = new com.amijayquiz.dao.AccessPremiumDAO();
    boolean estPremium = premiumDAO.estPremium(joueurId);
    request.setAttribute("estPremium", estPremium);

    // ===== VALEURS PAR DÉFAUT =====
    if (nombreParties == null) nombreParties = 0;
    if (meilleurScore == null) meilleurScore = 0.0;
    if (moyenneScore == null) moyenneScore = 0.0;
    if (niveauxTermines == null) niveauxTermines = 0;
    if (totalCours == null) totalCours = 0;
    if (totalCoursTermines == null) totalCoursTermines = 0;
    if (totalLecons == null) totalLecons = 0;
    if (totalLeconsTerminees == null) totalLeconsTerminees = 0;
    if (pourcentageCoursGlobal == null) pourcentageCoursGlobal = 0;
    if (totalCertificats == null) totalCertificats = 0;

    String meilleurScoreStr = String.format("%.0f%%", meilleurScore);
    String moyenneScoreStr = String.format("%.0f%%", moyenneScore);

    // ===== DATE ET SALUTATION =====
    SimpleDateFormat formatJour = new SimpleDateFormat("EEEE d MMMM yyyy", new java.util.Locale("fr", "FR"));
    String dateAujourdhui = formatJour.format(new Date());
    
    java.util.Calendar cal = java.util.Calendar.getInstance();
    int heure = cal.get(java.util.Calendar.HOUR_OF_DAY);
    String salutation;
    String emojiHeure;
    if (heure < 6) {
        salutation = "Bonne nuit";
        emojiHeure = "🌙";
    } else if (heure < 12) {
        salutation = "Bonjour";
        emojiHeure = "☀️";
    } else if (heure < 18) {
        salutation = "Bon après-midi";
        emojiHeure = "🌤️";
    } else {
        salutation = "Bonsoir";
        emojiHeure = "🌅";
    }

    // ===== GESTION "VOIR PLUS" POUR LES THÈMES COURS =====
    boolean afficherTousCours = "true".equals(request.getParameter("showAllCours"));
    int maxThemesCoursAffichage = afficherTousCours ? Integer.MAX_VALUE : 6;
    
    // ===== GESTION "VOIR PLUS" POUR LES THÈMES QUIZ =====
    boolean afficherTousQuiz = "true".equals(request.getParameter("showAllQuiz"));

    // ===== TITRE DE LA PAGE =====
    request.setAttribute("pageTitle", "Dashboard - AmijayQuiz");
%>

<%-- ============================================================
     STYLES SPÉCIFIQUES AU DASHBOARD
     ============================================================ --%>
<% 
    String dashboardCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/dashboard.css\">"
                        + "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/theme-card.css\">";
    request.setAttribute("extraHead", dashboardCSS);
%>
<%@ include file="../header.jsp" %>

<div class="dashboard-container">

    <%-- ===== MESSAGE DE SUCCÈS ===== --%>
    <% if (messageSucces != null) { %>
        <div class="message-alerte message-succes">
            <i class="fas fa-check-circle"></i>
            <span><%= messageSucces %></span>
        </div>
    <% } %>

    <%-- ============================================================
         EN-TÊTE DE BIENVENUE
         ============================================================ --%>
    <header class="welcome-header">
        <div class="welcome-content">
            <div class="welcome-greeting">
                <span class="welcome-emoji"><%= emojiHeure %></span>
                <div class="welcome-text">
                    <span class="welcome-salutation"><%= salutation %>, <strong><%= pseudo %></strong></span>
                    <h1>Prêt à apprendre aujourd'hui ?</h1>
                </div>
            </div>
            <p class="welcome-date">
                <i class="far fa-calendar-alt"></i>
                <%= dateAujourdhui %>
            </p>
            
            <div class="welcome-premium-status">
                <% if (estPremium) { %>
                    <span class="premium-badge-actif">
                        <i class="fas fa-crown"></i> Premium actif
                    </span>
                <% } else { %>
                    <a href="${pageContext.request.contextPath}/paiement" class="premium-badge-inactif">
                        <i class="fas fa-crown"></i> Devenir Premium (4$)
                    </a>
                <% } %>
            </div>
            
            <div class="welcome-quote">
                <i class="fas fa-quote-left"></i>
                <span>L'apprentissage est un trésor qui suivra son propriétaire partout.</span>
            </div>
        </div>
        
        <div class="welcome-avatar">
            <span class="avatar-text"><%= pseudo.substring(0, 1).toUpperCase() %></span>
        </div>
    </header>

    <%-- ============================================================
         SECTION : REPRENDRE MON PARCOURS (COURS EN PREMIER)
         ============================================================ --%>
    <% 
        // Chercher le prochain cours à suivre
        Map<String, Object> coursEnCours = null;
        String prochainThemeCours = null;
        boolean hasCoursEnCours = false;
        int pourcentageCoursEnCours = 0;
        String iconeCoursEnCours = "fas fa-graduation-cap";
        String couleurCoursEnCours = "#667eea";

        if (themesCours != null && !themesCours.isEmpty()) {
            for (Map<String, Object> theme : themesCours) {
                int pourcentage = (Integer) theme.get("pourcentageProgression");
                if (pourcentage > 0 && pourcentage < 100 && prochainThemeCours == null) {
                    prochainThemeCours = (String) theme.get("nom");
                    coursEnCours = theme;
                    hasCoursEnCours = true;
                    pourcentageCoursEnCours = pourcentage;
                    iconeCoursEnCours = (String) theme.get("icone");
                    couleurCoursEnCours = (String) theme.get("couleur");
                    break;
                }
            }
            if (!hasCoursEnCours) {
                Map<String, Object> premierTheme = themesCours.get(0);
                prochainThemeCours = (String) premierTheme.get("nom");
                coursEnCours = premierTheme;
                hasCoursEnCours = true;
                iconeCoursEnCours = (String) premierTheme.get("icone");
                couleurCoursEnCours = (String) premierTheme.get("couleur");
            }
        }

        // Chercher le prochain quiz à jouer
        Map<String, Object> themeActifQuiz = null;
        Map<String, Object> niveauActifQuiz = null;
        String prochaineCategorieQuiz = null;
        String prochainNiveauQuiz = null;
        boolean hasQuizEnCours = false;

        if (themesQuiz != null && !themesQuiz.isEmpty()) {
            for (Map<String, Object> theme : themesQuiz) {
                List<Map<String, Object>> niveaux = (List<Map<String, Object>>) theme.get("niveaux");
                for (Map<String, Object> niv : niveaux) {
                    boolean debloque = (Boolean) niv.get("debloque");
                    boolean termine = (Boolean) niv.get("termine");
                    if (debloque && !termine && prochaineCategorieQuiz == null) {
                        prochaineCategorieQuiz = (String) theme.get("nom");
                        prochainNiveauQuiz = (String) niv.get("nom");
                        themeActifQuiz = theme;
                        niveauActifQuiz = niv;
                        hasQuizEnCours = true;
                        break;
                    }
                }
                if (prochaineCategorieQuiz != null) break;
            }
        }
    %>
    
    
    <% if (!estPremium) { %>
        <div class="dashboard-premium-banner">
            <div class="premium-banner-glow"></div>
            <div class="premium-banner-content">
                <div class="premium-banner-icon">
                    <i class="fas fa-crown"></i>
                </div>
                <div class="premium-banner-text">
                    <h3>Débloquez tout le potentiel d'AmijayQuiz</h3>
                    <p>
                        <strong>4$ seulement</strong> — Accès à vie à tous les niveaux Moyen & Avancé.
                        <span class="premium-banner-features">
                            <span><i class="fas fa-check"></i> Tous les cours</span>
                            <span><i class="fas fa-check"></i> Tous les quiz</span>
                            <span><i class="fas fa-check"></i> Certificats</span>
                        </span>
                    </p>
                </div>
                <a href="${pageContext.request.contextPath}/paiement" class="premium-banner-btn">
                    <i class="fas fa-rocket"></i>
                    <span>Devenir Premium</span>
                    <small>4$ / 10 000 FC</small>
                </a>
            </div>
        </div>
    <% } %>

    <div class="resume-section">
        <div class="resume-grid">
            <%-- Carte Reprendre Cours (EN PREMIER) --%>
            <% if (hasCoursEnCours && coursEnCours != null) { %>
                <a href="${pageContext.request.contextPath}/joueur/cours/theme?theme=<%= java.net.URLEncoder.encode(prochainThemeCours, "UTF-8") %>" 
                   class="resume-card resume-card-cours">
                    <div class="resume-card-badge">📚 Cours</div>
                    <div class="resume-icon">
                        <i class="<%= iconeCoursEnCours %>"></i>
                    </div>
                    <div class="resume-info">
                        <span class="resume-label">
                            <% if (pourcentageCoursEnCours > 0) { %>Reprendre mes Cours<% } else { %>Explorer les Cours<% } %>
                        </span>
                        <h3>
                            <span style="color: <%= couleurCoursEnCours %>;">
                                <i class="<%= iconeCoursEnCours %>"></i>
                                <%= prochainThemeCours %>
                            </span>
                        </h3>
                        <div class="resume-progress">
                            <% if (pourcentageCoursEnCours > 0) { %>
                                <div class="mini-progress-bar">
                                    <div class="mini-progress-fill" style="width: <%= pourcentageCoursEnCours %>%; background: <%= couleurCoursEnCours %>;">
                                    </div>
                                </div>
                                <span><%= pourcentageCoursEnCours %>% complété</span>
                            <% } else { %>
                                <span><%= coursEnCours.get("totalCours") %> cours • <%= coursEnCours.get("totalLecons") %> leçons</span>
                            <% } %>
                        </div>
                    </div>
                    <span class="resume-btn resume-btn-cours">
                        <i class="fas fa-play"></i> 
                        <% if (pourcentageCoursEnCours > 0) { %>Continuer<% } else { %>Explorer<% } %>
                    </span>
                </a>
            <% } %>

            <%-- Carte Reprendre Quiz (EN SECOND) --%>
            <% if (hasQuizEnCours && themeActifQuiz != null && niveauActifQuiz != null) { %>
                <a href="${pageContext.request.contextPath}/joueur/niveau?categorie=<%= java.net.URLEncoder.encode(prochaineCategorieQuiz, "UTF-8") %>&niveau=<%= prochainNiveauQuiz %>" 
                   class="resume-card resume-card-quiz">
                    <div class="resume-card-badge">🎯 Quiz</div>
                    <div class="resume-icon">
                        <i class="fas fa-layer-group"></i>
                    </div>
                    <div class="resume-info">
                        <span class="resume-label">Reprendre mes Quiz</span>
                        <h3>
                            <span style="color: <%= themeActifQuiz.get("couleur") %>;">
                                <%= prochaineCategorieQuiz %>
                            </span>
                            <i class="fas fa-chevron-right" style="font-size: 12px; color: #ccc;"></i>
                            <span style="color: <%= niveauActifQuiz.get("couleur") %>;">
                                <%= niveauActifQuiz.get("nomFormate") %>
                            </span>
                        </h3>
                        <div class="resume-progress">
                            <div class="mini-progress-bar">
                                <div class="mini-progress-fill" style="width: <%= niveauActifQuiz.get("pourcentageProgression") %>%; 
                                     background: <%= niveauActifQuiz.get("couleur") %>;">
                                </div>
                            </div>
                            <span><%= niveauActifQuiz.get("partiesReussies") %>/<%= niveauActifQuiz.get("totalParties") %> parties</span>
                        </div>
                    </div>
                    <span class="resume-btn resume-btn-quiz">
                        <i class="fas fa-play"></i> Continuer
                    </span>
                </a>
            <% } else if (themesQuiz != null && !themesQuiz.isEmpty()) { %>
                <a href="${pageContext.request.contextPath}/joueur/dashboard#quiz-section" class="resume-card resume-card-quiz">
                    <div class="resume-card-badge">🎯 Quiz</div>
                    <div class="resume-icon">
                        <i class="fas fa-layer-group"></i>
                    </div>
                    <div class="resume-info">
                        <span class="resume-label">Commencer les Quiz</span>
                        <h3>Testez vos connaissances</h3>
                        <div class="resume-progress">
                            <span>6 thèmes disponibles</span>
                        </div>
                    </div>
                    <span class="resume-btn resume-btn-quiz">
                        <i class="fas fa-play"></i> Commencer
                    </span>
                </a>
            <% } %>
        </div>
    </div>

    <%-- ============================================================
         CARTES STATISTIQUES (6 cartes : 3 Cours + 3 Quiz)
         ============================================================ --%>
    <section class="stats-section">
        <div class="stats-grid stats-grid-6">
            <%-- Cours (EN PREMIER) --%>
            <div class="stat-card stat-card-cours">
                <div class="stat-icon-wrapper stat-icon-cours-total">
                    <i class="fas fa-book"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-number"><%= totalCours %></span>
                    <span class="stat-label">Cours disponibles</span>
                </div>
                <div class="stat-badge-theme">Cours</div>
            </div>

            <div class="stat-card stat-card-cours">
                <div class="stat-icon-wrapper stat-icon-cours-done">
                    <i class="fas fa-check-circle"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-number"><%= totalCoursTermines %></span>
                    <span class="stat-label">Cours terminés</span>
                </div>
                <div class="stat-badge-theme">Cours</div>
            </div>

            <div class="stat-card stat-card-cours">
                <div class="stat-icon-wrapper stat-icon-certificats">
                    <i class="fas fa-certificate"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-number"><%= totalCertificats %></span>
                    <span class="stat-label">Certificats obtenus</span>
                </div>
                <div class="stat-badge-theme">Cours</div>
            </div>

            <%-- Quiz (EN SECOND) --%>
            <div class="stat-card stat-card-quiz">
                <div class="stat-icon-wrapper stat-icon-parties">
                    <i class="fas fa-play-circle"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-number"><%= nombreParties %></span>
                    <span class="stat-label">Parties jouées</span>
                </div>
                <div class="stat-badge-theme">Quiz</div>
            </div>

            <div class="stat-card stat-card-quiz">
                <div class="stat-icon-wrapper stat-icon-best">
                    <i class="fas fa-trophy"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-number"><%= meilleurScoreStr %></span>
                    <span class="stat-label">Meilleur score</span>
                </div>
                <div class="stat-badge-theme">Quiz</div>
            </div>

            <div class="stat-card stat-card-quiz">
                <div class="stat-icon-wrapper stat-icon-levels">
                    <i class="fas fa-flag-checkered"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-number"><%= niveauxTermines %></span>
                    <span class="stat-label">Niveaux Quiz terminés</span>
                </div>
                <div class="stat-badge-theme">Quiz</div>
            </div>
                    
            <%-- 🆕 Statut Premium --%>
            <div class="stat-card <%= estPremium ? "stat-card-premium-actif" : "stat-card-premium-inactif" %>">
                <div class="stat-icon-wrapper stat-icon-premium">
                    <i class="fas fa-crown"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-number"><%= estPremium ? "Actif" : "Free" %></span>
                    <span class="stat-label">Statut Premium</span>
                </div>
                <% if (!estPremium) { %>
                    <a href="${pageContext.request.contextPath}/paiement" class="stat-upgrade-link">
                        <i class="fas fa-arrow-up"></i> Upgrade
                    </a>
                <% } %>
            </div>
        </div>
    </section>

    <%-- ============================================================
         SECTION : MES COURS (EN PREMIER - 12 THÈMES AVEC "VOIR PLUS")
         ============================================================ --%>
    <section class="themes-section" id="cours-section">
        <div class="section-header">
            <div class="section-header-left">
                <h2>
                    <i class="fas fa-graduation-cap"></i>
                    Cours Thématiques
                </h2>
                <span class="badge-count badge-cours">
                    <i class="fas fa-book"></i>
                    <%= themesCours != null ? themesCours.size() : 0 %> thème<%= themesCours != null && themesCours.size() > 1 ? "s" : "" %>
                </span>
            </div>
            <div class="section-header-right">
                <a href="${pageContext.request.contextPath}/joueur/cours" class="btn-voir-tout">
                    Voir tous les cours <i class="fas fa-arrow-right"></i>
                </a>
            </div>
        </div>

        <%-- Barre de progression globale des cours --%>
        <% if (totalLecons > 0) { %>
            <div class="cours-global-progress">
                <div class="cours-global-progress-header">
                    <span><i class="fas fa-chart-pie"></i> Progression globale Cours</span>
                    <span><strong><%= pourcentageCoursGlobal %>%</strong></span>
                </div>
                <div class="cours-global-progress-bar">
                    <div class="cours-global-progress-fill" style="width: <%= pourcentageCoursGlobal %>%;"></div>
                </div>
                <div class="cours-global-progress-detail">
                    <span><i class="fas fa-check-circle"></i> <%= totalLeconsTerminees %> / <%= totalLecons %> leçons terminées</span>
                    <span><i class="fas fa-trophy"></i> <%= totalCoursTermines %> / <%= totalCours %> cours validés</span>
                </div>
            </div>
        <% } %>

        <div class="themes-grid">
            <% if (themesCours != null && !themesCours.isEmpty()) {
                int count = 0;
                for (Map<String, Object> theme : themesCours) {
                    if (count >= maxThemesCoursAffichage) break;
                    count++;
                    
                    String nomThemeCours = (String) theme.get("nom");
                    String iconeThemeCours = (String) theme.get("icone");
                    String couleurThemeCours = (String) theme.get("couleur");
                    int totalCoursTheme = (Integer) theme.get("totalCours");
                    int coursTerminesTheme = (Integer) theme.get("coursTermines");
                    int totalLeconsTheme = (Integer) theme.get("totalLecons");
                    int leconsTermineesTheme = (Integer) theme.get("leconsTerminees");
                    int pourcentageTheme = (Integer) theme.get("pourcentageProgression");
            %>
                <a href="${pageContext.request.contextPath}/joueur/cours/theme?theme=<%= java.net.URLEncoder.encode(nomThemeCours, "UTF-8") %>" 
                   class="theme-card-link theme-card-cours">
                    <div class="theme-card theme-cours-card">
                        
                        <%-- Badge terminé --%>
                        <% if (pourcentageTheme >= 100 && totalCoursTheme > 0) { %>
                            <div class="theme-badge-termine">
                                <i class="fas fa-trophy"></i>
                            </div>
                        <% } %>
                        
                        <div class="theme-card-header">
                            <div class="theme-card-icon" style="background: linear-gradient(135deg, <%= couleurThemeCours %>, <%= couleurThemeCours %>dd);">
                                <i class="<%= iconeThemeCours %>"></i>
                            </div>
                            <div class="theme-card-info">
                                <h3 class="theme-card-nom"><%= nomThemeCours %></h3>
                                <div class="theme-card-meta">
                                    <span class="meta-item">
                                        <i class="fas fa-book"></i>
                                        <strong><%= totalCoursTheme %></strong> cours
                                    </span>
                                    <span class="meta-separator">•</span>
                                    <span class="meta-item">
                                        <i class="fas fa-list-ol"></i>
                                        <strong><%= totalLeconsTheme %></strong> leçons
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div class="theme-card-body">
                            <div class="theme-progress">
                                <div class="progress-label">
                                    <span>Progression</span>
                                    <span class="progress-value"><%= pourcentageTheme %>%</span>
                                </div>
                                <div class="progress-track">
                                    <div class="progress-fill" style="width: <%= pourcentageTheme %>%; background: <%= couleurThemeCours %>;"></div>
                                </div>
                            </div>
                        </div>

                        <div class="theme-card-footer">
                            <div class="footer-niveau-actif">
                                <% if (pourcentageTheme >= 100 && totalCoursTheme > 0) { %>
                                    <i class="fas fa-check-circle" style="color: #27ae60;"></i>
                                    <span>🎉 Thème terminé</span>
                                <% } else if (pourcentageTheme > 0) { %>
                                    <i class="fas fa-spinner" style="color: #667eea;"></i>
                                    <span>En cours</span>
                                <% } else { %>
                                    <i class="fas fa-play-circle" style="color: #f39c12;"></i>
                                    <span>À commencer</span>
                                <% } %>
                            </div>
                            <span class="btn-voir-parcours">
                                <i class="fas fa-eye"></i>
                                <span>Voir les cours</span>
                            </span>
                        </div>

                    </div>
                </a>
            <%  }
                // Bouton "Voir plus" si plus de 6 thèmes
                if (themesCours.size() > 6 && !afficherTousCours) { %>
                    <a href="${pageContext.request.contextPath}/joueur/dashboard?showAllCours=true#cours-section" 
                       class="voir-plus-card">
                        <div class="voir-plus-content">
                            <div class="voir-plus-icon">
                                <i class="fas fa-plus-circle"></i>
                            </div>
                            <h3>Voir plus de thèmes</h3>
                            <p><%= themesCours.size() - 6 %> autre<%= (themesCours.size() - 6) > 1 ? "s" : "" %> thème<%= (themesCours.size() - 6) > 1 ? "s" : "" %> disponible<%= (themesCours.size() - 6) > 1 ? "s" : "" %></p>
                            <span class="voir-plus-btn">
                                <i class="fas fa-chevron-down"></i> Afficher tout
                            </span>
                        </div>
                    </a>
            <%  }
            } else { %>
                <div class="empty-state full-width">
                    <div class="empty-icon"><i class="fas fa-inbox"></i></div>
                    <h3>Aucun cours disponible</h3>
                    <p>Les cours seront bientôt disponibles. Revenez prochainement !</p>
                </div>
            <% } %>
        </div>
    </section>

    <%-- ============================================================
         SECTION : MES PARCOURS THÉMATIQUES QUIZ (EN SECOND)
         ============================================================ --%>
    <section class="themes-section" id="quiz-section">
        <div class="section-header">
            <div class="section-header-left">
                <h2>
                    <i class="fas fa-layer-group"></i>
                    Quiz Thématiques
                </h2>
                <span class="badge-count badge-quiz">
                    <i class="fas fa-puzzle-piece"></i>
                    <%= themesQuiz != null ? themesQuiz.size() : 0 %> thème<%= themesQuiz != null && themesQuiz.size() > 1 ? "s" : "" %>
                </span>
            </div>
            <div class="section-header-right">
                <a href="${pageContext.request.contextPath}/joueur/historique" class="btn-voir-tout">
                    Historique Quiz <i class="fas fa-arrow-right"></i>
                </a>
            </div>
        </div>

        <div class="themes-grid">
            <% if (themesQuiz != null && !themesQuiz.isEmpty()) {
                int quizCount = 0;
                for (Map<String, Object> theme : themesQuiz) {
                    if (quizCount >= 3 && !afficherTousQuiz) break;
                    quizCount++;
                    
                    String nomTheme = (String) theme.get("nom");
                    String iconeTheme = (String) theme.get("icone");
                    String couleurTheme = (String) theme.get("couleur");
                    int totalParties = (Integer) theme.get("totalParties");
                    int partiesReussies = (Integer) theme.get("partiesReussies");
                    int pourcentageProgression = (Integer) theme.get("pourcentageProgression");
                    List<Map<String, Object>> niveaux = (List<Map<String, Object>>) theme.get("niveaux");
                    String niveauActif = (String) theme.get("niveauActif");

                    request.setAttribute("nomTheme", nomTheme);
                    request.setAttribute("iconeTheme", iconeTheme);
                    request.setAttribute("couleurTheme", couleurTheme);
                    request.setAttribute("totalParties", totalParties);
                    request.setAttribute("partiesReussies", partiesReussies);
                    request.setAttribute("pourcentageProgression", pourcentageProgression);
                    request.setAttribute("niveaux", niveaux);
                    request.setAttribute("niveauActif", niveauActif);
            %>
                    <jsp:include page="components/theme-card.jsp" />
            <%  }
                // Bouton "Voir plus" si plus de 3 thèmes Quiz
                if (themesQuiz != null && themesQuiz.size() > 3 && !afficherTousQuiz) { %>
                    <a href="${pageContext.request.contextPath}/joueur/dashboard?showAllQuiz=true#quiz-section" 
                       class="voir-plus-card voir-plus-quiz">
                        <div class="voir-plus-content">
                            <div class="voir-plus-icon">
                                <i class="fas fa-plus-circle"></i>
                            </div>
                            <h3>Voir plus de thèmes Quiz</h3>
                            <p><%= themesQuiz.size() - 3 %> autre<%= (themesQuiz.size() - 3) > 1 ? "s" : "" %> thème<%= (themesQuiz.size() - 3) > 1 ? "s" : "" %> disponible<%= (themesQuiz.size() - 3) > 1 ? "s" : "" %></p>
                            <span class="voir-plus-btn">
                                <i class="fas fa-chevron-down"></i> Afficher tout
                            </span>
                        </div>
                    </a>
            <%  }
            } else { %>
                <div class="empty-state full-width">
                    <div class="empty-icon"><i class="fas fa-inbox"></i></div>
                    <h3>Aucun thème de quiz disponible</h3>
                    <p>Les quiz ne sont pas encore configurés. Revenez bientôt !</p>
                </div>
            <% } %>
        </div>
    </section>

    <%-- ============================================================
         SECTION : DERNIÈRES ACTIVITÉS
         ============================================================ --%>
    <section class="historique-section">
        <div class="section-header">
            <div class="section-header-left">
                <h2>
                    <i class="fas fa-history"></i>
                    Dernières activités
                </h2>
                <span class="badge-count badge-neutral">
                    <i class="fas fa-clock-rotate-left"></i>
                    <%= dernieresParties != null ? dernieresParties.size() : 0 %> activité<%= dernieresParties != null && dernieresParties.size() > 1 ? "s" : "" %>
                </span>
            </div>
            <div class="section-header-right">
                <a href="${pageContext.request.contextPath}/joueur/historique" class="btn-voir-tout">
                    Historique Quiz <i class="fas fa-arrow-right"></i>
                </a>
            </div>
        </div>

        <% if (dernieresParties != null && !dernieresParties.isEmpty()) { %>
            <div class="table-container">
                <table class="table-historique">
                    <thead>
                        <tr>
                            <th><i class="fas fa-file-lines"></i> Activité</th>
                            <th><i class="fas fa-star"></i> Score</th>
                            <th><i class="fas fa-clock"></i> Temps</th>
                            <th><i class="fas fa-calendar"></i> Date</th>
                            <th class="th-action">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Partie partie : dernieresParties) {
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
                            
                            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new java.util.Locale("fr", "FR"));
                            String dateFormatee = sdf.format(partie.getDatePartie());
                        %>
                            <tr>
                                <td>
                                    <div class="quiz-cell">
                                        <i class="fas fa-question-circle quiz-cell-icon"></i>
                                        <span><%= partie.getTitreQuiz() %></span>
                                    </div>
                                </td>
                                <td>
                                    <span class="badge-score <%= badgeClass %>">
                                        <i class="fas <%= badgeIcon %>"></i>
                                        <%= partie.getScore() %>/<%= partie.getTotalQuestions() %>
                                        <small>(<%= String.format("%.0f%%", pourcentage) %>)</small>
                                    </span>
                                </td>
                                <td>
                                    <span class="cell-time">
                                        <i class="far fa-clock"></i>
                                        <%= partie.getTempsFormate() %>
                                    </span>
                                </td>
                                <td>
                                    <span class="cell-date">
                                        <i class="far fa-calendar-alt"></i>
                                        <%= dateFormatee %>
                                    </span>
                                </td>
                                <td class="td-action">
                                    <a href="${pageContext.request.contextPath}/joueur/demarrer-quiz?id=<%= partie.getIdQuiz() %>" 
                                       class="btn-rejouer" title="Rejouer ce quiz">
                                        <i class="fas fa-redo"></i>
                                    </a>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        <% } else { %>
            <div class="empty-state">
                <div class="empty-icon"><i class="fas fa-gamepad"></i></div>
                <h3>Aucune activité récente</h3>
                <p>Lancez un quiz ou commencez un cours pour voir votre historique.</p>
            </div>
        <% } %>
    </section>

    <%-- ============================================================
         SECTION : CONSEILS
         ============================================================ --%>
    <section class="tips-section">
        <div class="section-header">
            <h2><i class="fas fa-lightbulb"></i> Conseils pour progresser</h2>
        </div>
        <div class="tips-grid">
            <div class="tip-card">
                <div class="tip-icon"><i class="fas fa-book-reader"></i></div>
                <h4>Suivez les cours</h4>
                <p>Apprenez en profondeur avec nos cours complets, des explications détaillées et des exemples de code.</p>
            </div>
            <div class="tip-card">
                <div class="tip-icon"><i class="fas fa-redo"></i></div>
                <h4>Répétez les quiz</h4>
                <p>Rejouer un quiz améliore votre score et renforce votre mémoire.</p>
            </div>
            <div class="tip-card">
                <div class="tip-icon"><i class="fas fa-chart-line"></i></div>
                <h4>Suivez votre progression</h4>
                <p>Consultez régulièrement votre historique pour identifier vos points faibles.</p>
            </div>
        </div>
    </section>

</div>

<%-- ============================================================
     SCRIPTS SPÉCIFIQUES AU DASHBOARD
     ============================================================ --%>
<% 
    String dashboardJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/dashboard.js\"></script>";
    request.setAttribute("extraScripts", dashboardJS);
%>

<%@ include file="../footer.jsp" %>