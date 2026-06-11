<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.learnpath.models.Cours" %>
<%@ page import="com.learnpath.models.Chapitre" %>

<%-- ============================================================
     FICHIER : views/admin/admin-cours-chapitres.jsp
     RÔLE    : Page d'administration des chapitres d'un cours
              - Liste des chapitres groupés par module (1-15)
              - Pour chaque chapitre : titre, leçons, actions
              - Boutons : Ajouter, Modifier, Supprimer, Voir leçons
              - Navigation vers les modules et le cours
     ============================================================ --%>

<%
    // ===== RÉCUPÉRATION DES ATTRIBUTS =====
    Cours cours = (Cours) request.getAttribute("cours");
    List<Map<String, Object>> modules = (List<Map<String, Object>>) request.getAttribute("modules");
    Integer nombreModules = (Integer) request.getAttribute("nombreModules");
    Integer totalChapitres = (Integer) request.getAttribute("totalChapitres");
    Integer totalLecons = (Integer) request.getAttribute("totalLecons");
    String messageSucces = (String) request.getAttribute("messageSucces");
    String messageErreur = (String) request.getAttribute("messageErreur");

    if (cours == null) {
        response.sendRedirect(request.getContextPath() + "/admin/cours");
        return;
    }

    if (modules == null) modules = new java.util.ArrayList<>();
    if (nombreModules == null) nombreModules = 15;
    if (totalChapitres == null) totalChapitres = 0;
    if (totalLecons == null) totalLecons = 0;

    int coursId = cours.getId();

    request.setAttribute("pageTitle", "Chapitres - " + cours.getTitre() + " - Administration");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-cours-chapitres.css\">";
    request.setAttribute("extraHead", adminCSS);
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-cours-chapitres.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../header.jsp" %>

<div class="accp-container">

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
        <span class="breadcrumb-current">
            <i class="fas fa-bookmark"></i><span>Chapitres : <%= cours.getTitre() %></span>
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

    <%-- ===== EN-TÊTE DU COURS ===== --%>
    <div class="accp-header">
        <div class="accp-header-top">
            <div class="accp-header-icon" style="background: <%= cours.getCouleurTheme() %>;">
                <i class="<%= cours.getIconeTheme() %>"></i>
            </div>
            <div class="accp-header-info">
                <span class="accp-header-theme"><%= cours.getTheme() %> • <%= cours.getNiveauFormate() %></span>
                <h1><%= cours.getTitre() %></h1>
                <p>Gestion des chapitres — <%= totalChapitres %> chapitre<%= totalChapitres > 1 ? "s" : "" %> • <%= totalLecons %> leçon<%= totalLecons > 1 ? "s" : "" %></p>
            </div>
        </div>
        <div class="accp-header-actions">
            <a href="${pageContext.request.contextPath}/admin/cours/chapitres/creer?coursId=<%= coursId %>" class="accp-btn-primary">
                <i class="fas fa-plus-circle"></i><span>Ajouter un chapitre</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/cours/modules?coursId=<%= coursId %>" class="accp-btn-outline">
                <i class="fas fa-cubes"></i><span>Modules</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/cours/modifier?id=<%= coursId %>" class="accp-btn-outline">
                <i class="fas fa-edit"></i><span>Modifier le cours</span>
            </a>
        </div>
    </div>

    <%-- ===== STATISTIQUES ===== --%>
    <div class="accp-stats-bar">
        <div class="accp-stat-item">
            <i class="fas fa-bookmark"></i><strong><%= totalChapitres %></strong> chapitres
        </div>
        <div class="accp-stat-separator"></div>
        <div class="accp-stat-item">
            <i class="fas fa-file-alt"></i><strong><%= totalLecons %></strong> leçons
        </div>
        <div class="accp-stat-separator"></div>
        <div class="accp-stat-item">
            <i class="fas fa-cubes"></i><strong><%= nombreModules %></strong> modules
        </div>
    </div>

    <%-- ===== LISTE DES CHAPITRES PAR MODULE ===== --%>
    <div class="accp-modules-list">
        <% if (modules != null && !modules.isEmpty()) {
            for (Map<String, Object> moduleData : modules) {
                int numero = (Integer) moduleData.get("numero");
                List<Map<String, Object>> chapitresData = (List<Map<String, Object>>) moduleData.get("chapitres");
                int nbChapitres = (Integer) moduleData.get("nbChapitres");
                int nbLecons = (Integer) moduleData.get("nbLecons");

                if (chapitresData == null) chapitresData = new java.util.ArrayList<>();
        %>
            <div class="accp-module-card">
                
                <%-- En-tête du module --%>
                <div class="accp-module-header">
                    <div class="accp-module-header-left">
                        <span class="accp-module-numero"><%= numero %></span>
                        <div>
                            <h3>Module <%= numero %></h3>
                            <span class="accp-module-stats">
                                <%= nbChapitres %> chapitre<%= nbChapitres > 1 ? "s" : "" %> • <%= nbLecons %> leçon<%= nbLecons > 1 ? "s" : "" %>
                            </span>
                        </div>
                    </div>
                    <a href="${pageContext.request.contextPath}/admin/cours/chapitres/creer?coursId=<%= coursId %>&module=<%= numero %>" 
                       class="accp-btn-add-chapitre">
                        <i class="fas fa-plus"></i><span>Ajouter</span>
                    </a>
                </div>

                <%-- Liste des chapitres du module --%>
                <div class="accp-module-body">
                    <% if (chapitresData != null && !chapitresData.isEmpty()) { %>
                        <div class="accp-chapitres-liste">
                            <% for (Map<String, Object> chapData : chapitresData) {
                                Chapitre chapitre = (Chapitre) chapData.get("chapitre");
                                int nbLeconsChap = (Integer) chapData.get("nbLecons");
                                String titreChap = chapitre.getTitre() != null ? chapitre.getTitre() : "Sans titre";
                            %>
                                <div class="accp-chapitre-item">
                                    <div class="accp-chapitre-main">
                                        <span class="accp-chapitre-ordre"><%= chapitre.getOrdre() %></span>
                                        <div class="accp-chapitre-info">
                                            <strong class="accp-chapitre-titre"><%= titreChap %></strong>
                                            <span class="accp-chapitre-meta">
                                                <i class="fas fa-file-alt"></i> <%= nbLeconsChap %> leçon<%= nbLeconsChap > 1 ? "s" : "" %>
                                                <% if (chapitre.getDescription() != null && !chapitre.getDescription().isEmpty()) { %>
                                                    <span class="accp-chapitre-desc"> — <%= chapitre.getDescription().length() > 60 ? chapitre.getDescription().substring(0, 60) + "..." : chapitre.getDescription() %></span>
                                                <% } %>
                                            </span>
                                        </div>
                                    </div>
                                    <div class="accp-chapitre-actions">
                                        <a href="${pageContext.request.contextPath}/admin/cours/lecons?chapitreId=<%= chapitre.getId() %>&coursId=<%= coursId %>" 
                                           class="accp-btn-icon accp-btn-lecons" title="Voir les leçons">
                                            <i class="fas fa-file-alt"></i>
                                        </a>
                                        <a href="${pageContext.request.contextPath}/admin/cours/chapitres/modifier?id=<%= chapitre.getId() %>&coursId=<%= coursId %>" 
                                           class="accp-btn-icon accp-btn-modifier" title="Modifier">
                                            <i class="fas fa-pen"></i>
                                        </a>
                                        <form action="${pageContext.request.contextPath}/admin/cours/chapitres/supprimer" method="POST" class="accp-form-inline"
                                              onsubmit="return confirm('Supprimer le chapitre « <%= titreChap.replace("'", "\\'") %> » ?\n\nLes leçons associées seront également supprimées.')">
                                            <input type="hidden" name="id" value="<%= chapitre.getId() %>">
                                            <input type="hidden" name="coursId" value="<%= coursId %>">
                                            <button type="submit" class="accp-btn-icon accp-btn-supprimer" title="Supprimer">
                                                <i class="fas fa-trash-alt"></i>
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            <% } %>
                        </div>
                    <% } else { %>
                        <div class="accp-empty-mini">
                            <i class="fas fa-inbox"></i>
                            <p>Aucun chapitre dans ce module.</p>
                        </div>
                    <% } %>
                </div>

            </div>
        <%  }
        } else { %>
            <div class="accp-empty">
                <div class="accp-empty-icon"><i class="fas fa-bookmark"></i></div>
                <h3>Aucun chapitre trouvé</h3>
                <p>Commencez par ajouter des chapitres à ce cours.</p>
                <a href="${pageContext.request.contextPath}/admin/cours/chapitres/creer?coursId=<%= coursId %>" class="accp-btn-primary-empty">
                    <i class="fas fa-plus-circle"></i> Ajouter un chapitre
                </a>
            </div>
        <% } %>
    </div>

    <%-- ===== BOUTONS RETOUR ===== --%>
    <div class="accp-back">
        <a href="${pageContext.request.contextPath}/admin/cours" class="accp-btn-retour">
            <i class="fas fa-arrow-left"></i> Retour à la liste des cours
        </a>
        <a href="${pageContext.request.contextPath}/joueur/cours/plan?coursId=<%= coursId %>" class="accp-btn-retour" target="_blank">
            <i class="fas fa-eye"></i> Voir côté apprenant
        </a>
    </div>

</div>

<%@ include file="../footer-connecte.jsp" %>