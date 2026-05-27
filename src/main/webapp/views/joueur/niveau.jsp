<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.amijayquiz.models.Quiz" %>

<%-- ============================================================
     Fichier : niveau.jsp
     Rôle    : Page d'un niveau spécifique dans un thème
              - Affiche toutes les parties (quiz) du niveau
              - Met en évidence la prochaine partie à jouer
              - Barre de progression du niveau
              - Message de félicitations si niveau terminé
              - Alerte pour le niveau suivant débloqué
              - Grille responsive des parties (5 colonnes → 2 colonnes)
              - Chaque partie utilise le composant partie-card.jsp
     ============================================================ --%>

<%
    // Récupération des attributs depuis le servlet
    String nomCategorie = (String) request.getAttribute("nomCategorie");
    String nomNiveau = (String) request.getAttribute("nomNiveau");
    String nomNiveauFormate = (String) request.getAttribute("nomNiveauFormate");
    String iconeNiveau = (String) request.getAttribute("iconeNiveau");
    String couleurNiveau = (String) request.getAttribute("couleurNiveau");
    List<Map<String, Object>> parties = (List<Map<String, Object>>) request.getAttribute("parties");
    Integer totalParties = (Integer) request.getAttribute("totalParties");
    Integer partiesReussies = (Integer) request.getAttribute("partiesReussies");
    Integer pourcentageProgression = (Integer) request.getAttribute("pourcentageProgression");
    Quiz prochainQuiz = (Quiz) request.getAttribute("prochainQuiz");
    Boolean niveauTermine = (Boolean) request.getAttribute("niveauTermine");
    Boolean niveauDebloque = (Boolean) request.getAttribute("niveauDebloque");

    // Valeurs par défaut sécurisées
    if (nomCategorie == null) nomCategorie = "Thème";
    if (nomNiveau == null) nomNiveau = "elementaire";
    if (nomNiveauFormate == null) nomNiveauFormate = "Niveau";
    if (iconeNiveau == null) iconeNiveau = "fas fa-circle";
    if (couleurNiveau == null) couleurNiveau = "#888888";
    if (totalParties == null) totalParties = 0;
    if (partiesReussies == null) partiesReussies = 0;
    if (pourcentageProgression == null) pourcentageProgression = 0;
    if (niveauTermine == null) niveauTermine = false;
    if (niveauDebloque == null) niveauDebloque = false;

    // URL encodée pour les liens
    String categorieEncodee = java.net.URLEncoder.encode(nomCategorie, "UTF-8");
    
    // Message de statut
    String statutMessage = "";
    String statutIcone = "";
    if (niveauTermine) {
        statutMessage = "Niveau terminé - Félicitations !";
        statutIcone = "fas fa-trophy";
    } else if (partiesReussies == 0) {
        statutMessage = "Commencez votre première partie !";
        statutIcone = "fas fa-play-circle";
    } else {
        statutMessage = "Continuez vos efforts !";
        statutIcone = "fas fa-forward";
    }

    // Titre de la page
    request.setAttribute("pageTitle", nomNiveauFormate + " - " + nomCategorie + " - AmijayQuiz");
%>



<%
    String niveauCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/niveau.css\">"
                     + "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/partie-card.css\">";
    request.setAttribute("extraHead", niveauCSS);
%>

<%@ include file="../header.jsp" %>

<div class="niveau-page-container">

    <%-- ============================================================
         BREADCRUMB
         ============================================================ --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i>
            <span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/theme?categorie=<%= categorieEncodee %>" class="breadcrumb-link">
            <i class="fas fa-layer-group"></i>
            <span><%= nomCategorie %></span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="<%= iconeNiveau %>"></i>
            <span><%= nomNiveauFormate %></span>
        </span>
    </nav>

    <%-- ============================================================
         EN-TÊTE DU NIVEAU
         ============================================================ --%>
    <header class="niveau-header">
        <div class="niveau-header-left">
            <div class="niveau-header-icon" style="background: linear-gradient(135deg, <%= couleurNiveau %>, <%= couleurNiveau %>dd);">
                <i class="<%= iconeNiveau %>"></i>
            </div>
            <div class="niveau-header-text">
                <h1>
                    <%= nomNiveauFormate %>
                </h1>
                <p>
                    <i class="fas fa-check-circle"></i>
                    <strong><%= partiesReussies %></strong> / <%= totalParties %> parties réussies
                </p>
                <p class="niveau-theme-label">
                    <i class="fas fa-tag"></i>
                    Thème : <strong><%= nomCategorie %></strong>
                </p>
            </div>
        </div>
        
        <div class="niveau-statut-badge <%= niveauTermine ? "statut-termine" : "statut-encours" %>">
            <i class="<%= statutIcone %>"></i>
            <span><%= statutMessage %></span>
        </div>
    </header>

    <%-- ============================================================
         BARRE DE PROGRESSION
         ============================================================ --%>
    <section class="progression-section">
        <div class="progression-header">
            <h3>
                <i class="fas fa-chart-simple"></i>
                Progression du niveau
            </h3>
            <span class="progression-pourcentage"><%= pourcentageProgression %>%</span>
        </div>
        <div class="progression-bar-outer">
            <div class="progression-bar-inner" style="width: <%= pourcentageProgression %>%; background: <%= couleurNiveau %>;"></div>
        </div>
        <div class="progression-detail">
            <span>0%</span>
            <span class="progression-seuil">
                <i class="fas fa-flag-checkered"></i>
                Score minimum : <strong>8/10</strong>
            </span>
            <span>100%</span>
        </div>
    </section>

    <%-- ============================================================
         ALERTE : PROCHAINE PARTIE À JOUER
         ============================================================ --%>
    <% if (prochainQuiz != null && !niveauTermine) { %>
        <div class="alerte-info alerte-prochaine">
            <div class="alerte-icon-wrapper">
                <i class="fas fa-bullseye"></i>
            </div>
            <div class="alerte-content">
                <strong>Votre prochaine partie : Partie <%= prochainQuiz.getOrdrePartie() %></strong>
                <p>Elle est mise en évidence dans la grille ci-dessous. Cliquez sur <em>"Jouer"</em> pour la démarrer.</p>
            </div>
            <a href="${pageContext.request.contextPath}/joueur/demarrer-quiz?id=<%= prochainQuiz.getId() %>" class="alerte-btn">
                <i class="fas fa-play"></i> Jouer
            </a>
        </div>
    <% } %>

    <%-- ============================================================
         ALERTE : NIVEAU TERMINÉ (FÉLICITATIONS)
         ============================================================ --%>
    <% if (niveauTermine) { %>
        <div class="alerte-info alerte-termine">
            <div class="alerte-icon-wrapper">
                <i class="fas fa-trophy"></i>
            </div>
            <div class="alerte-content">
                <strong>Félicitations ! Niveau terminé avec succès !</strong>
                <p>
                    Vous avez réussi toutes les parties de ce niveau. 
                    <% if (!"avance".equals(nomNiveau)) { %>
                        Le niveau suivant est désormais <strong>débloqué</strong> !
                    <% } else { %>
                        Vous maîtrisez complètement ce thème !
                    <% } %>
                </p>
            </div>
        </div>
    <% } %>

    <%-- ============================================================
         GRILLE DES PARTIES
         ============================================================ --%>
    <section class="parties-section">
        <div class="section-header">
            <h2>
                <i class="fas fa-th-large"></i>
                Parties du niveau
            </h2>
            <span class="badge-count">
                <i class="fas fa-hashtag"></i>
                <%= totalParties %> partie<%= totalParties > 1 ? "s" : "" %>
            </span>
        </div>

        <% if (parties != null && !parties.isEmpty()) { %>
            <div class="parties-grid">
                <% for (Map<String, Object> partieData : parties) {
                    Quiz quiz = (Quiz) partieData.get("quiz");
                    boolean reussie = (Boolean) partieData.get("reussie");
                    boolean estProchaine = (Boolean) partieData.get("estProchaine");
                    int ordrePartie = (Integer) partieData.get("ordrePartie");

                    // Passer les attributs au composant partie-card.jsp
                    request.setAttribute("quiz", quiz);
                    request.setAttribute("reussie", reussie);
                    request.setAttribute("estProchaine", estProchaine);
                    request.setAttribute("ordrePartie", ordrePartie);
                %>
                    <jsp:include page="components/partie-card.jsp" />
                <% } %>
            </div>
        <% } else { %>
            <div class="empty-state">
                <div class="empty-icon">
                    <i class="fas fa-inbox"></i>
                </div>
                <h3>Aucune partie trouvée</h3>
                <p>Ce niveau ne contient pas encore de quiz configurés.</p>
                <a href="${pageContext.request.contextPath}/joueur/theme?categorie=<%= categorieEncodee %>" class="btn-retour-empty">
                    <i class="fas fa-arrow-left"></i> Retour au thème
                </a>
            </div>
        <% } %>
    </section>

    <%-- ============================================================
         BOUTON RETOUR
         ============================================================ --%>
    <div class="niveau-back">
        <a href="${pageContext.request.contextPath}/joueur/theme?categorie=<%= categorieEncodee %>" class="btn-retour-niveau">
            <i class="fas fa-arrow-left"></i>
            <span>Retour au thème <%= nomCategorie %></span>
        </a>
    </div>

</div>

<%-- ============================================================
     SCRIPTS SPÉCIFIQUES À LA PAGE NIVEAU
     ============================================================ --%>
<%
    String niveauJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/niveau.js\"></script>";
    request.setAttribute("extraScripts", niveauJS);
%>

<%@ include file="../footer.jsp" %>