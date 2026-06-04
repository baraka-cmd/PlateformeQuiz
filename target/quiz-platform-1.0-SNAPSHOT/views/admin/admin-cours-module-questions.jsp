<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.ExamenModule" %>
<%@ page import="com.amijayquiz.models.QuestionExamen" %>

<%-- ============================================================
     FICHIER : views/admin/admin-cours-module-questions.jsp
     RÔLE    : Page de gestion des questions d'un examen de module
              - Liste des questions existantes
              - Formulaire d'ajout/modification intégré
              - Compteur questions actuelles / requises
              - Actions : Modifier, Supprimer
     ============================================================ --%>

<%
    // ===== RÉCUPÉRATION DES ATTRIBUTS =====
    Cours cours = (Cours) request.getAttribute("cours");
    ExamenModule examen = (ExamenModule) request.getAttribute("examen");
    List<QuestionExamen> questions = (List<QuestionExamen>) request.getAttribute("questions");
    Integer nbQuestionsActuelles = (Integer) request.getAttribute("nbQuestionsActuelles");
    Integer nbQuestionsRequis = (Integer) request.getAttribute("nbQuestionsRequis");
    Boolean quizComplet = (Boolean) request.getAttribute("quizComplet");
    String messageSucces = (String) request.getAttribute("messageSucces");
    String messageErreur = (String) request.getAttribute("messageErreur");

    // Question en cours de modification
    QuestionExamen questionModification = (QuestionExamen) request.getAttribute("questionModification");
    boolean modeModification = questionModification != null;

    if (examen == null) {
        response.sendRedirect(request.getContextPath() + "/admin/cours");
        return;
    }

    if (questions == null) questions = new java.util.ArrayList<>();
    if (nbQuestionsActuelles == null) nbQuestionsActuelles = questions.size();
    if (nbQuestionsRequis == null) nbQuestionsRequis = examen.getNombreQuestions();
    if (quizComplet == null) quizComplet = nbQuestionsActuelles >= nbQuestionsRequis;

    int examenId = examen.getId();
    int coursId = cours != null ? cours.getId() : examen.getCoursId();
    int moduleNumero = examen.getModuleNumero();

    request.setAttribute("pageTitle", "Questions - " + examen.getModuleFormate() + " - Administration");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-cours-module-questions.css\">";
    request.setAttribute("extraHead", adminCSS);
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-cours-module-questions.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../header.jsp" %>

<div class="acmq-container">

    <%-- ===== BREADCRUMB ===== --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/admin/cours" class="breadcrumb-link">
            <i class="fas fa-graduation-cap"></i><span>Cours</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <a href="${pageContext.request.contextPath}/admin/cours/modules?coursId=<%= coursId %>" class="breadcrumb-link">
            <i class="fas fa-cubes"></i><span>Modules</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-question-circle"></i><span>Questions <%= examen.getModuleFormate() %></span>
        </span>
    </nav>

    <%-- ===== MESSAGES FLASH ===== --%>
    <% if (messageSucces != null) { %>
        <div class="message-alerte message-succes" data-timeout="6000">
            <i class="fas fa-check-circle"></i><span><%= messageSucces %></span>
        </div>
    <% } %>
    <% if (messageErreur != null) { %>
        <div class="message-alerte message-erreur" data-timeout="8000">
            <i class="fas fa-exclamation-circle"></i><span><%= messageErreur %></span>
        </div>
    <% } %>

    <%-- ===== EN-TÊTE ===== --%>
    <div class="acmq-header">
        <div class="acmq-header-left">
            <div class="acmq-header-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                <i class="fas fa-question-circle"></i>
            </div>
            <div class="acmq-header-text">
                <span class="acmq-header-module"><%= examen.getModuleFormate() %></span>
                <h1><%= examen.getTitre() != null ? examen.getTitre() : "Examen Module " + moduleNumero %></h1>
                <% if (cours != null) { %>
                    <p>Cours : <%= cours.getTitre() %> • Seuil : <%= examen.getSeuilReussite() %>%</p>
                <% } %>
            </div>
        </div>
        <div class="acmq-header-right">
            <div class="acmq-compteur <%= quizComplet ? "acmq-complet" : "" %>">
                <span class="acmq-compteur-nombre"><%= nbQuestionsActuelles %>/<%= nbQuestionsRequis %></span>
                <span class="acmq-compteur-label">questions</span>
            </div>
            <a href="${pageContext.request.contextPath}/admin/cours/modules/modifier?id=<%= examenId %>&coursId=<%= coursId %>" 
               class="acmq-btn-outline">
                <i class="fas fa-edit"></i><span>Modifier l'examen</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/cours/modules?coursId=<%= coursId %>" 
               class="acmq-btn-outline">
                <i class="fas fa-arrow-left"></i><span>Retour aux modules</span>
            </a>
        </div>
    </div>

    <%-- ===== DISPOSITION 2 COLONNES ===== --%>
    <div class="acmq-layout">

        <%-- ===== COLONNE GAUCHE : LISTE DES QUESTIONS ===== --%>
        <div class="acmq-col-liste">

            <div class="acmq-card">
                <div class="acmq-card-header">
                    <h2><i class="fas fa-list-ul"></i> Questions de l'examen</h2>
                    <span class="acmq-badge <%= quizComplet ? "acmq-badge-complet" : "" %>">
                        <%= nbQuestionsActuelles %>/<%= nbQuestionsRequis %>
                    </span>
                </div>
                <div class="acmq-card-body">
                    <% if (questions != null && !questions.isEmpty()) { %>
                        <div class="acmq-questions-liste">
                            <% 
                                String[] lettres = {"A", "B", "C", "D"};
                                int index = 1;
                                for (QuestionExamen q : questions) {
                                    String texteQuestion = q.getQuestion();
                                    if (texteQuestion != null && texteQuestion.length() > 80) {
                                        texteQuestion = texteQuestion.substring(0, 80) + "...";
                                    }
                            %>
                                <div class="acmq-question-item">
                                    <div class="acmq-question-numero"><%= index %></div>
                                    <div class="acmq-question-content">
                                        <p class="acmq-question-texte"><%= texteQuestion %></p>
                                        <span class="acmq-question-reponse">
                                            <i class="fas fa-check-circle"></i> Bonne réponse : <strong><%= lettres[q.getBonneReponse() - 1] %></strong>
                                        </span>
                                    </div>
                                    <div class="acmq-question-actions">
                                        <button type="button" class="acmq-btn-icon acmq-btn-modifier" 
                                                onclick="ouvrirModification(<%= q.getId() %>, '<%= q.getQuestion().replace("'", "\\'") %>', '<%= q.getProposition1() != null ? q.getProposition1().replace("'", "\\'") : "" %>', '<%= q.getProposition2() != null ? q.getProposition2().replace("'", "\\'") : "" %>', '<%= q.getProposition3() != null ? q.getProposition3().replace("'", "\\'") : "" %>', '<%= q.getProposition4() != null ? q.getProposition4().replace("'", "\\'") : "" %>', <%= q.getBonneReponse() %>)"
                                                title="Modifier">
                                            <i class="fas fa-pen"></i>
                                        </button>
                                        <form action="${pageContext.request.contextPath}/admin/cours/modules/questions" method="POST" class="acmq-form-inline"
                                              onsubmit="return confirm('Supprimer cette question ?')">
                                            <input type="hidden" name="action" value="supprimer">
                                            <input type="hidden" name="examenId" value="<%= examenId %>">
                                            <input type="hidden" name="coursId" value="<%= coursId %>">
                                            <input type="hidden" name="questionId" value="<%= q.getId() %>">
                                            <button type="submit" class="acmq-btn-icon acmq-btn-supprimer" title="Supprimer">
                                                <i class="fas fa-trash-alt"></i>
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            <% index++;
                                }
                            %>
                        </div>
                    <% } else { %>
                        <div class="acmq-empty-mini">
                            <i class="fas fa-inbox"></i>
                            <p>Aucune question configurée. Ajoutez votre première question.</p>
                        </div>
                    <% } %>
                </div>
            </div>
        </div>

        <%-- ===== COLONNE DROITE : FORMULAIRE AJOUT/MODIFICATION ===== --%>
        <div class="acmq-col-form">

            <div class="acmq-card">
                <div class="acmq-card-header">
                    <h2 id="formTitle"><i class="fas fa-plus-circle"></i> Ajouter une question</h2>
                </div>
                <div class="acmq-card-body">
                    <form action="${pageContext.request.contextPath}/admin/cours/modules/questions" method="POST" id="formQuestion" novalidate>
                        <input type="hidden" name="action" id="formAction" value="ajouter">
                        <input type="hidden" name="examenId" value="<%= examenId %>">
                        <input type="hidden" name="coursId" value="<%= coursId %>">
                        <input type="hidden" name="questionId" id="questionId" value="">

                        <%-- Question --%>
                        <div class="acmq-form-group">
                            <label for="question"><i class="fas fa-question-circle"></i> Question <span class="acmq-required">*</span></label>
                            <textarea id="question" name="question" class="acmq-textarea" rows="2" 
                                      placeholder="Texte de la question..." required></textarea>
                        </div>

                        <%-- Propositions --%>
                        <div class="acmq-propositions">
                            <% for (int i = 1; i <= 4; i++) { %>
                                <div class="acmq-form-group">
                                    <label for="proposition<%= i %>">
                                        <span class="acmq-prop-lettre"><%= lettres[i-1] %></span> Proposition <%= i == 1 || i == 2 ? "<span class='acmq-required'>*</span>" : "<span class='acmq-optional'>(optionnel)</span>" %>
                                    </label>
                                    <input type="text" id="proposition<%= i %>" name="proposition<%= i %>" class="acmq-input"
                                           placeholder="Proposition <%= lettres[i-1] %>..." <%= i <= 2 ? "required" : "" %>>
                                </div>
                            <% } %>
                        </div>

                        <%-- Bonne réponse --%>
                        <div class="acmq-form-group">
                            <label><i class="fas fa-check-circle"></i> Bonne réponse <span class="acmq-required">*</span></label>
                            <div class="acmq-bonne-reponse-options">
                                <% for (int i = 1; i <= 4; i++) { %>
                                    <label class="acmq-radio-label">
                                        <input type="radio" name="bonneReponse" value="<%= i %>" required>
                                        <span class="acmq-radio-lettre"><%= lettres[i-1] %></span>
                                    </label>
                                <% } %>
                            </div>
                        </div>

                        <%-- Boutons --%>
                        <div class="acmq-form-actions">
                            <button type="button" class="acmq-btn acmq-btn-annuler" id="btnAnnuler" style="display:none;" 
                                    onclick="reinitialiserFormulaire()">
                                <i class="fas fa-times"></i> Annuler
                            </button>
                            <button type="submit" class="acmq-btn acmq-btn-primary" id="btnSubmit">
                                <i class="fas fa-plus-circle"></i> <span id="btnSubmitText">Ajouter la question</span>
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <%-- ===== GUIDE ===== --%>
            <div class="acmq-card acmq-card-guide">
                <div class="acmq-card-header acmq-card-header-sm">
                    <h2><i class="fas fa-lightbulb"></i> Rappel</h2>
                </div>
                <div class="acmq-card-body">
                    <ul class="acmq-guide-list">
                        <li><i class="fas fa-check-circle"></i> Chaque question a <strong>4 propositions (A, B, C, D)</strong>.</li>
                        <li><i class="fas fa-check-circle"></i> Une seule <strong>bonne réponse</strong> possible.</li>
                        <li><i class="fas fa-check-circle"></i> Objectif : <strong><%= nbQuestionsRequis %> questions</strong> (actuellement <%= nbQuestionsActuelles %>).</li>
                        <li><i class="fas fa-check-circle"></i> Score minimum pour l'apprenant : <strong><%= examen.getSeuilReussite() %>%</strong>.</li>
                    </ul>
                </div>
            </div>

        </div>
    </div>
</div>

<%-- ===== SCRIPTS ===== --%>
<script>
    var lettres = ['A', 'B', 'C', 'D'];

    function ouvrirModification(id, question, p1, p2, p3, p4, bonneReponse) {
        document.getElementById('formAction').value = 'modifier';
        document.getElementById('questionId').value = id;
        document.getElementById('question').value = question;
        document.getElementById('proposition1').value = p1;
        document.getElementById('proposition2').value = p2;
        document.getElementById('proposition3').value = p3;
        document.getElementById('proposition4').value = p4;

        var radios = document.getElementsByName('bonneReponse');
        for (var i = 0; i < radios.length; i++) {
            radios[i].checked = (parseInt(radios[i].value) === bonneReponse);
        }

        document.getElementById('formTitle').innerHTML = '<i class="fas fa-pen"></i> Modifier la question';
        document.getElementById('btnSubmitText').textContent = 'Enregistrer la modification';
        document.getElementById('btnAnnuler').style.display = 'inline-flex';
        document.getElementById('question').focus();
    }

    function reinitialiserFormulaire() {
        document.getElementById('formAction').value = 'ajouter';
        document.getElementById('questionId').value = '';
        document.getElementById('question').value = '';
        document.getElementById('proposition1').value = '';
        document.getElementById('proposition2').value = '';
        document.getElementById('proposition3').value = '';
        document.getElementById('proposition4').value = '';

        var radios = document.getElementsByName('bonneReponse');
        for (var i = 0; i < radios.length; i++) radios[i].checked = false;

        document.getElementById('formTitle').innerHTML = '<i class="fas fa-plus-circle"></i> Ajouter une question';
        document.getElementById('btnSubmitText').textContent = 'Ajouter la question';
        document.getElementById('btnAnnuler').style.display = 'none';
    }

    document.getElementById('formQuestion').addEventListener('submit', function(e) {
        var question = document.getElementById('question');
        var p1 = document.getElementById('proposition1');
        var p2 = document.getElementById('proposition2');
        var bonneReponse = document.querySelector('input[name="bonneReponse"]:checked');
        var hasError = false;

        [question, p1, p2].forEach(function(el) { el.classList.remove('acmq-input-error'); });

        if (!question.value.trim()) { question.classList.add('acmq-input-error'); hasError = true; }
        if (!p1.value.trim()) { p1.classList.add('acmq-input-error'); hasError = true; }
        if (!p2.value.trim()) { p2.classList.add('acmq-input-error'); hasError = true; }
        if (!bonneReponse) { hasError = true; }

        if (hasError) {
            e.preventDefault();
            if (!question.value.trim()) question.focus();
            else if (!p1.value.trim()) p1.focus();
            else if (!p2.value.trim()) p2.focus();
        }
    });
</script>

<%@ include file="../footer-connecte.jsp" %>