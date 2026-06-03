<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.amijayquiz.models.Quiz" %>

<%-- ============================================================
     FICHIER : views/admin/admin-quiz.jsp
     RÔLE    : Page d'administration des quiz
              - Tableau de tous les quiz
              - Formulaire d'ajout/modification
              - Filtre par niveau
              - Recherche par titre
              - Suppression avec confirmation
     ============================================================ --%>

<%
    List<Quiz> quizList = (List<Quiz>) request.getAttribute("quizList");
    Integer totalQuiz = (Integer) request.getAttribute("totalQuiz");
    String recherche = (String) request.getAttribute("recherche");
    String filtreNiveau = (String) request.getAttribute("filtreNiveau");
    String messageSucces = (String) request.getAttribute("messageSucces");
    String messageErreur = (String) request.getAttribute("messageErreur");

    if (quizList == null) quizList = new java.util.ArrayList<Quiz>();
    if (totalQuiz == null) totalQuiz = 0;
    if (recherche == null) recherche = "";
    if (filtreNiveau == null) filtreNiveau = "tous";

    request.setAttribute("pageTitle", "Gestion des quiz - Administration - AmijayQuiz");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-quiz.css\">";
    request.setAttribute("extraHead", adminCSS);
%>

<%@ include file="../header.jsp" %>

<div class="admin-quiz-container">

    <%-- ============================================================
         BREADCRUMB
         ============================================================ --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-list-check"></i><span>Quiz</span>
        </span>
    </nav>

    <%-- ============================================================
         EN-TETE
         ============================================================ --%>
    <div class="admin-quiz-header">
        <div class="admin-header-left">
            <div class="admin-header-icon">
                <i class="fas fa-list-check"></i>
            </div>
            <div class="admin-header-text">
                <h1>Gestion des quiz</h1>
                <p><%= totalQuiz %> quiz disponible<%= totalQuiz > 1 ? "s" : "" %></p>
            </div>
        </div>
        <button type="button" class="btn-ajouter-quiz" onclick="ouvrirModalAjout()">
            <i class="fas fa-plus-circle"></i>
            <span>Creer un quiz</span>
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
         BARRE DE FILTRES ET RECHERCHE
         ============================================================ --%>
    <div class="admin-filtres-bar">
        <form action="${pageContext.request.contextPath}/admin/quiz" method="GET" class="form-filtres">
            <div class="recherche-input-wrapper">
                <i class="fas fa-search"></i>
                <input type="text" name="recherche" value="<%= recherche %>" 
                       placeholder="Rechercher par titre ou description..." 
                       class="recherche-input">
            </div>

            <select name="niveau" class="filtre-select" onchange="this.form.submit()">
                <option value="tous" <%= "tous".equals(filtreNiveau) ? "selected" : "" %>>Tous les niveaux</option>
                <option value="elementaire" <%= "elementaire".equals(filtreNiveau) ? "selected" : "" %>>Elementaire</option>
                <option value="moyen" <%= "moyen".equals(filtreNiveau) ? "selected" : "" %>>Moyen</option>
                <option value="avance" <%= "avance".equals(filtreNiveau) ? "selected" : "" %>>Avance</option>
            </select>

            <button type="submit" class="btn-rechercher">
                <i class="fas fa-search"></i> Filtrer
            </button>

            <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreNiveau)) { %>
                <a href="${pageContext.request.contextPath}/admin/quiz" class="btn-reset-filtres">
                    <i class="fas fa-times"></i> Reinitialiser
                </a>
            <% } %>
        </form>
    </div>

    <%-- ============================================================
         TABLEAU DES QUIZ
         ============================================================ --%>
    <div class="admin-tableau-section">
        <% if (quizList != null && !quizList.isEmpty()) { %>
            <div class="table-responsive">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th class="th-id">ID</th>
                            <th>Titre</th>
                            <th>Description</th>
                            <th class="th-center">Niveau</th>
                            <th class="th-center">Duree</th>
                            <th class="th-center">Partie</th>
                            <th class="th-center">Questions</th>
                            <th class="th-action">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            for (Quiz q : quizList) {
                                String titre = q.getTitre();
                                String description = q.getDescription();
                                if (titre == null) titre = "";
                                if (description == null) description = "";
                                if (titre.length() > 50) titre = titre.substring(0, 50) + "...";
                                if (description.length() > 60) description = description.substring(0, 60) + "...";
                        %>
                            <tr>
                                <td class="td-id">#<%= q.getId() %></td>
                                <td>
                                    <span class="cell-titre" title="<%= q.getTitre() != null ? q.getTitre().replace("\"", "&quot;") : "" %>">
                                        <%= titre %>
                                    </span>
                                </td>
                                <td>
                                    <span class="cell-description">
                                        <%= description.isEmpty() ? "-" : description %>
                                    </span>
                                </td>
                                <td class="td-center">
                                    <span class="badge-niveau badge-niveau-<%= q.getNiveau() %>">
                                        <i class="<%= q.getIconeNiveau() %>"></i>
                                        <%= q.getNiveauFormate() %>
                                    </span>
                                </td>
                                <td class="td-center">
                                    <span class="cell-duree">
                                        <i class="far fa-clock"></i> <%= q.getDureeMaxMinutes() %> min
                                    </span>
                                </td>
                                <td class="td-center">
                                    <span class="cell-partie">Partie <%= q.getOrdrePartie() %></span>
                                </td>
                                <td class="td-center">
                                    <span class="badge-questions"><%= q.getNombreQuestions() %></span>
                                </td>
                                <td class="td-actions">
                                    <button type="button" class="btn-action-icon btn-modifier" 
                                            title="Modifier ce quiz"
                                            onclick="ouvrirModalModification('<%= q.getId() %>', '<%= q.getTitre() != null ? q.getTitre().replace("'", "\\'").replace("\"", "&quot;") : "" %>', '<%= q.getDescription() != null ? q.getDescription().replace("'", "\\'") : "" %>', '<%= q.getDureeMaxMinutes() %>', '<%= q.getNiveau() %>', '<%= q.getOrdrePartie() %>')">
                                        <i class="fas fa-edit"></i>
                                    </button>
                                    <form action="${pageContext.request.contextPath}/admin/quiz" method="POST" class="form-action" onsubmit="return confirm('Supprimer ce quiz ? Toutes les questions associees seront dissociees.')">
                                        <input type="hidden" name="action" value="supprimer">
                                        <input type="hidden" name="id" value="<%= q.getId() %>">
                                        <button type="submit" class="btn-action-icon btn-supprimer" title="Supprimer ce quiz">
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
                <h3>Aucun quiz trouve</h3>
                <p>
                    <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreNiveau)) { %>
                        Aucun resultat avec ces filtres.
                    <% } else { %>
                        La base de donnees ne contient pas encore de quiz.
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
<div class="modal-overlay" id="modalQuiz" style="display: none;">
    <div class="modal-container modal-quiz">
        <div class="modal-header">
            <div class="modal-header-left">
                <div class="modal-header-icon">
                    <i class="fas fa-list-check"></i>
                </div>
                <div class="modal-header-text">
                    <h2 id="modalTitre">Creer un quiz</h2>
                </div>
            </div>
            <button type="button" class="modal-close-btn" onclick="fermerModalQuiz()" title="Fermer">
                <i class="fas fa-times"></i>
            </button>
        </div>
        <div class="modal-body">
            <form id="formQuiz" action="${pageContext.request.contextPath}/admin/quiz" method="POST">
                <input type="hidden" name="action" id="formAction" value="ajouter">
                <input type="hidden" name="id" id="formId" value="">

                <div class="form-group">
                    <label for="titre">Titre du quiz <span class="required">*</span></label>
                    <input type="text" id="titre" name="titre" 
                           placeholder="Ex: Informatique Generale - Partie 1" required>
                </div>

                <div class="form-group">
                    <label for="description">Description</label>
                    <textarea id="description" name="description" rows="3" 
                              placeholder="Description du quiz..."></textarea>
                </div>

                <div class="form-row">
                    <div class="form-group form-half">
                        <label for="dureeMaxMinutes">Duree (minutes) <span class="required">*</span></label>
                        <input type="number" id="dureeMaxMinutes" name="dureeMaxMinutes" 
                               value="10" min="1" max="60" required>
                    </div>

                    <div class="form-group form-half">
                        <label for="ordrePartie">Numero de partie</label>
                        <input type="number" id="ordrePartie" name="ordrePartie" 
                               value="1" min="1" max="20">
                    </div>
                </div>

                <div class="form-group">
                    <label for="niveau">Niveau <span class="required">*</span></label>
                    <select id="niveau" name="niveau" required>
                        <option value="">-- Choisissez le niveau --</option>
                        <option value="elementaire">Elementaire</option>
                        <option value="moyen">Moyen</option>
                        <option value="avance">Avance</option>
                    </select>
                </div>

                <div class="modal-buttons">
                    <button type="button" class="btn-modal btn-modal-annuler" onclick="fermerModalQuiz()">
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
        document.getElementById('modalTitre').textContent = 'Creer un quiz';
        document.getElementById('formAction').value = 'ajouter';
        document.getElementById('formId').value = '';
        document.getElementById('titre').value = '';
        document.getElementById('description').value = '';
        document.getElementById('dureeMaxMinutes').value = '10';
        document.getElementById('ordrePartie').value = '1';
        document.getElementById('niveau').value = '';
        document.getElementById('modalQuiz').style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }

    function ouvrirModalModification(id, titre, description, duree, niveau, ordre) {
        document.getElementById('modalTitre').textContent = 'Modifier le quiz #' + id;
        document.getElementById('formAction').value = 'modifier';
        document.getElementById('formId').value = id;
        document.getElementById('titre').value = titre.replace(/&quot;/g, '"').replace(/\\'/g, "'");
        document.getElementById('description').value = description.replace(/\\'/g, "'");
        document.getElementById('dureeMaxMinutes').value = duree;
        document.getElementById('ordrePartie').value = ordre;
        document.getElementById('niveau').value = niveau;
        document.getElementById('modalQuiz').style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }

    function fermerModalQuiz() {
        document.getElementById('modalQuiz').style.display = 'none';
        document.body.style.overflow = '';
    }

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') fermerModalQuiz();
    });

    document.getElementById('modalQuiz').addEventListener('click', function(e) {
        if (e.target === this) fermerModalQuiz();
    });
</script>

<%
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-quiz.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../footer-connecte.jsp" %>