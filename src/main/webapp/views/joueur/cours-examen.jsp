<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.Question" %>

<%-- ============================================================
     Fichier : cours-examen.jsp
     Rôle    : Page de passage de l'examen final d'un cours
              Affiche les questions une par une avec navigation
              Timer, barre de progression, modal de confirmation
     ============================================================ --%>

<%
    Cours cours = (Cours) request.getAttribute("cours");
    List<Question> questions = (List<Question>) request.getAttribute("questions");
    Integer nombreQuestions = (Integer) request.getAttribute("nombreQuestions");
    Double seuilReussite = (Double) request.getAttribute("seuilReussite");

    if (cours == null || questions == null || questions.isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/joueur/cours");
        return;
    }

    if (nombreQuestions == null) nombreQuestions = questions.size();
    if (seuilReussite == null) seuilReussite = 80.0;

    String themeEncode = java.net.URLEncoder.encode(cours.getTheme(), "UTF-8");
    String niveauEncode = java.net.URLEncoder.encode(cours.getNiveau(), "UTF-8");
%>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Examen Final - <%= cours.getTitre() %> - AmijayQuiz</title>
    
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/css/fonts.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/fontawesome/fontawesome-free-7.2.0-web/css/all.min.css">
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/css/cours-examen.css">
</head>
<body>

<%-- ============================================================
     BARRE SUPÉRIEURE (STICKY)
     ============================================================ --%>
<header class="examen-header">
    <div class="examen-header-inner">
        
        <%-- Breadcrumb compact --%>
        <nav class="examen-breadcrumb" aria-label="Fil d'Ariane">
            <a href="${pageContext.request.contextPath}/joueur/dashboard" class="examen-breadcrumb-link">
                <i class="fas fa-tachometer-alt"></i> Dashboard
            </a>
            <i class="fas fa-chevron-right examen-breadcrumb-sep"></i>
            <a href="${pageContext.request.contextPath}/joueur/cours" class="examen-breadcrumb-link">
                <i class="fas fa-graduation-cap"></i> Cours
            </a>
            <i class="fas fa-chevron-right examen-breadcrumb-sep"></i>
            <a href="${pageContext.request.contextPath}/joueur/cours/theme?theme=<%= themeEncode %>" class="examen-breadcrumb-link">
                <%= cours.getTheme() %>
            </a>
            <i class="fas fa-chevron-right examen-breadcrumb-sep"></i>
            <a href="${pageContext.request.contextPath}/joueur/cours/niveau?theme=<%= themeEncode %>&niveau=<%= niveauEncode %>" class="examen-breadcrumb-link">
                <%= cours.getNiveauFormate() %>
            </a>
            <i class="fas fa-chevron-right examen-breadcrumb-sep"></i>
            <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" class="examen-breadcrumb-link">
                <i class="fas fa-sitemap"></i> Plan
            </a>
            <i class="fas fa-chevron-right examen-breadcrumb-sep"></i>
            <span class="examen-breadcrumb-current">
                <i class="fas fa-trophy"></i> Examen Final
            </span>
        </nav>

        <%-- Infos examen + Timer --%>
        <div class="examen-header-main">
            <div class="examen-infos">
                <span class="examen-cours-badge" style="background: <%= cours.getCouleurTheme() %>;">
                    <i class="<%= cours.getIconeTheme() %>"></i> <%= cours.getTitre() %>
                </span>
                <h1 class="examen-titre">Examen Final</h1>
            </div>

            <%-- Timer --%>
            <div class="examen-timer-container">
                <div class="examen-timer-box" id="timerBox">
                    <i class="fas fa-clock timer-icon"></i>
                    <span class="timer-display" id="timerDisplay">30:00</span>
                </div>
                <span class="timer-label">restantes</span>
            </div>

            <%-- Progression --%>
            <div class="examen-progress-top">
                <span class="progress-text">Question <strong id="currentQuestion">1</strong> / <%= nombreQuestions %></span>
                <div class="progress-track">
                    <div class="progress-fill" id="progressFill" style="width: 0%;"></div>
                </div>
            </div>
        </div>
    </div>
</header>

<%-- ============================================================
     CONTENU PRINCIPAL
     ============================================================ --%>
<main class="examen-main">
    <form id="examenForm" action="${pageContext.request.contextPath}/joueur/cours/examen" method="POST">
        <input type="hidden" name="coursId" value="<%= cours.getId() %>">

        <%-- ===== CARTE QUESTION ===== --%>
        <div class="question-card" id="questionCard">
            <% for (int i = 0; i < questions.size(); i++) {
                Question question = questions.get(i);
            %>
                <div class="question-slide" id="questionSlide<%= i %>" style="display: <%= i == 0 ? "block" : "none" %>;">
                    
                    <%-- Numéro de question --%>
                    <div class="question-badge">
                        <span>Question <%= i + 1 %> sur <%= nombreQuestions %></span>
                    </div>

                    <%-- Texte de la question --%>
                    <h2 class="question-texte"><%= question.getTexteQuestion() %></h2>

                    <%-- Propositions --%>
                    <div class="propositions-list">
                        <% String[] lettres = {"A", "B", "C", "D"};
                           for (int p = 1; p <= 4; p++) {
                               String proposition = question.getProposition(p);
                        %>
                            <div class="proposition-item" data-question="<%= i %>" data-reponse="<%= p %>">
                                <input type="radio" 
                                       name="reponse_<%= i %>" 
                                       id="reponse_<%= i %>_<%= p %>" 
                                       value="<%= p %>"
                                       class="proposition-radio">
                                <label for="reponse_<%= i %>_<%= p %>" class="proposition-label">
                                    <span class="proposition-lettre"><%= lettres[p-1] %></span>
                                    <span class="proposition-texte"><%= proposition %></span>
                                    <span class="proposition-check">
                                        <i class="fas fa-check-circle"></i>
                                    </span>
                                </label>
                            </div>
                        <% } %>
                    </div>
                </div>
            <% } %>
        </div>

        <%-- ===== INDICATEURS DE QUESTIONS ===== --%>
        <div class="questions-indicators">
            <% for (int i = 0; i < questions.size(); i++) { %>
                <button type="button" 
                        class="indicator-dot" 
                        data-index="<%= i %>"
                        title="Question <%= i + 1 %>"
                        aria-label="Aller à la question <%= i + 1 %>"
                        onclick="naviguerVersQuestion(<%= i %>)">
                </button>
            <% } %>
        </div>

        <%-- ===== RAPPEL SEUIL ===== --%>
        <div class="seuil-rappel">
            <i class="fas fa-info-circle"></i>
            Score minimum requis pour réussir l'examen : <strong><%= String.format("%.0f", seuilReussite) %>%</strong>
        </div>

        <%-- ===== NAVIGATION ===== --%>
        <div class="examen-navigation">
            <button type="button" class="btn-nav-examen btn-precedent" id="btnPrecedent" onclick="naviguerQuestion(-1)">
                <i class="fas fa-arrow-left"></i>
                <span>Précédent</span>
            </button>

            <span class="nav-question-counter">
                <span id="navCurrent">1</span> / <%= nombreQuestions %>
            </span>

            <button type="button" class="btn-nav-examen btn-suivant" id="btnSuivant" onclick="naviguerQuestion(1)">
                <span>Suivant</span>
                <i class="fas fa-arrow-right"></i>
            </button>

            <button type="button" class="btn-nav-examen btn-terminer" id="btnTerminer" onclick="ouvrirModalConfirmation()" style="display: none;">
                <i class="fas fa-flag-checkered"></i>
                <span>Terminer l'examen</span>
            </button>
        </div>
    </form>
</main>

<%-- ============================================================
     MODAL DE CONFIRMATION
     ============================================================ --%>
<div class="modal-overlay" id="modalConfirmation" role="dialog" aria-modal="true" aria-labelledby="modalTitle">
    <div class="modal-card">
        <div class="modal-icon">
            <i class="fas fa-question-circle"></i>
        </div>
        <h3 id="modalTitle">Terminer l'examen ?</h3>
        <p>
            Vous ne pourrez plus modifier vos réponses après avoir terminé.<br>
            Score minimum requis : <strong><%= String.format("%.0f", seuilReussite) %>%</strong>
        </p>
        
        <%-- Questions non répondues --%>
        <div class="modal-questions-non-repondues" id="questionsNonRepondues" style="display: none;">
            <p class="warning-text">
                <i class="fas fa-exclamation-triangle"></i>
                <span>Attention : <strong id="nbNonRepondues">0</strong> question(s) sans réponse.</span>
            </p>
        </div>

        <div class="modal-buttons">
            <button type="button" class="btn-modal btn-modal-annuler" onclick="fermerModalConfirmation()">
                <i class="fas fa-times"></i> Annuler
            </button>
            <button type="button" class="btn-modal btn-modal-confirmer" onclick="terminerExamen()">
                <i class="fas fa-check"></i> Confirmer
            </button>
        </div>
    </div>
</div>

<%-- ============================================================
     SCRIPTS
     ============================================================ --%>
<script>
    var TOTAL_QUESTIONS = <%= nombreQuestions %>;
    var CURRENT_INDEX = 0;
    var TEMPS_RESTANT = 30 * 60; // 30 minutes en secondes
    var CONTEXT_PATH = '${pageContext.request.contextPath}';
    var COURS_ID = <%= cours.getId() %>;
</script>
<script src="${pageContext.request.contextPath}/views/assets/js/main.js"></script>
<script src="${pageContext.request.contextPath}/views/assets/js/cours-examen.js"></script>

</body>
</html>