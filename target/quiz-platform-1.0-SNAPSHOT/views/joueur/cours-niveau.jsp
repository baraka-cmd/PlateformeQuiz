<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.Chapitre" %>
<%@ page import="com.amijayquiz.models.Lecon" %>

<%-- ============================================================
     FICHIER : views/joueur/cours-niveau.jsp
     RÔLE    : Page niveau d'un theme de cours AmijayQuiz
              Affiche tous les cours disponibles pour un theme
              et un niveau donnes (elementaire, moyen, avance)
              - Banniere du niveau avec icone, statistiques
              - Guide de progression expliquant l'architecture
              - Barre de progression globale du niveau
              - Message Premium si non premium (niveaux moyen/avance)
              - Grille des cours avec cartes detaillees
              - Boutons Voir le plan et Commencer
     ============================================================ --%>

<%
    // ===== RECUPERATION DES ATTRIBUTS =====
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
    Boolean estPremium = (Boolean) request.getAttribute("estPremium");

    // ===== VALEURS PAR DEFAUT =====
    if (nomTheme == null) nomTheme = "Theme";
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
    if (estPremium == null) estPremium = false;

    String themeEncode = java.net.URLEncoder.encode(nomTheme, "UTF-8");

    // ===== MESSAGE DE STATUT =====
    String statutMessage;
    String statutIcone;
    String statutClass;
    if (niveauTermine) {
        statutMessage = "Niveau termine";
        statutIcone = "fas fa-trophy";
        statutClass = "termine";
    } else if (pourcentageNiveau > 0) {
        statutMessage = "En cours";
        statutIcone = "fas fa-forward";
        statutClass = "en-cours";
    } else {
        statutMessage = "A debuter";
        statutIcone = "fas fa-play-circle";
        statutClass = "a-debuter";
    }

    // ===== MESSAGE PREMIUM =====
    boolean afficherBannierePremium = false;
    if (!estPremium && ("moyen".equals(nomNiveau) || "avance".equals(nomNiveau))) {
        afficherBannierePremium = true;
    }

    request.setAttribute("pageTitle", nomNiveauFormate + " - " + nomTheme + " - AmijayQuiz");
%>

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
         BANNIERE DU NIVEAU
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
                <span class="banner-stat-separator">&bull;</span>
                <span class="banner-stat-item">
                    <i class="fas fa-list-ul"></i>
                    <strong><%= totalChapitres %></strong> chapitres
                </span>
                <span class="banner-stat-separator">&bull;</span>
                <span class="banner-stat-item">
                    <i class="fas fa-file-alt"></i>
                    <strong><%= totalLecons %></strong> lecons
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
                    <span>
                        <i class="fas fa-check-circle"></i>
                        <%= totalCoursTermines %>/<%= totalCours %> cours termines
                    </span>
                </div>
            </div>
        </div>

        <div class="niveau-banner-status">
            <div class="status-badge <%= statutClass %>">
                <i class="<%= statutIcone %>"></i>
                <span><%= statutMessage %></span>
            </div>
        </div>
    </header>

    <%-- ============================================================
         GUIDE D'ARCHITECTURE DES NIVEAUX
         ============================================================ --%>
    <div class="architecture-guide">
        <div class="architecture-guide-header">
            <div class="guide-icon-wrapper">
                <i class="fas fa-sitemap"></i>
            </div>
            <h2>Comment fonctionne l'apprentissage sur AmijayQuiz ?</h2>
        </div>

        <div class="architecture-levels">
            <%-- Niveau 1 : Theme --%>
            <div class="arch-level">
                <div class="arch-level-number" style="background: #667eea;">1</div>
                <div class="arch-level-content">
                    <h4><i class="fas fa-layer-group"></i> Choisissez un theme</h4>
                    <p>12 themes disponibles : Developpement Web, Java, Python, PHP, Bases de Donnees, Cybersecurite, Mobile, IA, Design UI/UX, Reseaux, Systemes, Data Science.</p>
                </div>
            </div>

            <%-- Niveau 2 : Niveau --%>
            <div class="arch-level">
                <div class="arch-level-number" style="background: <%= couleurNiveau %>;">2</div>
                <div class="arch-level-content">
                    <h4><i class="<%= iconeNiveau %>"></i> Progressez par niveau</h4>
                    <p>
                        Chaque theme contient <strong>3 niveaux</strong> :
                        <span style="color: #27ae60; font-weight: 600;">Elementaire</span>
                        <i class="fas fa-arrow-right" style="font-size: 10px; color: #ccc;"></i>
                        <span style="color: #f39c12; font-weight: 600;">Moyen</span>
                        <i class="fas fa-arrow-right" style="font-size: 10px; color: #ccc;"></i>
                        <span style="color: #e74c3c; font-weight: 600;">Avance</span>.
                        Terminez le niveau Elementaire pour debloquer le niveau Moyen, puis le niveau Avance.
                    </p>
                </div>
            </div>

            <%-- Niveau 3 : Cours --%>
            <div class="arch-level">
                <div class="arch-level-number" style="background: #f39c12;">3</div>
                <div class="arch-level-content">
                    <h4><i class="fas fa-book"></i> Suivez un cours</h4>
                    <p>Chaque niveau contient <strong>un ou plusieurs cours</strong>. Choisissez celui qui vous interesse et consultez son plan detaille.</p>
                </div>
            </div>

            <%-- Niveau 4 : Modules --%>
            <div class="arch-level">
                <div class="arch-level-number" style="background: #e74c3c;">4</div>
                <div class="arch-level-content">
                    <h4><i class="fas fa-cubes"></i> Progressez module par module</h4>
                    <p>
                        Chaque cours est divise en <strong>15 modules</strong>.
                        Lisez les lecons de chaque chapitre, puis <strong>reussissez l'examen du module (75%)</strong> pour debloquer le suivant.
                    </p>
                </div>
            </div>

            <%-- Niveau 5 : Certificat --%>
            <div class="arch-level">
                <div class="arch-level-number" style="background: #ffd700; color: #1a1a2e;">5</div>
                <div class="arch-level-content">
                    <h4><i class="fas fa-certificate"></i> Obtenez votre certificat</h4>
                    <p>
                        Apres avoir reussi les <strong>15 modules</strong>, passez l'<strong>examen final (80%)</strong>.
                        Vous recevrez un <strong>certificat verifiable</strong> avec un code unique.
                    </p>
                </div>
            </div>
        </div>
    </div>

    <%-- ============================================================
         BANNIERE PREMIUM (NIVEAUX MOYEN ET AVANCE)
         ============================================================ --%>
    <% if (afficherBannierePremium) { %>
        <div class="premium-banner">
            <div class="premium-banner-glow"></div>
            <div class="premium-banner-icon">
                <i class="fas fa-crown"></i>
            </div>
            <div class="premium-banner-content">
                <h3>Niveau <%= nomNiveauFormate %> - Premium requis</h3>
                <p>
                    Le niveau <strong><%= nomNiveauFormate %></strong> et le niveau
                    <strong><%="moyen".equals(nomNiveau) ? "Avance" : "Moyen"%></strong>
                    necessitent un compte Premium.
                </p>
                <ul class="premium-banner-features">
                    <li><i class="fas fa-check"></i> Tous les cours des niveaux Moyen et Avance</li>
                    <li><i class="fas fa-check"></i> Tous les modules (2 a 15) de chaque cours</li>
                    <li><i class="fas fa-check"></i> Tous les quiz (360+ parties)</li>
                    <li><i class="fas fa-check"></i> Certificats verifiables a vie</li>
                </ul>
                <p class="premium-banner-note">
                    <i class="fas fa-lightbulb"></i>
                    <em>Un seul paiement de <strong>4$ (10 000 FC)</strong> vous donne acces a toute la plateforme a vie.
                    Votre progression est conservee.</em>
                </p>
            </div>
            <div class="premium-banner-action">
                <span class="premium-price">4$ ou 10 000 FC</span>
                <span class="premium-duration">Paiement unique - Acces a vie</span>
                <a href="${pageContext.request.contextPath}/paiement" class="premium-btn">
                    <i class="fas fa-unlock-alt"></i> Debloquer maintenant
                </a>
            </div>
        </div>
    <% } %>

    <%-- ============================================================
         MESSAGE DE MOTIVATION
         ============================================================ --%>
    <div class="motivation-banner" style="border-left-color: <%= couleurNiveau %>;">
        <i class="fas fa-lightbulb" style="color: <%= couleurNiveau %>;"></i>
        <p>
            <% if (niveauTermine) { %>
                Vous avez termine tous les cours de ce niveau.
                <% if (!"avance".equals(nomNiveau)) { %>
                    Le niveau suivant est debloque. Continuez votre progression !
                <% } else { %>
                    Vous maîtrisez completement ce theme. Quel accomplissement !
                <% } %>
            <% } else if (pourcentageNiveau >= 50) { %>
                Vous etes a mi-chemin ! Continuez vos efforts pour terminer ce niveau.
            <% } else if (pourcentageNiveau > 0) { %>
                Bon debut ! Choisissez un cours ci-dessous et commencez votre apprentissage.
            <% } else { %>
                Choisissez un cours parmi ceux disponibles ci-dessous pour debuter votre formation.
            <% } %>
        </p>
    </div>

    <%-- ============================================================
         SECTION : LISTE DES COURS
         ============================================================ --%>
    <section class="cours-section">
        <div class="section-header">
            <div class="section-header-left">
                <h2>
                    <i class="fas fa-book-open"></i>
                    Choisissez un cours pour voir son plan detaille
                </h2>
                <span class="badge-count" style="background: <%= couleurNiveau %>;">
                    <i class="fas fa-book"></i>
                    <%= totalCours %> cours disponible<%= totalCours > 1 ? "s" : "" %>
                </span>
            </div>
            <div class="section-header-right">
                <span class="section-hint">
                    <i class="fas fa-info-circle"></i>
                    Cliquez sur <strong>Voir le plan</strong> pour acceder au plan complet du cours
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

                    String iconeCours = cours.getIconeTheme();
                    String couleurCours = cours.getCouleurTheme();

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

                    <%-- Badge Termine --%>
                    <% if (estTermine) { %>
                        <div class="cours-card-badge-termine">
                            <i class="fas fa-trophy"></i>
                        </div>
                    <% } %>

                    <%-- En-tete du cours --%>
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

                    <%-- Description --%>
                    <% if (cours.getDescription() != null && !cours.getDescription().isEmpty()) { %>
                        <p class="cours-card-description">
                            <%= cours.getDescription().length() > 150
                                ? cours.getDescription().substring(0, 150) + "..."
                                : cours.getDescription() %>
                        </p>
                    <% } %>

                    <%-- Statistiques --%>
                    <div class="cours-card-stats">
                        <div class="cours-stat">
                            <i class="fas fa-list-ul"></i>
                            <span><strong><%= nbChapitres %></strong> chapitres</span>
                        </div>
                        <div class="cours-stat">
                            <i class="fas fa-file-alt"></i>
                            <span><strong><%= nbLecons %></strong> lecons</span>
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
                                <%= leconsTerminees %>/<%= nbLecons %> lecons terminees
                            </span>
                        </div>
                    </div>

                    <%-- Statut --%>
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
                <div class="empty-state full-width">
                    <div class="empty-icon">
                        <i class="fas fa-inbox"></i>
                    </div>
                    <h3>Aucun cours disponible</h3>
                    <p>Ce niveau ne contient pas encore de cours. Revenez bientot !</p>
                    <a href="${pageContext.request.contextPath}/joueur/cours/theme?theme=<%= themeEncode %>" class="btn-retour-empty">
                        <i class="fas fa-arrow-left"></i> Retour au theme
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
                <p>Chaque lecon contient des explications detaillees et des exemples de code prêts a copier-coller.</p>
            </div>
        </div>

        <div class="info-card">
            <div class="info-card-icon">
                <i class="fas fa-code"></i>
            </div>
            <div class="info-card-content">
                <h4>Exemples pratiques</h4>
                <p>Des exemples de code complets et commentes pour comprendre et pratiquer immediatement.</p>
            </div>
        </div>
    </div>

    <%-- ============================================================
         BOUTON RETOUR
         ============================================================ --%>
    <div class="back-section">
        <a href="${pageContext.request.contextPath}/joueur/cours/theme?theme=<%= themeEncode %>" class="btn-back">
            <i class="fas fa-arrow-left"></i>
            <span>Retour au theme <%= nomTheme %></span>
        </a>
    </div>

</div>

<%-- ============================================================
     METHODE UTILITAIRE
     ============================================================ --%>
<%!
    private String formaterTempsLecture(int minutes) {
        if (minutes == 0) return "N/A";
        if (minutes < 60) return minutes + " min";
        int heures = minutes / 60;
        int minsRestantes = minutes % 60;
        if (minsRestantes == 0) return heures + "h";
        return heures + "h" + String.format("%02d", minsRestantes);
    }
%>

<%
    String coursNiveauJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/cours-niveau.js\"></script>";
    request.setAttribute("extraScripts", coursNiveauJS);
%>

<%@ include file="../footer.jsp" %>