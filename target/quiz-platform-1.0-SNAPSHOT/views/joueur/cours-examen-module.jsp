<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.ExamenModule" %>
<%@ page import="com.amijayquiz.models.QuestionExamen" %>
<%@ page import="com.amijayquiz.models.ProgressionModule" %>

<%-- ============================================================
     FICHIER : views/joueur/cours-examen-module.jsp
     RÔLE    : Page d'examen d'un module de cours AmijayQuiz
              - Questions a choix multiples (A, B, C, D)
              - Navigation entre les questions
              - Barre de progression avec indicateurs
              - Minuteur (optionnel, gere par le servlet)
              - Modal de confirmation avant de terminer
              - Avertissement questions sans reponse
              - Design epure et concentre sur l'examen
     ============================================================ --%>

<%
    // ===== RECUPERATION DES ATTRIBUTS =====
    Cours cours = (Cours) request.getAttribute("cours");
    ExamenModule examen = (ExamenModule) request.getAttribute("examen");
    List<QuestionExamen> questions = (List<QuestionExamen>) request.getAttribute("questions");
    Integer nombreQuestions = (Integer) request.getAttribute("nombreQuestions");
    Integer scoreMinimum = (Integer) request.getAttribute("scoreMinimum");
    Integer seuilReussite = (Integer) request.getAttribute("seuilReussite");
    ProgressionModule progressionExistante = (ProgressionModule) request.getAttribute("progressionExistante");

    // ===== VERIFICATION SECURITE =====
    if (cours == null || examen == null || questions == null || questions.isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/joueur/cours");
        return;
    }

    // ===== VALEURS PAR DEFAUT =====
    if (nombreQuestions == null) nombreQuestions = questions.size();
    if (scoreMinimum == null) scoreMinimum = 15;
    if (seuilReussite == null) seuilReussite = 75;

    String themeEncode = java.net.URLEncoder.encode(cours.getTheme(), "UTF-8");
    String niveauEncode = java.net.URLEncoder.encode(cours.getNiveau(), "UTF-8");

    boolean isRetentative = progressionExistante != null && !progressionExistante.isReussi();
    int tentativeNumero = progressionExistante != null ? progressionExistante.getTentatives() + 1 : 1;

    // Titre de la page
    request.setAttribute("pageTitle", "Examen " + examen.getModuleFormate() + " - " + cours.getTitre() + " - AmijayQuiz");
%>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Examen <%= examen.getModuleFormate() %> - <%= cours.getTitre() %> - AmijayQuiz</title>

    <%-- Polices et icones --%>
   
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/css/fonts.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/fontawesome/fontawesome-free-7.2.0-web/css/all.min.css">
    
    <%-- Font Awesome 6 --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/fontawesome/fontawesome-free-7.2.0-web/css/all.min.css">
    <%-- Styles --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/css/cours-examen-module.css">
</head>
<body>

<%-- ============================================================
     BARRE SUPERIEURE (STICKY)
     ============================================================ --%>
<header class="examen-module-header-bar">
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
                <i class="fas fa-clipboard-check"></i> <%= examen.getModuleFormate() %>
            </span>
        </nav>

        <%-- Infos examen --%>
        <div class="examen-header-main">
            <div class="examen-infos">
                <span class="examen-cours-badge" style="background: <%= cours.getCouleurTheme() %>;">
                    <i class="<%= cours.getIconeTheme() %>"></i> <%= cours.getTitre() %>
                </span>
                <h1 class="examen-titre">Examen <%= examen.getModuleFormate() %></h1>
            </div>

            <%-- Infos rapides --%>
            <div class="examen-infos-rapides">
                <span class="examen-info-item">
                    <i class="fas fa-flag-checkered"></i>
                    Score minimum : <strong><%= scoreMinimum %>/<%= nombreQuestions %> (<%= seuilReussite %>%)</strong>
                </span>
                <% if (isRetentative) { %>
                    <span class="examen-info-item retentative">
                        <i class="fas fa-redo"></i> Tentative <%= tentativeNumero %>
                    </span>
                <% } %>
            </div>
        </div>
    </div>
</header>

<%-- ============================================================
     BARRE DE PROGRESSION
     ============================================================ --%>
<div class="examen-progression-bar">
    <div class="progression-inner">
        <div class="progression-track">
            <div class="progression-fill" id="progressFill" style="width: 0%;"></div>
        </div>
        <div class="progression-stats">
            <span>Question <strong id="currentQuestionDisplay">1</strong> / <%= nombreQuestions %></span>
            <span class="progression-separator">|</span>
            <span><span id="answeredCount">0</span> / <%= nombreQuestions %> repondues</span>
        </div>
    </div>
</div>

<%-- ============================================================
     CONTENU PRINCIPAL
     ============================================================ --%>
<main class="examen-module-main">
    <%-- Notification de repassage --%>
    <%
        Boolean estRepassageAttr = (Boolean) request.getAttribute("estRepassage");
        boolean isRepassage = estRepassageAttr != null && estRepassageAttr;
        int currentModule = examen.getModuleNumero();

        if (isRepassage) {
    %>
        <div class="repassage-notification">
            <div class="repassage-notification-icon">
                <i class="fas fa-redo"></i>
            </div>
            <div class="repassage-notification-content">
                <strong>Mode repassage</strong>
                <p>
                    Vous repassez l'examen du Module <%= currentModule %>.
                    <% if (progressionExistante != null) { %>
                        Votre meilleur score actuel : <span class="repassage-best-score"><%= progressionExistante.getPourcentageFormate() %></span>.
                    <% } %>
                    Le nouveau score remplacera l'ancien seulement s'il est meilleur.
                </p>
            </div>
            <button type="button" class="repassage-notification-close" onclick="fermerNotificationRepassage()" title="Fermer">
                <i class="fas fa-times"></i>
            </button>
        </div>
    <% } %>

    <form id="examenForm" action="${pageContext.request.contextPath}/joueur/cours/examen-module" method="POST">
        <input type="hidden" name="coursId" value="<%= cours.getId() %>">
        <input type="hidden" name="module" value="<%= examen.getModuleNumero() %>">

        <%-- ===== CARTE QUESTION ===== --%>
        <div class="question-card-container">
            <%
                String[] lettres = {"A", "B", "C", "D"};
                for (int i = 0; i < questions.size(); i++) {
                    QuestionExamen q = questions.get(i);
                    if (q == null) continue;

                    String texteQuestion = q.getQuestion();
                    if (texteQuestion == null) texteQuestion = "";

                    texteQuestion = texteQuestion.replace("&", "&amp;")
                                                 .replace("<", "&lt;")
                                                 .replace(">", "&gt;")
                                                 .replace("\"", "&quot;");
            %>
                <div class="question-card" id="questionCard<%= i %>" style="display: <%= i == 0 ? "block" : "none" %>;">
                    <div class="question-header">
                        <span class="question-numero">Question <%= i + 1 %></span>
                        <span class="question-statut" id="questionStatut<%= i %>">
                            <i class="fas fa-circle"></i> En attente
                        </span>
                    </div>
                    <div class="question-texte"><%= texteQuestion %></div>
                    <div class="propositions-list">
                        <%
                            for (int p = 1; p <= 4; p++) {
                                String proposition = q.getProposition(p);
                                if (proposition != null && !proposition.trim().isEmpty()) {
                                    proposition = proposition.replace("&", "&amp;")
                                                             .replace("<", "&lt;")
                                                             .replace(">", "&gt;")
                                                             .replace("\"", "&quot;");
                        %>
                            <label class="proposition-item">
                                <input type="radio"
                                       name="reponse_<%= i %>"
                                       value="<%= p %>"
                                       onchange="onReponseChange(<%= i %>, <%= p %>)">
                                <span class="proposition-lettre"><%= lettres[p-1] %></span>
                                <span class="proposition-texte"><%= proposition %></span>
                                <span class="proposition-check">
                                    <i class="fas fa-check-circle"></i>
                                </span>
                            </label>
                        <%
                                }
                            }
                        %>
                    </div>
                </div>
            <%
                }
            %>
        </div>

        <%-- ===== INDICATEURS DE QUESTIONS ===== --%>
        <div class="questions-indicators">
            <span class="indicators-label">Navigation rapide :</span>
            <div class="indicators-grid">
                <% for (int i = 0; i < questions.size(); i++) { %>
                    <button type="button"
                            class="indicator-dot"
                            id="indicator<%= i %>"
                            data-index="<%= i %>"
                            onclick="naviguerVersQuestion(<%= i %>)"
                            title="Aller a la question <%= i + 1 %>"
                            aria-label="Question <%= i + 1 %>">
                        <%= i + 1 %>
                    </button>
                <% } %>
            </div>
        </div>

        <%-- ===== NAVIGATION ===== --%>
        <div class="examen-navigation">
            <button type="button" class="btn-nav-examen btn-precedent" id="btnPrecedent" onclick="naviguerQuestion(-1)">
                <i class="fas fa-arrow-left"></i>
                <span>Precedent</span>
            </button>

            <span class="nav-question-counter">
                <span id="navCurrent">1</span> / <%= nombreQuestions %>
            </span>

            <button type="button" class="btn-nav-examen btn-suivant" id="btnSuivant" onclick="naviguerQuestion(1)">
                <span>Suivant</span>
                <i class="fas fa-arrow-right"></i>
            </button>

            <button type="button" class="btn-nav-examen btn-terminer" id="btnTerminer" onclick="ouvrirModalConfirmation()">
                <i class="fas fa-flag-checkered"></i>
                <span>Terminer l'examen</span>
            </button>
        </div>
    </form>
</main>

<%-- ============================================================
     MODAL DE CONFIRMATION
     ============================================================ --%>
<div class="modal-overlay" id="modalConfirmation" style="display: none;" role="dialog" aria-modal="true" aria-labelledby="modalTitle">
    <div class="modal-card">
        <div class="modal-icon">
            <i class="fas fa-question-circle"></i>
        </div>
        <h3 id="modalTitle">Terminer l'examen ?</h3>
        <p>Vous ne pourrez plus modifier vos reponses apres avoir termine.</p>

        <div class="modal-info-rappel">
            <i class="fas fa-info-circle"></i>
            Score minimum requis : <strong><%= scoreMinimum %>/<%= nombreQuestions %> (<%= seuilReussite %>%)</strong>
        </div>

        <div class="modal-warning" id="questionsNonRepondues" style="display: none;">
            <i class="fas fa-exclamation-triangle"></i>
            <span>Attention : <strong id="nbNonRepondues">0</strong> question(s) sans reponse.</span>
        </div>

        <div class="modal-resume" id="modalResume" style="display: none;">
            <p><span id="modalRepondues">0</span> / <%= nombreQuestions %> questions repondues</p>
            <div class="modal-mini-progress">
                <div class="modal-mini-progress-fill" id="modalMiniProgressFill" style="width: 0%;"></div>
            </div>
        </div>

        <div class="modal-buttons">
            <button type="button" class="btn-modal btn-modal-annuler" onclick="fermerModalConfirmation()">
                <i class="fas fa-times"></i> Annuler
            </button>
            <button type="button" class="btn-modal btn-modal-confirmer" onclick="terminerExamen()">
                <i class="fas fa-check"></i> Confirmer et terminer
            </button>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/views/assets/js/cours-examen-module.js"></script>

</body>
</html>