<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.amijayquiz.models.Lecon" %>
<%@ page import="com.amijayquiz.models.Ressource" %>

<%-- ============================================================
     FICHIER : views/admin/admin-cours-ressources.jsp
     RÔLE    : Page de gestion des ressources d'une leçon
              - Liste des ressources existantes
              - Formulaire d'ajout de ressource
              - Suppression avec confirmation
              - Badges par type de ressource
     ============================================================ --%>

<%
    Lecon lecon = (Lecon) request.getAttribute("lecon");
    List<Ressource> ressources = (List<Ressource>) request.getAttribute("ressources");
    Integer totalRessources = (Integer) request.getAttribute("totalRessources");
    String messageSucces = (String) request.getAttribute("messageSucces");
    String messageErreur = (String) request.getAttribute("messageErreur");

    if (lecon == null) {
        response.sendRedirect(request.getContextPath() + "/admin/cours");
        return;
    }

    if (ressources == null) ressources = new java.util.ArrayList<>();
    if (totalRessources == null) totalRessources = 0;

    int leconId = lecon.getId();
    int chapitreId = lecon.getChapitreId();

    request.setAttribute("pageTitle", "Ressources - " + lecon.getTitre() + " - Administration");
%>

<%
    String adminCSS = "<link rel=\"stylesheet\" href=\"" + request.getContextPath() + "/views/assets/css/admin-cours-ressources.css\">";
    request.setAttribute("extraHead", adminCSS);
%>

<%@ include file="../header.jsp" %>

<div class="acr-container">

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
        <a href="${pageContext.request.contextPath}/admin/cours/lecons?chapitreId=<%= chapitreId %>" class="breadcrumb-link">
            <i class="fas fa-file-alt"></i><span>Leçons</span>
        </a>
        <i class="fas fa-chevron-right breadcrumb-separator"></i>
        <span class="breadcrumb-current">
            <i class="fas fa-paperclip"></i><span>Ressources : <%= lecon.getTitre() %></span>
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
    <div class="acr-header">
        <div class="acr-header-left">
            <div class="acr-header-icon">
                <i class="fas fa-paperclip"></i>
            </div>
            <div class="acr-header-text">
                <span class="acr-header-lecon">Leçon : <%= lecon.getTitre() %></span>
                <h1>Ressources téléchargeables</h1>
                <p><%= totalRessources %> ressource<%= totalRessources > 1 ? "s" : "" %> associée<%= totalRessources > 1 ? "s" : "" %></p>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/admin/cours/lecons?chapitreId=<%= chapitreId %>" class="acr-btn-outline">
            <i class="fas fa-arrow-left"></i><span>Retour aux leçons</span>
        </a>
    </div>

    <%-- ===== LAYOUT 2 COLONNES ===== --%>
    <div class="acr-layout">

        <%-- ===== COLONNE GAUCHE : LISTE DES RESSOURCES ===== --%>
        <div class="acr-col-liste">
            <div class="acr-card">
                <div class="acr-card-header">
                    <h2><i class="fas fa-list-ul"></i> Ressources existantes</h2>
                    <span class="acr-badge"><%= totalRessources %></span>
                </div>
                <div class="acr-card-body">
                    <% if (ressources != null && !ressources.isEmpty()) { %>
                        <div class="acr-ressources-liste">
                            <% for (Ressource ressource : ressources) { %>
                                <div class="acr-ressource-item">
                                    <div class="acr-ressource-icon" style="background: <%= ressource.getCouleurType() %>;">
                                        <i class="<%= ressource.getIcone() %>"></i>
                                    </div>
                                    <div class="acr-ressource-info">
                                        <strong><%= ressource.getTitre() %></strong>
                                        <span>
                                            <span class="acr-badge-type"><%= ressource.getType().toUpperCase() %></span>
                                            <% if (ressource.getTaille() != null && !ressource.getTaille().isEmpty()) { %>
                                                <span class="acr-taille"><%= ressource.getTaille() %></span>
                                            <% } %>
                                        </span>
                                    </div>
                                    <div class="acr-ressource-actions">
                                        <a href="<%= ressource.getUrlFichier() %>" class="acr-btn-icon acr-btn-download" target="_blank" title="Télécharger">
                                            <i class="fas fa-download"></i>
                                        </a>
                                        <form action="${pageContext.request.contextPath}/admin/cours/ressources" method="POST" class="acr-form-inline"
                                              onsubmit="return confirm('Supprimer cette ressource ?')">
                                            <input type="hidden" name="action" value="supprimer">
                                            <input type="hidden" name="leconId" value="<%= leconId %>">
                                            <input type="hidden" name="ressourceId" value="<%= ressource.getId() %>">
                                            <button type="submit" class="acr-btn-icon acr-btn-supprimer" title="Supprimer">
                                                <i class="fas fa-trash-alt"></i>
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            <% } %>
                        </div>
                    <% } else { %>
                        <div class="acr-empty-mini">
                            <i class="fas fa-inbox"></i>
                            <p>Aucune ressource pour cette leçon.</p>
                        </div>
                    <% } %>
                </div>
            </div>
        </div>

        <%-- ===== COLONNE DROITE : FORMULAIRE D'AJOUT ===== --%>
        <div class="acr-col-form">
            <div class="acr-card">
                <div class="acr-card-header">
                    <h2><i class="fas fa-plus-circle"></i> Ajouter une ressource</h2>
                </div>
                <div class="acr-card-body">
                    <form action="${pageContext.request.contextPath}/admin/cours/ressources" method="POST" id="formRessource" novalidate>
                        <input type="hidden" name="action" value="ajouter">
                        <input type="hidden" name="leconId" value="<%= leconId %>">

                        <div class="acr-form-group">
                            <label for="titre"><i class="fas fa-heading"></i> Titre <span class="acr-required">*</span></label>
                            <input type="text" id="titre" name="titre" class="acr-input" placeholder="Nom de la ressource" required autofocus>
                        </div>

                        <div class="acr-form-group">
                            <label for="type"><i class="fas fa-tag"></i> Type <span class="acr-required">*</span></label>
                            <select id="type" name="type" class="acr-select" required>
                                <option value="">-- Choisir --</option>
                                <option value="pdf">PDF</option>
                                <option value="image">Image</option>
                                <option value="zip">ZIP</option>
                                <option value="code">Code source</option>
                                <option value="lien">Lien externe</option>
                            </select>
                        </div>

                        <div class="acr-form-group">
                            <label for="urlFichier"><i class="fas fa-link"></i> URL du fichier <span class="acr-required">*</span></label>
                            <input type="url" id="urlFichier" name="urlFichier" class="acr-input" placeholder="https://..." required>
                        </div>

                        <div class="acr-form-group">
                            <label for="taille"><i class="fas fa-weight-hanging"></i> Taille <span class="acr-optional">(optionnel)</span></label>
                            <input type="text" id="taille" name="taille" class="acr-input" placeholder="Ex: 2.5 MB">
                        </div>

                        <button type="submit" class="acr-btn acr-btn-primary" id="btnAjouter">
                            <i class="fas fa-plus-circle"></i><span>Ajouter la ressource</span>
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<%-- ===== SCRIPTS ===== --%>
<script>
    document.getElementById('formRessource').addEventListener('submit', function(e) {
        var titre = document.getElementById('titre');
        var type = document.getElementById('type');
        var url = document.getElementById('urlFichier');
        var hasError = false;
        [titre, type, url].forEach(function(el) { el.classList.remove('acr-input-error'); });

        if (!titre.value.trim()) { titre.classList.add('acr-input-error'); hasError = true; }
        if (!type.value) { type.classList.add('acr-input-error'); hasError = true; }
        if (!url.value.trim()) { url.classList.add('acr-input-error'); hasError = true; }

        if (hasError) { e.preventDefault(); }
        else {
            var btn = document.getElementById('btnAjouter');
            if (btn) { btn.disabled = true; btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i><span>Ajout...</span>'; }
        }
    });
</script>

<%@ include file="../footer-connecte.jsp" %>