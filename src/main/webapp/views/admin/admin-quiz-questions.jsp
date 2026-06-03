<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.amijayquiz.models.Quiz" %>
<%@ page import="com.amijayquiz.models.Question" %>

<%-- ============================================================
     FICHIER : views/admin/admin-quiz-questions.jsp
     RÔLE    : Page d'administration des questions d'un quiz
              - Liste des questions associées au quiz
              - Ajout de questions depuis la base
              - Retrait de questions du quiz
              - Recherche dans les questions disponibles
     ============================================================ --%>

<%
    Quiz quiz = (Quiz) request.getAttribute("quiz");
    List<Question> questionsDuQuiz = (List<Question>) request.getAttribute("questionsDuQuiz");
    List<Question> questionsDisponibles = (List<Question>) request.getAttribute("questionsDisponibles");
    Integer totalQuestionsQuiz = (Integer) request.getAttribute("totalQuestionsQuiz");
    Integer totalDisponibles = (Integer) request.getAttribute("totalDisponibles");
    String recherche = (String) request.getAttribute("recherche");
    String messageSucces = (String) request.getAttribute("messageSucces");
    String messageErreur = (String) request.getAttribute("messageErreur");

    if (quiz == null) {
        response.sendRedirect(request.getContextPath() + "/admin/quiz");
        return;
    }
    if (questionsDuQuiz == null) questionsDuQuiz = new java.util.ArrayList<Question>();
    if (questionsDisponibles == null) questionsDisponibles = new java.util.ArrayList<Question>();
    if (totalQuestionsQuiz == null) totalQuestionsQuiz = 0;
    if (totalDisponibles == null) totalDisponibles = 0;
    if (recherche == null) recherche = "";

    request.setAttribute("pageTitle", "Questions du quiz - " + quiz.getTitre() + " - Administration");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-quiz.css\">";
    request.setAttribute("extraHead", adminCSS);
%>

<%@ include file="../header.jsp" %>

<div class="admin-quiz-questions-container">

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
        <span class="breadcrumb-current">
            <i class="fas fa-question-circle"></i><span>Questions</span>
        </span>
    </nav>

    <%-- ===== MESSAGES FLASH ===== --%>
    <% if (messageSucces != null) { %>
        <div class="message-alerte message-succes">
            <i class="fas fa-check-circle"></i>
            <span><%= messageSucces %></span>
        </div>
    <% } %>
    <% if (messageErreur != null) { %>
        <div class="message-alerte message-erreur">
            <i class="fas fa-exclamation-circle"></i>
            <span><%= messageErreur %></span>
        </div>
    <% } %>

    <%-- ===== EN-TETE DU QUIZ ===== --%>
    <div class="quiz-info-header">
        <div class="quiz-info-top">
            <a href="${pageContext.request.contextPath}/admin/quiz" class="btn-retour-quiz">
                <i class="fas fa-arrow-left"></i> Retour aux quiz
            </a>
            <div class="quiz-info-main">
                <div class="quiz-info-icon">
                    <i class="fas fa-question-circle"></i>
                </div>
                <div class="quiz-info-text">
                    <h1><%= quiz.getTitre() %></h1>
                    <div class="quiz-info-meta">
                        <span class="badge-niveau badge-niveau-<%= quiz.getNiveau() %>">
                            <i class="<%= quiz.getIconeNiveau() %>"></i>
                            <%= quiz.getNiveauFormate() %>
                        </span>
                        <span class="quiz-meta-item">
                            <i class="fas fa-question-circle"></i>
                            <strong><%= totalQuestionsQuiz %></strong> question<%= totalQuestionsQuiz > 1 ? "s" : "" %>
                        </span>
                        <span class="quiz-meta-item">
                            <i class="far fa-clock"></i>
                            <%= quiz.getDureeMaxMinutes() %> min
                        </span>
                    </div>
                </div>
            </div>
            <button type="button" class="btn-ajouter-questions" onclick="ouvrirModalAjoutQuestions()">
                <i class="fas fa-plus-circle"></i>
                <span>Ajouter des questions</span>
            </button>
        </div>
    </div>

    <%-- ===== TABLEAU DES QUESTIONS DU QUIZ ===== --%>
    <div class="admin-section">
        <div class="section-header">
            <h2><i class="fas fa-list-ul"></i> Questions associees au quiz</h2>
            <span class="badge-count"><%= totalQuestionsQuiz %> question<%= totalQuestionsQuiz > 1 ? "s" : "" %></span>
        </div>

        <% if (questionsDuQuiz != null && !questionsDuQuiz.isEmpty()) { %>
            <div class="table-responsive">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th class="th-id">#</th>
                            <th>Question</th>
                            <th>Proposition A</th>
                            <th>Proposition B</th>
                            <th>Proposition C</th>
                            <th>Proposition D</th>
                            <th class="th-bonne">Bonne</th>
                            <th class="th-action">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            String[] lettres = {"A", "B", "C", "D"};
                            int index = 1;
                            for (Question q : questionsDuQuiz) {
                                String texteQuestion = q.getTexteQuestion();
                                if (texteQuestion == null) texteQuestion = "";
                                if (texteQuestion.length() > 60) texteQuestion = texteQuestion.substring(0, 60) + "...";
                        %>
                            <tr>
                                <td class="td-id"><%= index %></td>
                                <td>
                                    <span class="cell-texte" title="<%= q.getTexteQuestion() != null ? q.getTexteQuestion().replace("\"", "&quot;") : "" %>">
                                        <%= texteQuestion %>
                                    </span>
                                </td>
                                <td><span class="cell-proposition"><%= q.getProposition1() != null ? q.getProposition1() : "-" %></span></td>
                                <td><span class="cell-proposition"><%= q.getProposition2() != null ? q.getProposition2() : "-" %></span></td>
                                <td><span class="cell-proposition"><%= q.getProposition3() != null && !q.getProposition3().isEmpty() ? q.getProposition3() : "-" %></span></td>
                                <td><span class="cell-proposition"><%= q.getProposition4() != null && !q.getProposition4().isEmpty() ? q.getProposition4() : "-" %></span></td>
                                <td class="td-bonne">
                                    <span class="badge-bonne-reponse"><%= lettres[q.getBonneReponse() - 1] %></span>
                                </td>
                                <td class="td-actions">
                                    <form action="${pageContext.request.contextPath}/admin/quiz/questions" method="POST" class="form-action" onsubmit="return confirm('Retirer cette question du quiz ?')">
                                        <input type="hidden" name="action" value="retirer">
                                        <input type="hidden" name="quizId" value="<%= quiz.getId() %>">
                                        <input type="hidden" name="questionId" value="<%= q.getId() %>">
                                        <button type="submit" class="btn-action-icon btn-supprimer" title="Retirer cette question du quiz">
                                            <i class="fas fa-unlink"></i>
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        <%
                                index++;
                            }
                        %>
                    </tbody>
                </table>
            </div>
        <% } else { %>
            <div class="empty-state">
                <div class="empty-icon">
                    <i class="fas fa-inbox"></i>
                </div>
                <h3>Aucune question dans ce quiz</h3>
                <p>Cliquez sur "Ajouter des questions" pour associer des questions existantes.</p>
            </div>
        <% } %>
    </div>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="admin-back">
        <a href="${pageContext.request.contextPath}/admin/quiz" class="btn-retour">
            <i class="fas fa-arrow-left"></i> Retour a la liste des quiz
        </a>
    </div>

</div>

<%-- ============================================================
     MODALE AJOUT DE QUESTIONS
     ============================================================ --%>
<div class="modal-overlay" id="modalAjoutQuestions" style="display: none;">
    <div class="modal-container modal-ajout-questions">
        <div class="modal-header">
            <div class="modal-header-left">
                <div class="modal-header-icon">
                    <i class="fas fa-plus-circle"></i>
                </div>
                <div class="modal-header-text">
                    <h2>Ajouter des questions au quiz</h2>
                    <span><%= quiz.getTitre() %></span>
                </div>
            </div>
            <button type="button" class="modal-close-btn" onclick="fermerModalAjoutQuestions()" title="Fermer">
                <i class="fas fa-times"></i>
            </button>
        </div>
        <div class="modal-body">
            <%-- Recherche dans les questions disponibles --%>
            <div class="recherche-questions-bar">
                <form action="${pageContext.request.contextPath}/admin/quiz/questions" method="GET" class="form-recherche-questions">
                    <input type="hidden" name="quizId" value="<%= quiz.getId() %>">
                    <div class="recherche-input-wrapper">
                        <i class="fas fa-search"></i>
                        <input type="text" name="recherche" value="<%= recherche %>" 
                               placeholder="Rechercher une question..." 
                               class="recherche-input">
                    </div>
                    <button type="submit" class="btn-rechercher">
                        <i class="fas fa-search"></i> Rechercher
                    </button>
                </form>
            </div>

            <%-- Formulaire d'ajout --%>
            <form action="${pageContext.request.contextPath}/admin/quiz/questions" method="POST" id="formAjoutQuestions">
                <input type="hidden" name="action" value="ajouter">
                <input type="hidden" name="quizId" value="<%= quiz.getId() %>">

                <div class="questions-disponibles-liste">
                    <% if (questionsDisponibles != null && !questionsDisponibles.isEmpty()) { %>
                        <div class="questions-count">
                            <span id="selectedCount">0</span> / <%= totalDisponibles %> question<%= totalDisponibles > 1 ? "s" : "" %> selectionnee<%= totalDisponibles > 1 ? "s" : "" %>
                        </div>
                        <%
                            for (Question q : questionsDisponibles) {
                                String texteQuestion = q.getTexteQuestion();
                                if (texteQuestion == null) texteQuestion = "";
                        %>
                            <label class="question-checkbox-item">
                                <input type="checkbox" name="idsQuestions" value="<%= q.getId() %>" 
                                       onchange="updateSelectedCount()">
                                <span class="checkbox-custom"></span>
                                <span class="question-checkbox-texte" title="<%= texteQuestion.replace("\"", "&quot;") %>">
                                    <%= texteQuestion.length() > 80 ? texteQuestion.substring(0, 80) + "..." : texteQuestion %>
                                </span>
                            </label>
                        <%
                            }
                        %>
                    <% } else { %>
                        <div class="empty-state">
                            <p>
                                <% if (recherche != null && !recherche.isEmpty()) { %>
                                    Aucune question trouvee pour "<strong><%= recherche %></strong>".
                                <% } else { %>
                                    Toutes les questions de la base sont deja dans ce quiz.
                                <% } %>
                            </p>
                        </div>
                    <% } %>
                </div>

                <div class="modal-buttons">
                    <button type="button" class="btn-modal btn-modal-annuler" onclick="fermerModalAjoutQuestions()">
                        <i class="fas fa-times"></i> Annuler
                    </button>
                    <button type="submit" class="btn-modal btn-modal-confirmer" id="btnAjouterQuestions" disabled>
                        <i class="fas fa-plus-circle"></i> Ajouter les questions selectionnees
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    function ouvrirModalAjoutQuestions() {
        document.getElementById('modalAjoutQuestions').style.display = 'flex';
        document.body.style.overflow = 'hidden';
        updateSelectedCount();
    }

    function fermerModalAjoutQuestions() {
        document.getElementById('modalAjoutQuestions').style.display = 'none';
        document.body.style.overflow = '';
    }

    function updateSelectedCount() {
        var checkboxes = document.querySelectorAll('input[name="idsQuestions"]:checked');
        var count = checkboxes.length;
        document.getElementById('selectedCount').textContent = count;
        document.getElementById('btnAjouterQuestions').disabled = count === 0;
    }

    document.getElementById('modalAjoutQuestions').addEventListener('click', function(e) {
        if (e.target === this) fermerModalAjoutQuestions();
    });

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') fermerModalAjoutQuestions();
    });
</script>

<%
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-quiz.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../footer-connecte.jsp" %>