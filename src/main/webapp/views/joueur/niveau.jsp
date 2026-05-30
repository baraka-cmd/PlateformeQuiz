<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.amijayquiz.models.Quiz" %>

<%-- ============================================================
     Fichier : niveau.jsp
     Rôle    : Page d'un niveau spécifique dans un thème Quiz
              MODIFIÉ : Parties 6+ masquées si non premium
              Badge "Premium" sur parties bloquées
              Message d'incitation au paiement
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

    // ===== 🆕 ATTRIBUTS PREMIUM =====
    Boolean estPremium = (Boolean) request.getAttribute("estPremium");
    Integer maxPartiesGratuites = (Integer) request.getAttribute("maxPartiesGratuites");
    String messagePremium = (String) request.getAttribute("messagePremium");

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
    if (estPremium == null) estPremium = false;
    if (maxPartiesGratuites == null) maxPartiesGratuites = 5;

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

    <%-- ===== BREADCRUMB ===== --%>
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

    <%-- ===== EN-TÊTE DU NIVEAU ===== --%>
    <header class="niveau-header">
        <div class="niveau-header-left">
            <div class="niveau-header-icon" style="background: linear-gradient(135deg, <%= couleurNiveau %>, <%= couleurNiveau %>dd);">
                <i class="<%= iconeNiveau %>"></i>
            </div>
            <div class="niveau-header-text">
                <h1>
                    <%= nomNiveauFormate %>
                    <%-- 🆕 BADGE PREMIUM --%>
                    <% if (estPremium) { %>
                        <span class="badge-premium-actif">
                            <i class="fas fa-crown"></i> Premium
                        </span>
                    <% } %>
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

    <%-- ===== 🆕 BANNIÈRE PREMIUM (NIVEAU ÉLÉMENTAIRE GRATUIT) ===== --%>
    <% if (!estPremium && "elementaire".equals(nomNiveau)) { %>
        <div class="premium-info-banner">
            <div class="premium-info-icon">
                <i class="fas fa-info-circle"></i>
            </div>
            <div class="premium-info-content">
                <strong>Accès gratuit limité</strong>
                <p>Vous avez accès aux <strong><%= maxPartiesGratuites %> premières parties</strong> gratuitement. 
                Passez Premium pour débloquer toutes les parties et tous les niveaux !</p>
            </div>
            <a href="${pageContext.request.contextPath}/paiement" class="premium-info-btn">
                <i class="fas fa-crown"></i> Premium (4$)
            </a>
        </div>
    <% } %>

    <%-- ===== BARRE DE PROGRESSION ===== --%>
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

    <%-- ===== ALERTE : PROCHAINE PARTIE À JOUER ===== --%>
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

    <%-- ===== ALERTE : NIVEAU TERMINÉ ===== --%>
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

    <%-- ===== GRILLE DES PARTIES ===== --%>
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
                    Boolean bloque = (Boolean) partieData.get("bloque");
                    boolean estBloque = (bloque != null && bloque);

                    // Passer les attributs au composant partie-card.jsp
                    request.setAttribute("quiz", quiz);
                    request.setAttribute("reussie", reussie);
                    request.setAttribute("estProchaine", estProchaine && !estBloque);
                    request.setAttribute("ordrePartie", ordrePartie);
                    request.setAttribute("estBloque", estBloque);
                %>
                    <% if (estBloque) { %>
                        <%-- 🆕 PARTIE BLOQUÉE (PREMIUM REQUIS) --%>
                        <div class="partie-card bloque-premium">
                            <div class="partie-card-lock">
                                <i class="fas fa-crown"></i>
                            </div>
                            <span class="partie-numero"><%= ordrePartie %></span>
                            <h4><%= quiz.getTitre() %></h4>
                            <p class="partie-premium-text">Premium requis</p>
                            <a href="${pageContext.request.contextPath}/paiement" class="partie-premium-link">
                                <i class="fas fa-unlock-alt"></i> 4$
                            </a>
                        </div>
                    <% } else { %>
                        <jsp:include page="components/partie-card.jsp" />
                    <% } %>
                <% } %>
            </div>

            <%-- 🆕 MESSAGE SI PARTIES BLOQUÉES ===== --%>
            <% 
                boolean hasPartiesBloquees = false;
                for (Map<String, Object> p : parties) {
                    if (p.get("bloque") != null && (Boolean) p.get("bloque")) {
                        hasPartiesBloquees = true;
                        break;
                    }
                }
                if (hasPartiesBloquees) { 
            %>
                <div class="parties-premium-cta">
                    <div class="premium-cta-card">
                        <div class="premium-cta-icon">
                            <i class="fas fa-crown"></i>
                        </div>
                        <div class="premium-cta-content">
                            <h4>Débloquez toutes les parties</h4>
                            <p>Accédez aux <%= totalParties - maxPartiesGratuites %> parties restantes de ce niveau, 
                            ainsi qu'à tous les niveaux Moyen et Avancé.</p>
                        </div>
                        <a href="${pageContext.request.contextPath}/paiement" class="premium-cta-btn">
                            <i class="fas fa-unlock-alt"></i> Premium (4$)
                        </a>
                    </div>
                </div>
            <% } %>
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

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="niveau-back">
        <a href="${pageContext.request.contextPath}/joueur/theme?categorie=<%= categorieEncodee %>" class="btn-retour-niveau">
            <i class="fas fa-arrow-left"></i>
            <span>Retour au thème <%= nomCategorie %></span>
        </a>
    </div>

</div>

<%
    String niveauJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/niveau.js\"></script>";
    request.setAttribute("extraScripts", niveauJS);
%>

<%@ include file="../footer.jsp" %>