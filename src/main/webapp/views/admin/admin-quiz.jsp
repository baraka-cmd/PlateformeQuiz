<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.amijayquiz.models.Quiz" %>

<%-- ============================================================
     FICHIER : views/admin/admin-quiz.jsp
     RÔLE    : Page d'administration des quiz
              - Tableau paginé des quiz
              - Filtres avancés (thème, niveau, recherche)
              - Statistiques globales
              - Liens vers création et modification
              - Menu d'actions par quiz
     ============================================================ --%>

<%
    List<Quiz> quizPage = (List<Quiz>) request.getAttribute("quizPage");
    Integer totalQuiz = (Integer) request.getAttribute("totalQuiz");
    Integer totalQuizGlobal = (Integer) request.getAttribute("totalQuizGlobal");
    Integer pageActuelle = (Integer) request.getAttribute("page");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    String recherche = (String) request.getAttribute("recherche");
    String filtreNiveau = (String) request.getAttribute("filtreNiveau");
    String filtreTheme = (String) request.getAttribute("filtreTheme");
    String tri = (String) request.getAttribute("tri");
    List<String> tousLesThemes = (List<String>) request.getAttribute("tousLesThemes");
    Map<String, Integer> stats = (Map<String, Integer>) request.getAttribute("stats");
    String urlPagination = (String) request.getAttribute("urlPagination");
    String messageSucces = (String) request.getAttribute("messageSucces");
    String messageErreur = (String) request.getAttribute("messageErreur");

    if (quizPage == null) quizPage = new java.util.ArrayList<Quiz>();
    if (totalQuiz == null) totalQuiz = 0;
    if (totalQuizGlobal == null) totalQuizGlobal = 0;
    if (pageActuelle == null) pageActuelle = 1;
    if (totalPages == null) totalPages = 1;
    if (recherche == null) recherche = "";
    if (filtreNiveau == null) filtreNiveau = "tous";
    if (filtreTheme == null) filtreTheme = "tous";
    if (tri == null) tri = "id";
    if (tousLesThemes == null) tousLesThemes = new java.util.ArrayList<String>();
    if (stats == null) stats = new java.util.LinkedHashMap<String, Integer>();
    if (urlPagination == null) urlPagination = "/admin/quiz?";

    request.setAttribute("pageTitle", "Gestion des quiz - Administration - AmijayQuiz");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-quiz.css\">";
    request.setAttribute("extraHead", adminCSS);
%>

<%@ include file="../header.jsp" %>

<div class="admin-quiz-container">

    <%-- ===== BREADCRUMB ===== --%>
    <nav class="breadcrumb" aria-label="Fil d'Ariane">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="breadcrumb-link">
            <i class="fas fa-tachometer-alt"></i><span>Dashboard</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-list-check"></i><span>Quiz</span>
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

    <%-- ===== EN-TETE ===== --%>
    <div class="admin-quiz-header">
        <div class="admin-header-left">
            <div class="admin-header-icon">
                <i class="fas fa-list-check"></i>
            </div>
            <div class="admin-header-text">
                <h1>Gestion des Quiz</h1>
                <p><%= totalQuizGlobal %> quiz au total</p>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/admin/quiz/creer" class="btn-ajouter-quiz">
            <i class="fas fa-plus-circle"></i>
            <span>Creer un quiz</span>
        </a>
    </div>

    <%-- ===== BARRE DE STATISTIQUES ===== --%>
    <div class="admin-stats-bar">
        <div class="stats-item">
            <i class="fas fa-layer-group"></i>
            <strong><%= stats.getOrDefault("themes", 0) %></strong> themes
        </div>
        <div class="stats-separator"></div>
        <div class="stats-item">
            <i class="fas fa-list-check"></i>
            <strong><%= stats.getOrDefault("total", 0) %></strong> quiz
        </div>
        <div class="stats-separator"></div>
        <div class="stats-item stats-elementaire">
            <i class="fas fa-seedling"></i>
            <strong><%= stats.getOrDefault("elementaire", 0) %></strong> elementaires
        </div>
        <div class="stats-separator"></div>
        <div class="stats-item stats-moyen">
            <i class="fas fa-fire"></i>
            <strong><%= stats.getOrDefault("moyen", 0) %></strong> moyens
        </div>
        <div class="stats-separator"></div>
        <div class="stats-item stats-avance">
            <i class="fas fa-star"></i>
            <strong><%= stats.getOrDefault("avance", 0) %></strong> avances
        </div>
    </div>

    <%-- ===== BARRE DE FILTRES ===== --%>
    <div class="admin-filtres-bar">
        <form action="${pageContext.request.contextPath}/admin/quiz" method="GET" class="form-filtres" id="formFiltres">
            <div class="recherche-input-wrapper">
                <i class="fas fa-search"></i>
                <input type="text" name="recherche" value="<%= recherche %>" 
                       placeholder="Rechercher par titre ou description..." 
                       class="recherche-input" id="rechercheInput">
            </div>

            <select name="theme" class="filtre-select" onchange="document.getElementById('formFiltres').submit()">
                <option value="tous" <%= "tous".equals(filtreTheme) ? "selected" : "" %>>Tous les themes</option>
                <% for (String theme : tousLesThemes) { %>
                    <option value="<%= theme %>" <%= theme.equals(filtreTheme) ? "selected" : "" %>><%= theme %></option>
                <% } %>
            </select>

            <select name="niveau" class="filtre-select" onchange="document.getElementById('formFiltres').submit()">
                <option value="tous" <%= "tous".equals(filtreNiveau) ? "selected" : "" %>>Tous les niveaux</option>
                <option value="elementaire" <%= "elementaire".equals(filtreNiveau) ? "selected" : "" %>>Elementaire</option>
                <option value="moyen" <%= "moyen".equals(filtreNiveau) ? "selected" : "" %>>Moyen</option>
                <option value="avance" <%= "avance".equals(filtreNiveau) ? "selected" : "" %>>Avance</option>
            </select>

            <select name="tri" class="filtre-select" onchange="document.getElementById('formFiltres').submit()">
                <option value="id" <%= "id".equals(tri) ? "selected" : "" %>>Tri : ID</option>
                <option value="titre" <%= "titre".equals(tri) ? "selected" : "" %>>Tri : Titre</option>
                <option value="niveau" <%= "niveau".equals(tri) ? "selected" : "" %>>Tri : Niveau</option>
                <option value="questions" <%= "questions".equals(tri) ? "selected" : "" %>>Tri : Questions</option>
            </select>

            <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreNiveau) || !"tous".equals(filtreTheme)) { %>
                <a href="${pageContext.request.contextPath}/admin/quiz" class="btn-reset-filtres">
                    <i class="fas fa-times"></i> Reinitialiser
                </a>
            <% } %>
        </form>
    </div>

    <%-- ===== TABLEAU DES QUIZ ===== --%>
    <div class="admin-tableau-section">
        <% if (quizPage != null && !quizPage.isEmpty()) { %>
            <div class="table-responsive">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th class="th-id">ID</th>
                            <th>Titre</th>
                            <th class="th-center">Niveau</th>
                            <th class="th-center">Partie</th>
                            <th class="th-center">Duree</th>
                            <th class="th-center">Questions</th>
                            <th class="th-action">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            for (Quiz q : quizPage) {
                                String titre = q.getTitre();
                                if (titre == null) titre = "";
                                if (titre.length() > 55) titre = titre.substring(0, 55) + "...";
                                
                                // Extraire le thème pour le lien de modification
                                String themeQuiz = "";
                                int indexPartie = titre.lastIndexOf(" - Partie");
                                if (indexPartie > 0) {
                                    themeQuiz = titre.substring(0, indexPartie);
                                }
                        %>
                            <tr>
                                <td class="td-id">#<%= q.getId() %></td>
                                <td>
                                    <span class="cell-titre" title="<%= q.getTitre() != null ? q.getTitre().replace("\"", "&quot;") : "" %>">
                                        <%= titre %>
                                    </span>
                                </td>
                                <td class="td-center">
                                    <span class="badge-niveau badge-niveau-<%= q.getNiveau() %>">
                                        <i class="<%= q.getIconeNiveau() %>"></i>
                                        <%= q.getNiveauFormate() %>
                                    </span>
                                </td>
                                <td class="td-center">
                                    <span class="cell-partie">Partie <%= q.getOrdrePartie() %></span>
                                </td>
                                <td class="td-center">
                                    <span class="cell-duree">
                                        <i class="far fa-clock"></i> <%= q.getDureeMaxMinutes() %> min
                                    </span>
                                </td>
                                <td class="td-center">
                                    <span class="badge-questions"><%= q.getNombreQuestions() %></span>
                                </td>
                                <td class="td-actions">
                                    <div class="dropdown-actions">
                                        <button type="button" class="btn-action-icon btn-menu" 
                                                title="Actions" onclick="toggleDropdown(this)">
                                            <i class="fas fa-ellipsis-v"></i>
                                        </button>
                                        <div class="dropdown-menu" style="display: none;">
                                            <a href="${pageContext.request.contextPath}/admin/quiz/questions?quizId=<%= q.getId() %>" 
                                               class="dropdown-item">
                                                <i class="fas fa-eye"></i> Voir les questions
                                            </a>
                                            <a href="${pageContext.request.contextPath}/admin/quiz/modifier?quizId=<%= q.getId() %>" 
                                               class="dropdown-item">
                                                <i class="fas fa-edit"></i> Modifier
                                            </a>
                                            <form action="${pageContext.request.contextPath}/admin/quiz/supprimer" method="POST" class="dropdown-item-form">
                                                <input type="hidden" name="id" value="<%= q.getId() %>">
                                                <button type="submit" class="dropdown-item dropdown-item-danger" 
                                                        onclick="return confirmSuppression('<%= titre.replace("'", "\\'") %>')">
                                                    <i class="fas fa-trash-alt"></i> Supprimer
                                                </button>
                                            </form>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        <%
                            }
                        %>
                    </tbody>
                </table>
            </div>

            <%-- ===== PAGINATION ===== --%>
            <% if (totalPages > 1) { %>
                <div class="pagination-bar">
                    <% if (pageActuelle > 1) { %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= pageActuelle - 1 %>" 
                           class="page-link" title="Page precedente">
                            <i class="fas fa-chevron-left"></i>
                        </a>
                    <% } else { %>
                        <span class="page-link disabled">
                            <i class="fas fa-chevron-left"></i>
                        </span>
                    <% } %>

                    <%
                        int debutPage = Math.max(1, pageActuelle - 2);
                        int finPage = Math.min(totalPages, pageActuelle + 2);

                        if (debutPage > 1) {
                    %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=1" class="page-link">1</a>
                        <% if (debutPage > 2) { %>
                            <span class="page-ellipsis">...</span>
                        <% } %>
                    <% } %>

                    <% for (int i = debutPage; i <= finPage; i++) { %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= i %>" 
                           class="page-link <%= i == pageActuelle ? "active" : "" %>">
                            <%= i %>
                        </a>
                    <% } %>

                    <% if (finPage < totalPages) { %>
                        <% if (finPage < totalPages - 1) { %>
                            <span class="page-ellipsis">...</span>
                        <% } %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= totalPages %>" 
                           class="page-link"><%= totalPages %></a>
                    <% } %>

                    <% if (pageActuelle < totalPages) { %>
                        <a href="${pageContext.request.contextPath}<%= urlPagination %>page=<%= pageActuelle + 1 %>" 
                           class="page-link" title="Page suivante">
                            <i class="fas fa-chevron-right"></i>
                        </a>
                    <% } else { %>
                        <span class="page-link disabled">
                            <i class="fas fa-chevron-right"></i>
                        </span>
                    <% } %>

                    <span class="page-info">
                        <%= totalQuiz %> resultat<%= totalQuiz > 1 ? "s" : "" %> - Page <%= pageActuelle %>/<%= totalPages %>
                    </span>
                </div>
            <% } %>

        <% } else { %>
            <div class="empty-state">
                <div class="empty-icon">
                    <i class="fas fa-inbox"></i>
                </div>
                <h3>Aucun quiz trouve</h3>
                <p>
                    <% if ((recherche != null && !recherche.isEmpty()) || !"tous".equals(filtreNiveau) || !"tous".equals(filtreTheme)) { %>
                        Aucun resultat avec ces filtres. Essayez de les modifier.
                    <% } else { %>
                        Aucun quiz n'a encore ete cree. Cliquez sur "Creer un quiz" pour commencer.
                    <% } %>
                </p>
            </div>
        <% } %>
    </div>

    <%-- ===== BOUTON RETOUR ===== --%>
    <div class="admin-back">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn-retour">
            <i class="fas fa-arrow-left"></i> Retour au dashboard admin
        </a>
    </div>

</div>

<script>
    function confirmSuppression(titre) {
        return confirm('Supprimer definitivement le quiz "' + titre + '" ?\n\nLes questions seront dissociees mais conservees.\nL\'historique des parties reste intact.\nCette action est irreversible.');
    }

    function toggleDropdown(btn) {
        var dropdown = btn.parentElement.querySelector('.dropdown-menu');
        var isVisible = dropdown.style.display === 'block';
        
        document.querySelectorAll('.dropdown-menu').forEach(function(d) {
            d.style.display = 'none';
        });
        
        dropdown.style.display = isVisible ? 'none' : 'block';
    }

    document.addEventListener('click', function(e) {
        if (!e.target.closest('.dropdown-actions')) {
            document.querySelectorAll('.dropdown-menu').forEach(function(d) {
                d.style.display = 'none';
            });
        }
    });
</script>

<%
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-quiz.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../footer-connecte.jsp" %>