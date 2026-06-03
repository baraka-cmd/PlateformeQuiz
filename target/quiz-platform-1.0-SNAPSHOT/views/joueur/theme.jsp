<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.amijayquiz.models.Quiz" %>

<%-- ============================================================
     Fichier : theme.jsp
     Rôle    : Page d'un thème spécifique
              - Affiche les 3 niveaux (élémentaire, moyen, avancé)
              - Chaque niveau est un composant niveau-card.jsp
              - Barre de progression globale du thème
              - Breadcrumb de navigation
              - Message de motivation selon la progression
              - Bouton retour au dashboard
     ============================================================ --%>

<%
    // Récupération des attributs depuis le servlet
    String nomCategorie = (String) request.getAttribute("nomCategorie");
    String iconeCategorie = (String) request.getAttribute("iconeCategorie");
    String couleurCategorie = (String) request.getAttribute("couleurCategorie");
    Integer totalPartiesTheme = (Integer) request.getAttribute("totalPartiesTheme");
    Integer partiesReussiesTheme = (Integer) request.getAttribute("partiesReussiesTheme");
    Integer pourcentageProgressionTheme = (Integer) request.getAttribute("pourcentageProgressionTheme");
    List<Map<String, Object>> niveaux = (List<Map<String, Object>>) request.getAttribute("niveaux");

    // Valeurs par défaut
    if (nomCategorie == null) nomCategorie = "Thème";
    if (iconeCategorie == null) iconeCategorie = "fas fa-book";
    if (couleurCategorie == null) couleurCategorie = "#667eea";
    if (totalPartiesTheme == null) totalPartiesTheme = 0;
    if (partiesReussiesTheme == null) partiesReussiesTheme = 0;
    if (pourcentageProgressionTheme == null) pourcentageProgressionTheme = 0;

    // Message d'erreur éventuel
    String messageErreur = (String) session.getAttribute("messageErreur");
    if (messageErreur != null) session.removeAttribute("messageErreur");

    // Message de motivation selon la progression
    String messageMotivation = "";
    String iconeMotivation = "";
    if (pourcentageProgressionTheme == 0) {
        messageMotivation = "Commencez par le niveau Élémentaire pour débloquer la suite !";
        iconeMotivation = "fas fa-rocket";
    } else if (pourcentageProgressionTheme < 33) {
        messageMotivation = "Bon début ! Continuez sur votre lancée pour débloquer le niveau Moyen.";
        iconeMotivation = "fas fa-thumbs-up";
    } else if (pourcentageProgressionTheme < 66) {
        messageMotivation = "Vous progressez bien ! Le niveau Avancé est à votre portée.";
        iconeMotivation = "fas fa-fire";
    } else if (pourcentageProgressionTheme < 100) {
        messageMotivation = "Plus que quelques parties pour maîtriser ce thème !";
        iconeMotivation = "fas fa-trophy";
    } else {
        messageMotivation = "Félicitations ! Vous maîtrisez complètement ce thème !";
        iconeMotivation = "fas fa-crown";
    }

    // Déterminer le niveau actif (premier non terminé)
    String niveauActif = "Élémentaire";
    if (niveaux != null) {
        for (Map<String, Object> niv : niveaux) {
            Boolean termine = (Boolean) niv.get("termine");
            if (termine != null && !termine) {
                niveauActif = (String) niv.get("nomFormate");
                break;
            }
        }
    }

    // Titre de la page
    request.setAttribute("pageTitle", nomCategorie + " - AmijayQuiz");
%>



<%
    String themeCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/theme.css\">"
                    + "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/niveau-card.css\">";
    request.setAttribute("extraHead", themeCSS);
%>

<%@ include file="../header.jsp" %>

<div class="theme-page-container">

    <%-- ===== MESSAGE D'ERREUR ===== --%>
    <% if (messageErreur != null) { %>
        <div class="message-alerte message-erreur">
            <i class="fas fa-exclamation-circle"></i>
            <span><%= messageErreur %></span>
        </div>
    <% } %>

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
            <i class="<%= iconeCategorie %>"></i>
            <span><%= nomCategorie %></span>
        </span>
    </nav>

    <%-- ============================================================
         EN-TÊTE DU THÈME
         ============================================================ --%>
    <header class="theme-header">
        <div class="theme-icon-large" style="background: linear-gradient(135deg, <%= couleurCategorie %>, <%= couleurCategorie %>dd);">
            <i class="<%= iconeCategorie %>"></i>
        </div>
        
        <h1 class="theme-title"><%= nomCategorie %></h1>
        
        <div class="theme-stats">
            <span class="theme-stat-item">
                <i class="fas fa-check-circle"></i>
                <strong><%= partiesReussiesTheme %></strong> / <%= totalPartiesTheme %> parties réussies
            </span>
            <span class="theme-stat-separator">•</span>
            <span class="theme-stat-item">
                <i class="fas fa-layer-group"></i>
                <strong><%= niveaux != null ? niveaux.size() : 0 %></strong> niveaux
            </span>
            <span class="theme-stat-separator">•</span>
            <span class="theme-stat-item">
                <i class="fas fa-bullseye"></i>
                Niveau actuel : <strong><%= niveauActif %></strong>
            </span>
        </div>

        <%-- Barre de progression globale --%>
        <div class="theme-progress-bar">
            <div class="progress-label">
                <span>Progression globale du thème</span>
                <span class="progress-value"><%= pourcentageProgressionTheme %>%</span>
            </div>
            <div class="progress-bar-outer">
                <div class="progress-bar-inner" style="width: <%= pourcentageProgressionTheme %>%; background: linear-gradient(90deg, <%= couleurCategorie %>, <%= couleurCategorie %>cc);"></div>
            </div>
        </div>

        <%-- Message de motivation --%>
        <div class="motivation-banner" style="border-left-color: <%= couleurCategorie %>;">
            <i class="<%= iconeMotivation %>" style="color: <%= couleurCategorie %>;"></i>
            <p><%= messageMotivation %></p>
        </div>
    </header>

    <%-- ============================================================
         GRILLE DES 3 NIVEAUX
         ============================================================ --%>
    <section class="niveaux-section">
        <div class="section-header">
            <h2>
                <i class="fas fa-signal"></i>
                Choisissez votre niveau
            </h2>
            <span class="section-hint">
                <i class="fas fa-info-circle"></i>
                Terminez un niveau pour débloquer le suivant
            </span>
        </div>

        <div class="niveaux-grid">
            <% if (niveaux != null && !niveaux.isEmpty()) {
                for (Map<String, Object> niveau : niveaux) {

                    // Extraire les données du niveau
                    String nomNiveau = (String) niveau.get("nom");
                    String nomFormate = (String) niveau.get("nomFormate");
                    String iconeNiveau = (String) niveau.get("icone");
                    String couleurNiveau = (String) niveau.get("couleur");
                    int totalParties = (Integer) niveau.get("totalParties");
                    boolean debloque = (Boolean) niveau.get("debloque");
                    boolean termine = (Boolean) niveau.get("termine");
                    int partiesReussies = (Integer) niveau.get("partiesReussies");
                    int pourcentageProgression = (Integer) niveau.get("pourcentageProgression");
                    Quiz prochainQuiz = (Quiz) niveau.get("prochainQuiz");

                    // Passer les attributs au composant niveau-card.jsp
                    request.setAttribute("nomCategorie", nomCategorie);
                    request.setAttribute("nomNiveau", nomNiveau);
                    request.setAttribute("nomFormate", nomFormate);
                    request.setAttribute("iconeNiveau", iconeNiveau);
                    request.setAttribute("couleurNiveau", couleurNiveau);
                    request.setAttribute("totalParties", totalParties);
                    request.setAttribute("partiesReussies", partiesReussies);
                    request.setAttribute("pourcentageProgression", pourcentageProgression);
                    request.setAttribute("debloque", debloque);
                    request.setAttribute("termine", termine);
                    request.setAttribute("prochainQuiz", prochainQuiz);
            %>
                    <jsp:include page="components/niveau-card.jsp" />
            <%  }
            } else { %>
                <div class="empty-state full-width">
                    <div class="empty-icon">
                        <i class="fas fa-inbox"></i>
                    </div>
                    <h3>Aucun niveau trouvé</h3>
                    <p>Ce thème ne contient pas encore de niveaux configurés.</p>
                    <a href="${pageContext.request.contextPath}/joueur/dashboard" class="btn-retour-empty">
                        <i class="fas fa-arrow-left"></i> Retour au dashboard
                    </a>
                </div>
            <% } %>
        </div>
    </section>

    <%-- ============================================================
         BOUTON RETOUR
         ============================================================ --%>
    <div class="theme-back">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="btn-retour-theme">
            <i class="fas fa-arrow-left"></i>
            <span>Retour au dashboard</span>
        </a>
    </div>

</div>

<%-- ============================================================
     SCRIPTS SPÉCIFIQUES À LA PAGE THÈME
     ============================================================ --%>
<%
    String themeJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/theme.js\"></script>";
    request.setAttribute("extraScripts", themeJS);
%>

<%@ include file="../footer-connecte.jsp" %>