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
              - Message de bienvenue personnalisé
              - Bannière Premium (si non premium)
              - Guide Cours : comment apprendre
              - Guide Quiz : comment se tester
              - 7 cartes statistiques (Cours + Quiz + Premium)
              - SECTION COURS (12 thèmes)
              - SECTION QUIZ (6 thèmes)
              - Dernières activités
              - Conseils pour progresser
     ============================================================ --%>

<%
    // ===== RÉCUPÉRATION DES ATTRIBUTS =====
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

    // Utilisateur
    Utilisateur joueur = (Utilisateur) session.getAttribute("utilisateur");
    String pseudo = (joueur != null) ? joueur.getPseudo() : "Joueur";
    int joueurId = (joueur != null) ? joueur.getId() : 0;

    // Premium
    com.amijayquiz.dao.AccessPremiumDAO premiumDAO = new com.amijayquiz.dao.AccessPremiumDAO();
    boolean estPremium = premiumDAO.estPremium(joueurId);
    java.util.Map<String, Object> infosPremium = premiumDAO.getInfosPremium(joueurId);

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
    if (heure < 6) { salutation = "Bonne nuit"; emojiHeure = "🌙"; }
    else if (heure < 12) { salutation = "Bonjour"; emojiHeure = "☀️"; }
    else if (heure < 18) { salutation = "Bon après-midi"; emojiHeure = "🌤️"; }
    else { salutation = "Bonsoir"; emojiHeure = "🌅"; }

    request.setAttribute("pageTitle", "Dashboard - AmijayQuiz");
%>

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
                <i class="far fa-calendar-alt"></i> <%= dateAujourdhui %>
            </p>
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
         STATUT PREMIUM
         ============================================================ --%>
    <div class="premium-status-bar <%= estPremium ? "premium-actif" : "premium-inactif" %>">
        <div class="premium-status-icon">
            <i class="fas fa-crown"></i>
        </div>
        <div class="premium-status-text">
            <% if (estPremium) { %>
                <strong>Compte Premium actif</strong>
                <span>
                    Activé le 
                    <%= infosPremium != null && infosPremium.get("dateActivation") != null 
                        ? new SimpleDateFormat("dd MMMM yyyy", new java.util.Locale("fr", "FR"))
                            .format((java.sql.Timestamp) infosPremium.get("dateActivation")) 
                        : "N/A" %>
                    — Accès à vie à toute la plateforme
                </span>
            <% } else { %>
                <strong>Compte Gratuit</strong>
                <span>Accès limité — Passez Premium pour débloquer toute la plateforme</span>
            <% } %>
        </div>
        <% if (!estPremium) { %>
            <a href="${pageContext.request.contextPath}/paiement" class="premium-status-btn">
                <i class="fas fa-rocket"></i> Devenir Premium (4$)
            </a>
        <% } %>
    </div>

    <%-- ============================================================
         BANNIÈRE PREMIUM (SI NON PREMIUM)
         ============================================================ --%>
    <% if (!estPremium) { %>
        <div class="dashboard-premium-banner">
            <div class="premium-banner-glow"></div>
            <div class="premium-banner-content">
                <div class="premium-banner-icon">
                    <i class="fas fa-crown"></i>
                </div>
                <div class="premium-banner-text">
                    <h3>Débloquez toute la plateforme</h3>
                    <p>
                        Un seul paiement de <strong>4$ (10 000 FC)</strong> vous donne accès à vie à :
                    </p>
                    <div class="premium-banner-features">
                        <span><i class="fas fa-check"></i> Tous les modules de cours (2 à 15)</span>
                        <span><i class="fas fa-check"></i> Tous les niveaux Moyen & Avancé</span>
                        <span><i class="fas fa-check"></i> Tous les quiz (360+ parties)</span>
                        <span><i class="fas fa-check"></i> Certificats vérifiables</span>
                    </div>
                    <p class="premium-banner-note">
                        <i class="fas fa-lightbulb"></i>
                        <em>Vous gardez votre progression actuelle. Vous débloquez l'accès, pas les niveaux.</em>
                    </p>
                </div>
                <a href="${pageContext.request.contextPath}/paiement" class="premium-banner-btn">
                    <i class="fas fa-unlock-alt"></i>
                    <span>Débloquer maintenant</span>
                    <small>4$ / 10 000 FC — Accès à vie</small>
                </a>
            </div>
        </div>
    <% } %>

    <%-- ============================================================
         GUIDES EXPLICATIFS
         ============================================================ --%>
    <div class="guides-grid">
        
        <%-- Guide Cours --%>
        <div class="guide-card guide-cours">
            <div class="guide-card-header">
                <div class="guide-icon">
                    <i class="fas fa-graduation-cap"></i>
                </div>
                <h3>📚 Comment fonctionnent les Cours ?</h3>
            </div>
            <div class="guide-card-body">
                <ol>
                    <li><strong>Choisissez un thème</strong> parmi les 12 disponibles (Développement Web, Java, Python...)</li>
                    <li><strong>Sélectionnez un cours</strong> dans le niveau de votre choix</li>
                    <li><strong>Lisez les leçons</strong> de chaque chapitre (texte enrichi + exemples de code)</li>
                    <li><strong>Passez l'examen du module</strong> (20 questions, score minimum : 75%)</li>
                    <li><strong>Débloquez le module suivant</strong> après réussite</li>
                    <li><strong>Répétez</strong> pour les 15 modules du cours</li>
                    <li><strong>Passez l'examen final</strong> (score minimum : 80%)</li>
                    <li><strong>Obtenez votre certificat vérifiable</strong> 🎓</li>
                </ol>
                <div class="guide-note">
                    <i class="fas fa-star"></i>
                    <span><strong>Module 1 gratuit</strong> sur tous les cours. Modules 2 à 15 : Premium requis.</span>
                </div>
            </div>
        </div>

        <%-- Guide Quiz --%>
        <div class="guide-card guide-quiz">
            <div class="guide-card-header">
                <div class="guide-icon">
                    <i class="fas fa-layer-group"></i>
                </div>
                <h3>🎮 Comment fonctionnent les Quiz ?</h3>
            </div>
            <div class="guide-card-body">
                <ol>
                    <li><strong>Choisissez un thème</strong> parmi les 6 disponibles</li>
                    <li><strong>Sélectionnez un niveau</strong> : Élémentaire, Moyen ou Avancé</li>
                    <li><strong>Jouez les parties dans l'ordre</strong> (Partie 1 → Partie 2 → ...)</li>
                    <li><strong>Réussissez avec 8/10 minimum</strong> pour débloquer la partie suivante</li>
                    <li><strong>Terminez toutes les parties d'un niveau</strong> pour débloquer le niveau supérieur</li>
                    <li><strong>Chronomètre</strong> : 3 minutes par quiz de 10 questions</li>
                </ol>
                <div class="guide-note">
                    <i class="fas fa-star"></i>
                    <span><strong>5 premières parties gratuites</strong> par thème. Parties 6+ et niveaux supérieurs : Premium requis.</span>
                </div>
            </div>
        </div>
    </div>

    <%-- ============================================================
         CARTES STATISTIQUES (7 cartes)
         ============================================================ --%>
    <section class="stats-section">
        <div class="stats-grid stats-grid-7">
            
            <%-- Cours disponibles --%>
            <div class="stat-card stat-card-cours">
                <div class="stat-icon-wrapper stat-icon-cours-total">
                    <i class="fas fa-book"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-number"><%= totalCours %></span>
                    <span class="stat-label">Cours disponibles</span>
                </div>
            </div>

            <%-- Cours terminés --%>
            <div class="stat-card stat-card-cours">
                <div class="stat-icon-wrapper stat-icon-cours-done">
                    <i class="fas fa-check-circle"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-number"><%= totalCoursTermines %></span>
                    <span class="stat-label">Cours terminés</span>
                </div>
            </div>

            <%-- Certificats --%>
            <div class="stat-card stat-card-cours">
                <div class="stat-icon-wrapper stat-icon-certificats">
                    <i class="fas fa-certificate"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-number"><%= totalCertificats %></span>
                    <span class="stat-label">Certificats obtenus</span>
                </div>
            </div>

            <%-- Parties jouées --%>
            <div class="stat-card stat-card-quiz">
                <div class="stat-icon-wrapper stat-icon-parties">
                    <i class="fas fa-play-circle"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-number"><%= nombreParties %></span>
                    <span class="stat-label">Parties jouées</span>
                </div>
            </div>

            <%-- Meilleur score --%>
            <div class="stat-card stat-card-quiz">
                <div class="stat-icon-wrapper stat-icon-best">
                    <i class="fas fa-trophy"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-number"><%= meilleurScoreStr %></span>
                    <span class="stat-label">Meilleur score</span>
                </div>
            </div>

            <%-- Niveaux terminés --%>
            <div class="stat-card stat-card-quiz">
                <div class="stat-icon-wrapper stat-icon-levels">
                    <i class="fas fa-flag-checkered"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-number"><%= niveauxTermines %></span>
                    <span class="stat-label">Niveaux Quiz finis</span>
                </div>
            </div>

            <%-- Statut Premium --%>
            <div class="stat-card <%= estPremium ? "stat-card-premium-actif" : "stat-card-premium-inactif" %>">
                <div class="stat-icon-wrapper stat-icon-premium">
                    <i class="fas fa-crown"></i>
                </div>
                <div class="stat-content">
                    <span class="stat-number"><%= estPremium ? "Actif" : "Gratuit" %></span>
                    <span class="stat-label">Statut Premium</span>
                </div>
            </div>
        </div>
    </section>

    <%-- ============================================================
         BARRE DE PROGRESSION GLOBALE COURS
         ============================================================ --%>
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

    <%-- ============================================================
         SECTION : MES COURS (12 THÈMES)
         ============================================================ --%>
    <section class="themes-section" id="cours-section">
        <div class="section-header">
            <div class="section-header-left">
                <h2><i class="fas fa-graduation-cap"></i> Mes Cours</h2>
                <span class="badge-count badge-cours">
                    <i class="fas fa-book"></i> <%= themesCours != null ? themesCours.size() : 0 %> thème<%= themesCours != null && themesCours.size() > 1 ? "s" : "" %>
                </span>
            </div>
            <div class="section-header-right">
                <a href="${pageContext.request.contextPath}/joueur/cours" class="btn-voir-tout">
                    Voir tous les cours <i class="fas fa-arrow-right"></i>
                </a>
            </div>
        </div>

        <div class="themes-grid">
            <% if (themesCours != null && !themesCours.isEmpty()) {
                for (Map<String, Object> theme : themesCours) {
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
                        <% if (pourcentageTheme >= 100 && totalCoursTheme > 0) { %>
                            <div class="theme-badge-termine"><i class="fas fa-trophy"></i></div>
                        <% } %>
                        <div class="theme-card-header">
                            <div class="theme-card-icon" style="background: linear-gradient(135deg, <%= couleurThemeCours %>, <%= couleurThemeCours %>dd);">
                                <i class="<%= iconeThemeCours %>"></i>
                            </div>
                            <div class="theme-card-info">
                                <h3 class="theme-card-nom"><%= nomThemeCours %></h3>
                                <div class="theme-card-meta">
                                    <span class="meta-item"><i class="fas fa-book"></i> <strong><%= totalCoursTheme %></strong> cours</span>
                                    <span class="meta-separator">•</span>
                                    <span class="meta-item"><i class="fas fa-list-ol"></i> <strong><%= totalLeconsTheme %></strong> leçons</span>
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
                                    <i class="fas fa-check-circle" style="color: #27ae60;"></i> <span>Terminé</span>
                                <% } else if (pourcentageTheme > 0) { %>
                                    <i class="fas fa-spinner" style="color: #667eea;"></i> <span>En cours</span>
                                <% } else { %>
                                    <i class="fas fa-play-circle" style="color: #f39c12;"></i> <span>À commencer</span>
                                <% } %>
                            </div>
                            <span class="btn-voir-parcours"><i class="fas fa-eye"></i> Voir les cours</span>
                        </div>
                    </div>
                </a>
            <%  }
            } else { %>
                <div class="empty-state full-width">
                    <div class="empty-icon"><i class="fas fa-inbox"></i></div>
                    <h3>Aucun cours disponible</h3>
                    <p>Les cours seront bientôt disponibles.</p>
                </div>
            <% } %>
        </div>
    </section>

    <%-- ============================================================
         SECTION : MES QUIZ (6 THÈMES)
         ============================================================ --%>
    <section class="themes-section" id="quiz-section">
        <div class="section-header">
            <div class="section-header-left">
                <h2><i class="fas fa-layer-group"></i> Mes Quiz</h2>
                <span class="badge-count badge-quiz">
                    <i class="fas fa-puzzle-piece"></i> <%= themesQuiz != null ? themesQuiz.size() : 0 %> thème<%= themesQuiz != null && themesQuiz.size() > 1 ? "s" : "" %>
                </span>
            </div>
            <div class="section-header-right">
                <a href="${pageContext.request.contextPath}/joueur/historique" class="btn-voir-tout">
                    Historique <i class="fas fa-arrow-right"></i>
                </a>
            </div>
        </div>

        <div class="themes-grid">
            <% if (themesQuiz != null && !themesQuiz.isEmpty()) {
                for (Map<String, Object> theme : themesQuiz) {
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
            } else { %>
                <div class="empty-state full-width">
                    <div class="empty-icon"><i class="fas fa-inbox"></i></div>
                    <h3>Aucun thème de quiz disponible</h3>
                    <p>Les quiz ne sont pas encore configurés.</p>
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
                <h2><i class="fas fa-history"></i> Dernières activités</h2>
                <span class="badge-count badge-neutral">
                    <i class="fas fa-clock-rotate-left"></i>
                    <%= dernieresParties != null ? dernieresParties.size() : 0 %> activité<%= dernieresParties != null && dernieresParties.size() > 1 ? "s" : "" %>
                </span>
            </div>
            <div class="section-header-right">
                <a href="${pageContext.request.contextPath}/joueur/historique" class="btn-voir-tout">
                    Historique complet <i class="fas fa-arrow-right"></i>
                </a>
            </div>
        </div>

        <% if (dernieresParties != null && !dernieresParties.isEmpty()) { %>
            <div class="table-container">
                <table class="table-historique">
                    <thead>
                        <tr>
                            <th><i class="fas fa-file-lines"></i> Quiz</th>
                            <th class="th-center"><i class="fas fa-star"></i> Score</th>
                            <th class="th-center"><i class="fas fa-clock"></i> Temps</th>
                            <th class="th-center"><i class="fas fa-calendar"></i> Date</th>
                            <th class="th-action">Rejouer</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Partie partie : dernieresParties) {
                            double pourcentage = partie.getPourcentage();
                            String badgeClass;
                            String badgeIcon;
                            if (pourcentage >= 80) { badgeClass = "badge-excellent"; badgeIcon = "fa-check-circle"; }
                            else if (pourcentage >= 60) { badgeClass = "badge-bon"; badgeIcon = "fa-check-circle"; }
                            else if (pourcentage >= 40) { badgeClass = "badge-moyen"; badgeIcon = "fa-minus-circle"; }
                            else { badgeClass = "badge-faible"; badgeIcon = "fa-times-circle"; }
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
                                <td class="td-center">
                                    <span class="badge-score <%= badgeClass %>">
                                        <i class="fas <%= badgeIcon %>"></i>
                                        <%= partie.getScore() %>/<%= partie.getTotalQuestions() %>
                                        <small>(<%= String.format("%.0f%%", pourcentage) %>)</small>
                                    </span>
                                </td>
                                <td class="td-center">
                                    <span class="cell-time"><i class="far fa-clock"></i> <%= partie.getTempsFormate() %></span>
                                </td>
                                <td class="td-center">
                                    <span class="cell-date"><i class="far fa-calendar-alt"></i> <%= dateFormatee %></span>
                                </td>
                                <td class="td-center">
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
                <p>Lancez un quiz ou commencez un cours pour voir votre historique ici.</p>
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
                <h4>Suivez les cours en profondeur</h4>
                <p>Lisez chaque leçon attentivement. Les exemples de code sont faits pour être copiés et testés.</p>
            </div>
            <div class="tip-card">
                <div class="tip-icon"><i class="fas fa-redo"></i></div>
                <h4>Répétez les quiz</h4>
                <p>Rejouer un quiz améliore votre score et renforce votre mémoire à long terme.</p>
            </div>
            <div class="tip-card">
                <div class="tip-icon"><i class="fas fa-chart-line"></i></div>
                <h4>Suivez votre progression</h4>
                <p>Consultez votre historique régulièrement pour identifier vos points forts et vos faiblesses.</p>
            </div>
        </div>
    </section>

</div>

<%
    String dashboardJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/dashboard.js\"></script>";
    request.setAttribute("extraScripts", dashboardJS);
%>

<%@ include file="../footer.jsp" %>