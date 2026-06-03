<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.amijayquiz.models.Quiz" %>
<%@ page import="com.amijayquiz.models.Question" %>

<%-- ============================================================
     FICHIER : views/admin/admin-quiz-modifier-question.jsp
     RÔLE    : Page de modification/ajout d'une question
              - Formulaire pré-rempli pour modification
              - Formulaire vide pour ajout
              - Validation avant enregistrement
     ============================================================ --%>

<%
    Quiz quiz = (Quiz) request.getAttribute("quiz");
    Question question = (Question) request.getAttribute("question");
    Integer indexQuestion = (Integer) request.getAttribute("indexQuestion");
    Boolean modeAjout = (Boolean) request.getAttribute("modeAjout");
    String messageErreur = (String) request.getAttribute("messageErreur");

    if (quiz == null || question == null) {
        response.sendRedirect(request.getContextPath() + "/admin/quiz");
        return;
    }

    if (modeAjout == null) modeAjout = false;
    if (indexQuestion == null) indexQuestion = -1;

    String titrePage = modeAjout ? "Ajouter une question" : "Modifier la question";
    int numeroQuestion = modeAjout ? 
        ((java.util.List<Question>) session.getAttribute("questionsEnModification")).size() + 1 : 
        indexQuestion + 1;

    request.setAttribute("pageTitle", titrePage + " - " + quiz.getTitre() + " - Administration");
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
        <a href="${pageContext.request.contextPath}/admin/quiz/modifier?quizId=<%= quiz.getId() %>" class="breadcrumb-link">
            <i class="fas fa-edit"></i><span>Modifier le quiz</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas <%= modeAjout ? "fa-plus-circle" : "fa-pen" %>"></i>
            <span><%= modeAjout ? "Ajouter" : "Modifier" %> une question</span>
        </span>
    </nav>

    <%-- ===== MESSAGE D'ERREUR ===== --%>
    <% if (messageErreur != null) { %>
        <div class="message-alerte message-erreur">
            <i class="fas fa-exclamation-circle"></i>
            <span><%= messageErreur %></span>
        </div>
    <% } %>

    <%-- ===== RAPPEL DU QUIZ ===== --%>
    <div class="quiz-rappel-card">
        <div class="rappel-icon">
            <i class="fas fa-edit"></i>
        </div>
        <div class="rappel-content">
            <span class="rappel-label">Quiz en modification</span>
            <strong><%= quiz.getTitre() %></strong>
            <span class="rappel-meta">
                <span class="badge-niveau badge-niveau-<%= quiz.getNiveau() %>">
                    <i class="<%= quiz.getIconeNiveau() %>"></i>
                    <%= quiz.getNiveauFormate() %>
                </span>
                <span class="rappel-compteur">
                    <i class="fas fa-question-circle"></i>
                    Question n°<strong><%= numeroQuestion %></strong>
                </span>
            </span>
        </div>
    </div>

    <%-- ===== FORMULAIRE ===== --%>
    <div class="ajouter-question-card">
        <div class="card-header">
            <div class="card-header-icon">
                <i class="fas <%= modeAjout ? "fa-plus-circle" : "fa-pen" %>"></i>
            </div>
            <h2><%= modeAjout ? "Nouvelle question n°" + numeroQuestion : "Modifier la question n°" + numeroQuestion %></h2>
        </div>
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/admin/quiz/modifier-question" method="POST" id="formQuestion">
                <input type="hidden" name="modeAjout" value="<%= modeAjout %>">
                <input type="hidden" name="indexQuestion" value="<%= indexQuestion %>">

                <%-- ===== TEXTE DE LA QUESTION ===== --%>
                <div class="form-group">
                    <label for="texteQuestion">
                        <i class="fas fa-question-circle"></i> Question <span class="required">*</span>
                    </label>
                    <textarea id="texteQuestion" name="texteQuestion" rows="3" 
                              placeholder="Entrez le texte de la question..." required><%= question.getTexteQuestion() != null ? question.getTexteQuestion() : "" %></textarea>
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
                                   value="<%= question.getProposition1() != null ? question.getProposition1() : "" %>"
                                   placeholder="Proposition A (obligatoire)" required>
                        </div>
                    </div>

                    <div class="proposition-row">
                        <div class="proposition-lettre">B</div>
                        <div class="proposition-input-wrapper">
                            <input type="text" id="proposition2" name="proposition2" 
                                   value="<%= question.getProposition2() != null ? question.getProposition2() : "" %>"
                                   placeholder="Proposition B (obligatoire)" required>
                        </div>
                    </div>

                    <div class="proposition-row">
                        <div class="proposition-lettre">C</div>
                        <div class="proposition-input-wrapper">
                            <input type="text" id="proposition3" name="proposition3" 
                                   value="<%= question.getProposition3() != null ? question.getProposition3() : "" %>"
                                   placeholder="Proposition C (optionnelle)">
                        </div>
                    </div>

                    <div class="proposition-row">
                        <div class="proposition-lettre">D</div>
                        <div class="proposition-input-wrapper">
                            <input type="text" id="proposition4" name="proposition4" 
                                   value="<%= question.getProposition4() != null ? question.getProposition4() : "" %>"
                                   placeholder="Proposition D (optionnelle)">
                        </div>
                    </div>
                </div>

                <%-- ===== BONNE RÉPONSE ===== --%>
                <div class="bonne-reponse-section">
                    <h3><i class="fas fa-check-circle"></i> Bonne reponse <span class="required">*</span></h3>
                    <div class="bonne-reponse-options">
                        <label class="bonne-reponse-option">
                            <input type="radio" name="bonneReponse" value="1" 
                                   <%= question.getBonneReponse() == 1 ? "checked" : "" %> required>
                            <span class="option-lettre">A</span>
                        </label>
                        <label class="bonne-reponse-option">
                            <input type="radio" name="bonneReponse" value="2" 
                                   <%= question.getBonneReponse() == 2 ? "checked" : "" %>>
                            <span class="option-lettre">B</span>
                        </label>
                        <label class="bonne-reponse-option">
                            <input type="radio" name="bonneReponse" value="3" 
                                   <%= question.getBonneReponse() == 3 ? "checked" : "" %>>
                            <span class="option-lettre">C</span>
                        </label>
                        <label class="bonne-reponse-option">
                            <input type="radio" name="bonneReponse" value="4" 
                                   <%= question.getBonneReponse() == 4 ? "checked" : "" %>>
                            <span class="option-lettre">D</span>
                        </label>
                    </div>
                </div>

                <%-- ===== BOUTONS ===== --%>
                <div class="form-actions">
                    <a href="${pageContext.request.contextPath}/admin/quiz/modifier?quizId=<%= quiz.getId() %>" 
                       class="btn-action btn-annuler">
                        <i class="fas fa-arrow-left"></i> Annuler
                    </a>
                    <button type="submit" class="btn-action btn-ajouter">
                        <i class="fas fa-save"></i> 
                        <%= modeAjout ? "Ajouter cette question" : "Enregistrer la modification" %>
                    </button>
                </div>
            </form>
        </div>
    </div>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="admin-back">
        <a href="${pageContext.request.contextPath}/admin/quiz/modifier?quizId=<%= quiz.getId() %>" class="btn-retour">
            <i class="fas fa-arrow-left"></i> Retour a la modification du quiz
        </a>
    </div>

</div>

<script>
    // Compteur de caracteres pour la question
    var textarea = document.getElementById('texteQuestion');
    var counter = document.getElementById('compteurQuestion');

    if (textarea && counter) {
        textarea.addEventListener('input', function() {
            var length = this.value.length;
            counter.textContent = length + ' caractere' + (length > 1 ? 's' : '');
            counter.style.color = length > 500 ? '#e74c3c' : (length > 300 ? '#f39c12' : '#8b8fa3');
        });
        // Initialiser le compteur
        var event = new Event('input');
        textarea.dispatchEvent(event);
    }

    // Validation avant soumission
    document.getElementById('formQuestion').addEventListener('submit', function(e) {
        var texte = document.getElementById('texteQuestion').value.trim();
        var prop1 = document.getElementById('proposition1').value.trim();
        var prop2 = document.getElementById('proposition2').value.trim();
        var bonneReponse = document.querySelector('input[name="bonneReponse"]:checked');

        if (!texte || !prop1 || !prop2 || !bonneReponse) {
            e.preventDefault();
            alert('Veuillez remplir la question, les propositions A et B, et selectionner la bonne reponse.');
        }
    });
</script>

<%
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-quiz-creer.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../footer-connecte.jsp" %>