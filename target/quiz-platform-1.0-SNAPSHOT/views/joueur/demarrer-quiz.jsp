<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Quiz" %>

<%-- ============================================================
     Fichier : demarrer-quiz.jsp
     Rôle    : Page de présentation d'un quiz avant de le lancer
              - Bannière avec icône du niveau et badges
              - Description du quiz
              - Informations (questions, durée, niveau, partie)
              - Règles du quiz
              - Seuil de réussite (8/10)
              - Bouton "Démarrer la partie"
              - Breadcrumb complet de navigation
     ============================================================ --%>

<%
    // Récupération des attributs depuis le servlet
    Quiz quiz = (Quiz) request.getAttribute("quiz");
    Integer nombreQuestions = (Integer) request.getAttribute("nombreQuestions");
    
    // Sécurité : redirection si quiz non trouvé
    if (quiz == null) {
        response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
        return;
    }
    
    // Extraire la catégorie et le nom de la partie du titre complet
    String titreComplet = quiz.getTitre();
    String categorie = "";
    String partieInfo = "";
    
    int indexSeparation = titreComplet.indexOf(" - ");
    if (indexSeparation > 0) {
        categorie = titreComplet.substring(0, indexSeparation);
        partieInfo = titreComplet.substring(indexSeparation + 3);
    } else {
        categorie = titreComplet;
        partieInfo = titreComplet;
    }

    // URLs encodées pour les liens du breadcrumb
    String categorieEncodee = java.net.URLEncoder.encode(categorie, "UTF-8");
    String niveauEncode = java.net.URLEncoder.encode(quiz.getNiveau(), "UTF-8");
    
    // Durée formatée
    int dureeMinutes = quiz.getDureeMaxMinutes();
    String dureeFormatee = dureeMinutes + " minute" + (dureeMinutes > 1 ? "s" : "");
    
    // Titre de la page
    request.setAttribute("pageTitle", partieInfo + " - " + categorie + " - AmijayQuiz");
%>


<%-- ============================================================
     STYLES SPÉCIFIQUES
     ============================================================ --%>
<%
    String demarrerCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/demarrer-quiz.css\">";
    request.setAttribute("extraHead", demarrerCSS);
%>

<%@ include file="../header.jsp" %>



<div class="demarrer-container">

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
            <span><%= categorie %></span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/niveau?categorie=<%= categorieEncodee %>&niveau=<%= niveauEncode %>" class="breadcrumb-link">
            <i class="<%= quiz.getIconeNiveau() %>"></i>
            <span><%= quiz.getNiveauFormate() %></span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-play-circle"></i>
            <span>Partie <%= quiz.getOrdrePartie() %></span>
        </span>
    </nav>

    <%-- ============================================================
         CARTE DE PRÉSENTATION DU QUIZ
         ============================================================ --%>
    <div class="quiz-presentation-card">
        
        <%-- Bannière supérieure --%>
        <div class="quiz-banner">
            <div class="banner-bg-animation"></div>
            
            <div class="banner-icon">
                <i class="<%= quiz.getIconeNiveau() %>"></i>
            </div>
            
            <h1 class="banner-titre"><%= partieInfo %></h1>
            <p class="banner-categorie"><%= categorie %></p>
            
            <div class="banner-badges">
                <span class="badge-niveau">
                    <i class="<%= quiz.getIconeNiveau() %>"></i>
                    <%= quiz.getNiveauFormate() %>
                </span>
                <span class="badge-niveau">
                    <i class="fas fa-hashtag"></i>
                    Partie <%= quiz.getOrdrePartie() %>
                </span>
                <span class="badge-niveau">
                    <i class="fas fa-clock"></i>
                    <%= dureeFormatee %>
                </span>
            </div>
        </div>
        
        <%-- Détails du quiz --%>
        <div class="quiz-details">
            
            <%-- Description du quiz --%>
            <% if (quiz.getDescription() != null && !quiz.getDescription().isEmpty()) { %>
                <div class="description-box">
                    <div class="description-icon">
                        <i class="fas fa-info-circle"></i>
                    </div>
                    <p class="description-text"><%= quiz.getDescription() %></p>
                </div>
            <% } %>
            
            <%-- Alerte seuil de réussite --%>
            <div class="seuil-alerte">
                <div class="seuil-icon">
                    <i class="fas fa-flag-checkered"></i>
                </div>
                <div class="seuil-content">
                    <strong>Objectif à atteindre</strong>
                    <p>Vous devez obtenir <span class="seuil-score">8/10</span> pour réussir cette partie et débloquer la suite du niveau.</p>
                </div>
            </div>
            
            <%-- Grille d'informations --%>
            <div class="infos-grid">
                <div class="info-card">
                    <div class="info-icon info-icon-questions">
                        <i class="fas fa-question-circle"></i>
                    </div>
                    <div class="info-text">
                        <h4><%= nombreQuestions %></h4>
                        <span>Questions</span>
                    </div>
                </div>
                
                <div class="info-card">
                    <div class="info-icon info-icon-duree">
                        <i class="fas fa-clock"></i>
                    </div>
                    <div class="info-text">
                        <h4><%= dureeMinutes %> min</h4>
                        <span>Durée maximale</span>
                    </div>
                </div>
                
                <div class="info-card">
                    <div class="info-icon info-icon-niveau">
                        <i class="<%= quiz.getIconeNiveau() %>"></i>
                    </div>
                    <div class="info-text">
                        <h4><%= quiz.getNiveauFormate() %></h4>
                        <span>Niveau de difficulté</span>
                    </div>
                </div>
                
                <div class="info-card">
                    <div class="info-icon info-icon-partie">
                        <i class="fas fa-hashtag"></i>
                    </div>
                    <div class="info-text">
                        <h4>Partie <%= quiz.getOrdrePartie() %></h4>
                        <span>Dans ce niveau</span>
                    </div>
                </div>
            </div>
            
            <%-- Règles du quiz --%>
            <div class="regles-box">
                <h3 class="regles-titre">
                    <i class="fas fa-gavel"></i>
                    Règles du quiz
                </h3>
                <ul class="regles-liste">
                    <li>
                        <i class="fas fa-dot-circle"></i>
                        <span>Une seule réponse possible par question.</span>
                    </li>
                    <li>
                        <i class="fas fa-stopwatch"></i>
                        <span>Le chronomètre démarre dès que vous lancez la partie.</span>
                    </li>
                    <li>
                        <i class="fas fa-arrows-left-right"></i>
                        <span>Vous pouvez naviguer librement entre les questions (précédent / suivant).</span>
                    </li>
                    <li>
                        <i class="fas fa-hourglass-end"></i>
                        <span>La partie s'arrête automatiquement si le temps imparti est écoulé.</span>
                    </li>
                    <li>
                        <i class="fas fa-trophy"></i>
                        <span>Score minimum requis : <strong>8/10</strong> pour valider la partie.</span>
                    </li>
                    <li>
                        <i class="fas fa-lock-open"></i>
                        <span>Validez toutes les parties d'un niveau pour débloquer le niveau supérieur.</span>
                    </li>
                </ul>
            </div>
            
            <%-- Bouton Démarrer --%>
            <form action="${pageContext.request.contextPath}/joueur/demarrer-quiz" method="POST" class="demarrer-form">
                <input type="hidden" name="idQuiz" value="<%= quiz.getId() %>">
                <button type="submit" class="btn-demarrer">
                    <i class="fas fa-play-circle"></i>
                    <span>Démarrer la partie</span>
                    <span class="btn-demarrer-hint">Appuyez sur Entrée ↵</span>
                </button>
            </form>
            
            <%-- Bouton retour --%>
            <a href="${pageContext.request.contextPath}/joueur/niveau?categorie=<%= categorieEncodee %>&niveau=<%= niveauEncode %>" class="btn-retour">
                <i class="fas fa-arrow-left"></i>
                <span>Retour au niveau <%= quiz.getNiveauFormate() %></span>
            </a>
            
        </div>
    </div>
</div>

<%-- ============================================================
     SCRIPTS SPÉCIFIQUES
     ============================================================ --%>
<%
    String demarrerJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/demarrer-quiz.js\"></script>";
    request.setAttribute("extraScripts", demarrerJS);
%>

<%@ include file="../footer.jsp" %>