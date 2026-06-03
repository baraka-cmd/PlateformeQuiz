<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.amijayquiz.models.Question" %>

<%-- ============================================================
     FICHIER : views/admin/admin-questions.jsp
     RÔLE    : Page d'administration des questions
              - Tableau de toutes les questions
              - Formulaire d'ajout/modification
              - Recherche par mot-clé
              - Suppression avec confirmation
     ============================================================ --%>

<%
    List<Question> questions = (List<Question>) request.getAttribute("questions");
    Integer totalQuestions = (Integer) request.getAttribute("totalQuestions");
    String recherche = (String) request.getAttribute("recherche");
    String messageSucces = (String) request.getAttribute("messageSucces");
    String messageErreur = (String) request.getAttribute("messageErreur");

    if (questions == null) questions = new java.util.ArrayList<Question>();
    if (totalQuestions == null) totalQuestions = 0;
    if (recherche == null) recherche = "";

    request.setAttribute("pageTitle", "Gestion des questions - Administration - AmijayQuiz");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-questions.css\">";
    request.setAttribute("extraHead", adminCSS);
%>

<%@ include file="../header.jsp" %>

<div class="admin-questions-container">

    <%-- ============================================================
         BREADCRUMB
         ============================================================ --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-question-circle"></i><span>Questions</span>
        </span>
    </nav>

    <%-- ============================================================
         EN-TETE
         ============================================================ --%>
    <div class="admin-questions-header">
        <div class="admin-header-left">
            <div class="admin-header-icon">
                <i class="fas fa-question-circle"></i>
            </div>
            <div class="admin-header-text">
                <h1>Gestion des questions</h1>
                <p><%= totalQuestions %> question<%= totalQuestions > 1 ? "s" : "" %> dans la base de donnees</p>
            </div>
        </div>
        <button type="button" class="btn-ajouter-question" onclick="ouvrirModalAjout()">
            <i class="fas fa-plus-circle"></i>
            <span>Ajouter une question</span>
        </button>
    </div>

    <%-- ============================================================
         MESSAGES FLASH
         ============================================================ --%>
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

    <%-- ============================================================
         BARRE DE RECHERCHE
         ============================================================ --%>
    <div class="admin-recherche-bar">
        <form action="${pageContext.request.contextPath}/admin/questions" method="GET" class="form-recherche">
            <div class="recherche-input-wrapper">
                <i class="fas fa-search"></i>
                <input type="text" name="recherche" value="<%= recherche %>" 
                       placeholder="Rechercher une question par mot-cle..." 
                       class="recherche-input">
            </div>
            <button type="submit" class="btn-rechercher">
                <i class="fas fa-search"></i> Rechercher
            </button>
            <% if (recherche != null && !recherche.isEmpty()) { %>
                <a href="${pageContext.request.contextPath}/admin/questions" class="btn-reset-recherche">
                    <i class="fas fa-times"></i> Reinitialiser
                </a>
            <% } %>
        </form>
    </div>

    <%-- ============================================================
         TABLEAU DES QUESTIONS
         ============================================================ --%>
    <div class="admin-tableau-section">
        <% if (questions != null && !questions.isEmpty()) { %>
            <div class="table-responsive">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th class="th-id">ID</th>
                            <th>Question</th>
                            <th>Proposition A</th>
                            <th>Proposition B</th>
                            <th>Proposition C</th>
                            <th>Proposition D</th>
                            <th class="th-bonne">Bonne rep.</th>
                            <th class="th-action">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            String[] lettres = {"A", "B", "C", "D"};
                            for (Question q : questions) {
                                String texteQuestion = q.getTexteQuestion();
                                if (texteQuestion == null) texteQuestion = "";
                                if (texteQuestion.length() > 60) texteQuestion = texteQuestion.substring(0, 60) + "...";
                        %>
                            <tr>
                                <td class="td-id">#<%= q.getId() %></td>
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
                                    <button type="button" class="btn-action-icon btn-modifier" 
                                            title="Modifier cette question"
                                            onclick="ouvrirModalModification('<%= q.getId() %>', '<%= q.getTexteQuestion() != null ? q.getTexteQuestion().replace("'", "\\'").replace("\"", "&quot;") : "" %>', '<%= q.getProposition1() != null ? q.getProposition1().replace("'", "\\'") : "" %>', '<%= q.getProposition2() != null ? q.getProposition2().replace("'", "\\'") : "" %>', '<%= q.getProposition3() != null ? q.getProposition3().replace("'", "\\'") : "" %>', '<%= q.getProposition4() != null ? q.getProposition4().replace("'", "\\'") : "" %>', '<%= q.getBonneReponse() %>')">
                                        <i class="fas fa-edit"></i>
                                    </button>
                                    <form action="${pageContext.request.contextPath}/admin/questions" method="POST" class="form-action" onsubmit="return confirm('Supprimer cette question ?')">
                                        <input type="hidden" name="action" value="supprimer">
                                        <input type="hidden" name="id" value="<%= q.getId() %>">
                                        <button type="submit" class="btn-action-icon btn-supprimer" title="Supprimer cette question">
                                            <i class="fas fa-trash-alt"></i>
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        <%
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
                <h3>Aucune question trouvee</h3>
                <p>
                    <% if (recherche != null && !recherche.isEmpty()) { %>
                        Aucun resultat pour "<strong><%= recherche %></strong>".
                    <% } else { %>
                        La base de donnees ne contient pas encore de questions.
                    <% } %>
                </p>
            </div>
        <% } %>
    </div>

    <%-- ============================================================
         BOUTON RETOUR
         ============================================================ --%>
    <div class="admin-back">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn-retour">
            <i class="fas fa-arrow-left"></i> Retour au dashboard admin
        </a>
    </div>

</div>

<%-- ============================================================
     MODALE AJOUT / MODIFICATION
     ============================================================ --%>
<div class="modal-overlay" id="modalQuestion" style="display: none;">
    <div class="modal-container modal-question">
        <div class="modal-header">
            <div class="modal-header-left">
                <div class="modal-header-icon">
                    <i class="fas fa-question-circle"></i>
                </div>
                <div class="modal-header-text">
                    <h2 id="modalTitre">Ajouter une question</h2>
                </div>
            </div>
            <button type="button" class="modal-close-btn" onclick="fermerModalQuestion()" title="Fermer">
                <i class="fas fa-times"></i>
            </button>
        </div>
        <div class="modal-body">
            <form id="formQuestion" action="${pageContext.request.contextPath}/admin/questions" method="POST">
                <input type="hidden" name="action" id="formAction" value="ajouter">
                <input type="hidden" name="id" id="formId" value="">

                <div class="form-group">
                    <label for="texteQuestion">Texte de la question <span class="required">*</span></label>
                    <textarea id="texteQuestion" name="texteQuestion" rows="3" 
                              placeholder="Entrez le texte de la question..." required></textarea>
                </div>

                <div class="form-group">
                    <label for="proposition1">Proposition A <span class="required">*</span></label>
                    <input type="text" id="proposition1" name="proposition1" 
                           placeholder="Proposition A..." required>
                </div>

                <div class="form-group">
                    <label for="proposition2">Proposition B <span class="required">*</span></label>
                    <input type="text" id="proposition2" name="proposition2" 
                           placeholder="Proposition B..." required>
                </div>

                <div class="form-group">
                    <label for="proposition3">Proposition C (optionnelle)</label>
                    <input type="text" id="proposition3" name="proposition3" 
                           placeholder="Proposition C...">
                </div>

                <div class="form-group">
                    <label for="proposition4">Proposition D (optionnelle)</label>
                    <input type="text" id="proposition4" name="proposition4" 
                           placeholder="Proposition D...">
                </div>

                <div class="form-group">
                    <label for="bonneReponse">Bonne reponse (1=A, 2=B, 3=C, 4=D) <span class="required">*</span></label>
                    <select id="bonneReponse" name="bonneReponse" required>
                        <option value="">-- Choisissez --</option>
                        <option value="1">A</option>
                        <option value="2">B</option>
                        <option value="3">C</option>
                        <option value="4">D</option>
                    </select>
                </div>

                <div class="modal-buttons">
                    <button type="button" class="btn-modal btn-modal-annuler" onclick="fermerModalQuestion()">
                        <i class="fas fa-times"></i> Annuler
                    </button>
                    <button type="submit" class="btn-modal btn-modal-confirmer">
                        <i class="fas fa-save"></i> Enregistrer
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<%-- ============================================================
     SCRIPT
     ============================================================ --%>
<script>
    function ouvrirModalAjout() {
        document.getElementById('modalTitre').textContent = 'Ajouter une question';
        document.getElementById('formAction').value = 'ajouter';
        document.getElementById('formId').value = '';
        document.getElementById('texteQuestion').value = '';
        document.getElementById('proposition1').value = '';
        document.getElementById('proposition2').value = '';
        document.getElementById('proposition3').value = '';
        document.getElementById('proposition4').value = '';
        document.getElementById('bonneReponse').value = '';
        document.getElementById('modalQuestion').style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }

    function ouvrirModalModification(id, texte, p1, p2, p3, p4, bonne) {
        document.getElementById('modalTitre').textContent = 'Modifier la question #' + id;
        document.getElementById('formAction').value = 'modifier';
        document.getElementById('formId').value = id;
        document.getElementById('texteQuestion').value = texte.replace(/&quot;/g, '"').replace(/\\'/g, "'");
        document.getElementById('proposition1').value = p1.replace(/\\'/g, "'");
        document.getElementById('proposition2').value = p2.replace(/\\'/g, "'");
        document.getElementById('proposition3').value = p3.replace(/\\'/g, "'");
        document.getElementById('proposition4').value = p4.replace(/\\'/g, "'");
        document.getElementById('bonneReponse').value = bonne;
        document.getElementById('modalQuestion').style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }

    function fermerModalQuestion() {
        document.getElementById('modalQuestion').style.display = 'none';
        document.body.style.overflow = '';
    }

    // Fermer avec Escape
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') fermerModalQuestion();
    });

    // Fermer en cliquant sur l'overlay
    document.getElementById('modalQuestion').addEventListener('click', function(e) {
        if (e.target === this) fermerModalQuestion();
    });
</script>

<%
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-questions.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../footer-connecte.jsp" %>