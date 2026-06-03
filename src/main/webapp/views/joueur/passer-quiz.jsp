<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Quiz" %>
<%@ page import="com.amijayquiz.models.Question" %>

<%-- ============================================================
     Fichier : passer-quiz.jsp
     Rôle    : Page de passage d'un quiz
              - Interface de quiz chronométrée (3 minutes fixes)
              - Navigation entre les questions (précédent/suivant)
              - Indicateurs visuels de progression
              - Timer avec alerte visuelle quand le temps est bas
              - Modal de confirmation avant de terminer
              - Protection contre la fermeture accidentelle
              - Design épuré et concentré sur l'expérience de quiz
     ============================================================ --%>

<%
    // Récupération des attributs depuis le servlet
    Quiz quiz = (Quiz) request.getAttribute("quiz");
    Question questionCourante = (Question) request.getAttribute("questionCourante");
    Integer indexCourant = (Integer) request.getAttribute("indexCourant");
    Integer nombreTotalQuestions = (Integer) request.getAttribute("nombreTotalQuestions");
    Integer reponseActuelle = (Integer) request.getAttribute("reponseActuelle");
    Long tempsRestantMs = (Long) request.getAttribute("tempsRestantMs");
    Long tempsMaxMs = (Long) request.getAttribute("tempsMaxMs");
    Boolean estPremiereQuestion = (Boolean) request.getAttribute("estPremiereQuestion");
    Boolean estDerniereQuestion = (Boolean) request.getAttribute("estDerniereQuestion");
    
    // Sécurité : redirection si données manquantes
    if (quiz == null || questionCourante == null) {
        response.sendRedirect(request.getContextPath() + "/joueur/dashboard");
        return;
    }
    
    // Calculs pour l'affichage
    int tempsRestantSecondes = (int) (tempsRestantMs / 1000);
    int pourcentageProgression = (int) (((indexCourant + 1) * 100.0) / nombreTotalQuestions);
    
    // Extraire la catégorie et le nom de la partie du titre
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
    
    // URLs encodées pour le breadcrumb
    String categorieEncodee = java.net.URLEncoder.encode(categorie, "UTF-8");
    String niveauEncode = java.net.URLEncoder.encode(quiz.getNiveau(), "UTF-8");
%>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Quiz - <%= partieInfo %> - AmijayQuiz</title>
    
    <%-- Polices et icônes --%>
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/css/fonts.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/fontawesome/fontawesome-free-7.2.0-web/css/all.min.css">
    
    <%-- Styles du quiz --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/assets/css/quiz.css">
</head>
<body>

<%-- ============================================================
     BARRE SUPÉRIEURE (STICKY)
     ============================================================ --%>
<header class="quiz-header">
    <div class="quiz-header-inner">
        
        <%-- Breadcrumb discret --%>
        <nav class="quiz-breadcrumb" aria-label="Fil d'Ariane">
            <a href="${pageContext.request.contextPath}/joueur/dashboard" class="qb-link">
                <i class="fas fa-tachometer-alt"></i> Dashboard
            </a>
            <i class="fas fa-chevron-right qb-sep"></i>
            <a href="${pageContext.request.contextPath}/joueur/theme?categorie=<%= categorieEncodee %>" class="qb-link">
                <%= categorie %>
            </a>
            <i class="fas fa-chevron-right qb-sep"></i>
            <a href="${pageContext.request.contextPath}/joueur/niveau?categorie=<%= categorieEncodee %>&niveau=<%= niveauEncode %>" class="qb-link">
                <%= quiz.getNiveauFormate() %>
            </a>
            <i class="fas fa-chevron-right qb-sep"></i>
            <span class="qb-current">Partie <%= quiz.getOrdrePartie() %></span>
        </nav>
        
        <%-- Infos du quiz + Timer --%>
        <div class="quiz-header-main">
            <div class="quiz-infos">
                <span class="quiz-categorie-label"><%= categorie %></span>
                <h1 class="quiz-partie-titre">
                    <i class="<%= quiz.getIconeNiveau() %>"></i>
                    <%= partieInfo %>
                    <span class="badge-niveau <%= quiz.getNiveau() %>">
                        <i class="<%= quiz.getIconeNiveau() %>"></i>
                        <%= quiz.getNiveauFormate() %>
                    </span>
                </h1>
            </div>
            
            <%-- Timer --%>
            <div class="timer-container">
                <div class="timer-box" id="timerBox">
                    <i class="fas fa-clock timer-icon"></i>
                    <span class="timer-display" id="timerDisplay">--:--</span>
                </div>
                <span class="timer-label" id="timerLabel">restantes</span>
            </div>
        </div>
    </div>
</header>

<%-- ============================================================
     BARRE DE PROGRESSION GLOBALE
     ============================================================ --%>
<div class="quiz-progression">
    <div class="quiz-progression-inner">
        <div class="progression-stats">
            <span>Question <strong><%= indexCourant + 1 %></strong> / <%= nombreTotalQuestions %></span>
            <span><%= pourcentageProgression %>% complété</span>
        </div>
        <div class="progression-track">
            <div class="progression-fill" style="width: <%= pourcentageProgression %>%;"></div>
        </div>
    </div>
</div>

<%-- ============================================================
     CONTENU PRINCIPAL : CARTE DE LA QUESTION
     ============================================================ --%>
<main class="quiz-main">
    <div class="question-card">
        
        <%-- Numéro de la question --%>
        <div class="question-badge">
            <span>Question <%= indexCourant + 1 %></span>
        </div>
        
        <%-- Texte de la question --%>
        <h2 class="question-texte"><%= questionCourante.getTexteQuestion() %></h2>
        
        <%-- Formulaire avec les propositions --%>
        <form id="quizForm" action="${pageContext.request.contextPath}/joueur/passer-quiz" method="POST">
            <input type="hidden" name="action" id="formAction" value="">
            
            <div class="propositions-list">
                <% 
                    String[] lettres = {"A", "B", "C", "D"};
                    for (int i = 1; i <= 4; i++) { 
                        String proposition = questionCourante.getProposition(i);
                        boolean isChecked = (reponseActuelle == i);
                %>
                    <div class="proposition-item <%= isChecked ? "selected" : "" %>" data-index="<%= i %>">
                        <input 
                            type="radio" 
                            name="reponse" 
                            id="reponse<%= i %>" 
                            value="<%= i %>"
                            <%= isChecked ? "checked" : "" %>
                        >
                        <label for="reponse<%= i %>" class="proposition-label">
                            <span class="proposition-lettre"><%= lettres[i-1] %></span>
                            <span class="proposition-texte"><%= proposition %></span>
                            <span class="proposition-check">
                                <i class="fas fa-check-circle"></i>
                            </span>
                        </label>
                    </div>
                <% } %>
            </div>
            
            <%-- Navigation --%>
            <div class="quiz-navigation">
                <% if (!estPremiereQuestion) { %>
                    <button type="button" class="btn-nav btn-precedent" onclick="naviguer('precedent')">
                        <i class="fas fa-arrow-left"></i>
                        <span>Précédent</span>
                    </button>
                <% } else { %>
                    <button type="button" class="btn-nav btn-precedent" disabled>
                        <i class="fas fa-arrow-left"></i>
                        <span>Précédent</span>
                    </button>
                <% } %>
                
                <div class="nav-spacer"></div>
                
                <% if (!estDerniereQuestion) { %>
                    <button type="button" class="btn-nav btn-suivant" onclick="naviguer('suivant')">
                        <span>Suivant</span>
                        <i class="fas fa-arrow-right"></i>
                    </button>
                <% } else { %>
                    <button type="button" class="btn-nav btn-terminer" onclick="ouvrirModalConfirmation()">
                        <i class="fas fa-flag-checkered"></i>
                        <span>Terminer le quiz</span>
                    </button>
                <% } %>
            </div>
        </form>
        
        <%-- Indicateurs de questions (points) --%>
        <div class="indicateurs-questions">
            <% for (int i = 0; i < nombreTotalQuestions; i++) { 
                String dotClass = "";
                if (i == indexCourant) {
                    dotClass = "active";
                } else if (i < indexCourant) {
                    dotClass = "repondu";
                }
            %>
                <span class="indicateur-dot <%= dotClass %>" 
                      title="Question <%= i + 1 %>"
                      data-index="<%= i %>">
                </span>
            <% } %>
        </div>
        
        <%-- Rappel du seuil de réussite --%>
        <div class="seuil-rappel">
            <i class="fas fa-info-circle"></i>
            Score minimum requis pour réussir cette partie : <strong>8 / <%= nombreTotalQuestions %></strong>
        </div>
        
    </div>
</main>

<%-- ============================================================
     MODAL DE CONFIRMATION (TERMINER LE QUIZ)
     ============================================================ --%>
<div class="modal-overlay" id="modalConfirmation" role="dialog" aria-modal="true" aria-labelledby="modalTitle">
    <div class="modal-confirmation">
        <div class="modal-icon">
            <i class="fas fa-question-circle"></i>
        </div>
        <h3 id="modalTitle">Terminer la partie ?</h3>
        <p>
            Vous ne pourrez plus modifier vos réponses après avoir terminé.<br>
            Score minimum requis : <strong><%= nombreTotalQuestions >= 10 ? "8/10" : "80%" %></strong> pour valider cette partie.
        </p>
        <div class="modal-buttons">
            <button type="button" class="btn-modal btn-modal-annuler" onclick="fermerModalConfirmation()">
                <i class="fas fa-times"></i> Annuler
            </button>
            <button type="button" class="btn-modal btn-modal-confirmer" onclick="terminerQuiz()">
                <i class="fas fa-check"></i> Confirmer
            </button>
        </div>
    </div>
</div>

<%-- ============================================================
     SCRIPTS
     ============================================================ --%>
<script>
    // Configuration du quiz passée au JavaScript
    // Le timer est géré côté client avec localStorage pour éviter la réinitialisation
    var QUIZ_CONFIG = {
        tempsTotal: 180,
        contextPath: '${pageContext.request.contextPath}',
        quizId: <%= quiz.getId() %>,
        questionCourante: <%= indexCourant + 1 %>,
        totalQuestions: <%= nombreTotalQuestions %>
    };
</script>
<script src="${pageContext.request.contextPath}/views/assets/js/quiz.js"></script>

</body>
</html>