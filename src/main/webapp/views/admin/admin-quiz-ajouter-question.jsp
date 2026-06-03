<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.amijayquiz.models.Quiz" %>
<%@ page import="com.amijayquiz.models.Question" %>

<%-- ============================================================
     FICHIER : views/admin/admin-quiz-ajouter-question.jsp
     RÔLE    : Page d'ajout d'une question au quiz en création
              - Formulaire complet de question
              - 4 propositions + bonne réponse
              - Validation avant ajout
              - Retour à la page de création
     ============================================================ --%>

<%
    // Récupération des données de la session
    Quiz quizEnCreation = (Quiz) session.getAttribute("quizEnCreation");
    List<Question> questionsEnCreation = (List<Question>) session.getAttribute("questionsEnCreation");
    Integer compteurQuestions = (Integer) session.getAttribute("compteurQuestions");

    // Vérifications
    if (quizEnCreation == null || quizEnCreation.getTitre() == null || quizEnCreation.getTitre().isEmpty()) {
        session.setAttribute("messageErreurCreation", "Veuillez d'abord remplir les informations du quiz.");
        response.sendRedirect(request.getContextPath() + "/admin/quiz/creer");
        return;
    }

    if (compteurQuestions == null) compteurQuestions = 0;

    int nbRequis = 10;
    int prochainNumero = compteurQuestions + 1;

    String messageErreur = (String) session.getAttribute("messageErreurCreation");
    if (messageErreur != null) session.removeAttribute("messageErreurCreation");

    request.setAttribute("pageTitle", "Ajouter une question - Quiz : " + quizEnCreation.getTitre() + " - Administration");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-quiz-creer.css\">";
    request.setAttribute("extraHead", adminCSS);
%>

<%@ include file="../header.jsp" %>

<div class="admin-ajouter-question-container">

    <%-- ===== BREADCRUMB ===== --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/admin/quiz" class="breadcrumb-link">
            <i class="fas fa-list-check"></i><span>Quiz</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/admin/quiz/creer" class="breadcrumb-link">
            <i class="fas fa-plus-circle"></i><span>Créer un quiz</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-question-circle"></i><span>Ajouter une question</span>
        </span>
    </nav>

    <%-- ===== MESSAGE D'ERREUR ===== --%>
    <% if (messageErreur != null) { %>
        <div class="message-alerte message-erreur">
            <i class="fas fa-exclamation-circle"></i>
            <span><%= messageErreur %></span>
        </div>
    <% } %>

    <%-- ===== RAPPEL DU QUIZ EN CRÉATION ===== --%>
    <div class="quiz-rappel-card">
        <div class="rappel-icon">
            <i class="fas fa-list-check"></i>
        </div>
        <div class="rappel-content">
            <span class="rappel-label">Quiz en création</span>
            <strong><%= quizEnCreation.getTitre() %></strong>
            <span class="rappel-meta">
                <span class="badge-niveau badge-niveau-<%= quizEnCreation.getNiveau() %>">
                    <i class="<%= quizEnCreation.getIconeNiveau() %>"></i>
                    <%= quizEnCreation.getNiveauFormate() %>
                </span>
                <span class="rappel-compteur">
                    <i class="fas fa-question-circle"></i>
                    Question <%= prochainNumero %> sur <%= nbRequis %>
                </span>
            </span>
        </div>
    </div>

    <%-- ===== FORMULAIRE D'AJOUT DE QUESTION ===== --%>
    <div class="ajouter-question-card">
        <div class="card-header">
            <div class="card-header-icon">
                <i class="fas fa-plus-circle"></i>
            </div>
            <h2>Nouvelle question n°<%= prochainNumero %></h2>
        </div>
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/admin/quiz/creer" method="POST" id="formAjoutQuestion">
                <input type="hidden" name="action" value="ajouterQuestion">

                <%-- ===== TEXTE DE LA QUESTION ===== --%>
                <div class="form-group">
                    <label for="texteQuestion">
                        <i class="fas fa-question-circle"></i> Question <span class="required">*</span>
                    </label>
                    <textarea id="texteQuestion" name="texteQuestion" rows="3" 
                              placeholder="Entrez le texte de la question..." required></textarea>
                    <span class="char-counter" id="compteurQuestion"></span>
                </div>

                <%-- ===== PROPOSITIONS ===== --%>
                <div class="propositions-section">
                    <h3><i class="fas fa-list-ul"></i> Propositions</h3>
                    <p class="section-hint">Vous devez remplir au moins les propositions A et B.</p>

                    <div class="proposition-row">
                        <div class="proposition-lettre">A</div>
                        <div class="proposition-input-wrapper">
                            <input type="text" id="proposition1" name="proposition1" 
                                   placeholder="Proposition A (obligatoire)" required>
                        </div>
                    </div>

                    <div class="proposition-row">
                        <div class="proposition-lettre">B</div>
                        <div class="proposition-input-wrapper">
                            <input type="text" id="proposition2" name="proposition2" 
                                   placeholder="Proposition B (obligatoire)" required>
                        </div>
                    </div>

                    <div class="proposition-row">
                        <div class="proposition-lettre">C</div>
                        <div class="proposition-input-wrapper">
                            <input type="text" id="proposition3" name="proposition3" 
                                   placeholder="Proposition C (optionnelle)">
                        </div>
                    </div>

                    <div class="proposition-row">
                        <div class="proposition-lettre">D</div>
                        <div class="proposition-input-wrapper">
                            <input type="text" id="proposition4" name="proposition4" 
                                   placeholder="Proposition D (optionnelle)">
                        </div>
                    </div>
                </div>

                <%-- ===== BONNE RÉPONSE ===== --%>
                <div class="bonne-reponse-section">
                    <h3><i class="fas fa-check-circle"></i> Bonne réponse <span class="required">*</span></h3>
                    <div class="bonne-reponse-options">
                        <label class="bonne-reponse-option">
                            <input type="radio" name="bonneReponse" value="1" required>
                            <span class="option-lettre">A</span>
                        </label>
                        <label class="bonne-reponse-option">
                            <input type="radio" name="bonneReponse" value="2">
                            <span class="option-lettre">B</span>
                        </label>
                        <label class="bonne-reponse-option">
                            <input type="radio" name="bonneReponse" value="3">
                            <span class="option-lettre">C</span>
                        </label>
                        <label class="bonne-reponse-option">
                            <input type="radio" name="bonneReponse" value="4">
                            <span class="option-lettre">D</span>
                        </label>
                    </div>
                </div>

                <%-- ===== BOUTONS ===== --%>
                <div class="form-actions">
                    <a href="${pageContext.request.contextPath}/admin/quiz/creer" class="btn-action btn-annuler">
                        <i class="fas fa-arrow-left"></i> Annuler
                    </a>
                    <button type="submit" class="btn-action btn-ajouter">
                        <i class="fas fa-plus-circle"></i> Ajouter cette question
                    </button>
                </div>
            </form>
        </div>
    </div>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="admin-back">
        <a href="${pageContext.request.contextPath}/admin/quiz/creer" class="btn-retour">
            <i class="fas fa-arrow-left"></i> Retour à la création du quiz
        </a>
    </div>

</div>

<script>
    // Compteur de caractères pour la question
    var textarea = document.getElementById('texteQuestion');
    var counter = document.getElementById('compteurQuestion');

    if (textarea && counter) {
        textarea.addEventListener('input', function() {
            var length = this.value.length;
            counter.textContent = length + ' caractère' + (length > 1 ? 's' : '');
            counter.style.color = length > 500 ? '#e74c3c' : (length > 300 ? '#f39c12' : '#8b8fa3');
        });
    }

    // Validation avant soumission
    document.getElementById('formAjoutQuestion').addEventListener('submit', function(e) {
        var texte = document.getElementById('texteQuestion').value.trim();
        var prop1 = document.getElementById('proposition1').value.trim();
        var prop2 = document.getElementById('proposition2').value.trim();
        var bonneReponse = document.querySelector('input[name="bonneReponse"]:checked');

        if (!texte || !prop1 || !prop2 || !bonneReponse) {
            e.preventDefault();
            alert('Veuillez remplir la question, les propositions A et B, et sélectionner la bonne réponse.');
        }
    });
</script>

<%
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-quiz-creer.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../footer-connecte.jsp" %>