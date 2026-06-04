<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.amijayquiz.models.Cours" %>
<%@ page import="com.amijayquiz.models.Chapitre" %>
<%@ page import="com.amijayquiz.models.Lecon" %>

<%-- ============================================================
     FICHIER : views/admin/admin-cours-lecons.jsp
     RÔLE    : Page d'administration des leçons d'un chapitre
              - Liste des leçons avec métadonnées
              - Badges : Gratuit/Premium, Code, Ressources
              - Actions : Modifier, Aperçu, Ressources, Supprimer
              - Navigation : chapitres, modules, cours
     ============================================================ --%>

<%
    // ===== RÉCUPÉRATION DES ATTRIBUTS =====
    Cours cours = (Cours) request.getAttribute("cours");
    Chapitre chapitre = (Chapitre) request.getAttribute("chapitre");
    List<Map<String, Object>> lecons = (List<Map<String, Object>>) request.getAttribute("lecons");
    Integer totalLecons = (Integer) request.getAttribute("totalLecons");
    Integer leconsGratuites = (Integer) request.getAttribute("leconsGratuites");
    Integer leconsPremium = (Integer) request.getAttribute("leconsPremium");
    String messageSucces = (String) request.getAttribute("messageSucces");
    String messageErreur = (String) request.getAttribute("messageErreur");

    if (chapitre == null) {
        response.sendRedirect(request.getContextPath() + "/admin/cours");
        return;
    }

    if (lecons == null) lecons = new java.util.ArrayList<>();
    if (totalLecons == null) totalLecons = 0;
    if (leconsGratuites == null) leconsGratuites = 0;
    if (leconsPremium == null) leconsPremium = 0;

    int chapitreId = chapitre.getId();
    int coursId = cours != null ? cours.getId() : chapitre.getCoursId();
    int moduleNumero = chapitre.getModuleNumero();

    request.setAttribute("pageTitle", "Leçons - " + chapitre.getTitre() + " - Administration");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-cours-lecons.css\">";
    request.setAttribute("extraHead", adminCSS);
    String adminJS = "<script src=\"" + request.getContextPath() + "/views/assets/js/admin-cours-lecons.js\"></script>";
    request.setAttribute("extraScripts", adminJS);
%>

<%@ include file="../header.jsp" %>

<div class="acl-container">

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
        <a href="${pageContext.request.contextPath}/admin/cours/chapitres?coursId=<%= coursId %>" class="breadcrumb-link">
            <i class="fas fa-bookmark"></i><span>Chapitres</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-file-alt"></i><span>Leçons : <%= chapitre.getTitre() %></span>
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
    <div class="acl-header">
        <div class="acl-header-top">
            <div class="acl-header-icon" style="background: <%= cours != null ? cours.getCouleurTheme() : "#667eea" %>;">
                <i class="<%= cours != null ? cours.getIconeTheme() : "fas fa-book" %>"></i>
            </div>
            <div class="acl-header-info">
                <span class="acl-header-fil">
                    <%= cours != null ? cours.getTitre() : "Cours" %> 
                    <i class="fas fa-chevron-right" style="font-size: 10px; margin: 0 4px;"></i> 
                    Module <%= moduleNumero %>
                    <i class="fas fa-chevron-right" style="font-size: 10px; margin: 0 4px;"></i> 
                    <%= chapitre.getTitre() %>
                </span>
                <h1>Leçons du chapitre</h1>
                <p><%= totalLecons %> leçon<%= totalLecons > 1 ? "s" : "" %> • <%= leconsGratuites %> gratuite<%= leconsGratuites > 1 ? "s" : "" %> • <%= leconsPremium %> premium</p>
            </div>
        </div>
        <div class="acl-header-actions">
            <a href="${pageContext.request.contextPath}/admin/cours/lecons/creer?chapitreId=<%= chapitreId %>&coursId=<%= coursId %>" class="acl-btn-primary">
                <i class="fas fa-plus-circle"></i><span>Ajouter une leçon</span>
            </a>
            <a href="${pageContext.request.contextPath}/admin/cours/chapitres?coursId=<%= coursId %>" class="acl-btn-outline">
                <i class="fas fa-arrow-left"></i><span>Retour aux chapitres</span>
            </a>
        </div>
    </div>

    <%-- ===== STATISTIQUES ===== --%>
    <div class="acl-stats-bar">
        <div class="acl-stat-item">
            <i class="fas fa-file-alt"></i><strong><%= totalLecons %></strong> leçons
        </div>
        <div class="acl-stat-separator"></div>
        <div class="acl-stat-item acl-stat-gratuit">
            <i class="fas fa-check-circle"></i><strong><%= leconsGratuites %></strong> gratuites
        </div>
        <div class="acl-stat-separator"></div>
        <div class="acl-stat-item acl-stat-premium">
            <i class="fas fa-crown"></i><strong><%= leconsPremium %></strong> premium
        </div>
    </div>

    <%-- ===== LISTE DES LEÇONS ===== --%>
    <div class="acl-tableau-section">
        <% if (lecons != null && !lecons.isEmpty()) { %>
            <div class="acl-table-responsive">
                <table class="acl-table">
                    <thead>
                        <tr>
                            <th class="acl-th-ordre">#</th>
                            <th class="acl-th-titre">Titre</th>
                            <th class="acl-th-acces">Accès</th>
                            <th class="acl-th-lecture">Lecture</th>
                            <th class="acl-th-code">Code</th>
                            <th class="acl-th-ressources">Ressources</th>
                            <th class="acl-th-actions">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% int index = 1;
                           for (Map<String, Object> data : lecons) {
                               Lecon lecon = (Lecon) data.get("lecon");
                               boolean hasCodeSource = (Boolean) data.get("hasCodeSource");
                               boolean hasRessources = (Boolean) data.get("hasRessources");
                               int nbRessources = (Integer) data.get("nbRessources");
                               String tempsLecture = (String) data.get("tempsLecture");
                               int nombreMots = (Integer) data.get("nombreMots");

                               String titreLecon = lecon.getTitre() != null ? lecon.getTitre() : "Sans titre";
                               boolean estGratuit = lecon.isGratuit();
                        %>
                            <tr>
                                <td class="acl-td-ordre">
                                    <span class="acl-ordre-badge"><%= lecon.getOrdre() %></span>
                                </td>
                                <td class="acl-td-titre">
                                    <span class="acl-titre"><%= titreLecon %></span>
                                    <% if (nombreMots > 0) { %>
                                        <span class="acl-mots"><%= nombreMots %> mot<%= nombreMots > 1 ? "s" : "" %></span>
                                    <% } %>
                                </td>
                                <td class="acl-td-acces">
                                    <span class="acl-badge-acces <%= estGratuit ? "acl-gratuit" : "acl-premium" %>">
                                        <i class="fas <%= estGratuit ? "fa-check-circle" : "fa-crown" %>"></i>
                                        <%= estGratuit ? "Gratuit" : "Premium" %>
                                    </span>
                                </td>
                                <td class="acl-td-lecture">
                                    <span class="acl-lecture"><i class="fas fa-book-reader"></i> <%= tempsLecture %></span>
                                </td>
                                <td class="acl-td-code">
                                    <% if (hasCodeSource) { %>
                                        <span class="acl-badge-code"><i class="fas fa-code"></i> Oui</span>
                                    <% } else { %>
                                        <span class="acl-text-muted">—</span>
                                    <% } %>
                                </td>
                                <td class="acl-td-ressources">
                                    <% if (hasRessources) { %>
                                        <span class="acl-badge-ressources"><i class="fas fa-paperclip"></i> <%= nbRessources %></span>
                                    <% } else { %>
                                        <span class="acl-text-muted">—</span>
                                    <% } %>
                                </td>
                                <td class="acl-td-actions">
                                    <div class="acl-actions-group">
                                        <a href="${pageContext.request.contextPath}/admin/cours/lecons/modifier?id=<%= lecon.getId() %>&chapitreId=<%= chapitreId %>&coursId=<%= coursId %>" 
                                           class="acl-btn-icon acl-btn-modifier" title="Modifier">
                                            <i class="fas fa-pen"></i>
                                        </a>
                                        <a href="${pageContext.request.contextPath}/admin/cours/lecons/apercu?id=<%= lecon.getId() %>" 
                                           class="acl-btn-icon acl-btn-voir" title="Aperçu" target="_blank">
                                            <i class="fas fa-eye"></i>
                                        </a>
                                        <a href="${pageContext.request.contextPath}/admin/cours/ressources?leconId=<%= lecon.getId() %>&coursId=<%= coursId %>" 
                                           class="acl-btn-icon acl-btn-ressources" title="Ressources">
                                            <i class="fas fa-paperclip"></i>
                                        </a>
                                        <form action="${pageContext.request.contextPath}/admin/cours/lecons/supprimer" method="POST" class="acl-form-inline"
                                              onsubmit="return confirm('Supprimer la leçon « <%= titreLecon.replace("'", "\\'") %> » ?')">
                                            <input type="hidden" name="id" value="<%= lecon.getId() %>">
                                            <input type="hidden" name="chapitreId" value="<%= chapitreId %>">
                                            <input type="hidden" name="coursId" value="<%= coursId %>">
                                            <button type="submit" class="acl-btn-icon acl-btn-supprimer" title="Supprimer">
                                                <i class="fas fa-trash-alt"></i>
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        <% index++;
                           } %>
                    </tbody>
                </table>
            </div>
        <% } else { %>
            <div class="acl-empty">
                <div class="acl-empty-icon"><i class="fas fa-file-alt"></i></div>
                <h3>Aucune leçon dans ce chapitre</h3>
                <p>Ajoutez votre première leçon pour commencer à construire le contenu du chapitre.</p>
                <a href="${pageContext.request.contextPath}/admin/cours/lecons/creer?chapitreId=<%= chapitreId %>&coursId=<%= coursId %>" class="acl-btn-primary-empty">
                    <i class="fas fa-plus-circle"></i> Ajouter une leçon
                </a>
            </div>
        <% } %>
    </div>

    <%-- ===== BOUTONS RETOUR ===== --%>
    <div class="acl-back">
        <a href="${pageContext.request.contextPath}/admin/cours/chapitres?coursId=<%= coursId %>" class="acl-btn-retour">
            <i class="fas fa-arrow-left"></i> Retour aux chapitres
        </a>
    </div>

</div>

<%@ include file="../footer-connecte.jsp" %>