<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.ExamenModule" %>
<%@ page import="com.amijayquiz.models.QuestionExamen" %>
<%@ page import="com.amijayquiz.models.ProgressionModule" %>

<%
    Cours cours = (Cours) request.getAttribute("cours");
    ExamenModule examen = (ExamenModule) request.getAttribute("examen");
    List<QuestionExamen> questions = (List<QuestionExamen>) request.getAttribute("questions");
    Integer nombreQuestions = (Integer) request.getAttribute("nombreQuestions");
    Integer scoreMinimum = (Integer) request.getAttribute("scoreMinimum");
    Integer seuilReussite = (Integer) request.getAttribute("seuilReussite");
    ProgressionModule progressionExistante = (ProgressionModule) request.getAttribute("progressionExistante");

    if (cours == null || examen == null || questions == null || questions.isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/joueur/cours");
        return;
    }

    if (nombreQuestions == null) nombreQuestions = questions.size();
    if (scoreMinimum == null) scoreMinimum = 15;
    if (seuilReussite == null) seuilReussite = 75;

    String themeEncode = java.net.URLEncoder.encode(cours.getTheme(), "UTF-8");
    String niveauEncode = java.net.URLEncoder.encode(cours.getNiveau(), "UTF-8");
    
    boolean isRetentative = progressionExistante != null && !progressionExistante.isReussi();
    int tentativeNumero = progressionExistante != null ? progressionExistante.getTentatives() + 1 : 1;
%>

<%
    String extraHead = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/cours-examen-module.css\">";
    request.setAttribute("extraHead", extraHead);
%>

<%@ include file="../header.jsp" %>

<div class="examen-module-container">

    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/joueur/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours" class="breadcrumb-link">
            <i class="fas fa-graduation-cap"></i><span>Cours</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours/theme?theme=<%= themeEncode %>" class="breadcrumb-link">
            <span><%= cours.getTheme() %></span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours/niveau?theme=<%= themeEncode %>&niveau=<%= niveauEncode %>" class="breadcrumb-link">
            <span><%= cours.getNiveauFormate() %></span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= cours.getId() %>" class="breadcrumb-link">
            <i class="fas fa-sitemap"></i><span>Plan</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-clipboard-check"></i><span><%= examen.getModuleFormate() %></span>
        </span>
    </nav>

    <div class="examen-module-header">
        <span class="examen-cours-badge" style="background: <%= cours.getCouleurTheme() %>;">
            <i class="<%= cours.getIconeTheme() %>"></i> <%= cours.getTitre() %>
        </span>
        <h1>Examen <%= examen.getModuleFormate() %></h1>
        <p>Score minimum requis : <strong><%= scoreMinimum %>/<%= nombreQuestions %> (<%= seuilReussite %>%)</strong></p>
        <% if (isRetentative) { %>
            <span class="retentative-badge">
                <i class="fas fa-redo"></i> Tentative <%= tentativeNumero %>
            </span>
        <% } %>
    </div>

    <div class="examen-progression">
        <div class="progression-track">
            <div class="progression-fill" id="progressFill" style="width: 0%;"></div>
        </div>
        <div class="progression-stats">
            <span>Question <strong id="currentQuestionDisplay">1</strong> / <%= nombreQuestions %></span>
            <span><span id="answeredCount">0</span> / <%= nombreQuestions %> répondues</span>
        </div>
    </div>

    <form id="examenForm" action="${pageContext.request.contextPath}/joueur/cours/examen-module" method="POST">
        <input type="hidden" name="coursId" value="<%= cours.getId() %>">
        <input type="hidden" name="module" value="<%= examen.getModuleNumero() %>">

        <div class="questions-container">
            <%
            String[] lettres = {"A", "B", "C", "D"};
            for (int i = 0; i < questions.size(); i++) {
                QuestionExamen q = questions.get(i);
                if (q == null) continue;
                
                String texteQuestion = q.getQuestion();
                if (texteQuestion == null) texteQuestion = "";
                
                // Échappement manuel sans méthode
                texteQuestion = texteQuestion.replace("&", "&amp;")
                                             .replace("<", "&lt;")
                                             .replace(">", "&gt;")
                                             .replace("\"", "&quot;");
            %>
                <div class="question-card" id="questionCard<%= i %>" style="display: <%= i == 0 ? "block" : "none" %>;">
                    <div class="question-header">
                        <span class="question-numero">Question <%= i + 1 %></span>
                    </div>
                    <div class="question-texte"><%= texteQuestion %></div>
                    <div class="propositions-list">
                        <%
                        for (int p = 1; p <= 4; p++) {
                            String proposition = q.getProposition(p);
                            if (proposition != null && !proposition.trim().isEmpty()) {
                                // Échappement manuel
                                proposition = proposition.replace("&", "&amp;")
                                                         .replace("<", "&lt;")
                                                         .replace(">", "&gt;")
                                                         .replace("\"", "&quot;");
                        %>
                            <label class="proposition-item">
                                <input type="radio" 
                                       name="reponse_<%= i %>" 
                                       value="<%= p %>"
                                       onchange="onReponseChange(<%= i %>)">
                                <span class="proposition-lettre"><%= lettres[p-1] %></span>
                                <span class="proposition-texte"><%= proposition %></span>
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

        <div class="questions-indicators">
            <span class="indicators-label">Navigation rapide :</span>
            <div class="indicators-grid">
                <% for (int i = 0; i < questions.size(); i++) { %>
                    <button type="button" 
                            class="indicator-dot" 
                            id="indicator<%= i %>"
                            data-index="<%= i %>"
                            onclick="naviguerVersQuestion(<%= i %>)">
                        <%= i + 1 %>
                    </button>
                <% } %>
            </div>
        </div>

        <div class="examen-navigation">
            <button type="button" class="btn-nav btn-precedent" id="btnPrecedent" onclick="naviguerQuestion(-1)">
                <i class="fas fa-arrow-left"></i>
                <span>Précédent</span>
            </button>

            <button type="button" class="btn-nav btn-suivant" id="btnSuivant" onclick="naviguerQuestion(1)">
                <span>Suivant</span>
                <i class="fas fa-arrow-right"></i>
            </button>

            <button type="button" class="btn-nav btn-terminer" id="btnTerminer" onclick="ouvrirModalConfirmation()">
                <i class="fas fa-flag-checkered"></i>
                <span>Terminer l'examen</span>
            </button>
        </div>
    </form>

    <div class="modal-overlay" id="modalConfirmation" style="display: none;">
        <div class="modal-card">
            <div class="modal-icon">
                <i class="fas fa-question-circle"></i>
            </div>
            <h3>Terminer l'examen ?</h3>
            <p>Vous ne pourrez plus modifier vos réponses.</p>
            <p>Score minimum requis : <strong><%= scoreMinimum %>/<%= nombreQuestions %> (<%= seuilReussite %>%)</strong></p>
            
            <div class="modal-warning" id="questionsNonRepondues" style="display: none;">
                <i class="fas fa-exclamation-triangle"></i>
                <span><strong id="nbNonRepondues">0</strong> question(s) sans réponse.</span>
            </div>

            <div class="modal-buttons">
                <button type="button" class="btn-modal btn-annuler" onclick="fermerModalConfirmation()">
                    <i class="fas fa-times"></i> Annuler
                </button>
                <button type="button" class="btn-modal btn-confirmer" onclick="terminerExamen()">
                    <i class="fas fa-check"></i> Confirmer
                </button>
            </div>
        </div>
    </div>

</div>

<script>
// Variables globales
var NOMBRE_QUESTIONS = <%= nombreQuestions %>;
var reponsesUtilisateur = new Array(NOMBRE_QUESTIONS).fill(null);
var currentQuestion = 0;

function onReponseChange(index) {
    var radios = document.getElementsByName('reponse_' + index);
    for (var i = 0; i < radios.length; i++) {
        if (radios[i].checked) {
            reponsesUtilisateur[index] = radios[i].value;
            updateIndicator(index);
            break;
        }
    }
    updateAnsweredCount();
    updateProgressFill();
}

function updateIndicator(index) {
    var indicator = document.getElementById('indicator' + index);
    if (indicator && reponsesUtilisateur[index] !== null) {
        indicator.classList.add('answered');
    }
}

function updateAnsweredCount() {
    var count = 0;
    for (var i = 0; i < NOMBRE_QUESTIONS; i++) {
        if (reponsesUtilisateur[i] !== null) count++;
    }
    var answeredSpan = document.getElementById('answeredCount');
    if (answeredSpan) answeredSpan.innerText = count;
}

function updateProgressFill() {
    var count = 0;
    for (var i = 0; i < NOMBRE_QUESTIONS; i++) {
        if (reponsesUtilisateur[i] !== null) count++;
    }
    var percent = (count / NOMBRE_QUESTIONS) * 100;
    var fill = document.getElementById('progressFill');
    if (fill) fill.style.width = percent + '%';
}

function naviguerQuestion(direction) {
    var newIndex = currentQuestion + direction;
    if (newIndex >= 0 && newIndex < NOMBRE_QUESTIONS) {
        document.getElementById('questionCard' + currentQuestion).style.display = 'none';
        currentQuestion = newIndex;
        document.getElementById('questionCard' + currentQuestion).style.display = 'block';
        document.getElementById('currentQuestionDisplay').innerText = (currentQuestion + 1);
        updateActiveIndicator();
    }
    updateButtonsState();
}

function naviguerVersQuestion(index) {
    if (index >= 0 && index < NOMBRE_QUESTIONS) {
        document.getElementById('questionCard' + currentQuestion).style.display = 'none';
        currentQuestion = index;
        document.getElementById('questionCard' + currentQuestion).style.display = 'block';
        document.getElementById('currentQuestionDisplay').innerText = (currentQuestion + 1);
        updateActiveIndicator();
    }
}

function updateActiveIndicator() {
    for (var i = 0; i < NOMBRE_QUESTIONS; i++) {
        var indicator = document.getElementById('indicator' + i);
        if (indicator) {
            if (i === currentQuestion) {
                indicator.classList.add('active');
            } else {
                indicator.classList.remove('active');
            }
        }
    }
}

function updateButtonsState() {
    var btnPrecedent = document.getElementById('btnPrecedent');
    var btnSuivant = document.getElementById('btnSuivant');
    if (btnPrecedent) btnPrecedent.style.opacity = (currentQuestion === 0) ? '0.5' : '1';
    if (btnSuivant) btnSuivant.style.opacity = (currentQuestion === NOMBRE_QUESTIONS - 1) ? '0.5' : '1';
}

function ouvrirModalConfirmation() {
    var nonRepondues = 0;
    for (var i = 0; i < NOMBRE_QUESTIONS; i++) {
        if (reponsesUtilisateur[i] === null) nonRepondues++;
    }
    
    var warningDiv = document.getElementById('questionsNonRepondues');
    var nbSpan = document.getElementById('nbNonRepondues');
    
    if (nonRepondues > 0 && warningDiv && nbSpan) {
        nbSpan.innerText = nonRepondues;
        warningDiv.style.display = 'flex';
    } else if (warningDiv) {
        warningDiv.style.display = 'none';
    }
    
    var modal = document.getElementById('modalConfirmation');
    if (modal) modal.style.display = 'flex';
}

function fermerModalConfirmation() {
    var modal = document.getElementById('modalConfirmation');
    if (modal) modal.style.display = 'none';
}

function terminerExamen() {
    document.getElementById('examenForm').submit();
}

// Initialisation
document.addEventListener('DOMContentLoaded', function() {
    updateButtonsState();
    updateActiveIndicator();
    
    for (var i = 0; i < NOMBRE_QUESTIONS; i++) {
        var radios = document.getElementsByName('reponse_' + i);
        for (var j = 0; j < radios.length; j++) {
            if (radios[j].checked) {
                reponsesUtilisateur[i] = radios[j].value;
                updateIndicator(i);
                break;
            }
        }
    }
    updateAnsweredCount();
    updateProgressFill();
});
</script>

<%@ include file="../footer.jsp" %>